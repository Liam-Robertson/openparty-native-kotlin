import os
import json
import random
import string
from datetime import datetime, timedelta
import sys

SCRIPT_DIR = os.path.abspath(os.path.dirname(__file__))
RESOURCES_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "resources", "dummyData"))

COUNCIL_MEETINGS_PATH = os.path.abspath(os.path.join(RESOURCES_DIR, "council_meetings.json"))
COUNCIL_MEETING_VOTES_PATH = os.path.abspath(os.path.join(RESOURCES_DIR, "council_meeting_votes.json"))
COMMENTS_PATH = os.path.abspath(os.path.join(RESOURCES_DIR, "comments.json"))
COMMENT_VOTES_PATH = os.path.abspath(os.path.join(RESOURCES_DIR, "comment_votes.json"))
USERS_PATH = os.path.abspath(os.path.join(RESOURCES_DIR, "users.json"))

DISCUSSIONS_PATH = os.path.abspath(os.path.join(RESOURCES_DIR, "discussions.json"))
DISCUSSION_VOTES_PATH = os.path.abspath(os.path.join(RESOURCES_DIR, "discussion_votes.json"))

INPUT_DIR = os.path.abspath(os.path.join(RESOURCES_DIR, "input"))
INPUT_ARTICLES_PATH = os.path.abspath(os.path.join(INPUT_DIR, "inputArticles.txt"))
INPUT_DISCUSSIONS_PATH = os.path.abspath(os.path.join(INPUT_DIR, "inputDiscussions.txt"))
INPUT_COMMENT_TEXT_PATH = os.path.abspath(os.path.join(INPUT_DIR, "commentText.txt"))

PROCESSED_ARTICLES_PATH = os.path.abspath(os.path.join(RESOURCES_DIR, "processed", "processedArticles.txt"))
PROCESSED_DISCUSSIONS_PATH = os.path.abspath(os.path.join(RESOURCES_DIR, "processed", "processedDiscussions.txt"))

def error_and_exit(message):
    print(f"[ERROR] {message}")
    sys.exit(1)

def info_log(message):
    print(f"[INFO] {message}")

info_log("Starting script...")

if not os.path.isdir(RESOURCES_DIR):
    error_and_exit(f"The resources directory '{RESOURCES_DIR}' does not exist.")
if not os.path.isdir(INPUT_DIR):
    error_and_exit(f"The input directory '{INPUT_DIR}' does not exist.")
if not os.path.isfile(PROCESSED_ARTICLES_PATH):
    error_and_exit(f"The processed file '{PROCESSED_ARTICLES_PATH}' does not exist.")
if not os.path.isfile(PROCESSED_DISCUSSIONS_PATH):
    error_and_exit(f"The processed file '{PROCESSED_DISCUSSIONS_PATH}' does not exist.")

info_log(f"Found resources directory: {RESOURCES_DIR}")
info_log(f"Found input directory: {INPUT_DIR}")
info_log(f"Found processed articles file: {PROCESSED_ARTICLES_PATH}")
info_log(f"Found processed discussions file: {PROCESSED_DISCUSSIONS_PATH}")

if not os.path.isfile(USERS_PATH):
    error_and_exit(f"The users file '{USERS_PATH}' does not exist.")
info_log(f"Found users file: {USERS_PATH}")

if not os.path.isfile(INPUT_ARTICLES_PATH):
    error_and_exit(f"The input articles file '{INPUT_ARTICLES_PATH}' does not exist.")
info_log(f"Found input articles file: {INPUT_ARTICLES_PATH}")

if not os.path.isfile(INPUT_COMMENT_TEXT_PATH):
    error_and_exit(f"The input comment text file '{INPUT_COMMENT_TEXT_PATH}' does not exist.")
info_log(f"Found input comment text file: {INPUT_COMMENT_TEXT_PATH}")

has_discussions = os.path.isfile(INPUT_DISCUSSIONS_PATH)
if has_discussions:
    info_log(f"Found input discussions file: {INPUT_DISCUSSIONS_PATH}")
else:
    info_log("No discussions input file found. Will proceed without discussions.")

def load_or_create_json(filepath):
    if not os.path.exists(filepath):
        info_log(f"File '{filepath}' does not exist. Creating empty JSON file.")
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump([], f)
    else:
        info_log(f"Loading JSON from existing file: {filepath}")
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)
    return data

def save_json(filepath, data):
    info_log(f"Saving JSON to file: {filepath}")
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2)

def random_id(length=20):
    return ''.join(random.choices(string.ascii_letters + string.digits, k=length))

def random_timestamp_last_year():
    now = datetime.utcnow()
    past = now - timedelta(days=365)
    random_time = past + (now - past) * random.random()
    return random_time.strftime("%Y-%m-%dT%H:%M:%SZ")

