
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

### Basic script to run gravity inversion with escript ###

# Filename for input data
DATASET='${inversion-file}'
# maximum depth (in meters)
DEPTH=${max-depth}
# buffer zone above data (in meters; 6-10km recommended)
AIR=${air-buffer}
# number of mesh elements in vertical direction (~1 element per 2km recommended)
NE_Z=${vertical-mesh-elements}
# amount of horizontal padding (this affects end result, about 20% recommended)
PAD_X=${x-padding}
PAD_Y=${y-padding}

####### Do not change anything below this line #######

import os
import subprocess
import sys

try:
    from esys.downunder import *
    from esys.weipa import saveSilo
except ImportError:
    line=["/opt/escript/bin/run-escript","-t4"]+sys.argv
    ret=subprocess.call(line)
    sys.exit(ret)

def saveAndUpload(fn, **args):
    saveSilo(fn, **args)
    subprocess.call(["cloud", "upload", fn, fn, "--set-acl=public-read"])

source=NetCdfData(DataSource.GRAVITY, DATASET)
db=DomainBuilder()
db.addSource(source)
db.setVerticalExtents(depth=DEPTH, air_layer=AIR, num_cells=NE_Z)
db.setPadding(PAD_X, PAD_Y)
inv=GravityInversion()
inv.setup(db)
g, chi =  db.getGravitySurveys()[0]
density=inv.run()
saveAndUpload('result.silo', gravity_anomaly=g, gravity_weight=chi, density=density)


# Visualise result.silo using VisIt
import visit
visit.LaunchNowin()
saveatts = visit.SaveWindowAttributes()
saveatts.fileName = 'result-visit.png'
saveatts.family = 0 # this disables appending of a number # other interesting attributes:
saveatts.width = 1024
saveatts.height = 768
#saveatts.outputDirectory =
saveatts.outputToCurrentDirectory = 1
visit.SetSaveWindowAttributes(saveatts)
visit.OpenDatabase('result.silo')
visit.AddPlot('Contour', 'density') # or Pseudocolor
c=visit.ContourAttributes()
c.colorType=c.ColorByColorTable
c.colorTableName = "hot"
visit.SetPlotOptions(c)
visit.DrawPlots()
visit.SaveWindow()
subprocess.call(["cloud", "upload", "result-visit.png", "result-visit.png", "--set-acl=public-read"])
