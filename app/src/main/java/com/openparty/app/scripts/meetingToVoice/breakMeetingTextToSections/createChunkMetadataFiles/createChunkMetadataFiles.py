import os
import logging
import re
import time
from openai import OpenAI

client = OpenAI()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler()]
)
logger = logging.getLogger(__name__)

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
INPUT_DIR = os.path.join(SCRIPT_DIR, "../../textTranscriptions/outputTextTranscriptions")
OUTPUT_DIR = os.path.join(SCRIPT_DIR, "outputIssueMetadataFiles")

MAX_TEXT_LENGTH = 400000
CHUNK_SIZE = 6000
MAX_RETRIES = 5
INITIAL_BACKOFF = 1
MODEL = "gpt-4o"


def read_file(file_path):
    try:
        with open(file_path, "r", encoding="utf-8") as file:
            return file.read()
    except Exception as e:
        logger.error(f"Error reading file {file_path}: {e}")
        return None


def write_file(file_path, content, mode="w"):
    try:
        with open(file_path, mode, encoding="utf-8") as file:
            file.write(content)
        logger.info(f"File written to {file_path}")
    except Exception as e:
        logger.error(f"Error writing file {file_path}: {e}")


def call_openai_with_retries(prompt):
    retries = 0
    backoff = INITIAL_BACKOFF
    while retries < MAX_RETRIES:
        try:
            completion = client.chat.completions.create(
                model=MODEL,
                messages=[
                    {"role": "system", "content": "You are a helpful assistant."},
                    {"role": "user", "content": prompt}
                ]
            )
            return completion.choices[0].message.content
        except Exception as e:
            if "rate_limit_exceeded" in str(e):
                retries += 1
                wait_time = backoff * 2 ** retries
                logger.warning(f"Rate limit exceeded. Retrying in {wait_time} seconds...")
                time.sleep(wait_time)
            else:
                logger.error(f"Error during OpenAI call: {e}")
                return None
    logger.error("Max retries exceeded.")
    return None


def chunk_text(text, chunk_size):
    chunks = []
    start = 0
    while start < len(text):
        end = start + chunk_size
        chunk = text[start:end]
        if end < len(text) and not text[end].isspace():
            last_space = chunk.rfind(" ")
            if last_space != -1:
                chunk = chunk[:last_space]
        chunks.append(chunk.strip())
        start += len(chunk)
    return chunks


def process_large_text(text):
    chunks = chunk_text(text, CHUNK_SIZE)
    results = []
    for i, chunk in enumerate(chunks):
        logger.info(f"Processing chunk {i + 1}/{len(chunks)}")
        prompt = f"""
You are analyzing a transcription of a UK council meeting. Identify the key issues discussed in the meeting. 
For each issue, provide:
1. This number: {i + 1} in the <chunk> section. 
    [IMPORTANT] It should always be this number - {i + 1}. Don't add to this number, don't increment this number.
    Always add this number in the chunk section without any changes {i + 1}
2. Topic of the issue
2. A summary of the issue.

Return results in the following format for each issue:
<issue>
    <chunk>{i + 1}</chunk>
    <topic>TOPIC</topic>
    <summary>SUMMARY</summary>
</issue>

An example might look like this: 
<issue>
    <chunk>2</chunk>
    <topic>Housing</topic>
    <summary>Discussion about the ongoing and projected homeless demand, driven by factors such as the cost of living and constraints in the private rented sector.</summary>
</issue>

Don't include any other text other than what's in the xml, only include text in an xml format and nothing else. 

Text to analyze:
{chunk}
"""
        response = call_openai_with_retries(prompt)
        if response:
            results.append(f"Chunk {i + 1}:\n{response}\n")
    return "\n".join(results)


def clean_non_xml_content(file_path):
    try:
        with open(file_path, "r", encoding="utf-8") as file:
            content = file.read()
        xml_content = re.findall(r"<issue>.*?</issue>", content, re.DOTALL)
        clean_content = "\n".join(xml_content)
        with open(file_path, "w", encoding="utf-8") as file:
            file.write(clean_content)
        logger.info(f"Cleaned non-XML content from {file_path}")
    except Exception as e:
        logger.error(f"Error cleaning file {file_path}: {e}")


def main():
    for directory in [INPUT_DIR, OUTPUT_DIR]:
        if not os.path.exists(directory):
            logger.error(f"Required directory not found: {directory}")
            raise FileNotFoundError(f"Required directory not found: {directory}")
    input_files = [f for f in os.listdir(INPUT_DIR) if f.endswith(".txt")]
    if not input_files:
        logger.error(f"No text files found in input directory: {INPUT_DIR}")
        raise FileNotFoundError(f"No text files found in input directory: {INPUT_DIR}")
    for file_name in input_files:
        input_path = os.path.join(INPUT_DIR, file_name)
        logger.info(f"Processing file: {file_name}")
        text = read_file(input_path)
        if not text or len(text) > MAX_TEXT_LENGTH:
            logger.warning(f"File {file_name} exceeds the maximum allowed length of {MAX_TEXT_LENGTH} characters.")
            continue
        metadata = process_large_text(text)
        if metadata:
            output_file_name = f"{os.path.splitext(file_name)[0]}_metadata.txt"
            output_path = os.path.join(OUTPUT_DIR, output_file_name)
            write_file(output_path, metadata, mode="a")
            clean_non_xml_content(output_path)
        else:
            logger.error(f"Failed to process {file_name}")
    logger.info("All files processed.")


if __name__ == "__main__":
    main()
