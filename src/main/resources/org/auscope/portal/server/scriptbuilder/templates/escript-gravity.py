
##############################################################################
#
# Copyright (c) 2009-2013 by University of Queensland
# http://www.uq.edu.au
#
# Primary Business: Queensland, Australia
# Licensed under the Open Software License version 3.0
# http://www.opensource.org/licenses/osl-3.0.php
#
# Development until 2012 by Earth Systems Science Computational Center (ESSCC)
# Development since 2012 by School of Earth Sciences
#
##############################################################################

"""3D gravity inversion using netCDF data"""

# Filename for input data
DATASET = '${inversion-file}'
# maximum depth (in meters)
DEPTH = ${max-depth}
# buffer zone above data (in meters; 6-10km recommended)
AIR = ${air-buffer}
# number of mesh elements in vertical direction (~1 element per 2km recommended)
NE_Z = ${vertical-mesh-elements}
# amount of horizontal padding (this affects end result, about 20% recommended)
PAD_X = ${x-padding}
PAD_Y = ${y-padding}

####### Do not change anything below this line #######

import os
import subprocess
import sys

try:
    from esys.downunder import *
    from esys.escript import unitsSI as U
    from esys.weipa import saveSilo
except ImportError:
    line=["/opt/escript/bin/run-escript","-t4"]+sys.argv
    ret=subprocess.call(line)
    sys.exit(ret)

def saveAndUpload(fn, **args):
    saveSilo(fn, **args)
    subprocess.call(["cloud", "upload", fn, fn, "--set-acl=public-read"])

DATA_UNITS = 1e-6 * U.m/(U.sec**2)
source=NetCdfData(DataSource.GRAVITY, DATASET, scale_factor=DATA_UNITS)
db=DomainBuilder()
db.addSource(source)
db.setVerticalExtents(depth=DEPTH, air_layer=AIR, num_cells=NE_Z)
db.setFractionalPadding(PAD_X, PAD_Y)
db.fixDensityBelow(depth=DEPTH)
inv=GravityInversion()
inv.setup(db)
g, chi =  db.getGravitySurveys()[0]
density=inv.run()
saveAndUpload('result.silo', gravity_anomaly=g, gravity_weight=chi, density=density)
print("Results saved in result.silo")


# Visualise result.silo using VisIt
import visit
visit.LaunchNowin()
saveatts = visit.SaveWindowAttributes()
saveatts.fileName = 'result-visit.png'
saveatts.family = 0
saveatts.width = 1024
saveatts.height = 768
saveatts.resConstraint = saveatts.NoConstraint
saveatts.outputToCurrentDirectory = 1
visit.SetSaveWindowAttributes(saveatts)
visit.OpenDatabase('result.silo')
visit.AddPlot('Contour', 'density')
c=visit.ContourAttributes()
c.colorType=c.ColorByColorTable
c.colorTableName = "hot"
visit.SetPlotOptions(c)
visit.DrawPlots()
v=visit.GetView3D()
v.viewNormal=(-0.554924, 0.703901, 0.443377)
v.viewUp=(0.272066, -0.3501, 0.896331)
visit.SetView3D(v)
visit.SaveWindow()
subprocess.call(["cloud", "upload", "result-visit.png", "result-visit.png", "--set-acl=public-read"])
visit.DeleteAllPlots()
visit.CloseDatabase('result.silo')

