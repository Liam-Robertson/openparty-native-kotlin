import os
import logging
import shutil
from openai import OpenAI
import re
import time

# Set up OpenAI client
client = OpenAI()

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler()]
)
logger = logging.getLogger(__name__)

# Directories (relative to script location)
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
INPUT_DIR = os.path.join(SCRIPT_DIR, "../createChunkMetadataFiles/outputIssueMetadataFiles")
PROCESSED_DIR = os.path.join(SCRIPT_DIR, "../createChunkMetadataFiles/processedMetadataFiles")
OUTPUT_DIR = os.path.join(SCRIPT_DIR, "outputReportSummaryFiles")

# Constants
MAX_RETRIES = 5
INITIAL_BACKOFF = 1
MODEL = "gpt-4o"  # Use the correct OpenAI model


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
        logger.info(f"File written to {file_path}")
    except Exception as e:
        logger.error(f"Error writing file {file_path}: {e}")


def call_openai_with_retries(prompt):
    """Calls the OpenAI API with retries on failure."""
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


def process_metadata_file(metadata_content):
    """Processes a metadata file by generating news reports."""
    prompt = f"""
I have a long text file about local government issues, for now let's call it governmentIssues.txt

I created a metadata file about governmentIssues.txt. The metadata file was created by doing this: 
- Split governmentIssues.txt into chunks of 6000 characters
- Iterate through the chunks, feeding each to an AI as a prompt
- The AI would then read the chunk, identify the key issues in that chunk and for each issue it would do this: 
    - Mention what chunk that issue is in, identify the topic of that issue, create a summary of that issue
- The AI would do this for each chunk, appending each output xml issue to the metadata file

An example of a metadata file would look like this:
<issue>
    <chunk>1</chunk>
    <topic>Housing Emergency Action Plan</topic>
    <summary>The council discussed a draft housing emergency action plan in response to a housing emergency declared in West Lothian on May 28, 2024. The plan aims to address key areas such as reducing costs, improving income generation, and enhancing homeless services, especially for vulnerable groups like young people and those with addictions. The plan was developed without additional funding and will be submitted to the Scottish Parliament for consideration as part of an investigation by the Local Government, Housing and Planning Committee.</summary>
</issue>
<issue>
    <chunk>2</chunk>
    <topic>Meeting with the Minister for Housing</topic>
    <summary>The council had a meeting on July 8 with the Minister for Housing to discuss the housing emergency in West Lothian. During the meeting, the council presented several key issues and requests, including additional funding for new housing supply and suspension of certain legislative provisions. The Minister expressed willingness to consider these issues and showed interest in further discussions.</summary>
</issue>
.....

Each issue has these fields: chunk, topic, summary.

chunk represents the current chunk the issue is in e.g. chunk1, chunk 12 etc
    - In this case, chunk1 would represent the first 6000 characters in governmentIssues.txt i.e. characters 0-6000
    - Chunk2 would represent the 12th block of 6000 characters i.e. characters 72000-78000
Topic would be the topic covered in that particular section, for example education
summary is a summary of the content of the chunk. For example if that chunk discussed education then the summary might be a brief statement about teacher's pay. 

- This is everything I've done so far. However now I need to group some of these chunks by issue. 
- For example, if I have lots of chunks which are about housing, I wouldn't want to keep them all separate, I would want to group them together
- Likewise if I had a bunch of issues about education, I would want them all grouped together. 
- To do this, I want you to create a new metadata file, which takes in the old metadata file but groups all the chunks by issue
- The output of the new metadata file should be stored in xml and have this format: 

<issue>
    <title>{{content}}</title>
    <summary>{{content}}</summary>
    <relevantChunks>{{content}}</relevantChunks>
</issue>

Here is an example of what an issue might look like: 

<issue>
    <title>West Lothian Council Tackles Escalating Housing Crisis Amidst Soaring Demand</title>
    <summary>West Lothian Council declares a housing emergency, unveiling an action plan to address soaring demand and seeking government support amid rising cost pressures and rental sector constraints.</summary>
    <relevantChunks>3,6,4,18</relevantChunks>
</issue>

- In the above case, it shows that chunks 3,6,4,18 are all related to "West Lothian Council Tackles Escalating Housing Crisis Amidst Soaring Demand"
- The point of this is that it tells the user what the title of the issue should be, what the summary of this issue is and what the relevant chunks associated with this issue are 
- For example in the input metadata file, if chunks 3,6,24,27 were all related to education, then you might have an issue that looked like this: 

<issue>  
    <title>West Lothian Council Confronts Overcrowded Classrooms and Strained Resources in Schools</title>  
    <summary>West Lothian Council highlights a growing education crisis, with schools facing overcrowded classrooms, teacher shortages, and inadequate resources. A new strategic framework is proposed to address these challenges, prioritizing teacher recruitment, expanded facilities, and enhanced support for students, while seeking additional funding from the government to ensure equitable education for all.</summary>  
    <relevantChunks>3,6,24,27</relevantChunks>  
</issue>  

[IMPORTANT]
- Relevant chunks should look like this: 
    <relevantChunks>3,6,24,27</relevantChunks>  
- Not like this: 
    <relevantChunks>3, 6, 24, 27</relevantChunks>  
- i.e. don't have spaces between commas

- By looking at this, the user would know that there is an issue with overcrowded classrooms, what the summary of this issue is, and that if they wanted to find the council meeting text related to this, it would be in chunks 3,6,24,27

I want you to do this - go through the input metadata file, group the related issues together, tell me the relevant chunks for each issue and output in this format: 
<issue>
    <title>{{content}}</title>
    <summary>{{content}}</summary>
    <relevantChunks>{{content}}</relevantChunks>
</issue>

Also you don't need to split this up into multiple reports. If all the issues in all the chunks fit nicely into one topic like education or social care, then you can just group all the issuses into that one news reports. I don't need you to create lots of news reports if they would all be better in a single report. 

Here is the input metadata file - {metadata_content}
"""
    return call_openai_with_retries(prompt)


