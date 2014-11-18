#!/usr/bin/env python
from novaclient.client import Client
import logging
import smtplib
import socket
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

'''logging.basicConfig(filename='example.log',level=logging.DEBUG)'''
logging.basicConfig(format='%(asctime)s %(levelname)s  %(message)s', level=logging.WARN)
logging.root.setLevel(logging.WARN)

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

hostname = socket.gethostname()

# An array of dictionaries representing info about each cloud
# to poll
clouds = [{
    "name" : "NCI-VHIRL-private (Canberra)",
    "username" : "USERNAME",
    "password" : "PASSWORD",
    "project" : "PROJECT",
    "version": 2,
    "auth": "http://130.56.241.100:5000/v2.0",
    "terminated_state" : unicode("shutoff"),
    "error_state" : unicode("error")
    },{
    "name" : "NeCTAR-VHIRL",
    "username":"USERNAME",
    "password":"PASSWORD",
    "project" :"PROJECT",
    "auth":"https://keystone.rc.nectar.org.au:5000/v2.0/",
    "version": 2,
    "terminated_state" : unicode("shutoff"),
    "error_state" : unicode("error")
}]

# Returns a connection for a given 'cloud' dictionary
def setup(cloud):
    connection = Client(cloud["version"], cloud["username"], cloud["password"], cloud["project"], cloud["auth"])
    return connection

def killthemall(connection, cloud):
    theList = connection.servers.list()

    for server in theList:
        serverConsole = "" 
        try:
            serverConsole = server.get_console_output()
        except:
            logger.error('server %s@%s console not available' % (server.name, cloud['name']))
            pass
        
        if server.status.lower() == cloud["error_state"]:
            logger.warn("[%s] terminate error instance: %s (%s)" % (cloud["name"], server.id, server.name))
            response = server.delete()
            logger.debug("[%s] deleted %s (%s) - response: %s" % (cloud["name"], server.id, server.name, response))
            tellSomeone(server, cloud, response, serverConsole)
        elif server.status.lower() == cloud["terminated_state"]:
            logger.debug("%s\n%s\n\n\n\n" % (server.id, serverConsole))
            if "Power down." in serverConsole \
                    and \
                "Sending all processes the KILL signal" in serverConsole:
                logger.warn("[%s] terminating %s(%s), launch time = %s" % (cloud["name"], server.name, server.id, server.created))
                logger.debug("[%s] response: %s" % (cloud["name"], server.delete()))
            else:
                logger.warn("[%s] powerdown not detected %s(%s), launch time = %s" % (cloud["name"], server.name, server.id, server.created))
        elif server.status.lower() == "active":
            if "No user-data available" in serverConsole and \
               "Giving up" in serverConsole:
                logger.warn("Doh! - found a broken one")
                logger.warn("[%s] terminate error instance: %s (%s)" % (cloud["name"], server.id, server.name))
                response = server.delete()
                logger.warn("[%s] deleted %s (%s) - response: %s" % (cloud["name"], server.id, response))
                tellSomeone(server, cloud, response, serverConsole)
            else:
                logger.info("[%s] dont terminate %s(%s) - it is %s" % (cloud["name"], server.name, server.id, server.status))

        else:
            logger.info("[%s] dont terminate %s(%s) - it is %s" % (cloud["name"], server.name, server.id, server.status))

def tellSomeone(server, cloud, response, serverConsole):
    msg = MIMEMultipart()

    text = MIMEText( \
"""Found another one:

The system has another bad image - %s@%s.

We did try to terminate it.
Cloud response: %s.

Regards,
System Ghost @ %s.""" % (
                   server.id,
                   cloud,
                   response,
                   hostname
                   ))
    attachment = MIMEText(serverConsole)
    attachment.add_header('Content-Disposition', 'attachment', filename="console.txt")
    msg.attach(attachment)
    msg.attach(text)
    msg['Subject'] = "BAD Cloud machine"
    msg['To'] = "cg-portal@csiro.au"
    msg['From'] = "system-ghost@%s" % hostname


    s = smtplib.SMTP('localhost')
    s.sendmail(msg['From'], [msg['To']], msg.as_string())
    s.quit()

if __name__ == "__main__":
    for cloud in clouds:
        con = setup(cloud)
        killthemall(con, cloud)
