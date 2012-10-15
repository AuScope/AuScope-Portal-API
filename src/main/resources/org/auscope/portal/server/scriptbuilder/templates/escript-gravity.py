
########################################################
#
# Copyright (c) 2003-2012 by University of Queensland
# Earth Systems Science Computational Center (ESSCC)
# http://www.uq.edu.au/esscc
#
# Primary Business: Queensland, Australia
# Licensed under the Open Software License version 3.0
# http://www.opensource.org/licenses/osl-3.0.php
#
########################################################

__copyright__="""Copyright (c) 2003-2012 by University of Queensland
Earth Systems Science Computational Center (ESSCC)
http://www.uq.edu.au/esscc
Primary Business: Queensland, Australia"""
__license__="""Licensed under the Open Software License version 3.0
http://www.opensource.org/licenses/osl-3.0.php"""
__url__="https://launchpad.net/escript-finley"

# Hack to enable eScript in a vanilla python environment
try:
    import esys.escript
except ImportError:
    import subprocess
    import sys
    import os
    #Disabled temporarily
    #line=["/opt/escript/bin/run-escript","-p2"]+sys.argv
    line=["/opt/escript/bin/run-escript","-t4"]+sys.argv
    print line
    ret=subprocess.call(line)
    sys.exit(ret)

import logging
import os
import subprocess
from esys.escript import unitsSI as U
from esys.ripley import Brick
from esys.weipa import *
from esys.downunder.datasources import NetCDFDataSource
from esys.downunder.inversions import GravityInversion

loglevel=logging.DEBUG
formatter=logging.Formatter('[%(name)s] \033[1;30m%(message)s\033[0m')

inversionLogFn='inversion.log'
logger=logging.getLogger('inv')
logger.setLevel(loglevel)
handler=logging.StreamHandler()
handler.setFormatter(formatter)
handler.setLevel(loglevel)
logger.addHandler(handler)
handler=logging.FileHandler(inversionLogFn)
formatter=logging.Formatter('%(asctime)s - [%(name)s] %(message)s')
handler.setFormatter(formatter)
handler.setLevel(loglevel)
logger.addHandler(handler)

source=NetCDFDataSource(domainclass=Brick, gravfile='${inversion-file}')
#source.setPadding(5, 0.1)
inv=GravityInversion()
inv.setDataSource(source)
inv.setSolverTolerance(1e-7)
inv.setSolverMaxIterations(300)
inv.setSolverOptions(initialHessian=1e-2)

def cloudUpload(inFilePath, cloudKey):
    cloudBucket = os.environ["STORAGE_BUCKET"]
    cloudDir = os.environ["STORAGE_BASE_KEY_PATH"]
    queryPath = (cloudBucket + "/" + cloudDir + "/" + cloudKey).replace("//", "/")
    retcode = subprocess.call(["cloud", "upload", cloudKey, inFilePath, "--set-acl=public-read"])
    logger.debug("cloudUpload: " + inFilePath + " to " + queryPath + " returned " + str(retcode))

def solverCallback(k, x, fx, gfx):
    fn='inv.%d.silo'%k
    ds=createDataset(rho=inv.mapping.getValue(x))
    ds.setCycleAndTime(k,k)
    ds.saveSilo(fn)
    logger.debug("Jreg(m) = %e"%inv.regularization.getValue(x))
    logger.debug("f(m) = %e"%fx)
    cloudUpload(fn, fn)

inv.setSolverCallback(solverCallback)
inv.setup()
rho_new=inv.run()
cloudUpload(inversionLogFn, inversionLogFn)
