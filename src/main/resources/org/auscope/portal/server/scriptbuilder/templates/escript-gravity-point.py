
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

####### Start of data preparation #########

#! /usr/bin/python2.7
import csv
import xml.etree.ElementTree as ET
import sys
import subprocess

# File name for pre process input file
DATAFILE = '${inversion-file}'


class Vgl(file):

    def __init__(self, file):
        self.header = ['lat','long','elevation'];
        self.run(file);


    def run(self,file):
        dics = self.getXMLDict(file);
        self.writeToCSV(dics,"dem.csv");
        self.writeVRT("dem.vrt");


    def writeToCSV(self,dictionaryData,filename):
        with open(filename,'w') as f:
            writer = csv.DictWriter(f,fieldnames=self.header);
            writer.writeheader();
            for d in dictionaryData:
                writer.writerow(d);

    def writeVRT(self,filename):
        with open(filename,'w') as f:
            f.write("<OGRVRTDataSource>\n");
            f.write("       <OGRVRTLayer name=\"dem\">\n");
            f.write("           <SrcDataSource>dem.csv</SrcDataSource>\n");
            f.write("           <LayerSRS>EPSG:4283</LayerSRS>\n");
            f.write("           <GeometryField encoding=\"PointFromColumns\" x=\"long\" y=\"lat\" z=\"elevation\"/>\n");
            f.write("       </OGRVRTLayer>\n");
            f.write("</OGRVRTDataSource>\n");



    def getXMLDict(self,filename):
        tree = ET.parse(filename);
        root = tree.getroot();
        csvArray=[];
        for featureMembers in root:
            for gravitypoints in featureMembers:
                dict={};
                dict['elevation'] = gravitypoints.find('{http://ga.gov.au/}spherical_cap_bouguer_anomaly').text;
                points = (gravitypoints.find('{http://www.opengis.net/gml}location/{http://www.opengis.net/gml}Point/{http://www.opengis.net/gml}pos').text).split();
                dict['lat'] = points[0];
                dict['long']= points[1];
                csvArray.append(dict);
        return csvArray;

def main(args):
    Vgl(DATAFILE);
    p = subprocess.Popen(["gdal_grid", "-zfield", "Elevation", "-a", "invdist:power=2.0:smoothing=1.0", "-txe", "85000", "89000", "-tye", "894000", "890000", "-outsize", "400", "400", "-of", "netCDF", "-ot", "Float64", "-l", "dem", "dem.vrt", "dem.nc", "--config", "GDAL_NUM_THREADS", "ALL_CPUS"],stdout=subprocess.PIPE);
    output, err = p.communicate()
    print  output


if __name__ == '__main__':
    main(sys.argv)
				
				
				

####### End of data preparation #########




"""3D gravity inversion using netCDF data"""

# Filename for post process input data 
DATASET = "dem.nc" 
# maximum depth (in meters)
DEPTH = ${max-depth}
# buffer zone above data (in meters; 6-10km recommended)
AIR = ${air-buffer}
# number of mesh elements in vertical direction (~1 element per 2km recommended)
NE_Z = ${vertical-mesh-elements}
# amount of horizontal padding (this affects end result, about 20% recommended)
PAD_X = ${x-padding}
PAD_Y = ${y-padding}

N_THREADS = ${n-threads}

####### Do not change anything below this line #######

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

