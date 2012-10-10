#!/usr/bin/env bash
# description: cloud - Wrapper class for swift tools and aws tool
# cloud upload [bucket] [baseKey] [uploadedFileName] [file]
# cloud download [bucket] [baseKey] [cloudFileName] [outputFile]
# cloud list [bucket] [baseKey]

#wrapper for swift upload. Swift tool uses the file name as key
if [ "$STORAGE_TYPE" == "swift" ] && [ "$1" == "upload" ]
then
        #To overcome swift tool using directory structure as part of key
        #Change to directory before running this command (then change back)
        originalDir=`pwd`
        fileDir=`dirname "$5"`
        fileName=`basename "$5"`

        cd "$fileDir"
        swift upload "$2/$3" "$fileName"
        cd "$originalDir"
fi

#wrapper for aws upload
if [ "$STORAGE_TYPE" == "aws" ] && [ "$1" == "upload" ]
then
        aws put "$2/$3/$4" "$5"
fi

#wrapper for swift download
if [ "$STORAGE_TYPE" == "swift" ] && [ "$1" == "download" ]
then
        swift download -o "$5" "$2" "$3/$4"
fi


#wrapper for aws download
if [ "$STORAGE_TYPE" == "aws" ] && [ "$1" == "download" ]
then
        aws get "$2/$3/$4" "$5"
fi


#wrapper for swift download
if [ "$STORAGE_TYPE" == "swift" ] && [ "$1" == "list" ]
then
        swift list "$2" -p "$3"
fi
