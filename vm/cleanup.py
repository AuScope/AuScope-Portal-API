'''
CSIRO quick evil cleanup script

Designed to delete instances from a endpoint we 'think' are shutdown.

Tuned to work on NCI Diablo release, tested for the VEGL resevation owner @ 31st July 2012

Authors: Josh Vote, Terry Rankine
'''

import boto
import logging

from boto.ec2.connection import EC2Connection
from boto.ec2.regioninfo import *

'''logging.basicConfig(filename='example.log',level=logging.DEBUG)'''
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)



openstack = {
    "owner":"vegl",
    "aws_access_key_id":"83ea19e7-e6cc-4529-8b97-2b4668f476e5:vegl",
    "aws_secret_access_key":"CHANGEME!",
    }


def setup():
    region = RegionInfo(name="NeCTAR", endpoint="openstack.nci.org.au")
    connection = boto.connect_ec2(aws_access_key_id=openstack["aws_access_key_id"],
                        aws_secret_access_key=openstack["aws_secret_access_key"],
                        is_secure=False,
                        region=region,
                        port=8773,
                        path="/services/Cloud")
    return connection    

def killthemall(connection):
    reservations = connection.get_all_instances()
    # import pdb; pdb.set_trace()

    for reservation in reservations:
        if reservation.owner_id == openstack["owner"]:
            for instance in reservation.instances:
                if instance.state_code == 0:
                    logger.debug("%s\n%s\n\n\n\n" % (instance.id, instance.get_console_output().output))
                    if "Power down." in instance.get_console_output().output \
                            and \
                        "Sending all processes the KILL signal" in instance.get_console_output().output:
                        logger.warn("terminating %s, launch time = %s" % (instance.id, instance.launch_time))
                        logger.debug("response: %s" % (connection.terminate_instances([instance.id])))
                    else:
                        logger.warn("powerdown not detected %s, launch time = %s" % (instance.id, instance.launch_time))
                else:
                    logger.warn("dont terminate %s" % instance.id)


if __name__ == "__main__":
    con = setup()
    killthemall(con)
