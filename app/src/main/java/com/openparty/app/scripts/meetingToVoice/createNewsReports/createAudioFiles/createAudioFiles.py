import os
import requests
import json
import textwrap
import io  # Import io for handling in-memory byte streams
import shutil  # To move files after processing
from pydub import AudioSegment  # To merge audio files


def chunk_text(text, max_length):
    """Split text into chunks of at most `max_length` characters."""
    return textwrap.wrap(text, max_length, break_long_words=False, replace_whitespace=False)


def main():
    # Define directories relative to the Alloyscript's location
    BASE_DIR = os.path.dirname(os.path.abspath(__file__))
    REPORT_TEXT_DIR = os.path.join(BASE_DIR, '..', '..', 'finalOutput', 'unprocessedReportTextFiles')
    PROCESSED_TEXT_DIR = os.path.join(BASE_DIR, '..', '..', 'finalOutput', 'reportTextFiles')  # Directory for processed files
    AUDIO_OUTPUT_DIR = os.path.join(BASE_DIR, '..', '..', 'finalOutput', 'audioFiles')

    # Check if the input directory exists
    if not os.path.exists(REPORT_TEXT_DIR):
        raise FileNotFoundError(f"Input directory not found: {REPORT_TEXT_DIR}")

    # Check if the processed directory exists, and create it if it doesn't
    if not os.path.exists(PROCESSED_TEXT_DIR):
        print(f"Creating processed directory at {PROCESSED_TEXT_DIR}")
        os.makedirs(PROCESSED_TEXT_DIR)

    # Check if the output directory exists, and create it if it doesn't
    if not os.path.exists(AUDIO_OUTPUT_DIR):
        print(f"Creating output directory at {AUDIO_OUTPUT_DIR}")
        os.makedirs(AUDIO_OUTPUT_DIR)

    # Find all .txt files in the input directory
    text_files = [f for f in os.listdir(REPORT_TEXT_DIR) if f.endswith('.txt')]
    if not text_files:
        raise FileNotFoundError(f"No text files found in input directory: {REPORT_TEXT_DIR}")

    # Fetch the OpenAI API key from environment variables
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        raise EnvironmentError("Please set the OPENAI_API_KEY environment variable.")

    # Iterate through text files and process them
    for text_file in text_files:
        text_file_path = os.path.join(REPORT_TEXT_DIR, text_file)

        try:
            with open(text_file_path, "r", encoding="utf-8") as file:
                text = file.read()
        except Exception as e:
            print(f"Error reading file {text_file}: {e}")
            continue

        # Split text into chunks to meet the API's character limit
        text_chunks = chunk_text(text, 4096)
        combined_audio = None  # To hold the merged audio

        for i, chunk in enumerate(text_chunks):
            # Prepare the request payload
            tts_request = {
                "model": "tts-1",
                "input": chunk,
                "voice": "fable",
                "responseFormat": "mp3"
            }

            headers = {
                "Authorization": f"Bearer {api_key}",
                "Content-Type": "application/json"
            }

            try:
                response = requests.post(
                    "https://api.openai.com/v1/audio/speech",
                    headers=headers,
                    data=json.dumps(tts_request)
                )

                if response.status_code != 200:
                    print(f"Error: {response.status_code} - {response.text}")
                    continue

                response_data = response.content
                if not response_data:
                    print(f"Error: Empty response for file {text_file}")
                    continue

                # Load the audio chunk into an AudioSegment
                audio_chunk = AudioSegment.from_file(io.BytesIO(response_data), format="mp3")

                # Merge the chunk into the combined audio
                if combined_audio is None:
                    combined_audio = audio_chunk
                else:
                    combined_audio += audio_chunk

                print(f"Processed chunk {i+1} of {text_file}")

            except Exception as e:
                print(f"Error processing chunk {i+1} of file {text_file}: {e}")
                continue

        if combined_audio:
            # Export the combined audio to a single MP3 file
            output_audio_path = os.path.join(AUDIO_OUTPUT_DIR, f"{os.path.splitext(text_file)[0]}.mp3")
            combined_audio.export(output_audio_path, format="mp3")
            print(f"Combined audio saved to {output_audio_path}")

            # Move the processed file to the processed directory
            processed_file_path = os.path.join(PROCESSED_TEXT_DIR, text_file)
            try:
                shutil.move(text_file_path, processed_file_path)
                print(f"Moved processed file to {processed_file_path}")
            except Exception as e:
                print(f"Error moving file {text_file}: {e}")


if __name__ == "__main__":
    try:
        main()
    except FileNotFoundError as e:
        print(f"Error: {e}")
        exit(1)
    except EnvironmentError as e:
        print(f"Error: {e}")
        exit(1)
