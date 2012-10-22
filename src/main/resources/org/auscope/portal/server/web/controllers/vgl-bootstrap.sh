#!/usr/bin/env bash
# chkconfig: 2345 90 10
# description: vgl-bootstrap.sh - Shell Script for managing the download and running of the VGL portal workflow script
#              It is expected that the the VGL Portal will customise this script with appropriate values for each job

# Some constants
export VGL_BOOTSTRAP_VERSION="1"
export WORKING_DIR="/root"
export WORKFLOW_SCRIPT="$WORKING_DIR/vgl.sh"

# These will be replaced with hardcoded values by the VGL Portal (varying for each job)
export STORAGE_BUCKET="{0}"
export STORAGE_BASE_KEY_PATH="{1}"
export STORAGE_ACCESS_KEY="{2}"
export STORAGE_SECRET_KEY="{3}"
export WORKFLOW_URL="{4}"
export STORAGE_ENDPOINT="{5}"
export STORAGE_TYPE="{6}"
export STORAGE_AUTH_VERSION="{7}"

# Load our profile so this run is the same as a regular user login (to make debugging easier)
source /etc/profile

echo "------ VEGL Bootstrap Script ---------"
echo "                                      "
echo "------ Printing Environment ----------"
echo "VEGL_BOOTSTRAP_VERSION = $VEGL_BOOTSTRAP_VERSION"
echo "WORKING_DIR = $WORKING_DIR"
echo "WORKFLOW_URL = $WORKFLOW_URL"
echo "WORKFLOW_SCRIPT = $WORKFLOW_SCRIPT"
echo "PATH = $PATH"
echo "LD_LIBRARY_PATH = $LD_LIBRARY_PATH"v
echo "STORAGE_ENDPOINT = $STORAGE_ENDPOINT"
echo "STORAGE_TYPE = $STORAGE_TYPE"
echo "--------------------------------------"


#Download our workflow and make it executable
echo "Downloading workflow script from $WORKFLOW_URL and storing it at $WORKFLOW_SCRIPT"
curl -L "$WORKFLOW_URL" > "$WORKFLOW_SCRIPT"
echo "curl result $?"
echo "Making $WORKFLOW_SCRIPT executable"
chmod +x "$WORKFLOW_SCRIPT"
echo "chmod result $?"

echo "executing workflow script $WORKFLOW_SCRIPT"
$WORKFLOW_SCRIPT
