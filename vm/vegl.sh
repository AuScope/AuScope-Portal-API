#!/usr/bin/env bash
# chkconfig: 2345 90 10
# description: vegl.sh - Shell Script performing the "workflow" of the VEGL VM
# NOTE: Please ensure that VEGL_WORKFLOW_VERSION gets incremented with any changes

#configure our environment
export PATH="/usr/lib64/openmpi/1.4-gcc/bin:/opt/ubc:$PATH"
export LD_LIBRARY_PATH="/usr/lib64/openmpi/1.4-gcc/lib:/opt/intel64"
WORKING_DIR="/root"
EXAMPLE_DATA_DIR="${WORKING_DIR}/ubc-example-data"
SUBSET_FILE="/tmp/subset.bin"
VEGL_LOG_FILE="${WORKING_DIR}/vegl.sh.log"
INITIAL_SLEEP_LENGTH="30s"
FINAL_SLEEP_LENGTH="15m"
NTP_DATE_SERVER="pool.ntp.org"
VEGL_WORKFLOW_VERSION="1"

echo "VEGL Workflow Script... starting"
echo "All future console output will be redirected to ${VEGL_LOG_FILE}"
exec &> "$VEGL_LOG_FILE"

echo "------ VEGL Workflow Script ----------"
echo "                                      "
echo "------ Printing Environment ----------"
echo "VEGL_WORKFLOW_VERSION = ${VEGL_WORKFLOW_VERSION}"
echo "PATH = ${PATH}"
echo "LD_LIBRARY_PATH = ${LD_LIBRARY_PATH}"
echo "WORKING_DIR = ${WORKING_DIR}"
echo "EXAMPLE_DATA_DIR = ${EXAMPLE_DATA_DIR}"
echo "SUBSET_FILE = ${SUBSET_FILE}"
echo "VEGL_LOG_FILE = ${VEGL_LOG_FILE}"
echo "INITIAL_SLEEP_LENGTH = ${INITIAL_SLEEP_LENGTH}"
echo "FINAL_SLEEP_LENGTH = ${FINAL_SLEEP_LENGTH}"
echo "NTP_DATE_SERVER = ${NTP_DATE_SERVER}"
echo "--------------------------------------"

#Start by waiting until the system starts up
echo "Sleeping for ${INITIAL_SLEEP_LENGTH}"
sleep "$INITIAL_SLEEP_LENGTH"

#Lets get started by moving to our working directory
cd $WORKING_DIR

#We NEED the time to be up to date otherwise AWS requests will fail
echo "Synchronising date with ${NTP_DATE_SERVER}"
ntpdate "$NTP_DATE_SERVER"

#next we want to download our user data string
userDataHost=`route -n | awk '$4 ~ ".*G.*" {print $2}'`
userDataUrl="http://$userDataHost:8773/latest/user-data"
echo "Downloading user data from ${userDataUrl}"
userDataString=`curl -L "$userDataUrl"`

#Decompose our user data string into the individual parameter strings
s3Bucket=`echo $userDataString | jsawk 'return this.s3Bucket'`
s3BaseKeyPath=`echo $userDataString | jsawk 'return this.s3BaseKeyPath'`
s3AccessKey=`echo $userDataString | jsawk 'return this.s3AccessKey'`
s3SecretKey=`echo $userDataString | jsawk 'return this.s3SecretKey'`

echo "s3Bucket = ${s3Bucket}"
echo "s3BaseKeyPath = ${s3BaseKeyPath}"

#Configure our AWS environment variables
export AWS_SECRET_ACCESS_KEY="$s3SecretKey"
export AWS_ACCESS_KEY_ID="$s3AccessKey"

#Download our input files from S3 and load them into variables or files
echo "Downloading inputfiles from S3"
downloadQueryPath=`echo "${s3Bucket}/${s3BaseKeyPath}/query.txt" | sed "s/\/\/*/\//g"`
echo "downloadQueryPath = ${downloadQueryPath}"
downloadQueryUrl=`aws get "${downloadQueryPath}"`
echo "downloadQueryUrl = ${downloadQueryUrl}"

#With our input files in place we can make our subset requests
curl -L "$downloadQueryUrl" > "$SUBSET_FILE"

#Perform our UBC code
#TODO - this is currently a "fake" job that is NOT using any of the input files generated above
echo "Performing example UBC code operations"
cd "${EXAMPLE_DATA_DIR}"
mpirun -np 4 --mca btl self,sm  /opt/ubc/gzsen3d_MPI "${EXAMPLE_DATA_DIR}/grav_sns.inp"
mpirun -np 4 --mca btl self,sm  /opt/ubc/gzinv3d_MPI "${EXAMPLE_DATA_DIR}/grav_inv.inp"
echo "Operations complete"
cd "${WORKING_DIR}"

#Upload our UBC code results to S3
#param 1 - The filepath of the file to upload
#param 2 - The bucket name to upload to
#param 3 - The keypath the uploaded file will use (no leading /)
upload-aws() {
	#Remove any duplicate forward slashes
	bucketKeyPath=`echo "$2/$3" | sed "s/\/\/*/\//g"`
	echo "Uploading $1 to S3 bucket $2 with path $3 (bucket/keypath= $bucketKeyPath)"
	aws put "$bucketKeyPath" "$1"
}

#TODO: We are only uploading specific files RATHER than the genuine output
upload-aws "${EXAMPLE_DATA_DIR}/gzinv3d.log" "${s3Bucket}" "${s3BaseKeyPath}/output/gzinv3d.log"
upload-aws "${EXAMPLE_DATA_DIR}/gzsen3d.log" "${s3Bucket}" "${s3BaseKeyPath}/output/gzsen3d.log"
upload-aws "${EXAMPLE_DATA_DIR}/sensitivity.txt" "${s3Bucket}" "${s3BaseKeyPath}/output/sensitivity.txt"
upload-aws "${VEGL_LOG_FILE}" "${s3Bucket}" "${s3BaseKeyPath}/output/vegl.sh.log"

#At this point we can give developers a grace period in which they can login to the VM for debugging
echo "Sleeping for ${FINAL_SLEEP_LENGTH} to allow someone to abort my shutdown..."
sleep "${FINAL_SLEEP_LENGTH}"
if ["$VEGL_KEEP_ALIVE" -ne ""]; then
    echo 'VEGL_KEEP_ALIVE has been set'
else
    #TODO: shutdown code
    echo "I should be shutting down here. Its a TODO"
fi