def load_users():
    info_log(f"Loading users from: {USERS_PATH}")
    with open(USERS_PATH, 'r', encoding='utf-8') as f:
        users_data = json.load(f)
    for user in users_data:
        if "fullyVerified" in user:
            user["manuallyVerified"] = user.pop("fullyVerified")
        if "manuallyVerified" not in user:
            user["manuallyVerified"] = False
    return users_data

def pick_random_user(users):
    if not users:
        return None
    return random.choice(users)

def load_lines(filepath):
    if not os.path.isfile(filepath):
        return []
    info_log(f"Loading lines from: {filepath}")
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = [line.strip() for line in f if line.strip()]
    info_log(f"Loaded {len(lines)} lines from {filepath}")
    return lines

def move_line_to_processed(line, input_filepath, processed_filepath):
    info_log(f"Moving processed line '{line}' to processed file: {processed_filepath}.")
    with open(processed_filepath, 'a', encoding='utf-8') as pf:
        pf.write(line + "\n")
    with open(input_filepath, 'r', encoding='utf-8') as inf:
        lines = inf.readlines()
    with open(input_filepath, 'w', encoding='utf-8') as outf:
        for l in lines:
            if l.strip() != line.strip():
                outf.write(l)

def get_council_meeting_by_id(council_meetings, meeting_id):
    for c in council_meetings:
        if c["councilMeetingId"] == meeting_id:
            return c
    return None

def get_discussion_by_id(discussions, discussion_id):
    for d in discussions:
        if d["discussionId"] == discussion_id:
            return d
    return None

def get_comment_by_id(comments, comment_id):
    for c in comments:
        if c["commentId"] == comment_id:
            return c
    return None

def generate_votes_for_council_meeting(meeting_id, users, council_meeting_votes, council_meetings):
    meeting = get_council_meeting_by_id(council_meetings, meeting_id)
    if meeting is None:
        return
    if "upvoteCount" not in meeting:
        meeting["upvoteCount"] = 0
    if "downvoteCount" not in meeting:
        meeting["downvoteCount"] = 0
    if "commentCount" not in meeting:
        meeting["commentCount"] = 0
    if not users:
        num_votes = 0
        info_log("No users found, no council meeting votes will be generated.")
    else:
        num_votes = random.randint(5, 50)
        info_log(f"Generating {num_votes} council meeting votes for: {meeting_id}")
    for _ in range(num_votes):
        vote_id = random_id()
        user = pick_random_user(users)
        if user is None:
            continue
        user_id = user["userId"]
        vote_type = "upvote" if random.random() < 0.8 else "downvote"
        if vote_type == "upvote":
            meeting["upvoteCount"] += 1
        else:
            meeting["downvoteCount"] += 1
        vote_timestamp = random_timestamp_last_year()
        vote_entry = {
            "voteId": vote_id,
            "councilMeetingId": meeting_id,
            "userId": user_id,
            "type": vote_type,
            "timestamp": vote_timestamp
        }
        council_meeting_votes.append(vote_entry)

def generate_comments_for_council_meeting(meeting_id, users, comment_texts, comments, council_meetings):
    meeting = get_council_meeting_by_id(council_meetings, meeting_id)
    if meeting is None:
        return []
    if "commentCount" not in meeting:
        meeting["commentCount"] = 0
    if "upvoteCount" not in meeting:
        meeting["upvoteCount"] = 0
    if "downvoteCount" not in meeting:
        meeting["downvoteCount"] = 0
    num_comments = random.randint(5, 30)
    info_log(f"Generating {num_comments} comments for council meeting: {meeting_id}")
    comment_entries = []
    for _ in range(num_comments):
        user = pick_random_user(users)
        if user is None:
            info_log("No user found to assign to comment, skipping comment.")
            continue
        comment_id = random_id()
        user_id = user["userId"]
        screen_name = user["screenName"]
        content_text = random.choice(comment_texts) if comment_texts else "No comment text available."
        comment_timestamp = random_timestamp_last_year()
        parentCommentId = None
        if comment_entries and random.random() < 0.5:
            parent = random.choice(comment_entries)
            parentCommentId = parent["commentId"]
        comment_entry = {
            "commentId": comment_id,
            "councilMeetingId": meeting_id,
            "discussionId": None,
            "parentCommentId": parentCommentId,
            "userId": user_id,
            "screenName": screen_name,
            "contentText": content_text,
            "timestamp": comment_timestamp
        }
        if "upvoteCount" not in comment_entry:
            comment_entry["upvoteCount"] = 0
        if "downvoteCount" not in comment_entry:
            comment_entry["downvoteCount"] = 0
        comment_entries.append(comment_entry)
        comments.append(comment_entry)
        meeting["commentCount"] += 1
    return comment_entries

