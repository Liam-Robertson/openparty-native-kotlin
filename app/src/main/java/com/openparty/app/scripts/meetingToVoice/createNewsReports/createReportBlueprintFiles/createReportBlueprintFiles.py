import os
import logging
import re
import html
import xml.etree.ElementTree as ET
from openai import OpenAI

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler()]
)
logger = logging.getLogger(__name__)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
INPUT_DIR = os.path.join(BASE_DIR, "../../breakMeetingTextToSections/groupRawTextByIssue/outputSectionChunks")
OUTPUT_DIR = os.path.join(BASE_DIR, "./outputChunkMetadataFiles")
PROCESSED_DIR = os.path.join(BASE_DIR, "../../breakMeetingTextToSections/groupRawTextByIssue/processedRawTextSections")

client = OpenAI()
MODEL = "gpt-4o"
MAX_RETRIES = 5
INITIAL_BACKOFF = 1

PROMPT_TEMPLATE = """
I am writing a report that covers what happens in a local council meeting. It will follow  these guidelines: 
- The report should be engaging
- It should be written as though it were going to be read on TV not written in a newspaper (i.e. it will be written as though it were going to be spoken)
- It should cover and summarise the main points of what happened in the council meeting while also adding some engaging commentary 

Important:
- It should cover and summarise the main points of what happened in the council meeting while also adding some engaging commentary 
- The tone of the report should be this: 
    - Conversational
    - Engaging
    - Funny 
- In short, it has to be an engaging report but it still has to cover all the real points and be a factual report 

Important:
- The report should be on this topic - {{reportSubject}}.
- Any text that is not directly related to that subject should be ignored.

Any reader who reads the report should know this:
- They would understand what happened in the council meeting
- They would understand the big important points and issues that were covered
- They should understand roughly what individual councillors believe and voted for 
- They should understand the impact this will have on the community

- It would be very hard to have a report that covers what every council member said or believed. 
- Instead, just focus on the important parts. If a council member says something important or news worthy, make sure the reader understands who said it and why

- Skip any introductory sections of the council meeting and just to the first news items
- I only want to focus on  things that are newsworthy
- The introductory section that you should skip likely covers things like this:
    - Food breaks
    - That the meeting is being broadcast online
    - What the agenda is
    - Calls for absences 

I'm doing it based on a piece of text from a local council meeting. I'll give you this piece of text at the bottom. For now, I'll refer to the text file as councilMeeting.txt

I want you to analyse the text, think about how you would write the report, then give me a blueprint of how the report should be structured. 

You should output this as xml. I will give you examples below of what this might look like so you can better understand it. The format of your output should be this: 

<newsReport>
    <headline>HEADLINE</headline>
    <overview>OVERVIEW</overview>
    <sections>
        <section>
            <summary>SUMMARY</summary>
            <lineStart>LINE START</lineStart>
            <lineEnd>LINE END</lineEnd>
        </section>
        <section>
            <summary>SUMMARY</summary>
            <lineStart>LINE START</lineStart>
            <lineEnd>LINE END</lineEnd>
        </section>
        ...
    </sections>
</newsReport>

For example, if the text was on housing, you might give an output that looks like this: 

<newsReport>
    <headline>West Lothian Council Declares Housing Emergency Amid Nationwide Crisis</headline>
    <overview>
        The report discusses West Lothian Council's declaration of a housing emergency, the action plan developed to address it, and the discussions held during a council meeting. It includes financial aspects, political commentary, third-sector engagement, and systemic housing challenges affecting Scotland.
    </overview>
    <sections>
        <section>
            <summary>The Housing Emergency Action Plan focuses on reducing costs, generating income, supporting vulnerable groups, and fostering partnerships with the third sector. Planned submission to the Scottish Parliament and the Local Government, Housing, and Planning Committee for review.</summary>
            <lineStart>Good afternoon, welcome to the meeting on hou</lineStart>
            <lineEnd>There will be some time lags before a few seconds as the </lineEnd>
        </section>
        <section>
            <summary>During the council meeting chaired by Councillor George Paul, hybrid arrangements were utilized, and live broadcasting was conducted. Updates included discussions on homeless application trends and challenges in the private rental sector.</summary>
            <lineStart>consideration and around the horizon scanning that we have done ourselves</lineStart>
            <lineEnd>those most in need, who quite often we find the third sector do </lineEnd>
        </section>
    </sections>
</newsReport>

The <headline> tag is the headline of the news report
The <overview> tag is a summary of everything that will be in the news report 
The <section> tag represents a section that will be in the news report 
The <summary> tag represents a summary of what will be in that section 
The <lineStart> tag represents the line in the councilMeeting.txt file where this section begins. For example, if the "Housing Emergency Action Plan" section begins by saying "Yeah, so we have just completed this year's RRT" then you would have <lineStart>Yeah, so we have just completed this year's RRT</lineStart>
The <lineEnd> tag represents the line in the councilMeeting.txt file where this section ends. For example, the line where "Housing Emergency Action Plan" section ends

- The point of <lineStart> and <lineEnd> is that I need to be able to identify the start and end of each section you're talking about. 
- I will use <lineStart> and <lineEnd> to identify the chunk of text related to that section of the blueprint
- For example, if I'm writing the report part about the "Housing Emergency Action Plan", I will look at  <lineStart> and <lineEnd> to know which part of the councilMeeting.txt relates to "Housing Emergency Action Plan"
- The text you put in <lineStart> and <lineEnd> must be exactly correct. Verify that the line start and end is correct before writing it
[IMPORTANT] Don't fix any mistakes in <lineStart> and <lineEnd>
- For example, if the raw text has this: "process of developing options for delivery of that. That funding"
- Then don't change it to this: "process of developing options for delivery of that funding"
- The input in the raw text should always be taken exactly as it is, even if there are mistakes in it 

councilMeeting.txt - {{input text}}
"""

