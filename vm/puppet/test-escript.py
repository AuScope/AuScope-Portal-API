# This is just a barebones test to run to ensure escript/visit successfully installed in VL environment
# You probably want to run "source /etc/profile" before running this script 

N_THREADS = 1


import os
import subprocess
import sys

try:
    from esys.downunder import *
    from esys.escript import unitsSI as U
    from esys.weipa import saveSilo
except ImportError:
    line=["/opt/escript/bin/run-escript","-t" + str(N_THREADS)]+sys.argv
    ret=subprocess.call(line)
    sys.exit(ret)


print("Successfully imported escript")


# Visualise result.silo using VisIt
import visit

print("Successfully imported visit")

import urllib2
response = urllib2.urlopen("https://wci.llnl.gov/content/assets/docs/simulation/computer-codes/silo/datafiles/csg.silo")
with open('example.silo','wb') as f:
    f.write(response.read())

print("Successfully downloaded example silo")

visit.LaunchNowin()
saveatts = visit.SaveWindowAttributes()
saveatts.fileName = 'result-visit.png'
saveatts.family = 0
saveatts.width = 1024
saveatts.height = 768
saveatts.resConstraint = saveatts.NoConstraint
saveatts.outputToCurrentDirectory = 1
visit.SetSaveWindowAttributes(saveatts)
visit.OpenDatabase('example.silo')
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

visit.DeleteAllPlots()
visit.CloseDatabase('example.silo')

print("Successfully rendered output raster")
print("All done!")