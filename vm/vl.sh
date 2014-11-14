#!/usr/bin/env bash
# chkconfig: 2345 90 10
# description: vl.sh - Shell Script performing the "workflow" of the VL VM
# NOTE: Please ensure that VL_WORKFLOW_VERSION gets incremented with any changes

#configure our environment
export VL_WORKFLOW_VERSION="1"
export VL_LOG_FILE_NAME="vl.sh.log"
export VL_LOG_FILE="${WORKING_DIR}/$VL_LOG_FILE_NAME"
export EC2_METADATA_SCRIPT="${WORKING_DIR}/ec2-metadata"
export FINAL_SLEEP_LENGTH="15m"
export NTP_DATE_SERVER="pool.ntp.org"
export CLOUD_STORAGE_WRAPPER_URL="https://raw.githubusercontent.com/AuScope/VEGL-Portal/master/vm/cloud.sh"
export VL_SCRIPT_PATH="${WORKING_DIR}/vl_script.py"
export SUBSET_REQUEST_PATH="${WORKING_DIR}/vl-download.sh"
export ABORT_SHUTDOWN_PATH="${WORKING_DIR}/abort_shutdown"

echo "VL Workflow Script... starting"
echo "All future console output will be redirected to ${VL_LOG_FILE}"
exec > >(tee -a "$VL_LOG_FILE")

echo "Loading system wide profile:"
source /etc/profile

# Print environment variables (don't print any credentials here)
echo "#### Environment start ####"
echo "------ VL Workflow Script ----------"
echo "                                      "
echo "------ Printing Environment ----------"
echo "VL_WORKFLOW_VERSION = ${VL_WORKFLOW_VERSION}"
echo "PATH = ${PATH}"
echo "LD_LIBRARY_PATH = ${LD_LIBRARY_PATH}"
echo "WORKING_DIR = ${WORKING_DIR}"
echo "VL_LOG_FILE = ${VL_LOG_FILE}"
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
echo "STORAGE_AUTH_VERSION = ${STORAGE_AUTH_VERSION}"
echo "--------------------------------------"
echo "#### Environment end ####"

#Lets get started by moving to our working directory
cd $WORKING_DIR

#We NEED the time to be up to date otherwise AWS requests will fail
echo "Synchronising date with ${NTP_DATE_SERVER}"
ntpdate "$NTP_DATE_SERVER"

#Download our cloud storage tool wrapper and make it executable
echo "Downloading wrapper script from ${CLOUD_STORAGE_WRAPPER_URL} and storing it at /bin/cloud"
curl -f -L "${CLOUD_STORAGE_WRAPPER_URL}" -o "/bin/cloud"
echo "Making /bin/cloud executable"
chmod +x "/bin/cloud"
echo "chmod result $?"

#Configure variables for our cloud storage wrapper
export ST_AUTH="$STORAGE_ENDPOINT"
export ST_USER="$STORAGE_ACCESS_KEY"
export ST_KEY="$STORAGE_SECRET_KEY"
echo "Storage wrapper configured to use: $STORAGE_TYPE"

#next we download our AWS metadata script which allows us to fetch information
#about our instance
curl -f -L http://s3.amazonaws.com/ec2metadata/ec2-metadata -o "${EC2_METADATA_SCRIPT}"
chmod +x "${EC2_METADATA_SCRIPT}"

echo "------ Printing SVN FILE INFO---------"
echo "svn info ${WORKFLOW_URL}"
svn info ${WORKFLOW_URL}
echo "                                      "
echo "svn info ${CLOUD_STORAGE_WRAPPER_URL}"
svn info ${CLOUD_STORAGE_WRAPPER_URL}
echo "--------------------------------------"

#Upload a file indicating that work has started
echo "Uploading script version file..."
echo "${VL_WORKFLOW_VERSION}" > workflow-version.txt
echo "cloud upload workflow-version.txt workflow-version.txt"
cloud upload workflow-version.txt workflow-version.txt

#Download our input files from swift storage and load them into files in the current working directory
echo "Downloading inputfiles from S3..."
echo "cloud list"
downloadStartTime=`date +%s`
for line in `cloud list`;do
       downloadOutputFile=`basename "${line}"`
       echo "cloud download ${downloadOutputFile} ${downloadOutputFile} ${downloadOutputFile}"
       cloud download ${downloadOutputFile} ${downloadOutputFile} ${downloadOutputFile}
done
downloadEndTime=`date +%s`
echo "... finished downloading input files"

#With our input files in place we can make our subset requests
chmod +x "$SUBSET_REQUEST_PATH"
echo "About to execute ${SUBSET_REQUEST_PATH} as a shell script"
sh $SUBSET_REQUEST_PATH
cd $WORKING_DIR

#Next we can perform our actual work (make sure we indicate where the python logs start/finish)
chmod +x "$VL_SCRIPT_PATH"
echo "About to execute ${VL_SCRIPT_PATH} as a python script"
echo "#### Python start ####"
computeStartTime=`date +%s`
python $VL_SCRIPT_PATH
computeEndTime=`date +%s`
echo "#### Python end ####"

echo "#### Time start ####"
totalComputeTime=`expr $computeEndTime - $computeStartTime`
totalDownloadTime=`expr $downloadEndTime - $downloadStartTime`
echo "Total compute time was `expr $totalComputeTime / 3600` hour(s), `expr $totalComputeTime % 3600 / 60` minutes and `expr $totalComputeTime % 60` seconds"
echo "Total time to download input data was `expr $totalDownloadTime / 3600` hour(s), `expr $totalDownloadTime % 3600 / 60` minutes and `expr $totalDownloadTime % 60` seconds"
echo "#### Time end ####"
cd $WORKING_DIR

#Finally upload our logs for debug purposes
echo "About to upload output log..."
echo "cloud upload $VL_LOG_FILE_NAME $VL_LOG_FILE"
cloud upload $VL_LOG_FILE_NAME $VL_LOG_FILE_NAME

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
