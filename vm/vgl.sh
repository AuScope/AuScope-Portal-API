#!/usr/bin/env bash
# chkconfig: 2345 90 10
# description: vgl.sh - Shell Script performing the "workflow" of the VGL VM
# NOTE: Please ensure that VEGL_WORKFLOW_VERSION gets incremented with any changes

#configure our environment
export VGL_WORKFLOW_VERSION="1"
export VEGL_LOG_FILE_NAME="vegl.sh.log"
export VEGL_LOG_FILE="${WORKING_DIR}/$VEGL_LOG_FILE_NAME"
export EC2_METADATA_SCRIPT="${WORKING_DIR}/ec2-metadata"
export FINAL_SLEEP_LENGTH="15m"
export NTP_DATE_SERVER="pool.ntp.org"
export CLOUD_STORAGE_CLOUD_STORAGE_WRAPPER_URL="https://svn.auscope.org/subversion/AuScopePortal/VEGL-Portal/trunk/vm/cloud.sh"
export VEGL_SCRIPT_PATH="${WORKING_DIR}/vegl_script.py"
export SUBSET_REQUEST_PATH="${WORKING_DIR}/subset_request.sh"
export ABORT_SHUTDOWN_PATH="${WORKING_DIR}/abort_shutdown"

echo "VEGL Workflow Script... starting"
echo "All future console output will be redirected to ${VEGL_LOG_FILE}"
exec &> "$VEGL_LOG_FILE"

# Print environment variables (don't print any credentials here)
echo "------ VEGL Workflow Script ----------"
echo "                                      "
echo "------ Printing Environment ----------"
echo "VGL_WORKFLOW_VERSION = ${VGL_WORKFLOW_VERSION}"
echo "PATH = ${PATH}"
echo "LD_LIBRARY_PATH = ${LD_LIBRARY_PATH}"
echo "WORKING_DIR = ${WORKING_DIR}"
echo "VEGL_LOG_FILE = ${VEGL_LOG_FILE}"
echo "EC2_METADATA_SCRIPT = ${EC2_METADATA_SCRIPT}"
echo "FINAL_SLEEP_LENGTH = ${FINAL_SLEEP_LENGTH}"
echo "NTP_DATE_SERVER = ${NTP_DATE_SERVER}"
echo "SUBSET_REQUEST_PATH = ${SUBSET_REQUEST_PATH}"
echo "ABORT_SHUTDOWN_PATH = ${ABORT_SHUTDOWN_PATH}"
echo "--------------------------------------"
echo "--- Printing Bootstrap Environment ---"
echo "STORAGE_BUCKET = ${STORAGE_BUCKET}"
echo "STORAGE_ENDPOINT = ${STORAGE_ENDPOINT}"
echo "STORAGE_BASE_KEY_PATH = ${STORAGE_BASE_KEY_PATH}"
echo "WORKFLOW_URL = ${WORKFLOW_URL}"
echo "STORAGE_ENDPOINT = ${STORAGE_ENDPOINT}"
echo "--------------------------------------"

#Lets get started by moving to our working directory
cd $WORKING_DIR

#We NEED the time to be up to date otherwise AWS requests will fail
echo "Synchronising date with ${NTP_DATE_SERVER}"
ntpdate "$NTP_DATE_SERVER"

#Download our cloud storage tool wrapper and make it executable
echo "Downloading wrapper script from ${CLOUD_STORAGE_WRAPPER_URL} and storing it at /bin/cloud"
curl -L "${CLOUD_STORAGE_WRAPPER_URL}" > "/bin/cloud"
echo "Making /bin/cloud executable"
chmod +x "/bin/cloud"
echo "chmod result $?"

#Configure variables for our cloud storage wrapper
export STORAGE_TYPE="swift"
export ST_AUTH="$STORAGE_ENDPOINT"
export ST_USER="$STORAGE_ACCESS_KEY"
export ST_KEY="$STORAGE_SECRET_KEY"
echo "Configured storage wrapper to use: $STORAGE_TYPE"

#next we download our AWS metadata script which allows us to fetch information
#about our instance
curl -L http://s3.amazonaws.com/ec2metadata/ec2-metadata > "${EC2_METADATA_SCRIPT}"
chmod +x "${EC2_METADATA_SCRIPT}"


uploadQueryPath=`echo "${STORAGE_BUCKET}/${STORAGE_BASE_KEY_PATH}" | sed "s/\/\/*/\//g"`

echo "------ Printing SVN FILE INFO---------"
echo "svn info ${WORKFLOW_URL}"
svn info ${WORKFLOW_URL}
echo "                                      "
echo "svn info ${CLOUD_STORAGE_WRAPPER_URL}"
svn info ${CLOUD_STORAGE_WRAPPER_URL}
echo "--------------------------------------"

echo "STORAGE_BUCKET = ${STORAGE_BUCKET}"
echo "STORAGE_BASE_KEY_PATH = ${STORAGE_BASE_KEY_PATH}"

#Configure our swift storage environment variables
export ST_AUTH="$storageEndpoint"
export ST_USER="$s3AccessKey"
export ST_KEY="$s3SecretKey"

#Upload a file indicating that work has started
echo "Uploading script version file..."
echo "${VEGL_WORKFLOW_VERSION}" > workflow-version.txt
echo "cloud upload $STORAGE_BUCKET $STORAGE_BASE_KEY_PATH workflow-version.txt workflow-version.txt"
cloud upload $STORAGE_BUCKET $STORAGE_BASE_KEY_PATH workflow-version.txt workflow-version.txt

#Write storage information for abstract cloud usage
echo -e "StorageType=swift\ncloudStorageAccessKey=${s3AccessKey}\ncloudStorageSecretKey=${s3SecretKey}\ncloudStorageEndPoint=${storageEndpoint}" > "/root/.jobInfo"

#Download our input files from swift storage and load them into files in the current working directory
echo "Downloading inputfiles from S3..."
echo "cloud list ${STORAGE_BUCKET} ${STORAGE_BASE_KEY_PATH}"
for line in `cloud list ${STORAGE_BUCKET} ${STORAGE_BASE_KEY_PATH}`;do
       downloadOutputFile=`basename "${line}"`
       echo "cloud download -o ${STORAGE_BUCKET} ${STORAGE_BASE_KEY_PATH} ${downloadOutputFile} ${downloadOutputFile} ${downloadOutputFile}"
       cloud download ${STORAGE_BUCKET} ${STORAGE_BASE_KEY_PATH} ${downloadOutputFile} ${downloadOutputFile} ${downloadOutputFile}
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
echo "cloud upload $STORAGE_BUCKET $STORAGE_BASE_KEY_PATH $VEGL_LOG_FILE_NAME $VEGL_LOG_FILE"
cloud upload $STORAGE_BUCKET $STORAGE_BASE_KEY_PATH $VEGL_LOG_FILE_NAME $VEGL_LOG_FILE

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
