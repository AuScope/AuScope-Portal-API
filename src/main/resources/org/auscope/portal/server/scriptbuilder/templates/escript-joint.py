
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

"""3D gravity/magnetic joint inversion using netCDF data"""

# Set parameters
MAGNETIC_DATASET = '${magnetic-file}'
GRAVITY_DATASET = '${gravity-file}'
# background magnetic flux density (B_north, B_east, B_vertical) in nano Tesla.
B_b = [${bb-north}, ${bb-east}, ${bb-vertical}]
# amount of horizontal padding (this affects end result, about 20% recommended)
PAD_X = ${x-padding}
PAD_Y = ${y-padding}
# maximum depth (in meters)
DEPTH = ${max-depth}
# buffer zone above data (in meters; 6-10km recommended)
AIR = ${air-buffer}
# number of mesh elements in vertical direction (~1 element per 2km recommended)
NE_Z = ${vertical-mesh-elements}
# trade-off factors
mu_gravity = ${mu-gravity}
mu_magnetic = ${mu-magnetic}

N_THREADS = ${n-threads}

####### Do not change anything below this line #######

import os
import subprocess
import sys

try:
    from esys.downunder import *
    from esys.escript import unitsSI as U
    from esys.weipa import *

except ImportError:
    line=["/opt/escript/bin/run-escript","-t" + str(N_THREADS)]+sys.argv
    ret=subprocess.call(line)
    sys.exit(ret)

def saveAndUpload(fn, **args):
    saveSilo(fn, **args)
    subprocess.call(["cloud", "upload", fn, fn, "--set-acl=public-read"])

def statusCallback(k, x, Jx, g_Jx, norm_dx):
     print("Iteration %s complete. Error=%s" % (k, norm_dx))

B_b=[b*U.Nano*U.Tesla for b in B_b]
MAG_UNITS = U.Nano * U.Tesla
GRAV_UNITS = 1e-6 * U.m/(U.sec**2)

# Setup and run the inversion
grav_source=NetCdfData(NetCdfData.GRAVITY, GRAVITY_DATASET, scale_factor=GRAV_UNITS)
mag_source=NetCdfData(NetCdfData.MAGNETIC, MAGNETIC_DATASET, scale_factor=MAG_UNITS)
db=DomainBuilder(dim=3)
db.addSource(grav_source)
db.addSource(mag_source)
db.setVerticalExtents(depth=DEPTH, air_layer=AIR, num_cells=NE_Z)
db.setFractionalPadding(pad_x=PAD_X, pad_y=PAD_Y)
db.setBackgroundMagneticFluxDensity(B_b)
db.fixDensityBelow(depth=DEPTH)
db.fixSusceptibilityBelow(depth=DEPTH)

inv=JointGravityMagneticInversion()
inv.setup(db)
inv.setSolverCallback(statusCallback)
inv.getCostFunction().setTradeOffFactorsModels([mu_gravity, mu_magnetic])
inv.getCostFunction().setTradeOffFactorsRegularization(mu = [1.,1.], mu_c=1.)

density, susceptibility = inv.run()
print("density = %s"%density)
print("susceptibility = %s"%susceptibility)

g, wg = db.getGravitySurveys()[0]
B, wB = db.getMagneticSurveys()[0]
saveAndUpload("result.silo", density=density, gravity_anomaly=g, gravity_weight=wg, susceptibility=susceptibility, magnetic_anomaly=B, magnetic_weight=wB)
print("Results saved in result.silo")


# Visualise result.silo using VisIt
import visit
visit.LaunchNowin()
saveatts = visit.SaveWindowAttributes()
saveatts.family = 0
saveatts.width = 1024
saveatts.height = 768
saveatts.resConstraint = saveatts.NoConstraint
saveatts.outputToCurrentDirectory = 1
saveatts.fileName = 'result-susceptibility.png'
visit.SetSaveWindowAttributes(saveatts)
visit.OpenDatabase('result.silo')
visit.AddPlot('Contour', 'susceptibility')
c=visit.ContourAttributes()
c.colorType=c.ColorByColorTable
c.colorTableName = "hot"
visit.SetPlotOptions(c)
visit.DrawPlots()
visit.SaveWindow() # save susceptibility image
visit.ChangeActivePlotsVar('density')
saveatts.fileName = 'result-density.png'
visit.SetSaveWindowAttributes(saveatts)
v=visit.GetView3D()
v.viewNormal=(-0.554924, 0.703901, 0.443377)
v.viewUp=(0.272066, -0.3501, 0.896331)
visit.SetView3D(v)
visit.SaveWindow() # save density image
visit.DeleteAllPlots()
visit.CloseDatabase('result.silo')

subprocess.call(["cloud", "upload", "result-density.png", "result-density.png", "--set-acl=public-read"])
subprocess.call(["cloud", "upload", "result-susceptibility.png", "result-susceptibility.png", "--set-acl=public-read"])

