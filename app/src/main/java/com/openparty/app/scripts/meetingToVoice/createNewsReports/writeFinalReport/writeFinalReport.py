import os
import re
import shutil
import xml.etree.ElementTree as ET
import logging
from openai import OpenAI
import openai
import re
import html

# Configure logging
logging.basicConfig(
    filename='../script.log',
    filemode='a',
    format='%(asctime)s - %(levelname)s - %(message)s',
    level=logging.INFO
)

console_handler = logging.StreamHandler()
console_handler.setLevel(logging.INFO)
console_handler.setFormatter(logging.Formatter('%(asctime)s - %(levelname)s - %(message)s'))
logging.getLogger().addHandler(console_handler)

# Initialize OpenAI API key
api_key = os.getenv("OPENAI_API_KEY")
if not api_key:
    logging.error("OpenAI API key not found.")
    raise ValueError("OpenAI API key not found. Ensure 'OPENAI_API_KEY' is set in your environment.")

client = OpenAI(api_key=api_key)
openai.api_key = api_key

# Count files in a directory
def count_input_files(directory):
    file_count = 0
    for folder in os.listdir(directory):
        folder_path = os.path.join(directory, folder)
        if os.path.isdir(folder_path):
            file_count += len([f for f in os.listdir(folder_path) if os.path.isfile(os.path.join(folder_path, f))])
    logging.info(f"Found {file_count} input files in directory: {directory}")
    return file_count


