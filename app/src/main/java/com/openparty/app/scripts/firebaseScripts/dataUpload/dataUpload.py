import firebase_admin
from firebase_admin import credentials, firestore
import os
import json
import sys
from datetime import datetime, timezone
import argparse

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
BASE_RESOURCES_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "resources"))
DUMMY_DATA_DIR = os.path.join(BASE_RESOURCES_DIR, "dummyData")
REAL_DATA_DIR = os.path.join(BASE_RESOURCES_DIR, "realData")

ARTICLE_TEXT_SUBDIR = os.path.join("input", "articleText")
DISCUSSION_CONTENT_SUBDIR = os.path.join("input", "discussionText")

UPLOAD_ORDER = [
    'comment_votes.json',
    'comments.json',
    'council_meeting_votes.json',
    'council_meetings.json',
    'discussion_votes.json',
    'discussions.json',
    'users.json'
]

def initialize_firebase():
    service_account_path = os.getenv('OpenPartyFirbaseCredentials')
    if not service_account_path or not os.path.exists(service_account_path):
        print("Error: The service account key file is not found. Please set the OpenPartyFirbaseCredentials environment variable.")
        sys.exit(1)
    cred = credentials.Certificate(service_account_path)
    firebase_admin.initialize_app(cred)
    return firestore.client()

def upload_users(db, data):
    collection_ref = db.collection('users')
    for doc in data:
        user_id = doc.get('userId')
        if not user_id:
            print(f"Error: Missing 'userId' in user data: {doc}")
            sys.exit(1)
        try:
            collection_ref.document(user_id).set(doc)
        except Exception as e:
            print(f"Error uploading user with ID {user_id}: {e}")
            sys.exit(1)
    print(f"Uploaded {len(data)} users successfully.")

def upload_council_meetings(db, data, data_dir):
    collection_ref = db.collection('council_meetings')
    for meeting in data:
        meeting_data = meeting.copy()
        meeting_id = meeting_data.get('councilMeetingId')
        if not meeting_id:
            print(f"Error: Missing 'councilMeetingId' in council meeting data: {meeting}")
            sys.exit(1)
        timestamp_str = meeting_data.get('timestamp')
        if timestamp_str and isinstance(timestamp_str, str):
            try:
                meeting_data['timestamp'] = datetime.fromisoformat(
                    timestamp_str.replace("Z", "+00:00")
                ).replace(tzinfo=timezone.utc)
            except ValueError as e:
                print(f"Error: Invalid timestamp format in council meeting ID {meeting_id}: {e}")
                sys.exit(1)
        content_path = meeting_data.pop('contentText', None)
        if content_path and os.path.basename(content_path):
            absolute_content_path = os.path.join(data_dir, ARTICLE_TEXT_SUBDIR, os.path.basename(content_path))
            if not os.path.exists(absolute_content_path):
                print(f"Error: Content file not found for council meeting ID {meeting_id}: {absolute_content_path}")
                sys.exit(1)
            try:
                with open(absolute_content_path, 'r', encoding='utf-8') as content_file:
                    meeting_data['contentText'] = content_file.read()
            except Exception as e:
                print(f"Error: Unable to read content file for council meeting ID {meeting_id}: {e}")
                sys.exit(1)
        try:
            collection_ref.document(meeting_id).set(meeting_data)
        except Exception as e:
            print(f"Error uploading council meeting with ID {meeting_id}: {e}")
            sys.exit(1)
    print(f"Uploaded {len(data)} council meetings successfully.")

def upload_council_meeting_votes(db, data):
    collection_ref = db.collection('council_meeting_votes')
    for doc in data:
        doc_data = doc.copy()
        vote_id = doc_data.get('voteId')
        if not vote_id:
            print(f"Error: Missing 'voteId' in council meeting vote data: {doc}")
            sys.exit(1)
        timestamp_str = doc_data.get('timestamp')
        if timestamp_str and isinstance(timestamp_str, str):
            try:
                doc_data['timestamp'] = datetime.fromisoformat(
                    timestamp_str.replace("Z", "+00:00")
                ).replace(tzinfo=timezone.utc)
            except ValueError as e:
                print(f"Error: Invalid timestamp format in council meeting vote ID {vote_id}: {e}")
                sys.exit(1)
        try:
            collection_ref.document(vote_id).set(doc_data)
        except Exception as e:
            print(f"Error uploading council meeting vote with ID {vote_id}: {e}")
            sys.exit(1)
    print(f"Uploaded {len(data)} council meeting votes successfully.")

def upload_discussions(db, data, data_dir):
    collection_ref = db.collection('discussions')
    for discussion in data:
        discussion_data = discussion.copy()
        discussion_id = discussion_data.get('discussionId')
        if not discussion_id:
            print(f"Error: Missing 'discussionId' in discussion data: {discussion}")
            sys.exit(1)
        timestamp_str = discussion_data.get('timestamp')
        if timestamp_str and isinstance(timestamp_str, str):
            try:
                discussion_data['timestamp'] = datetime.fromisoformat(
                    timestamp_str.replace("Z", "+00:00")
                ).replace(tzinfo=timezone.utc)
            except ValueError as e:
                print(f"Error: Invalid timestamp format in discussion ID {discussion_id}: {e}")
                sys.exit(1)
        content_path = discussion_data.pop('contentText', None)
        if content_path and os.path.basename(content_path):
            absolute_content_path = os.path.join(data_dir, DISCUSSION_CONTENT_SUBDIR, os.path.basename(content_path))
            if not os.path.exists(absolute_content_path):
                print(f"Error: Content JSON file not found for discussion ID {discussion_id}: {absolute_content_path}")
                sys.exit(1)
            try:
                with open(absolute_content_path, 'r', encoding='utf-8') as content_file:
                    content_json = json.load(content_file)
                    discussion_data['contentText'] = content_json.get('content', '')
            except Exception as e:
                print(f"Error: Unable to read or parse content JSON file for discussion ID {discussion_id}: {e}")
                sys.exit(1)
        try:
            collection_ref.document(discussion_id).set(discussion_data)
        except Exception as e:
            print(f"Error uploading discussion with ID {discussion_id}: {e}")
            sys.exit(1)
    print(f"Uploaded {len(data)} discussions successfully.")

