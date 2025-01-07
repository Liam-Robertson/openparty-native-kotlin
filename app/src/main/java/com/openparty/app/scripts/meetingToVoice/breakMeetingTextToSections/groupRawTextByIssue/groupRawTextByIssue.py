import os
import re
import logging
from pathlib import Path
import shutil  # For file operations like overwriting during move

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler()]
)
logger = logging.getLogger(__name__)

# Directories (use relative paths from script's location)
SCRIPT_DIR = Path(__file__).parent
METADATA_DIR = SCRIPT_DIR / "../metadataSecondPass/outputReportSummaryFiles"
TEXT_DIR = SCRIPT_DIR / "../../TextTranscriptions/outputTextTranscriptions"
PROCESSED_METADATA_DIR = SCRIPT_DIR / "../metadataSecondPass/processedMetadataFiles"
OUTPUT_DIR = SCRIPT_DIR / "./outputSectionChunks"
PROCESSED_TEXT_DIR = SCRIPT_DIR / "../../TextTranscriptions/processedTextTranscriptions"

# Constants
CHUNK_SIZE = 6000
MAX_FILENAME_LENGTH = 255


def validate_directories():
    """Validate that all required directories exist."""
    logger.info("Validating required directories...")
    required_dirs = [METADATA_DIR, TEXT_DIR, PROCESSED_METADATA_DIR, OUTPUT_DIR, PROCESSED_TEXT_DIR]
    for dir_path in required_dirs:
        if not dir_path.exists():
            logger.error(f"Required directory does not exist: {dir_path}")
            raise FileNotFoundError(f"Required directory does not exist: {dir_path}")
    logger.info("All required directories are present.")


def validate_input_files():
    """Ensure there are input files to process."""
    logger.info(f"Validating input files in {METADATA_DIR}...")
    if not any(METADATA_DIR.glob("*_metadata_news_reports.txt")):
        logger.error(f"No metadata files found in {METADATA_DIR}")
        raise FileNotFoundError(f"No metadata files found in {METADATA_DIR}")
    logger.info("Metadata files found and ready for processing.")


def read_file(file_path):
    """Reads the contents of a text file."""
    try:
        with open(file_path, "r", encoding="utf-8") as file:
            return file.read()
    except Exception as e:
        logger.error(f"Error reading file {file_path}: {e}")
        return None


def write_file(file_path, content, mode="w"):
    """Writes content to a text file."""
    try:
        with open(file_path, mode, encoding="utf-8") as file:
            file.write(content)
        logger.info(f"Successfully wrote to file: {file_path}")
    except Exception as e:
        logger.error(f"Error writing file {file_path}: {e}")


def parse_metadata(metadata_content):
    """Parses metadata content and extracts <issue> fields."""
    logger.debug("Parsing metadata content...")
    issues = re.findall(
        r"<issue>.*?<title>(.*?)</title>.*?<summary>(.*?)</summary>.*?<relevantChunks>(.*?)</relevantChunks>.*?</issue>",
        metadata_content,
        re.DOTALL
    )
    logger.debug(f"Found {len(issues)} issues in metadata.")
    return issues


def extract_relevant_text(meeting_text, chunks):
    """Extracts relevant text based on chunk numbers."""
    logger.debug("Extracting relevant text based on chunk numbers...")
    relevant_text = []
    for chunk in chunks:
        start_index = (chunk - 1) * CHUNK_SIZE
        end_index = start_index + CHUNK_SIZE
        relevant_text.append(meeting_text[start_index:end_index])
    return "\n".join(relevant_text)


def generate_output_filename(date, title):
    """Generates the output file name based on date and title."""
    # Replace special characters in title for filesystem safety
    safe_title = re.sub(r"[^\w\s-]", "", title)  # Retain spaces and hyphens in the title
    file_name = f"{date}_{safe_title}.txt"

    if len(file_name) > MAX_FILENAME_LENGTH:
        raise ValueError(f"File name exceeds maximum allowed length ({MAX_FILENAME_LENGTH} characters): {file_name}")

    return file_name