def find_partial_match(input_text, target_text):
    """
    Tries to find a match for the target_text in the input_text.
    If no exact match is found, tries partial matches (first 50%, last 50%).
    Returns the match type and position or raises an error if no unique match is found.
    """
    def normalize_text(s):
        return ' '.join(s.lower().split())

    normalized_input = normalize_text(input_text)
    normalized_target = normalize_text(target_text)

    # Try exact match
    if normalized_target in normalized_input:
        matches = [m.start() for m in re.finditer(re.escape(normalized_target), normalized_input)]
        if len(matches) == 1:
            return "exact", matches[0]
        elif matches:
            raise ValueError(f"Match for '{target_text}' found but not unique.")

    # Try first 50% match
    first_half = normalized_target[:len(normalized_target) // 2]
    if first_half in normalized_input:
        matches = [m.start() for m in re.finditer(re.escape(first_half), normalized_input)]
        if len(matches) == 1:
            return "partial_first_half", matches[0]
        elif matches:
            raise ValueError(f"First-half match for '{target_text}' found but not unique.")

    # Try last 50% match
    last_half = normalized_target[len(normalized_target) // 2:]
    if last_half in normalized_input:
        matches = [m.start() for m in re.finditer(re.escape(last_half), normalized_input)]
        if len(matches) == 1:
            return "partial_last_half", matches[0]
        elif matches:
            raise ValueError(f"Last-half match for '{target_text}' found but not unique.")

    # No match found
    raise ValueError(f"No match could be found for '{target_text}'.")


def find_lines_between(file_path, start_line, end_line):
    """
    Finds the lines between `start_line` and `end_line` in the given file.
    Uses partial matching logic if exact matches are not found.
    """
    logging.info(f"Finding lines between '{start_line}' and '{end_line}' in file: {file_path}")
    with open(file_path, 'r', encoding='utf-8') as file:
        text = file.read()

    # Decode any HTML entities
    start_line = html.unescape(start_line.strip())
    end_line = html.unescape(end_line.strip())

    try:
        # Validate and find the positions of start_line and end_line
        match_type_start, start_idx = find_partial_match(text, start_line)
        match_type_end, end_idx = find_partial_match(text, end_line)

        logging.debug(f"start_idx: {start_idx}, end_idx: {end_idx}")

        # Check if start_line occurs after end_line
        if start_idx > end_idx:
            raise ValueError(f"lineStart occurs after lineEnd.\nLine start: '{start_line}'\nLine end:'{end_line}'\nIn file {file_path}")

        # Return the substring from start_idx to end_idx
        return text[start_idx:end_idx]

    except ValueError as e:
        logging.error(f"Validation error while finding lines:\n{e}")
        raise


# Create a report section with OpenAI
def create_report_section(client, raw_meeting_text, metadata_text, current_metadata_section, current_council_section, report_so_far):
    logging.info("Creating report section using OpenAI API.")
    prompt = f"""
I want you to write a report that covers a local council meeting in the UK. 

I'm going to ask you to write this report section by section and I'm going to store the report in a text file called reportSoFar.txt

To do this, I have split the council meeting text up into sub topics and I will feed you the raw text for each sub topic one by one.

For example, if the overall council meeting focusses on education, the raw text I give you might be a specific part of the meeting that focussed on teacher's pay. Let's call this sub section of raw text councilSubtopic.txt

You will then take the sub topic text councilSubtopic.txt and use it to write the section of the report relating to that sub topic.

I will give you reportSoFar.txt so that you understand what the report has covered so far and you understand how to integrate your new text into the overall report. 

I want the report to follow these guidelines: 
- Don't give any opening or closing sentences (for example, don't start with "Good evening")
- Write it in a way that it can be spoken (i.e. it should be written like a tv news report that is to be spoken by a reporter, not written like a newspaper report that is going to be read)
- [IMPORTANT] The only text you write should be text that will be read out in a report. Don't add any text that will not be read out. For example: 
    - Don't use placeholder text e.g. don't write - [Wait for audience applause]
- If a council member says a quote you think is important, you might want to write the quote out and quote that person 
- The report should focus on news worthy items. For example if there is a section where the councillors praise each other then leave that out since it's not a newsworthy item

Any reader who reads the report should know this:
- They would understand what happened in the council meeting
- They would understand the big important points and issues that were covered
- They should understand roughly what individual councillors believe and voted for 
- They should understand the impact this will have on the community

Notes: 
- It would be very hard to have a report that covers what every council member said or believed. Instead, just focus on the important parts - if a council member says something important or news worthy, make sure the reader understands who said it and why
- In the report don't mention any formalities, you need to only focus on newsworthy items. For this reason you should exclude things such as these: 
    - Mentions that the meeting will be broadcast online
    - Mentions that there is going to be breaks for food etc 
    - Any calls for absences
    
Important:
- Skip any introductions that are mentioned, just straight to the first newsworthy items

Important:
- It should cover and summarise the main points of what happened in the council meeting while also adding some engaging commentary 
- The tone of the report should be this: 
    - Conversational
    - Engaging
    - Funny 
- In short, it has to be an engaging report but it still has to cover all the real points and be a factual report 

Here is the input: 

reportSoFar.txt:
{report_so_far}

--

councilSubtopic.txt:
{current_council_section}

"""
    try:
        response = client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {"role": "system", "content": "You are a helpful assistant."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=1500,
            temperature=0.7
        )
        logging.info("Report section created successfully.")
        return response.choices[0].message.content.strip()
    except Exception as e:
        logging.error(f"Error generating report section: {e}")
        raise

# Process a single metadata file
def process_metadata_file(metadata_file, output_dir, metadata_dir, meeting_text_dir, processed_metadata_dir):
    logging.info(f"Processing metadata file: {metadata_file}")
    try:
        tree = ET.parse(metadata_file)
        root = tree.getroot()

        sections = root.find('sections')

        # Determine the corresponding meeting text file dynamically
        relative_path = os.path.relpath(metadata_file, metadata_dir)
        meeting_file = os.path.join(meeting_text_dir, relative_path.replace('.xml', '.txt'))

        logging.info(f"Looking for corresponding meeting text file for metadata file: {metadata_file}")
        logging.info(f"Expected meeting text file location: {meeting_file}")

        if not os.path.exists(meeting_file):
            logging.error(f"Meeting text file not found for metadata: {metadata_file}")
            raise FileNotFoundError(f"Corresponding council meeting text file for {metadata_file} not found at {meeting_file}.")

        logging.info(f"Found corresponding meeting text file: {meeting_file}")

        with open(meeting_file, 'r', encoding='utf-8') as f:
            raw_meeting_text = f.read()

        report_so_far = ""

        for section in sections.findall('section'):
            summary = section.find('summary').text
            line_start = section.find('lineStart').text
            line_end = section.find('lineEnd').text

            current_metadata_section = ET.tostring(section, encoding='unicode', method='xml')
            current_council_section = find_lines_between(meeting_file, line_start, line_end)

            section_report = create_report_section(
                client,
                raw_meeting_text,
                "",
                current_metadata_section,
                current_council_section,
                report_so_far
            )
            report_so_far += section_report + "\n\n"

        output_path = os.path.join(output_dir, os.path.basename(metadata_file).replace('.xml', '.txt'))
        os.makedirs(os.path.dirname(output_path), exist_ok=True)
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(report_so_far)
        logging.info(f"Saved processed report to: {output_path}")

        # Move the processed file to the processed metadata directory
        processed_file_path = os.path.join(processed_metadata_dir, os.path.relpath(metadata_file, metadata_dir))
        os.makedirs(os.path.dirname(processed_file_path), exist_ok=True)
        shutil.move(metadata_file, processed_file_path)
        logging.info(f"Moved processed metadata file to: {processed_file_path}")

        # Check if the original folder is now empty and delete it if it is
        original_folder = os.path.dirname(metadata_file)
        if not os.listdir(original_folder):  # Folder is empty
            os.rmdir(original_folder)
            logging.info(f"Deleted empty folder: {original_folder}")

    except ET.ParseError as e:
        logging.error(f"XML parsing error in file {metadata_file}: {e}")
        raise
    except Exception as e:
        logging.error(f"Error:\n{e}")
        raise


# Move processed files and clean up empty folders
def move_processed_files(input_dir, processed_dir):
    logging.info(f"Moving processed files from {input_dir} to {processed_dir}.")
    for folder in os.listdir(input_dir):
        folder_path = os.path.join(input_dir, folder)
        if os.path.isdir(folder_path):
            processed_folder_path = os.path.join(processed_dir, folder)
            os.makedirs(processed_folder_path, exist_ok=True)

            for file in os.listdir(folder_path):
                file_path = os.path.join(folder_path, file)
                try:
                    shutil.move(file_path, os.path.join(processed_folder_path, file))
                    logging.info(f"Moved processed file: {file_path} to {processed_folder_path}")

                except Exception as e:
                    logging.error(f"Error moving file {file_path}: {e}")
                    raise

            if not os.listdir(folder_path):  # If the folder is now empty
                os.rmdir(folder_path)
                logging.info(f"Deleted empty folder: {folder_path}")

# Main function
def main():
    logging.info("Script started.")
    script_dir = os.path.dirname(os.path.abspath(__file__))
    blueprint_root_dir = os.path.join(script_dir, "..", "createReportBlueprintFiles", "outputChunkMetadataFiles")
    output_dir = os.path.join(script_dir, "..", "..", "finalOutput", "unprocessedReportTextFiles")
    processed_metadata_dir = os.path.join(script_dir, "..", "createReportBlueprintFiles", "processedSectionChunkFiles")
    meeting_raw_text_dir = os.path.join(script_dir, "..", "..", "breakMeetingTextToSections", "groupRawTextByIssue", "processedRawTextSections")
    processed_meeting_text_dir = os.path.join(script_dir, "..", "..", "breakMeetingTextToSections", "groupRawTextByIssue", "processedProcessedRawTextSections")

    required_directories = {
        "blueprint_root_dir": blueprint_root_dir,
        "output_dir": output_dir,
        "processed_metadata_dir": processed_metadata_dir,
        "meeting_raw_text_dir": meeting_raw_text_dir
    }

    missing_dirs = [name for name, path in required_directories.items() if not os.path.exists(path)]
    if missing_dirs:
        for missing in missing_dirs:
            logging.error(f"Missing required directory: {missing} -> {required_directories[missing]}")
        raise FileNotFoundError(f"Missing required directories: {', '.join(missing_dirs)}")

    if count_input_files(blueprint_root_dir) == 0:
        logging.error(f"No input files found in metadata directory: {blueprint_root_dir}")
        raise FileNotFoundError(f"No input files found in metadata directory: {blueprint_root_dir}")

    try:
        for folder in os.listdir(blueprint_root_dir):
            folder_path = os.path.join(blueprint_root_dir, folder)
            for metadata_file in os.listdir(folder_path):
                metadata_file_path = os.path.join(folder_path, metadata_file)
                try:
                    process_metadata_file(metadata_file_path, output_dir, blueprint_root_dir, meeting_raw_text_dir, processed_metadata_dir)
                except Exception as e:
                    logging.error(f"Error processing file {metadata_file_path}: {e}")
                    raise

        move_processed_files(meeting_raw_text_dir, processed_meeting_text_dir)

    except Exception as e:
        logging.error(f"Script failed due to error: {e}")
        raise

    logging.info("Script finished.")

# Entry point
if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        logging.error(f"Script terminated with an error: {e}")
        exit(1)
