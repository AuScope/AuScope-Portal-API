#!/usr/bin/env bash
# chkconfig: 2345 90 10
# description: vl-bootstrap.sh - Shell Script for managing the download and running of the VL portal workflow script
#              It is expected that the the VL Portal will customise this script with appropriate values for each job

# Some constants
export VGL_BOOTSTRAP_VERSION="2"
export WORKING_DIR="/root"
export WORKFLOW_SCRIPT="$WORKING_DIR/vl.sh"
export SHUTDOWN_SCRIPT="$WORKING_DIR/vl-shutdown.sh"

# These will be replaced with hardcoded values by the VL Portal (varying for each job)
export STORAGE_BUCKET="{0}"
export STORAGE_BASE_KEY_PATH="{1}"
export STORAGE_ACCESS_KEY="{2}"
export STORAGE_SECRET_KEY="{3}"
export WORKFLOW_URL="{4}"
export STORAGE_ENDPOINT="{5}"
export STORAGE_TYPE="{6}"
export STORAGE_AUTH_VERSION="{7}"
export OS_REGION_NAME="{8}"
export SHUTDOWN_URL="{10}"
export WALLTIME="{11}"
export VL_LOG_FILE_NAME="vl.sh.log"
export VL_LOG_FILE="$WORKING_DIR/$VL_LOG_FILE_NAME"
export VL_TERMINATION_FILE_NAME="vl.end"
export VL_TERMINATION_FILE="$WORKING_DIR/$VL_TERMINATION_FILE_NAME"

# Load our profile so this run is the same as a regular user login (to make debugging easier)
source /etc/profile

echo "------ VL Bootstrap Script ---------"
echo "                                      "
echo "------ Provisioning ----------"
echo ""
{9}
echo ""
echo "------ Printing Environment ----------"
echo "VL_BOOTSTRAP_VERSION = $VL_BOOTSTRAP_VERSION"
echo "WORKING_DIR = $WORKING_DIR"
echo "WORKFLOW_URL = $WORKFLOW_URL"
echo "WORKFLOW_SCRIPT = $WORKFLOW_SCRIPT"
echo "PATH = $PATH"
echo "LD_LIBRARY_PATH = $LD_LIBRARY_PATH"
echo "STORAGE_ENDPOINT = $STORAGE_ENDPOINT"
echo "STORAGE_TYPE = $STORAGE_TYPE"
echo "VL_LOG_FILE = $VL_LOG_FILE"
if [ $WALLTIME > 0 ]; then
    echo "SHUTDOWN_SCRIPT = $SHUTDOWN_SCRIPT"
    echo "WALLTIME = $WALLTIME"
fi
echo "VL_TERMINATION_FILE = $VL_TERMINATION_FILE"
echo "--------------------------------------"

# If a walltime is present, set walltime shutdown parameters
if [ $WALLTIME > 0 ]; then
    #Download shutdown script and make it executable
    echo "Downloading shutdown script from $SHUTDOWN_URL and storing it at $SHUTDOWN_SCRIPT"
    curl -f -L "$SHUTDOWN_URL" -o "$SHUTDOWN_SCRIPT"
    echo "curl result $?"
    echo "Making $SHUTDOWN_SCRIPT executable"
    chmod +x "$SHUTDOWN_SCRIPT"
    echo "chmod result $?"
    at -f $SHUTDOWN_SCRIPT now + $WALLTIME min | tee -a "$VL_LOG_FILE"
fi

#Download our workflow and make it executable
echo "Downloading workflow script from $WORKFLOW_URL and storing it at $WORKFLOW_SCRIPT"
curl -f -L "$WORKFLOW_URL" -o "$WORKFLOW_SCRIPT"
echo "curl result $?"
echo "Making $WORKFLOW_SCRIPT executable"
chmod +x "$WORKFLOW_SCRIPT"
echo "chmod result $?"

echo "executing workflow script $WORKFLOW_SCRIPT"

# If we have unbuffer - lets use that to get stdout as it gets written
# otherwise we just get the buffered version
if command -v unbuffer > /dev/null 2>&1 ; then
  unbuffer $WORKFLOW_SCRIPT 2>&1 | tee -a "$VL_LOG_FILE"
else
  $WORKFLOW_SCRIPT 2>&1 | tee -a "$VL_LOG_FILE"
fi