def process_metadata_file(metadata_file, meeting_text_file):
    """Processes a single metadata file and its corresponding meeting text file."""
    logger.info(f"Processing metadata file: {metadata_file}")
    metadata_content = read_file(metadata_file)
    meeting_text = read_file(meeting_text_file)

    if not metadata_content or not meeting_text:
        logger.error(f"Skipping {metadata_file} due to missing or unreadable content.")
        return

    # Parse <issue> fields from metadata
    issues = parse_metadata(metadata_content)
    if not issues:
        logger.warning(f"No valid <issue> fields found in {metadata_file}")
        return

    # Create output folder for this metadata file
    metadata_name = metadata_file.stem.replace("_metadata_news_reports", "")
    output_folder = OUTPUT_DIR / metadata_name
    output_folder.mkdir(parents=True, exist_ok=True)

    for issue in issues:
        title = issue[0].strip()  # Extract the <title>
        relevant_chunks = [int(chunk) for chunk in issue[2].split(",")]

        # Extract relevant meeting text
        relevant_text = extract_relevant_text(meeting_text, relevant_chunks)

        # Generate output file name
        date = metadata_name.split(" - ")[0]  # Extract date from metadata filename
        try:
            file_name = generate_output_filename(date, title)
        except ValueError as e:
            logger.error(e)
            continue

        output_file = output_folder / file_name

        # Write only the relevant text to the output file
        write_file(output_file, relevant_text)

    # Move processed metadata file
    processed_metadata_path = PROCESSED_METADATA_DIR / metadata_file.name
    if processed_metadata_path.exists():
        logger.warning(f"Overwriting existing file in processed directory: {processed_metadata_path}")
        processed_metadata_path.unlink()  # Remove existing file if present
    shutil.move(metadata_file, processed_metadata_path)
    logger.info(f"Moved {metadata_file} to {processed_metadata_path}")

    # Move processed meeting text file
    processed_text_path = PROCESSED_TEXT_DIR / meeting_text_file.name
    if processed_text_path.exists():
        logger.warning(f"Overwriting existing file in processed directory: {processed_text_path}")
        processed_text_path.unlink()  # Remove existing file if present
    shutil.move(meeting_text_file, processed_text_path)
    logger.info(f"Moved {meeting_text_file} to {processed_text_path}")


def process_files():
    """Processes all metadata files in the metadata directory."""
    logger.info("Starting file processing...")
    validate_directories()  # Ensure all directories exist
    validate_input_files()  # Ensure there are input files to process

    for metadata_file in METADATA_DIR.glob("*_metadata_news_reports.txt"):
        # Find the corresponding meeting text file
        meeting_text_file = TEXT_DIR / metadata_file.name.replace("_metadata_news_reports.txt", ".txt")
        if not meeting_text_file.exists():
            logger.error(f"Meeting text file not found for {metadata_file}")
            continue

        # Process metadata file with its corresponding meeting text file
        process_metadata_file(metadata_file, meeting_text_file)

    logger.info("File processing completed successfully.")

def remove_newlines_in_files(directory):
    """Removes all newlines from files in the specified directory."""
    logger.info(f"Removing newlines from files in {directory}...")
    for file_path in directory.rglob("*.txt"):  # Recursively find all text files
        try:
            content = read_file(file_path)  # Read the file content
            if content:
                cleaned_content = content.replace("\n", "")  # Remove all newlines
                write_file(file_path, cleaned_content)  # Overwrite the file with cleaned content
                logger.info(f"Newlines removed from file: {file_path}")
            else:
                logger.warning(f"Skipping empty or unreadable file: {file_path}")
        except Exception as e:
            logger.error(f"Error processing file {file_path}: {e}")


if __name__ == "__main__":
    try:
        process_files()  # Process all files
        # Remove newlines in output files
        remove_newlines_in_files(OUTPUT_DIR)
        logger.info("All files processed and cleaned successfully. Exiting program.")
    except FileNotFoundError as e:
        logger.error(f"Critical error: {e}")
        exit(1)
    except Exception as e:
        logger.error(f"Unexpected error occurred: {e}")
        exit(1)