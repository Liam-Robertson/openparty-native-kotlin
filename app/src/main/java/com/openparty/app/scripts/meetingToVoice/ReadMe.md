- Youtube downloader
yt-dlp -o "2024-09-24 - West Lothian Council" https://www.youtube.com/watch?v=JDn7gXRLC0s-A -x --audio-format mp3 -P "C:\Users\liamj\programming\OpenParty\app\src\main\java\com\openparty\app\scripts\meetingToVoice\transcribeMeetingAudioToText\inputAudioFiles"

python app\src\main\java\com\openparty\app\scripts\meetingToVoice\transcribeMeetingAudioToText\transcribeMeetingAudioToText.py
python app\src\main\java\com\openparty\app\scripts\meetingToVoice\breakMeetingTextToSections\createChunkMetadataFiles\createChunkMetadataFiles.py
python app\src\main\java\com\openparty\app\scripts\meetingToVoice\breakMeetingTextToSections\metadataSecondPass\metadataSecondPass.py
- manual intervention
python app\src\main\java\com\openparty\app\scripts\meetingToVoice\breakMeetingTextToSections\groupRawTextByIssue\groupRawTextByIssue.py
python app\src\main\java\com\openparty\app\scripts\meetingToVoice\createNewsReports\createReportBlueprintFiles\createReportBlueprintFiles.py
- Repeat previous command if it errors
python app\src\main\java\com\openparty\app\scripts\meetingToVoice\createNewsReports\writeFinalReport\writeFinalReport.py
python app\src\main\java\com\openparty\app\scripts\meetingToVoice\createNewsReports\createAudioFiles\createAudioFiles.py
  
Flow: 
- First, the audio from the council meeting is transcribed to text
- Then the metadata files describing this text are created
  - This is done by passing in the council meeting roughly 1000 words at a time
  - The metadata file then describes what's in that 1000 words
  - This creates a detailed metadata file, but it doesn't group all the text by issue, it just describes the issue in each chunk
- Then the metadata second pass happens 
  - The metadata second pass goes in and groups the chunks together into issues
  - For example, it might go in, identify Housing as a major issue, then say all the relevant chunks that are in housing
  - This gives you a blueprint of all the issues, and the chunks relating to them
- Then a python script goes into each issue, identifies the raw text associated with that issue (by looking at chunks) and then splits the text
  - The output of this is a separate file for each issue - each containing text from the meeting that relates only to that issue 
  - This is necessary because, in order to write a news report, you need all the distinct raw text for each issue
- A script then goes into these issue raw text files and creates a report blueprint for them 
  - The blueprint gives you a header, a summary and then a breakdown of all the sub sections in that issue
  - For example, if the issue is education, then a sub section might be teacher's pay, then a sub section might be teachers strikes 
- Then a script goes in and uses the raw text for each issue, with the blueprint to create the final report
  - It does this by feeding in the entire raw text, with the entire blueprint, then giving the current blueprint section, and the current raw text section
  - This is to give the prompt context for how the report should be written, while also focussing it on its current section 
- Then a script is used to convert the final report to audio files