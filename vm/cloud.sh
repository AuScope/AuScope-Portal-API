#!/usr/bin/env bash
# description: cloud - Wrapper class for swift tools and aws tool. Requires STORAGE_TYPE, STORAGE_BUCKET, STORAGE_BASE_KEY_PATH and STORAGE_AUTH_VERSION to be set.
# cloud upload [uploadedFileName] [file]
# cloud download [cloudFileName] [outputFile]
# cloud list

#wrapper for swift upload. Swift tool uses the file name as key
if [[ $STORAGE_TYPE == swift* ]]
then
        #There may be some flags that get set depending on env
        additionalFlags=""
        if [[ -n $STORAGE_AUTH_VERSION ]]
        then
                additionalFlags="-V $STORAGE_AUTH_VERSION"
        fi
        
        if [[ "$1" == "list" ]]
        then
                swift list "$STORAGE_BUCKET" -p "$STORAGE_BASE_KEY_PATH" $additionalFlags
        fi

        if [[ "$1" == "download" ]]
        then
                swift download -o "$3" "$STORAGE_BUCKET" "$STORAGE_BASE_KEY_PATH/$2" $additionalFlags
        fi

        if [[ "$1" == "upload" ]]
        then
                #To overcome swift tool using directory structure as part of key
                #Change to directory before running this command (then change back)
                originalDir=`pwd`
                fileDir=`dirname "$3"`
                fileName=`basename "$3"`

                cd "$fileDir"
                swift upload "$STORAGE_BUCKET/$STORAGE_BASE_KEY_PATH" "$fileName" $additionalFlags
                cd "$originalDir"
        fi
fi

#wrapper for aws upload
if [[ "$STORAGE_TYPE" == "aws" ]]
then
        if [[ "$1" == "list" ]]
        then
                aws list "$STORAGE_BUCKET/$STORAGE_BASE_KEY_PATH"
        fi

        if [[ "$1" == "download" ]]
        then
                aws get "$STORAGE_BUCKET/$STORAGE_BASE_KEY_PATH/$2" "$3"
        fi

        if [[ "$1" == "upload" ]]
        then
                aws put "$STORAGE_BUCKET/$STORAGE_BASE_KEY_PATH/$2" "$3"
        fi      
fi
