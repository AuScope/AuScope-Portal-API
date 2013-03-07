
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

"""3D gravity/magnetic joint inversion example using netCDF data"""

# Set parameters
MAGNETIC_DATASET = '${magnetic-file}'
GRAVITY_DATASET = '${gravity-file}'
latitude = ${latitude}
# amount of horizontal padding (this affects end result, about 20% recommended)
PAD_X= ${x-padding}
PAD_Y= ${y-padding}
# maximum depth (in meters)
thickness = ${max-depth}
# buffer zone above data (in meters; 6-10km recommended)
l_air = ${air-buffer}
# number of mesh elements in vertical direction (~1 element per 2km recommended)
n_cells_v = ${vertical-mesh-elements}
mu_gravity = ${mu-gravity}
mu_magnetic = ${mu-magnetic}

####### Do not change anything below this line #######

import os
import subprocess
import sys

try:
    from esys.downunder import *
    from esys.escript import unitsSI as U
    from esys.escript import saveDataCSV
    from esys.weipa import *

except ImportError:
    line=["/opt/escript/bin/run-escript","-t4"]+sys.argv
    ret=subprocess.call(line)
    sys.exit(ret)

def saveAndUpload(fn, **args):
    saveSilo(fn, **args)
    subprocess.call(["cloud", "upload", fn, fn, "--set-acl=public-read"])

MAG_UNITS = U.Nano * U.V * U.sec / (U.m**2)
GRAV_UNITS = 1e-6 * U.m/(U.sec**2)

# Setup and run the inversion
B_b=simpleGeoMagneticFluxDensity(latitude=latitude)
grav_source=NetCdfData(NetCdfData.GRAVITY, GRAVITY_DATASET, scale_factor=GRAV_UNITS)
mag_source=NetCdfData(NetCdfData.MAGNETIC, MAGNETIC_DATASET, scale_factor=MAG_UNITS)
db=DomainBuilder(dim=3)
db.addSource(grav_source)
db.addSource(mag_source)
db.setVerticalExtents(depth=thickness, air_layer=l_air, num_cells=n_cells_v)
db.setFractionalPadding(pad_x=PAD_X, pad_y=PAD_Y)
db.setBackgroundMagneticFluxDensity(B_b)
db.fixDensityBelow(depth=thickness)
db.fixSusceptibilityBelow(depth=thickness)

inv=JointGravityMagneticInversion()
inv.setSolverTolerance(1e-4)
inv.setSolverMaxIterations(50)
inv.setup(db)
inv.getCostFunction().setTradeOffFactorsModels([mu_gravity, mu_magnetic])
inv.getCostFunction().setTradeOffFactorsRegularization(mu = [1.,1.], mu_c=1.)

density, susceptibility = inv.run()
print("density = %s"%density)
print("susceptibility = %s"%susceptibility)

g, wg = db.getGravitySurveys()[0]
B, wB = db.getMagneticSurveys()[0]
saveAndUpload("result_gravmag.silo", density=density, gravity_anomaly=g, gravity_weight=wg, susceptibility=susceptibility, magnetic_anomaly=B, magnetic_weight=wB)
print("Results saved in result_gravmag.silo")

