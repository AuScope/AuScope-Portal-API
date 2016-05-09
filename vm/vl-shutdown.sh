###############################################################################
#
# Shell script for shutting down a portal VM if walltime has been exceeded.
#
# Uploads a file (vl-shutdown.txt) to indicate walltime has been exceeded
# before commencing shutdown (provided ABORT_SHUTDOWN has not been set).
#
###############################################################################

# If ABORT_SHUTDOWN has not been set, commence shutdown
if [ -f $ABORT_SHUTDOWN_PATH ]; then
    echo 'Instance shutdown has been aborted'
else
    # Upload a file indicating walltime has been exceeded
    echo "Walltime exceeded" > walltime-exceeded.txt
    echo "cloud upload walltime-exceeded.txt walltime-exceeded.txt"
    cloud upload walltime-exceeded.txt walltime-exceeded.txt
    # Shut it down
    shutdown -h now
fi
