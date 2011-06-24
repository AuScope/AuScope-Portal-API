 #!/usr/bin/env bash
# chkconfig: 2345 90 10
# description: vegl-bootstrap.sh - Shell Script for managing the download and running of the VEGL portal workflow script

VEGL_BOOTSTRAP_VERSION="1"
WORKING_DIR="/root"
WORKFLOW_URL="http://vegl-portal.s3.amazonaws.com/vm/vegl.sh"
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