def validate_directories():
    for dir_path in [INPUT_DIR, OUTPUT_DIR]:
        if not os.path.exists(dir_path):
            raise FileNotFoundError(f"Required directory does not exist: {dir_path}")
    has_txt_files = any(
        file.endswith(".txt")
        for root, _, files in os.walk(INPUT_DIR)
        for file in files
    )
    if not has_txt_files:
        raise FileNotFoundError(f"No text files found in input directory or its subdirectories: {INPUT_DIR}")

def read_file(file_path):
    try:
        with open(file_path, "r", encoding="utf-8") as file:
            return file.read()
    except Exception as e:
        logger.error(f"Error reading file {file_path}: {e}")
        return None

def extract_report_subject(file_name):
    """
    Extracts the {issue} part of the file name.
    Assumes file name format is {date}_{issue}.txt.
    """
    base_name = os.path.basename(file_name)  # Get the base file name
    name_without_extension = os.path.splitext(base_name)[0]  # Remove the extension
    parts = name_without_extension.split("_")  # Split by underscore
    if len(parts) < 2:
        raise ValueError(f"File name format is invalid: {file_name}")
    return "_".join(parts[1:])  # Join everything after the date


def write_file(file_path, content, mode="w"):
    try:
        if os.path.exists(file_path):
            os.remove(file_path)  # Remove the file if it exists
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
                time.sleep(backoff * 2 ** retries)
            else:
                logger.error(f"Error during OpenAI call: {e}")
                return None
    logger.error("Max retries exceeded.")
    return None

def escape_special_characters_in_content(content):
    """
    Escapes special characters in the content of XML tags.
    """
    def escape_content(match):
        inner_content = match.group(1)
        inner_content = inner_content.replace("&", "&amp;")
        inner_content = inner_content.replace("<", "&lt;")
        inner_content = inner_content.replace(">", "&gt;")
        inner_content = inner_content.replace('"', "&quot;")
        inner_content = inner_content.replace("'", "&apos;")
        return f">{inner_content}<"

    # Escape content between XML tags
    escaped_content = re.sub(r">(.*?)<", escape_content, content, flags=re.DOTALL)
    return escaped_content

