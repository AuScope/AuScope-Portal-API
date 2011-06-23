#!/usr/bin/env bash
# chkconfig: 2345 90 10
# description: vegl.sh - Shell Script for managing the startup of the VEGL portal

#Start by waiting until the system starts up
sleep 30s

#hardcoded paths - should probably be moved to environment variables
WORKING_DIR="/root"
EXAMPLE_DATA_DIR="${WORKING_DIR}/ubc-example-data"
SUBSET_FILE="/tmp/subset.bin"

#Lets get started by moving to our working directory
cd $WORKING_DIR

#We NEED the time to be up to date otherwise AWS requests will fail
ntpdate pool.ntp.org

#next we want to download our user data string
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

#Download our input files from S3 and load them into variables or files
downloadQueryUrl=`aws get "${s3Bucket}/${s3BaseKeyPath}/query.txt"`

#With our input files in place we can make our subset requests
curl -L "$downloadQueryUrl" > "$SUBSET_FILE"

#Perform our UBC code
#TODO - this is currently a "fake" job that is NOT using any of the input files generated above
cd "${EXAMPLE_DATA_DIR}"
mpirun -np 4 --mca btl self,sm  /opt/ubc/gzsen3d_MPI "${EXAMPLE_DATA_DIR}/grav_sns.inp"
mpirun -np 4 --mca btl self,sm  /opt/ubc/gzinv3d_MPI "${EXAMPLE_DATA_DIR}/grav_inv.inp"
cd "${WORKING_DIR}"

#Upload our UBC code results to S3
#TODO: We are only uploading specific files RATHER than the genuine output
aws put "${s3Bucket}/${s3BaseKeyPath}/output/gzinv3d.log" "${EXAMPLE_DATA_DIR}/gzinv3d.log"
aws put "${s3Bucket}/${s3BaseKeyPath}/output/gzsen3d.log" "${EXAMPLE_DATA_DIR}/gzsen3d.log"
aws put "${s3Bucket}/${s3BaseKeyPath}/output/sensitivity.txt" "${EXAMPLE_DATA_DIR}/sensitivity.txt"

#At this point we can give developers a grace period in which they can login to the VM for debugging
sleep 15m
if ["$VEGL_KEEP_ALIVE" -ne ""]; then
   echo 'VEGL_KEEP_ALIVE has been set'
else
   #TODO: shutdown code
fi
