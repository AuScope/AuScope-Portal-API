#!/usr/bin/env bash
# description: cloud - Wrapper class for swift tools and aws tool

export WORKING_DIR="/root"

cd $WORKING_DIR

storageType=`sed '/^\#/d' .jobInfo | grep 'StorageType'  | tail -n 1 | cut -d "=" -f2-`
st_auth=`sed '/^\#/d' .jobInfo | grep 'cloudStorageEndPoint'  | tail -n 1 | cut -d "=" -f2-`
st_user=`sed '/^\#/d' .jobInfo | grep 'cloudStorageAccessKey'  | tail -n 1 | cut -d "=" -f2-`
st_key=`sed '/^\#/d' .jobInfo | grep 'cloudStorageSecretKey'  | tail -n 1 | cut -d "=" -f2-`

export ST_AUTH="$st_auth"
export ST_USER="$st_user"
export ST_KEY="$st_key"


#wrapper for swift upload
if [ "$storageType" == "swift" ] && [ "$1" == "upload" ]
then
        swift upload ${2}/${3} ${4}
fi


#wrapper for aws upload
if [ "$storageType" == "aws" ] && [ "$1" == "upload" ]
then
        aws put ${2}/${3}/${4} ${4}
fi
