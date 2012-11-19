'''
CSIRO quick evil cleanup script

Designed to delete instances from a endpoint we 'think' are shutdown.

Tuned to work on NCI Essex release, tested for the VGL resevation owner @ 31st July 2012

Authors: Josh Vote, Terry Rankine
'''

import boto
import logging

from boto.ec2.connection import EC2Connection
from boto.ec2.regioninfo import *

'''logging.basicConfig(filename='example.log',level=logging.DEBUG)'''
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


# An array of dictionaries representing info about each cloud
# to poll
clouds = [{
    "name" : "NCI (Canberra)",
    "owner":"a92448ea8ad34b0691b5669f50485f48",
    "aws_access_key_id":"ENTER_AWS_ACCESS_KEY",
    "aws_secret_access_key":"ENTER_AWS_SECRET_KEY",
    "endpoint":"openstack.nci.org.au",
    "path":"/services/Cloud",
    "port":8773,
    "is_secure" : False,
    "terminated_state" : unicode("shutoff")
    },{
    "name" : "NeCTAR (Melbourne)",
    "owner":None,
    "aws_access_key_id":"ENTER_AWS_ACCESS_KEY",
    "aws_secret_access_key":"ENTER_AWS_SECRET_KEY",
    "endpoint":"nova.rc.nectar.org.au",
    "path":"/services/Cloud",
    "port":8773,
    "is_secure" : True,
    "terminated_state" : unicode("stopped")
}]

# Returns a connection for a given 'cloud' dictionary
def setup(cloud):
    region = RegionInfo(name=cloud["name"], endpoint=cloud["endpoint"])
    connection = boto.connect_ec2(aws_access_key_id=cloud["aws_access_key_id"],
                        aws_secret_access_key=cloud["aws_secret_access_key"],
                        is_secure=cloud["is_secure"],
                        region=region,
                        port=cloud["port"],
                        path=cloud["path"])
    return connection

def killthemall(connection, cloud):
    reservations = connection.get_all_instances()
    # import pdb; pdb.set_trace()

    for reservation in reservations:
        if (cloud["owner"] == None) or (reservation.owner_id == cloud["owner"]):
            for instance in reservation.instances:
                if instance.state == cloud["terminated_state"]:
                    logger.debug("%s\n%s\n\n\n\n" % (instance.id, instance.get_console_output().output))
                    if "Power down." in instance.get_console_output().output \
                            and \
                        "Sending all processes the KILL signal" in instance.get_console_output().output:
                        logger.warn("[%s] terminating %s, launch time = %s" % (cloud["name"], instance.id, instance.launch_time))
                        logger.debug("[%s] response: %s" % (cloud["name"], connection.terminate_instances([instance.id])))
                    else:
                        logger.warn("[%s] powerdown not detected %s, launch time = %s" % (cloud["name"], instance.id, instance.launch_time))
                else:
                    logger.warn("[%s] dont terminate %s" % (cloud["name"], instance.id))


if __name__ == "__main__":
    for cloud in clouds:
        con = setup(cloud)
        killthemall(con, cloud)
