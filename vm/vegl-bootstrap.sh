 #!/usr/bin/env bash
# chkconfig: 2345 90 10
# description: vegl-bootstrap.sh - Shell Script for managing the download and running of the VEGL portal workflow script

# we want to download our user data string
userDataHost=`route -n | awk '$4 ~ ".*G.*" {print $2}'`
userDataUrl="http://$userDataHost:8773/latest/user-data"
echo "Downloading user data from ${userDataUrl}"
userDataString=`curl -L "$userDataUrl"`

#Decompose our user data string into the individual parameter strings
veglShellScript=`echo $userDataString | jsawk 'return this.veglShellScript'`

VEGL_BOOTSTRAP_VERSION="1"
WORKING_DIR="/root"
WORKFLOW_URL="${veglShellScript}"
WORKFLOW_SCRIPT="${WORKING_DIR}/vegl.sh"
echo "------ VEGL Bootstrap Script ---------"
echo "                                      "
echo "------ Printing Environment ----------"
echo "VEGL_BOOTSTRAP_VERSION = ${VEGL_BOOTSTRAP_VERSION}"
echo "WORKING_DIR = ${WORKING_DIR}"
echo "WORKFLOW_URL = ${WORKFLOW_URL}"
echo "WORKFLOW_SCRIPT = ${WORKFLOW_SCRIPT}"
echo "--------------------------------------"


#Download our workflow and make it executable
echo "Downloading workflow script from ${WORKFLOW_URL} and storing it at ${WORKFLOW_SCRIPT}"
curl -L "${WORKFLOW_URL}" > "${WORKFLOW_SCRIPT}"
echo "curl result $?"
echo "Making ${WORKFLOW_SCRIPT} executable"
chmod +x "${WORKFLOW_SCRIPT}"
echo "chmod result $?"

echo "executing workflow script $WORKFLOW_SCRIPT"
$WORKFLOW_SCRIPT
