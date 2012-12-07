import subprocess, csv, math, os, sys, urllib, glob;
import logging


def cloudUpload(inFilePath, cloudKey):
    cloudBucket = os.environ["STORAGE_BUCKET"]
    cloudDir = os.environ["STORAGE_BASE_KEY_PATH"]
    queryPath = (cloudBucket + "/" + cloudDir + "/" + cloudKey).replace("//", "/")
    retcode = subprocess.call(["cloud", "upload", cloudKey, inFilePath, "--set-acl=public-read"])
    print "cloudUpload: " + inFilePath + " to " + queryPath + " returned " + str(retcode)

#Build the default list of arguments
argList = ["uwGocadGAMaterialsModelGenerator.sh", "--voxetFilename=${voxet-filename}", "--materialsPropName=${materials-property}", "--keyFilename=${voxet-key}", "--run"]

#Add optional arguments
lowerBoundaryFlux = "${lower-boundary-flux}"
lowerBoundaryTemp = "${lower-boundary-temp}"
if (len(lowerBoundaryFlux) > 0):
    argList.append("--useLowerFluxBC")
    argList.append("--lowerBoundaryFlux=" + lowerBoundaryFlux)

if (len(lowerBoundaryTemp) > 0):
    argList.append("--lowerBoundaryTemp=" + lowerBoundaryTemp)

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
