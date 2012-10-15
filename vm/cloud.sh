#!/usr/bin/env bash
# description: cloud - Wrapper class for swift tools and aws tool. Requires STORAGE_TYPE, STORAGE_BUCKET and STORAGE_BASE_KEY_PATH to be set.
# cloud upload [uploadedFileName] [file]
# cloud download [cloudFileName] [outputFile]
# cloud list

#wrapper for swift upload. Swift tool uses the file name as key
if [ "$STORAGE_TYPE" == "swift" ] && [ "$1" == "upload" ]
then
        #To overcome swift tool using directory structure as part of key
        #Change to directory before running this command (then change back)
        originalDir=`pwd`
        fileDir=`dirname "$3"`
        fileName=`basename "$3"`

        cd "$fileDir"
        swift upload "$STORAGE_BUCKET/$STORAGE_BASE_KEY_PATH" "$fileName"
        cd "$originalDir"
fi

#wrapper for aws upload
if [ "$STORAGE_TYPE" == "aws" ] && [ "$1" == "upload" ]
then
        aws put "$STORAGE_BUCKET/$STORAGE_BASE_KEY_PATH/$2" "$3"
fi

#wrapper for swift download
if [ "$STORAGE_TYPE" == "swift" ] && [ "$1" == "download" ]
then
        swift download -o "$3" "$STORAGE_BUCKET" "$STORAGE_BASE_KEY_PATH/$2"
fi


#wrapper for aws download
if [ "$STORAGE_TYPE" == "aws" ] && [ "$1" == "download" ]
then
        aws get "$STORAGE_BUCKET/$STORAGE_BASE_KEY_PATH/$2" "$3"
fi


#wrapper for swift download
if [ "$STORAGE_TYPE" == "swift" ] && [ "$1" == "list" ]
then
        swift list "$STORAGE_BUCKET" -p "$STORAGE_BASE_KEY_PATH"
fi
