#!/bin/bash

export VL_WORKFLOW_VERSION="1"

function finish {
  if [ -d "$VL_WORKING_DIR" ] && [ ${#VL_WORKING_DIR} -gt 4 ]
  then
    rm -rf "$VL_WORKING_DIR"
  fi
  
  date > "$VL_TERMINATION_FILE"
  cloud upload "$VL_TERMINATION_FILE"

  if [ "$2" != "" ]
  then
    log "$2"
  fi

  exit $1
}

function log {
  timestamp="`date +%Y-%m-%d:%H:%M:%S`"
  echo "$PBS_JOBID $timestamp $@"
}

function cloud {
  if [ "$1" == "upload" ]
  then
    cp "$2" "$VL_OUTPUT_DIR/$2" || finish 3 "ERROR: Unable to copy file $2 to output directory $VL_OUTPUT_DIR/$2"
  fi
}