def generate_votes_for_discussion(discussion_id, users, discussion_votes, discussions):
    discussion = get_discussion_by_id(discussions, discussion_id)
    if discussion is None:
        return
    if "upvoteCount" not in discussion:
        discussion["upvoteCount"] = 0
    if "downvoteCount" not in discussion:
        discussion["downvoteCount"] = 0
    if "commentCount" not in discussion:
        discussion["commentCount"] = 0
    if not users:
        num_votes = 0
        info_log("No users found, no discussion votes will be generated.")
    else:
        num_votes = random.randint(5, 50)
        info_log(f"Generating {num_votes} discussion votes for: {discussion_id}")
    for _ in range(num_votes):
        vote_id = random_id()
        user = pick_random_user(users)
        if user is None:
            continue
        user_id = user["userId"]
        vote_type = "upvote" if random.random() < 0.8 else "downvote"
        if vote_type == "upvote":
            discussion["upvoteCount"] += 1
        else:
            discussion["downvoteCount"] += 1
        vote_timestamp = random_timestamp_last_year()
        vote_entry = {
            "voteId": vote_id,
            "discussionId": discussion_id,
            "userId": user_id,
            "type": vote_type,
            "timestamp": vote_timestamp
        }
        discussion_votes.append(vote_entry)

def generate_comments_for_discussion(discussion_id, users, comment_texts, comments, discussions):
    discussion = get_discussion_by_id(discussions, discussion_id)
    if discussion is None:
        return []
    if "commentCount" not in discussion:
        discussion["commentCount"] = 0
    if "upvoteCount" not in discussion:
        discussion["upvoteCount"] = 0
    if "downvoteCount" not in discussion:
        discussion["downvoteCount"] = 0
    num_comments = random.randint(5, 30)
    info_log(f"Generating {num_comments} comments for discussion: {discussion_id}")
    comment_entries = []
    for _ in range(num_comments):
        user = pick_random_user(users)
        if user is None:
            info_log("No user found to assign to comment, skipping comment.")
            continue
        comment_id = random_id()
        user_id = user["userId"]
        screen_name = user["screenName"]
        content_text = random.choice(comment_texts) if comment_texts else "No comment text available."
        comment_timestamp = random_timestamp_last_year()
        parentCommentId = None
        if comment_entries and random.random() < 0.5:
            parent = random.choice(comment_entries)
            parentCommentId = parent["commentId"]
        comment_entry = {
            "commentId": comment_id,
            "councilMeetingId": None,
            "discussionId": discussion_id,
            "parentCommentId": parentCommentId,
            "userId": user_id,
            "screenName": screen_name,
            "contentText": content_text,
            "timestamp": comment_timestamp
        }
        if "upvoteCount" not in comment_entry:
            comment_entry["upvoteCount"] = 0
        if "downvoteCount" not in comment_entry:
            comment_entry["downvoteCount"] = 0
        comment_entries.append(comment_entry)
        comments.append(comment_entry)
        discussion["commentCount"] += 1
    return comment_entries

def generate_comment_votes_for_comments(comment_entries, users, comment_votes):
    for comment_entry in comment_entries:
        if not users:
            num_comment_votes = 0
            info_log("No users found, no comment votes will be generated.")
        else:
            num_comment_votes = random.randint(0, 20)
        for _ in range(num_comment_votes):
            vote_id = random_id()
            user = pick_random_user(users)
            if user is None:
                continue
            user_id = user["userId"]
            vote_type = "upvote" if random.random() < 0.8 else "downvote"
            if vote_type == "upvote":
                comment_entry["upvoteCount"] += 1
            else:
                comment_entry["downvoteCount"] += 1
            vote_timestamp = random_timestamp_last_year()
            comment_vote_entry = {
                "voteId": vote_id,
                "commentId": comment_entry["commentId"],
                "userId": user_id,
                "type": vote_type,
                "timestamp": vote_timestamp
            }
            comment_votes.append(comment_vote_entry)

