#!/usr/bin/env bash
# chkconfig: 2345 90 10
# description: vegl.sh - Shell Script performing the "workflow" of the VEGL VM
# NOTE: Please ensure that VEGL_WORKFLOW_VERSION gets incremented with any changes

#configure our environment
export PATH="/usr/lib64/openmpi/1.4-gcc/bin:/opt/ubc:$PATH"
export LD_LIBRARY_PATH="/usr/lib64/openmpi/1.4-gcc/lib:/opt/intel64"
export WORKING_DIR="/root"
export EXAMPLE_DATA_DIR="${WORKING_DIR}/ubc-example-data"
export VEGL_LOG_FILE="${WORKING_DIR}/vegl.sh.log"
export EC2_METADATA_SCRIPT="${WORKING_DIR}/ec2-metadata"
INITIAL_SLEEP_LENGTH="30s"
FINAL_SLEEP_LENGTH="15m"
NTP_DATE_SERVER="pool.ntp.org"
VEGL_WORKFLOW_VERSION="1"
#cloud storage tool wrapper url
WRAPPER_URL="http://vegl-portal.s3.amazonaws.com/vm/cloud.sh"
export VEGL_SCRIPT_PATH="${WORKING_DIR}/vegl_script.py"
export SUBSET_REQUEST_PATH="${WORKING_DIR}/subset_request.sh"
ABORT_SHUTDOWN_PATH="${WORKING_DIR}/abort_shutdown"

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
echo "VEGL_LOG_FILE = ${VEGL_LOG_FILE}"
echo "EC2_METADATA_SCRIPT = ${EC2_METADATA_SCRIPT}"
echo "INITIAL_SLEEP_LENGTH = ${INITIAL_SLEEP_LENGTH}"
echo "FINAL_SLEEP_LENGTH = ${FINAL_SLEEP_LENGTH}"
echo "NTP_DATE_SERVER = ${NTP_DATE_SERVER}"
echo "SUBSET_REQUEST_PATH = ${SUBSET_REQUEST_PATH}"
echo "VEGL_SCRIPT_PATH = ${VEGL_SCRIPT_PATH}"
echo "ABORT_SHUTDOWN_PATH = ${ABORT_SHUTDOWN_PATH}"
echo "--------------------------------------"

#Start by waiting until the system starts up
echo "Sleeping for ${INITIAL_SLEEP_LENGTH}"
sleep "$INITIAL_SLEEP_LENGTH"

#Lets get started by moving to our working directory
cd $WORKING_DIR

#We NEED the time to be up to date otherwise AWS requests will fail
echo "Synchronising date with ${NTP_DATE_SERVER}"
ntpdate "$NTP_DATE_SERVER"

#Download our cloud storage tool wrapper and make it executable
echo "Downloading wrapper script from ${WRAPPER_URL} and storing it at /bin/cloud"
curl -L "${WRAPPER_URL}" > "/bin/cloud"
echo "Making /bin/cloud executable"
chmod +x "/bin/cloud"
echo "chmod result $?"

#next we download our AWS metadata script which allows us to fetch information
#about our instance
curl -L http://s3.amazonaws.com/ec2metadata/ec2-metadata > "${EC2_METADATA_SCRIPT}"
chmod +x "${EC2_METADATA_SCRIPT}"

#next we want to download our user data string
userDataHost=`route -n | awk '$4 ~ ".*G.*" {print $2}'`
userDataUrl="http://$userDataHost:8773/latest/user-data"
echo "Downloading user data from ${userDataUrl}"
userDataString=`curl -L "$userDataUrl"`

#Decompose our user data string into the individual parameter strings
s3Bucket=`echo $userDataString | jsawk 'return this.s3OutputBucket'`
s3BaseKeyPath=`echo $userDataString | jsawk 'return this.s3OutputBaseKeyPath'`
s3AccessKey=`echo $userDataString | jsawk 'return this.s3OutputAccessKey'`
s3SecretKey=`echo $userDataString | jsawk 'return this.s3OutputSecretKey'`
veglShellScript=`echo $userDataString | jsawk 'return this.veglShellScript'`

echo "------ Printing SVN FILE INFO---------"
echo "svn info ${veglShellScript}"
svn info ${veglShellScript}
echo "                                      "
echo "svn info ${WRAPPER_URL}"
svn info ${WRAPPER_URL}
echo "--------------------------------------"

echo "s3Bucket = ${s3Bucket}"
echo "s3BaseKeyPath = ${s3BaseKeyPath}"

#Configure our AWS environment variables
export AWS_SECRET_ACCESS_KEY="$s3SecretKey"
export AWS_ACCESS_KEY_ID="$s3AccessKey"
export S3_OUTPUT_BUCKET="$s3Bucket"
export S3_BASE_KEY_PATH="$s3BaseKeyPath"

#Write our secret key to our secret key file
echo -e "${AWS_ACCESS_KEY_ID}\n${AWS_SECRET_ACCESS_KEY}\n" > "/root/.awssecret"

#Write storage information for abstract cloud usage
echo -e "StorageType=aws\ncloudStorageAccessKey=${AWS_ACCESS_KEY_ID}\ncloudStorageSecretKey=${AWS_SECRET_ACCESS_KEY}" > "/root/.jobInfo"

#Download our input files from S3 and load them into files in the current working directory
echo "Downloading inputfiles from S3..."
downloadInputFilesBase=`echo "${s3Bucket}/${s3BaseKeyPath}" | sed "s/\/\/*/\//g"`
for line in `aws ls "${downloadInputFilesBase}" -l | awk '{print $7}'`;do
    downloadQueryPath=`echo "${s3Bucket}/${line}" | sed "s/\/\/*/\//g"`
	downloadOutputFile=`basename "${line}"`
	
	echo "Attempting to download ${downloadQueryPath} and store it to ${downloadOutputFile}"
	aws get "${downloadQueryPath}" > "${downloadOutputFile}"
done
echo "... finished downloading input files"

#With our input files in place we can make our subset requests
chmod +x "$SUBSET_REQUEST_PATH"
echo "About to execute ${SUBSET_REQUEST_PATH} as a shell script"
sh $SUBSET_REQUEST_PATH
cd $WORKING_DIR

#Next we can perform our actual work
chmod +x "$VEGL_SCRIPT_PATH"
echo "About to execute ${VEGL_SCRIPT_PATH} as a python script"
python $VEGL_SCRIPT_PATH
cd $WORKING_DIR

#Finally upload our logs for debug purposes
echo "About to upload output log..."
uploadQueryPath=`echo "${s3Bucket}/${s3BaseKeyPath}/vegl.sh.log" | sed "s/\/\/*/\//g"`
aws put "${uploadQueryPath}" "${VEGL_LOG_FILE}" --set-acl=public-read

#At this point we can give developers a grace period in which they can login to the VM for debugging
echo "Sleeping for ${FINAL_SLEEP_LENGTH} before shutting down"
echo "To abort the shutdown run: echo '1' > ${ABORT_SHUTDOWN_PATH}"
sleep "${FINAL_SLEEP_LENGTH}"
if [ -f $ABORT_SHUTDOWN_PATH ]; then
    echo 'Instance shutdown has been aborted'
else
	echo 'Shutting down this instance...'
    shutdown -h now
fi
