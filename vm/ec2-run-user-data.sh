#!/bin/bash

### BEGIN INIT INFO
# Provides:          ec2-run-user-data
# Required-Start:    $network $local_fs $remote_fs $syslog $all
# Required-Stop:     
# Default-Start:     2 3 4 5
# Default-Stop:      
# Short-Description: Run EC2 user-data scripts
# Description:       On first boot of EC2 instance, runs user-data if it starts with #!
#
### END INIT INFO

# ec2-run-user-data - Run instance user-data if it looks like a script.
#
# Only retrieves and runs the user-data script once per instance.  If
# you want the user-data script to run again (e.g., on the next boot)
# then add this command in the user-data script:
#   rm -f /var/ec2/ec2-run-user-data.*
#
# Originally sourced from http://ec2ubuntu.googlecode.com/svn/trunk/etc/init.d/ec2-run-user-data
#
# History:
#
#   2012-07-24 Josh Vote
#   - Modified original script to work with CentOS 6
#
#   2011-03-25 Eric Hammond <ehammond@thinksome.com>
#   - Add LSB info to support update-rc.d
#   - Improve check for compressed user-data
#
#   2010-01-07 Tom White
#   - Add support for gzip-compressed user data
#
#   2008-05-16 Eric Hammond <ehammond@thinksome.com>
#   - Initial version including code from Kim Scheibel, Jorge Oliveira
#
prog=$(basename $0)
logger="echo"
curl="curl --retry 3 --silent --show-error --fail"
instance_data_url=http://169.254.169.254/2008-02-01

# Wait until meta-data is available.
perl -MIO::Socket::INET -e '
 until(new IO::Socket::INET("169.254.169.254:80")){print"Waiting for meta-data...\n";sleep 1}
' | $logger

# Exit if we have already run on this instance (e.g., previous boot).
ami_id=$($curl $instance_data_url/meta-data/ami-id)
been_run_file=/var/ec2/$prog.$ami_id
mkdir -p $(dirname $been_run_file)
if [ -f $been_run_file ]; then
  $logger < $been_run_file
  exit
fi

# Retrieve the instance user-data and run it if it looks like a script
user_data_file=/tmp/ec2-user-data.tmp
$logger "Retrieving user-data"
$curl -o $user_data_file $instance_data_url/user-data 2>&1 | $logger
if [ "$(file -bi $user_data_file| cut -f1 -d';')" = 'application/x-gzip' ]; then
  $logger "Uncompressing gzip'd user-data"
  mv $user_data_file $user_data_file.gz
  gunzip $user_data_file.gz
fi
if [ ! -s $user_data_file ]; then
  $logger "No user-data available"
  echo "user-data was not available" > $been_run_file
elif head -1 $user_data_file | egrep -v '^#!'; then
  $logger "Skipping user-data as it does not begin with #!"
  echo "user-data did not begin with #!" > $been_run_file
else
  $logger "Running user-data"
  echo "user-data has already been run on this instance" > $been_run_file
  chmod +x $user_data_file
  $user_data_file
  $logger "user-data exit code: $?"
fi
rm -f $user_data_file
