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

#Next we want to download our user data string
userDataHost=`route -n | awk '$4 ~ ".*G.*" {print $2}'`
userDataString=`curl -L "http://$userDataHost:8773/latest/user-data"`

s3Bucket=`echo $userDataString | jsawk 'return this.s3Bucket'`
echo $s3Bucket
