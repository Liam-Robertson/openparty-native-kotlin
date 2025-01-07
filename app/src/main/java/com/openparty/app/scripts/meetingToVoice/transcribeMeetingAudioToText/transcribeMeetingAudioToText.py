import os
import requests
import subprocess
import logging

# Configure logging to only log to the console
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[logging.StreamHandler()]  # Only use console logging
)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
INPUT_DIR = os.path.join(BASE_DIR, 'inputAudioFiles')
OUTPUT_DIR = os.path.join(BASE_DIR, '..', 'textTranscriptions', 'outputTextTranscriptions')
PROCESSED_DIR = os.path.join(BASE_DIR, 'processedAudioFiles')
TEMP_DIR = os.path.join(BASE_DIR, 'tempChunks')

for directory in [INPUT_DIR, OUTPUT_DIR, PROCESSED_DIR, TEMP_DIR]:
    if not os.path.exists(directory):
        logging.error(f"Required directory not found: {directory}")
        raise FileNotFoundError(f"Required directory not found: {directory}")

API_URL = "https://api.openai.com/v1/audio/transcriptions"
API_KEY = os.getenv("OPENAI_API_KEY")

if not API_KEY:
    logging.error("OPENAI_API_KEY environment variable not set.")
    raise EnvironmentError("Error: Please set the OPENAI_API_KEY environment variable.")

CHUNK_DURATION_SECONDS = 10 * 60

def split_audio_file(input_file):
    chunk_files = []
    command = [
        "ffmpeg", "-i", input_file, "-f", "segment",
        "-segment_time", str(CHUNK_DURATION_SECONDS),
        "-ar", "16000",
        "-ac", "1",
        os.path.join(TEMP_DIR, "chunk_%03d.wav")
    ]
    try:
        subprocess.run(command, check=True)
        logging.info(f"File {input_file} split into chunks.")
    except subprocess.CalledProcessError as e:
        logging.error(f"Error during file splitting with ffmpeg: {e}")
        raise RuntimeError(f"Error during file splitting with ffmpeg: {e}")

    for filename in os.listdir(TEMP_DIR):
        if filename.startswith("chunk_") and filename.endswith(".wav"):
            chunk_files.append(os.path.join(TEMP_DIR, filename))
    return chunk_files

def transcribe_chunk(chunk_path):
    if os.path.getsize(chunk_path) == 0:
        logging.warning(f"Skipping empty chunk: {chunk_path}")
        return ""

    headers = {"Authorization": f"Bearer {API_KEY}"}
    with open(chunk_path, 'rb') as chunk:
        files = {"file": (chunk_path, chunk, "audio/wav")}
        data = {"model": "whisper-1", "language": "en"}

        try:
            response = requests.post(API_URL, headers=headers, files=files, data=data, timeout=300)
            response.raise_for_status()
            logging.info(f"Chunk {chunk_path} transcribed successfully.")
            return response.json().get("text", "")
        except requests.exceptions.RequestException as e:
            logging.error(f"Failed to process chunk {chunk_path}: {e}")
            return ""

logging.info(f"Starting transcription process in {INPUT_DIR}")

if not os.listdir(INPUT_DIR):
    logging.error(f"No files found in the input directory: {INPUT_DIR}")
    raise FileNotFoundError(f"No files found in the input directory: {INPUT_DIR}")

errors = []

for filename in os.listdir(INPUT_DIR):
    file_path = os.path.join(INPUT_DIR, filename)
    file_extension = os.path.splitext(filename)[1].lower()

    if file_extension != '.mp3':
        logging.info(f"Skipping unsupported file type: {filename}")
        continue

    logging.info(f"Processing file: {filename}")

    try:
        chunk_files = split_audio_file(file_path)
    except RuntimeError as e:
        logging.error(f"Error during file splitting for {filename}: {e}")
        errors.append(f"Error during file splitting for {filename}: {e}")
        continue

    transcription_text = ""

    for i, chunk_file in enumerate(chunk_files, start=1):
        logging.info(f"Transcribing chunk {i}/{len(chunk_files)} for file {filename}")
        chunk_text = transcribe_chunk(chunk_file)
        if not chunk_text.strip():
            errors.append(f"Failed to transcribe chunk {i} of file {filename}")
        transcription_text += chunk_text + "\n"

    if transcription_text.strip():
        output_filename = f"{os.path.splitext(filename)[0]}.txt"
        output_path = os.path.join(OUTPUT_DIR, output_filename)

        with open(output_path, 'w', encoding='utf-8') as output_file:
            output_file.write(transcription_text)

        logging.info(f"Transcription for {filename} written to {output_path}")

        processed_file_path = os.path.join(PROCESSED_DIR, filename)
        os.rename(file_path, processed_file_path)
        logging.info(f"Moved processed file {filename} to {PROCESSED_DIR}")
    else:
        logging.error(f"Failed to transcribe any chunks for file {filename}")
        errors.append(f"Failed to transcribe any chunks for file {filename}")

for temp_file in os.listdir(TEMP_DIR):
    os.remove(os.path.join(TEMP_DIR, temp_file))
logging.info("Temporary chunk files cleaned up.")

if errors:
    logging.error("Errors encountered during transcription:")
    for error in errors:
        logging.error(f"- {error}")
else:
    logging.info("Transcription process completed successfully without errors.")