def clean_xml_content(content):
    match = re.search(r"<newsReport>.*</newsReport>", content, re.DOTALL)
    if not match:
        return ""
    return escape_special_characters_in_content(match.group(0))

def escape_special_characters(content):
    """
    Escapes special characters in the XML content.
    """
    content = content.replace("&", "&amp;")
    content = content.replace("<", "&lt;")
    content = content.replace(">", "&gt;")
    content = re.sub(r'"([^"]*?)"', r'&quot;\1&quot;', content)
    content = re.sub(r"'([^']*?)'", r"&apos;\1&apos;", content)
    return content

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

def process_chunk_file(input_file_path):
    input_text = read_file(input_file_path)
    if not input_text:
        logger.error(f"Skipping file {input_file_path} due to missing or unreadable content.")
        return

    try:
        report_subject = extract_report_subject(input_file_path)  # Extract report subject
    except ValueError as e:
        logger.error(e)
        return

    prompt_with_subject = PROMPT_TEMPLATE.replace("{{input text}}", input_text).replace("{{reportSubject}}", report_subject)
    response = call_openai_with_retries(prompt_with_subject)
    if not response:
        logger.error(f"Failed to generate metadata for {input_file_path}")
        return
    cleaned_response = clean_xml_content(response)
    if not cleaned_response:
        logger.error(f"Failed to extract valid XML from the response for {input_file_path}")
        return

    validation_failed = False  # Track validation status

    # Begin validation logic
    try:
        # Parse the XML
        root = ET.fromstring(cleaned_response)

        # For each section
        for section in root.findall('.//section'):
            line_start = section.find('lineStart').text
            line_end = section.find('lineEnd').text

            # Ensure line_start and line_end are not None
            if line_start is None or line_end is None:
                raise ValueError(f"lineStart or lineEnd is missing in a section.")

            # Unescape any HTML entities
            line_start_unescaped = html.unescape(line_start.strip())
            line_end_unescaped = html.unescape(line_end.strip())

            # Validate lineStart
            try:
                match_type_start, start_index = find_partial_match(input_text, line_start_unescaped)
            except ValueError as e:
                logger.error(f"Validation error for lineStart in {input_file_path}: {e}")
                raise

            # Validate lineEnd
            try:
                match_type_end, end_index = find_partial_match(input_text, line_end_unescaped)
            except ValueError as e:
                logger.error(f"Validation error for lineEnd in {input_file_path}: {e}")
                raise

            # Check that line_start occurs before line_end
            if start_index > end_index:
                raise ValueError(f"lineStart occurs after lineEnd.\nLine start: '{line_start}'\nLine end: '{line_end}'.")

    except (ET.ParseError, ValueError) as e:
        logger.error(f"Validation error:\n{e}")
        validation_failed = True  # Mark the validation as failed

    # Write the output file regardless of validation status
    relative_folder = os.path.relpath(os.path.dirname(input_file_path), INPUT_DIR)
    output_folder = os.path.join(OUTPUT_DIR, relative_folder)
    os.makedirs(output_folder, exist_ok=True)
    output_file_name = os.path.basename(input_file_path)
    output_file_path = os.path.join(output_folder, output_file_name)
    write_file(output_file_path, cleaned_response)

    if not validation_failed:
        processed_folder = os.path.join(PROCESSED_DIR, relative_folder)
        os.makedirs(processed_folder, exist_ok=True


def process_files():
    validate_directories()
    for root, _, files in os.walk(INPUT_DIR):
        for file_name in files:
            if file_name.endswith(".txt"):
                input_file_path = os.path.join(root, file_name)
                process_chunk_file(input_file_path)

if __name__ == "__main__":
    try:
        process_files()
    except FileNotFoundError as e:
        logger.error(e)
        exit(1)
