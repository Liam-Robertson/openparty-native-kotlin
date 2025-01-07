#!/usr/bin/env python3

import sys
import os
import json
import logging
import yaml

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)]
)

def error(msg):
    logging.error(msg)
    sys.exit(1)

mapping = {
    "CORE": "core",
    "DATABASE_OBJECTS": "notes/database_objects",
    "DI": "di",
    "NAVIGATION": "navigation",
    "ENGAGEMENT": "features/engagement",
    "NEWSFEED": "features/newsfeed",
    "SHARED": "features/shared",
    "STARTUP": "features/startup",
    "COMMENTS_SECTION": "features/engagement/feature_comments_section",
    "NEWSFEED_ENGAGEMENT": "features/engagement/feature_newsfeed_engagement",
    "CACHING": "features/newsfeed/feature_caching",
    "COUNCIL_MEETING": "features/newsfeed/feature_council_meeting",
    "DISCUSSIONS": "features/newsfeed/feature_discussions",
    "USER": "features/shared/feature_user",
    "WRITE_PROTECTION": "features/shared/feature_write_protection",
    "AUTHENTICATION": "features/startup/feature_authentication",
    "LOGIN": "features/startup/feature_login",
    "SPLASH": "features/startup/feature_splash",
    "VERIFICATION": "features/startup/verification",
    "EMAIL_VERIFICATION": "features/startup/verification/feature_email_verification",
    "LOCATION_VERIFICATION": "features/startup/verification/feature_location_verification",
    "MANUAL_VERIFICATION": "features/startup/verification/feature_manual_verification",
    "SCREEN_NAME_GENERATION": "features/startup/feature_screen_name_generation",
}

base_dir = os.path.dirname(os.path.abspath(__file__))
logging.info(f"Base directory resolved: {base_dir}")

input_yaml_path = os.path.join(base_dir, "inputFeatures.yaml")
if not os.path.exists(input_yaml_path):
    error("inputFeatures.yaml not found in the script's directory: " + input_yaml_path)

logging.info(f"Reading input YAML from: {input_yaml_path}")
try:
    with open(input_yaml_path, "r", encoding="utf-8") as f:
        data = yaml.safe_load(f)
except Exception as e:
    error(f"Error reading YAML file {input_yaml_path}: {e}")

if "features" not in data:
    error("'features' key not found in inputFeatures.yaml")

features = data["features"]
if not isinstance(features, list):
    error("'features' should be a list")

project_root = os.path.abspath(os.path.join(base_dir, "../../"))
logging.info(f"Project root resolved: {project_root}")

main_prompt_path = os.path.join(project_root, "notes", "prompts", "main prompt.txt")

if not os.path.exists(main_prompt_path):
    error("main prompt.txt not found at expected location: " + main_prompt_path)

logging.info(f"Main prompt file found at: {main_prompt_path}")

output_file = os.path.join(project_root, "notes", "prompts", "outputPrompt.txt")

def get_all_files(dir_path):
    file_list = []
    for root, dirs, files in os.walk(dir_path):
        for file in files:
            file_list.append(os.path.join(root, file))
    return file_list

collected_code = ""
for feature in features:
    feature_upper = feature.upper()
    if feature_upper not in mapping:
        error("Feature '" + feature + "' not recognized.")
    folder_name = mapping[feature_upper]
    feature_path = os.path.join(project_root, folder_name)
    if not os.path.exists(feature_path) or not os.path.isdir(feature_path):
        error("Directory for feature '" + feature + "' not found: " + feature_path)

    logging.info(f"Processing feature: {feature} (mapped to folder: {folder_name})")
    files = get_all_files(feature_path)
    for fpath in sorted(files):
        if not os.path.isfile(fpath):
            continue
        try:
            with open(fpath, "r", encoding="utf-8") as fc:
                content = fc.read()
        except Exception as e:
            error(f"Error reading file {fpath}: {e}")
        if collected_code:
            collected_code += "\n\n----\n\n"
        collected_code += content

try:
    with open(main_prompt_path, "r", encoding="utf-8") as mpf:
        main_content = mpf.read()
except Exception as e:
    error(f"Error reading main prompt file: {e}")

if "{{MAIN_CODE_BLOCK}}" not in main_content:
    error("{{MAIN_CODE_BLOCK}} not found in main prompt.")

main_content = main_content.replace("{{MAIN_CODE_BLOCK}}", collected_code)

try:
    with open(output_file, "w", encoding="utf-8") as outf:
        outf.write(main_content)
    logging.info(f"Output successfully written to: {output_file}")
except Exception as e:
    error(f"Error writing to output file: {e}")

logging.info("Script finished successfully without errors.")