def validate_directories_and_files():
    """Validate that all required directories and files exist."""
    # Ensure required directories exist
    for directory in [INPUT_DIR, PROCESSED_DIR, OUTPUT_DIR]:
        if not os.path.exists(directory):
            raise FileNotFoundError(f"Required directory not found: {directory}")

    # Ensure there are input files
    input_files = [f for f in os.listdir(INPUT_DIR) if f.endswith(".txt")]
    if not input_files:
        raise FileNotFoundError(f"No text files found in input directory: {INPUT_DIR}")


def clean_output_file(file_path):
    """
    Cleans the output text file to retain only XML-like content.
    """
    try:
        with open(file_path, "r", encoding="utf-8") as file:
            content = file.read()

        # Use a regex to extract all content enclosed in XML-like tags
        cleaned_content = "\n".join(re.findall(r"<issue>.*?</issue>", content, re.DOTALL))

        # Overwrite the file with the cleaned content
        with open(file_path, "w", encoding="utf-8") as file:
            file.write(cleaned_content)
        logger.info(f"Cleaned output file: {file_path}")
    except Exception as e:
        logger.error(f"Error cleaning output file {file_path}: {e}")

def process_files():
    """Processes all files in the input directory."""
    # Validate directories and input files
    validate_directories_and_files()

    for file_name in os.listdir(INPUT_DIR):
        input_path = os.path.join(INPUT_DIR, file_name)

        # Skip non-text files
        if not file_name.endswith(".txt"):
            continue

        logger.info(f"Processing file: {file_name}")
        metadata_content = read_file(input_path)

        if not metadata_content:
            logger.warning(f"Skipping empty or unreadable file: {file_name}")
            continue

        # Generate news reports using the AI
        news_reports = process_metadata_file(metadata_content)
        if not news_reports:
            logger.warning(f"Failed to generate news reports for file: {file_name}")
            continue

        # Write the output as plain text
        output_file_name = f"{os.path.splitext(file_name)[0]}_news_reports.txt"
        output_path = os.path.join(OUTPUT_DIR, output_file_name)
        write_file(output_path, news_reports)

        # Clean the output file to retain only XML
        clean_output_file(output_path)

        # Move processed input file
        processed_path = os.path.join(PROCESSED_DIR, file_name)
        shutil.move(input_path, processed_path)
        logger.info(f"Moved processed file to {processed_path}")


if __name__ == "__main__":
    try:
        process_files()
    except FileNotFoundError as e:
        logger.error(e)
        exit(1)