def upload_discussion_votes(db, data):
    collection_ref = db.collection('discussion_votes')
    for doc in data:
        doc_data = doc.copy()
        vote_id = doc_data.get('voteId')
        if not vote_id:
            print(f"Error: Missing 'voteId' in discussion vote data: {doc}")
            sys.exit(1)
        timestamp_str = doc_data.get('timestamp')
        if timestamp_str and isinstance(timestamp_str, str):
            try:
                doc_data['timestamp'] = datetime.fromisoformat(
                    timestamp_str.replace("Z", "+00:00")
                ).replace(tzinfo=timezone.utc)
            except ValueError as e:
                print(f"Error: Invalid timestamp format in discussion vote ID {vote_id}: {e}")
                sys.exit(1)
        try:
            collection_ref.document(vote_id).set(doc_data)
        except Exception as e:
            print(f"Error uploading discussion vote with ID {vote_id}: {e}")
            sys.exit(1)
    print(f"Uploaded {len(data)} discussion votes successfully.")

def upload_comments(db, data):
    collection_ref = db.collection('comments')
    for doc in data:
        doc_data = doc.copy()
        comment_id = doc_data.get('commentId')
        if not comment_id:
            print(f"Error: Missing 'commentId' in comment data: {doc}")
            sys.exit(1)
        timestamp_str = doc_data.get('timestamp')
        if timestamp_str and isinstance(timestamp_str, str):
            try:
                doc_data['timestamp'] = datetime.fromisoformat(
                    timestamp_str.replace("Z", "+00:00")
                ).replace(tzinfo=timezone.utc)
            except ValueError as e:
                print(f"Error: Invalid timestamp format in comment ID {comment_id}: {e}")
                sys.exit(1)
        try:
            collection_ref.document(comment_id).set(doc_data)
        except Exception as e:
            print(f"Error uploading comment with ID {comment_id}: {e}")
            sys.exit(1)
    print(f"Uploaded {len(data)} comments successfully.")

def upload_comment_votes(db, data):
    collection_ref = db.collection('comment_votes')
    for doc in data:
        doc_data = doc.copy()
        vote_id = doc_data.get('voteId')
        if not vote_id:
            print(f"Error: Missing 'voteId' in comment vote data: {doc}")
            sys.exit(1)
        timestamp_str = doc_data.get('timestamp')
        if timestamp_str and isinstance(timestamp_str, str):
            try:
                doc_data['timestamp'] = datetime.fromisoformat(
                    timestamp_str.replace("Z", "+00:00")
                ).replace(tzinfo=timezone.utc)
            except ValueError as e:
                print(f"Error: Invalid timestamp format in comment vote ID {vote_id}: {e}")
                sys.exit(1)
        try:
            collection_ref.document(vote_id).set(doc_data)
        except Exception as e:
            print(f"Error uploading comment vote with ID {vote_id}: {e}")
            sys.exit(1)
    print(f"Uploaded {len(data)} comment votes successfully.")

def process_json_file(db, file_path, data_dir):
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except Exception as e:
        print(f"Error: Unable to read JSON file {file_path}: {e}")
        sys.exit(1)
    filename = os.path.basename(file_path)
    if filename == 'council_meetings.json':
        upload_council_meetings(db, data, data_dir)
    elif filename == 'council_meeting_votes.json':
        upload_council_meeting_votes(db, data)
    elif filename == 'discussions.json':
        upload_discussions(db, data, data_dir)
    elif filename == 'discussion_votes.json':
        upload_discussion_votes(db, data)
    elif filename == 'comments.json':
        upload_comments(db, data)
    elif filename == 'comment_votes.json':
        upload_comment_votes(db, data)
    elif filename == 'users.json':
        upload_users(db, data)
    else:
        print(f"Error: Unknown JSON file '{filename}'.")
        sys.exit(1)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--dummy', choices=['y'], default='n')
    args = parser.parse_args()
    if args.dummy == 'y':
        input_folder = DUMMY_DATA_DIR
    else:
        input_folder = REAL_DATA_DIR
    print(f"Using input folder: {input_folder}")
    if not os.path.exists(input_folder):
        print(f"Error: The specified folder '{input_folder}' does not exist.")
        sys.exit(1)
    db = initialize_firebase()
    try:
        available_files = [f for f in os.listdir(input_folder) if f.endswith('.json')]
    except Exception as e:
        print(f"Error: Unable to access input folder '{input_folder}': {e}")
        sys.exit(1)
    if not available_files:
        print(f"No JSON files found in the input folder '{input_folder}'.")
        sys.exit(0)
    for filename in UPLOAD_ORDER:
        if filename in available_files:
            file_path = os.path.join(input_folder, filename)
            print(f"Processing file: {file_path}")
            process_json_file(db, file_path, input_folder)
        else:
            print(f"File '{filename}' not found in '{input_folder}'. Skipping.")

if __name__ == '__main__':
    main()
