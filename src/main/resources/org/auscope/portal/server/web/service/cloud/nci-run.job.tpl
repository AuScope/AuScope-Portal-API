#!/bin/bash
#PBS -P {0}
#PBS -q normal
#PBS -l walltime={4}
#PBS -l mem={6}
#PBS -l jobfs={7}
#PBS -l ncpus={5}
#PBS -l wd
#PBS -l storage=scratch/{0}+gdata/{0}{10}
#PBS -j oe
#PBS -N vl{1}
#PBS -o {3}/.run.log

# This batch file is expected to be copied into and then run directly from the VL_OUTPUT_DIR
# It is responsible for running the users job script in an environment with all modules loaded
# and data downloaded. The script will write all pertinent output files to  VL_OUTPUT_DIR

#Redirect all output to our log file (after preserving the current contents) 
echo "stdout/stderr to be redirected to {3}/vl.sh.log"
DL_LOG_CONTENT=`cat "{3}/.download.log"`
echo "" > "{3}/vl.sh.log"
exec >> "{3}/vl.sh.log"
exec 2>&1
echo "$DL_LOG_CONTENT"

export VL_PROJECT_ID="{0}"
export VL_JOB_ID="{1}"
export VL_WORKING_DIR="{2}"
export VL_OUTPUT_DIR="{3}"
export VL_TERMINATION_FILE="$VL_OUTPUT_DIR/vl.end"
export VL_TOTAL_CPU_COUNT="{5}"
if [ `expr $VL_TOTAL_CPU_COUNT % 16` -eq "0" ]; then
    export VL_TOTAL_NODES=`expr $VL_TOTAL_CPU_COUNT / 16`
else
    export VL_TOTAL_NODES=`expr $VL_TOTAL_CPU_COUNT / 16 + 1`
fi
export VL_CPUS_PER_NODE=`expr $VL_TOTAL_CPU_COUNT / $VL_TOTAL_NODES`

echo "#### Compute Environment start ####"
env | sort
echo "#### Compute Environment end ####"

source nci-util.sh

# Move our working data to the job node file system
cp -r "$VL_OUTPUT_DIR/." "$VL_WORKING_DIR" || finish 2 "ERROR: Unable to copy data from $VL_OUTPUT_DIR to working dir at $VL_WORKING_DIR"
cd "$VL_WORKING_DIR" || finish 2 "ERROR: Unable to access working directory at $VL_WORKING_DIR"

# Load Modules
module purge
{8}

# Emulate our "cloud" command line tool
export PATH="$VL_WORKING_DIR:$PATH"
echo ''#!/bin/bash'' > cloud
echo ''cp "$3" "$VL_OUTPUT_DIR/$2"'' >> cloud
chmod +x cloud

# Run User Script
echo "#### Python start ####"
computeStartTime=`date +%s`
{9} "vl_script.py"
computeEndTime=`date +%s` 
echo "#### Python start ####"

echo "#### Compute Time start ####"
totalComputeTime=`expr $computeEndTime - $computeStartTime`
echo "Total compute time was `expr $totalComputeTime / 3600` hour(s), `expr $totalComputeTime % 3600 / 60` minutes and `expr $totalComputeTime % 60` seconds"
echo "#### Compute Time end ####"

# Tidy up
finish 0 "INFO: Finished run job"