def process_council_meetings(council_meetings, council_meeting_votes, comments, comment_votes, users, comment_texts):
    input_titles = load_lines(INPUT_ARTICLES_PATH)
    meetings_processed_count = 0
    for title_line in input_titles:
        info_log(f"Processing council meeting title: {title_line}")
        if "_" not in title_line:
            info_log("Title format invalid (no underscore), skipping.")
            continue
        parts = title_line.split("_", 1)
        if len(parts) < 2:
            info_log("Title format invalid (not enough parts), skipping.")
            continue
        header = parts[1]
        council_meeting_id = random_id()
        audioUrl = f"gs://open-party-ad47a.firebasestorage.app/articleAudio/{title_line}.mp3"
        thumbnailUrl = f"gs://open-party-ad47a.firebasestorage.app/articleThumbnail/{title_line}.jpg"
        contentText = f"./articleText/{header}.txt"
        meeting_timestamp = random_timestamp_last_year()
        meeting_entry = {
            "councilMeetingId": council_meeting_id,
            "audioUrl": audioUrl,
            "contentText": contentText,
            "thumbnailUrl": thumbnailUrl,
            "timestamp": meeting_timestamp,
            "title": header,
            "upvoteCount": 0,
            "downvoteCount": 0,
            "commentCount": 0
        }
        council_meetings.append(meeting_entry)
        generate_votes_for_council_meeting(
            council_meeting_id,
            users,
            council_meeting_votes,
            council_meetings
        )
        comment_entries_for_this_meeting = generate_comments_for_council_meeting(
            council_meeting_id,
            users,
            comment_texts,
            comments,
            council_meetings
        )
        generate_comment_votes_for_comments(
            comment_entries_for_this_meeting,
            users,
            comment_votes
        )
        move_line_to_processed(title_line, INPUT_ARTICLES_PATH, PROCESSED_ARTICLES_PATH)
        meetings_processed_count += 1
    return council_meetings, council_meeting_votes, comments, comment_votes, meetings_processed_count

def process_discussions(discussions, discussion_votes, comments, comment_votes, users, comment_texts):
    if not has_discussions:
        return discussions, discussion_votes, comments, comment_votes, 0
    input_discussions = load_lines(INPUT_DISCUSSIONS_PATH)
    discussions_processed_count = 0
    for discussion_line in input_discussions:
        info_log(f"Processing discussion title: {discussion_line}")
        discussion_id = random_id()
        contentText = f"./discussionText/{discussion_line}.json"
        discussion_timestamp = random_timestamp_last_year()
        discussion_entry = {
            "discussionId": discussion_id,
            "contentText": contentText,
            "timestamp": discussion_timestamp,
            "title": discussion_line,
            "upvoteCount": 0,
            "downvoteCount": 0,
            "commentCount": 0
        }
        discussions.append(discussion_entry)
        generate_votes_for_discussion(
            discussion_id,
            users,
            discussion_votes,
            discussions
        )
        comment_entries_for_this_discussion = generate_comments_for_discussion(
            discussion_id,
            users,
            comment_texts,
            comments,
            discussions
        )
        generate_comment_votes_for_comments(
            comment_entries_for_this_discussion,
            users,
            comment_votes
        )
        move_line_to_processed(discussion_line, INPUT_DISCUSSIONS_PATH, PROCESSED_DISCUSSIONS_PATH)
        discussions_processed_count += 1
    return discussions, discussion_votes, comments, comment_votes, discussions_processed_count

def main():
    info_log("Loading or creating JSON files.")
    council_meetings = load_or_create_json(COUNCIL_MEETINGS_PATH)
    council_meeting_votes = load_or_create_json(COUNCIL_MEETING_VOTES_PATH)
    comments = load_or_create_json(COMMENTS_PATH)
    comment_votes = load_or_create_json(COMMENT_VOTES_PATH)
    users = load_users()
    discussions = load_or_create_json(DISCUSSIONS_PATH)
    discussion_votes = load_or_create_json(DISCUSSION_VOTES_PATH)
    comment_texts = load_lines(INPUT_COMMENT_TEXT_PATH)
    (
        council_meetings,
        council_meeting_votes,
        comments,
        comment_votes,
        meetings_processed_count
    ) = process_council_meetings(
        council_meetings,
        council_meeting_votes,
        comments,
        comment_votes,
        users,
        comment_texts
    )
    if has_discussions:
        (
            discussions,
            discussion_votes,
            comments,
            comment_votes,
            discussions_processed_count
        ) = process_discussions(
            discussions,
            discussion_votes,
            comments,
            comment_votes,
            users,
            comment_texts
        )
    else:
        discussions_processed_count = 0
    save_json(COUNCIL_MEETINGS_PATH, council_meetings)
    save_json(COUNCIL_MEETING_VOTES_PATH, council_meeting_votes)
    save_json(COMMENTS_PATH, comments)
    save_json(COMMENT_VOTES_PATH, comment_votes)
    save_json(DISCUSSIONS_PATH, discussions)
    save_json(DISCUSSION_VOTES_PATH, discussion_votes)
    info_log("All JSON files saved successfully.")
    info_log(f"Processed {meetings_processed_count} council meetings.")
    info_log(f"Processed {discussions_processed_count} discussions.")

if __name__ == "__main__":
    main()
    info_log("Script finished.")
