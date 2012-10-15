import subprocess, csv, math, os, sys, urllib, glob;
import logging

def cloudUpload(inFilePath, cloudKey):
    cloudBucket = os.environ["STORAGE_BUCKET"]
    cloudDir = os.environ["STORAGE_BASE_KEY_PATH"]
    queryPath = (cloudBucket + "/" + cloudDir + "/" + cloudKey).replace("//", "/")
    retcode = subprocess.call(["cloud", "upload", cloudKey, inFilePath, "--set-acl=public-read"])
    print "cloudUpload: " + inFilePath + " to " + queryPath + " returned " + str(retcode)

print "starting"
retcode = subprocess.call(["uwGocadGAMaterialsModelGenerator.sh", "--voxetFilename=${voxet-filename}", "--materialsPropName=${materials-property}", "--keyFilename=${voxet-key}", "--run"])
print "result: " + str(retcode)


dirName = "output_GAGocadGeothermal/"
print "Fetching output files"
outputFiles =  glob.glob(dirName + "*")
print outputFiles
for invFile in outputFiles:
    cloudUpload(invFile, invFile.replace(dirName, ""))
print "Done"
