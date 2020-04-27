#!/bin/bash
#PBS -P {0}
#PBS -q copyq
#PBS -l walltime={4}
#PBS -l mem=300MB
#PBS -l ncpus=1
#PBS -l wd
#PBS -l storage=scratch/{0}+gdata/{0}
#PBS -j oe
#PBS -N vl{1}
#PBS -o {3}/.download.log

# This batch file is expected to be copied into and then run directly from the VL_OUTPUT_DIR
# It is responsible for downloading all remote data services into the working directory
# And then submitting the actual run job 

export VL_PROJECT_ID="{0}"
export VL_JOB_ID="{1}"
export VL_WORKING_DIR="{2}"
export VL_OUTPUT_DIR="{3}"
export VL_TERMINATION_FILE="$VL_OUTPUT_DIR/vl.end"
export VL_JOBID_FILE="$VL_OUTPUT_DIR/.jobid"
export VL_WORKFLOW_VERSION="1"

echo "#### Download Environment start ####"
env | sort
echo "#### Download Environment end ####"

source nci-util.sh

echo $PBS_JOBID > $VL_JOBID_FILE

# Set our workflow version to indicate that the job is running
echo "$VL_WORKFLOW_VERSION" > "$VL_OUTPUT_DIR/workflow-version.txt" || finish 2 "ERROR: Set workflow version in $VL_WORKING_DIR/workflow-version.txt"

# Create our working directory to receive downloaded data
mkdir -p "$VL_WORKING_DIR" || finish 2 "ERROR: Unable to create $VL_WORKING_DIR"
cp "$VL_OUTPUT_DIR/vl-download.sh" "$VL_WORKING_DIR" || finish 2 "ERROR: Unable to copy $VL_OUTPUT_DIR/vl-download.sh to $VL_WORKING_DIR"
cd "$VL_WORKING_DIR" || finish 2 "ERROR: Unable to access $VL_WORKING_DIR"

echo "#### Download start ####"
downloadStartTime=`date +%s`
source vl-download.sh
downloadEndTime=`date +%s`
totalDownloadTime=`expr $downloadEndTime - $downloadStartTime`
echo "Total download time was `expr $totalDownloadTime / 3600` hour(s), `expr $totalDownloadTime % 3600 / 60` minutes and `expr $totalDownloadTime % 60` seconds"
echo "#### Download end ####"

# Submit our actual processing job
cd "$VL_OUTPUT_DIR" || finish 2 "ERROR: Unable to return to $VL_OUTPUT_DIR"
RAWID=`qsub nci-run.job` || finish 3 "ERROR: Unable to submit nci-run.job"
echo "$'{'RAWID%.*'}'" > $VL_JOBID_FILE
