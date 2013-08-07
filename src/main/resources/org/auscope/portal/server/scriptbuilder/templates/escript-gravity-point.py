#!/usr/bin/python2.6

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


import csv
import xml.etree.ElementTree as ET
import sys
import subprocess
import os

N_THREADS = ${n-threads}
DATAFILE = '${inversion-file}'

try:
    from esys.downunder import *
    from esys.escript import unitsSI as U
    from esys.weipa import saveSilo
except ImportError:
    line=["/opt/escript/bin/run-escript","-t" + str(N_THREADS)]+sys.argv
    ret=subprocess.call(line)
    sys.exit(ret)


# File name for pre process input file



class Vgl(file):

    def __init__(self, file):
        self.header = ['lat','long','elevation'];
        self.run(file);


    def run(self,file):
        dics = self.getXMLDict(file);
        self.writeToCSV(dics,"dem.csv");
        self.writeVRT("dem.vrt");
		self.convertToGridWithGDAL();

    def writeToCSV(self,dictionaryData,filename):
        with open(filename,'w') as f:
            writer = csv.DictWriter(f,fieldnames=self.header);
            #python2.7 only- writer.writeheader();
			writer.writerow(dict((fn,fn) for fn in writer.fieldnames));
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
		self.latMin=90.00;
        self.latMax=-90.00;
        self.longMin=180.00;
        self.longMax=-180.00;

        for featureMembers in root:
            for gravitypoints in featureMembers:
                dict={};
                dict['elevation'] = gravitypoints.find('{http://ga.gov.au/}spherical_cap_bouguer_anomaly').text;
                points = (gravitypoints.find('{http://www.opengis.net/gml}location/{http://www.opengis.net/gml}Point/{http://www.opengis.net/gml}pos').text).split();
                #we will eventually need to add some smarts to determine lat/long long/lat
				dict['lat'] = points[1];
                dict['long']= points[0];
                if (float(points[0]) > self.longMax):
					self.longMax=float(points[0]);
                if (float(points[0]) < self.longMin):
					self.longMin=float(points[0]);
                if (float(points[1]) > self.latMax):
					self.latMax=float(points[1]);
                if (float(points[1]) < self.latMin):
					self.latMin=float(points[1]);				
                csvArray.append(dict);
				
		self.srs=(root[0][0].find('{http://www.opengis.net/gml}location/{http://www.opengis.net/gml}Point')).get('srsName');
		self.srs='EPSG:' + self.srs[-4:];		
        return csvArray;


	def convertToGridWithGDAL(self):		
		print self.srs;
		print "latMax:"+str(self.latMax);
		print "latMin:"+str(self.latMin);
		print "longMax:"+str(self.longMax);
		print "longMin:"+str(self.longMin);
		p = subprocess.call(["gdal_grid", "-zfield", "elevation","-a_srs",self.srs, "-a", "invdist:power=2.0:smoothing=1.0", "-txe", str(self.longMin), str(self.longMax), "-tye", str(self.latMin), str(self.latMax), "-outsize", "400", "400", "-of", "netCDF", "-ot", "Float64", "-l", "dem", "dem.vrt", "dem.nc", "--config", "GDAL_NUM_THREADS", "ALL_CPUS"]);
		subprocess.call(["cloud", "upload", "dem.nc", "dem.nc", "--set-acl=public-read"]);	
		subprocess.call(["cloud", "upload", "dem.csv", "dem.csv", "--set-acl=public-read"]);
		subprocess.call(["cloud", "upload", "dem.vrt", "dem.vrt", "--set-acl=public-read"]);
	


if __name__ == '__main__':
    Vgl(DATAFILE);
			

####### End of data preparation #########




"""3D gravity inversion using netCDF data"""

# Filename for post process input data 
DATASET = "/root/dem.nc" 
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





def saveAndUpload(fn, **args):
    saveSilo(fn, **args)
    subprocess.call(["cloud", "upload", fn, fn, "--set-acl=public-read"])


print("Processing GDAL file now");
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

