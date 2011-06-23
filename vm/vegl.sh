#!/usr/bin/env bash
# ===============================================================
#
#
#    Shell Script for managing the startup of the VEGL portal
#
#
# ==============================================================

#Start by waiting until the system starts up
#TODO: Disabled for testing
#sleep 30s

#First we want to download our user data string
userDataHost=`route -n | awk '$4 ~ ".*G.*" {print $2}'`
userDataString=`curl -L "http://$userDataHost:8773/latest/user-data"`

#Decompose our user data string into the individual parameter strings
s3Bucket=`echo $userDataString | jsawk 'return this.s3Bucket'`
s3BaseKeyPath=`echo $userDataString | jsawk 'return this.s3BaseKeyPath'`
s3AccessKey=`echo $userDataString | jsawk 'return this.s3AccessKey'`
s3SecretKey=`echo $userDataString | jsawk 'return this.s3SecretKey'`

#Configure our AWS environment variables
export AWS_SECRET_ACCESS_KEY="$s3SecretKey"
export AWS_ACCESS_KEY_ID="$s3AccessKey"

#Download our input files from S3
downloadQueryUrl=`aws get "${s3Bucket}/${s3BaseKeyPath}/query.txt"`
echo $downloadQueryUrl
