"""
	The following are additional unused uwGocadGAMaterialsModelGenerator.sh parameters
	
    --defaultConductivity=(1.)          Default thermal conductivity to be used in regions where voxets do not specify a value.
    --defaultHeatProduction=(0.)        Default heat production to be used in regions where voxets do not specify a value.
    --minX=(FromVoxet)                  Minimum domain X coordinate. If available, default is value from voxet definition.
    --maxX=(FromVoxet)                  Maximum domain X coordinate. If available, default is value from voxet definition.
    --minY=(FromVoxet)                  Minimum domain Y coordinate. If available, default is value from voxet definition.
    --maxY=(FromVoxet)                  Maximum domain Y coordinate. If available, default is value from voxet definition.
    --minZ=(FromVoxet)                  Minimum domain Z coordinate. If available, default is value from voxet definition.
    --maxZ=(FromVoxet)                  Maximum domain Z coordinate. If available, default is value from voxet definition.
    
    The following are the descriptions for the remaining uwGocadGAMaterialsModelGenerator.sh parameters
    
    --useLowerFluxBC                    Use a lower boundary heat flux (Neumann) boundary condition?
    --name=(GAGocadGeothermal)          A name for your simulation
    --voxetFilename=                    Filename for the voxet dataset
    --elementResI=(16)                  Number of FEM elements along X axis
    --elementResJ=(16)                  Number of FEM elements along Y axis
    --elementResK=(16)                  Number of FEM elements along Z axis
    --lowerBoundaryTemp=(300)           Lower boundary (minZ) temperature BC value. This value is ignored if --useLowerFluxBC is set.
    --upperBoundaryTemp=(0)             Upper boundary (maxZ) temperature BC value
    --lowerBoundaryFlux=(0.0125)        Lower boundary (minZ) heat flux value.  This value is ignored if --useLowerFluxBC is not set.
    --nprocs=(1)                        Number of processors to run job with
    --bin=(/usr/local/underworld/ImporteUnderworld binary executable, including absolute path
    --materialsPropName=                Name of voxet property corresponding to the material definitions
    --keyFilename=                      Name of your materials key CSV file.  Data be in format:  Name, Index, Conductivity, HeatProduction.  First line a header.
    
"""


import subprocess, csv, math, os, sys, urllib, glob;
import logging


def cloudUpload(inFilePath, cloudKey):
    cloudBucket = os.environ["STORAGE_BUCKET"]
    cloudDir = os.environ["STORAGE_BASE_KEY_PATH"]
    queryPath = (cloudBucket + "/" + cloudDir + "/" + cloudKey).replace("//", "/")
    retcode = subprocess.call(["cloud", "upload", cloudKey, inFilePath, "--set-acl=public-read"])
    print "cloudUpload: " + inFilePath + " to " + queryPath + " returned " + str(retcode)

#Build the default list of arguments
argList = ["uwGocadGAMaterialsModelGenerator.sh", "--voxetFilename=${voxet-filename}", "--materialsPropName=${materials-property}", "--keyFilename=${voxet-key}", "--nprocs=${n-threads}", "--run"]

#Add optional arguments
lowerBoundaryFlux = "${lower-boundary-flux}"
lowerBoundaryTemp = "${lower-boundary-temp}"
if (len(lowerBoundaryFlux) > 0):
    argList.append("--useLowerFluxBC")
    argList.append("--lowerBoundaryFlux=" + lowerBoundaryFlux)

if (len(lowerBoundaryTemp) > 0):
    argList.append("--lowerBoundaryTemp=" + lowerBoundaryTemp)

argList.append("--elementResI=${n-fem-x}")
argList.append("--elementResJ=${n-fem-y}")
argList.append("--elementResK=${n-fem-z}")


#Begin processing
print argList
retcode = subprocess.call(argList)
print "result: " + str(retcode)

#Upload results directory
dirName = "output_GAGocadGeothermal/"
print "Fetching output files"
outputFiles =  glob.glob(dirName + "*")
print outputFiles
for invFile in outputFiles:
    cloudUpload(invFile, invFile.replace(dirName, ""))
print "Done"
