import subprocess, csv, os, sys, glob, fnmatch

'''hack std-out and std-err for a nicer log file...'''
if sys.stdout.name == '<stdout>':
    sys.stdout = os.fdopen(sys.stdout.fileno(), 'w', 0)

if sys.stderr.name == '<stderr>':
    sys.stderr = os.fdopen(sys.stderr.fileno(), 'w', 0)

try:
    subprocess.call(['pip', 'install', 'h5py'])
except Exception, e:
    try:
	    subprocess.call(['pip-python', 'install', 'h5py'])
	except:
	    print "unable to install pre-requisites"
		sys.exit(1)
	


class unitProperty:
    '''This class describes the unitProperties required for a given geology'''
    name="GenericRock"
    comment=""
    thermal_cond=0.0
    heat_prod=0.0
    geoID=0

    def __init__(self, geoID, thermal_cond, head_prod, comment):
        self.thermal_cond = thermal_cond
        self.comment  = comment
        self.heat_prod = head_prod
        self.geoID = geoID

    def getList(self):
        return [self.geoID, self.thermal_cond, self.heat_prod, self.comment]

    def __lt__(self, other):
        return self.geoID < other.geoID


def cloudUpload(inFilePath, cloudKey):
    cloudBucket = os.environ["STORAGE_BUCKET"]
    cloudDir = os.environ["STORAGE_BASE_KEY_PATH"]
    queryPath = (cloudBucket + "/" + cloudDir + "/" + cloudKey).replace("//", "/")
    retcode = subprocess.call(["cloud", "upload", cloudKey, inFilePath, "--set-acl=public-read"])
    print "cloudUpload: " + inFilePath + " to " + queryPath + " returned " + str(retcode)


''' Alter everything but unified stepping a[0]=run0, a[1]=run1 '''
arrayRunner = { "Basement":  {"heat_prod":  [1e-06, 1e-06, 3e-06, 3e-06, 3e-06, 3e-06, 1e-06,],
                              "thermal_cond": [3.54, 3.54, 3.54, 3.54, 10.0, 10.0, 1.0,] },
                "Basement NW": {"heat_prod": [3e-06, 1e-06, 1e-06, 3e-06, 5e-06, 1e-06, 5e-06,],  },
                "Basement SE": {"heat_prod": [1e-06, 3e-06, 1e-06, 3e-06, 1e-06, 5e-06, 5e-06,],  },
                }


cvsFormat = ["Model Unit", "Geology Property Number", "Thermal Conductivity, W/mK", "Heat Production, W/m3", "Comment"]
csvKeyFile = {"Air": unitProperty(0, 80, 0, "Some Comment"),
            "Surficial": unitProperty( 1, 2.8135, 1.40E-06, "Some Comment" ),
            "Allaru": unitProperty( 2, 2.04, 1.40E-06, "Some Comment" ),
            "Cadna-owie": unitProperty( 3, 2.499, 1.40E-06, "Some Comment" ),
            "Westbourne": unitProperty( 4, 2.7455, 1.40E-06, "Some Comment" ),
            "Nappamerri": unitProperty( 5, 2.2695, 1.20E-06, "Some Comment" ),
            "Toolachee": unitProperty( 6, 1.3855, 1.20E-06, "Some Comment" ),
            "Patchawarra": unitProperty( 7, 1.785, 1.20E-06, "Some Comment" ),
            "Tirrawarra": unitProperty( 8, 2.5415, 1.20E-06, "Some Comment" ),
            "Basement": unitProperty( 9, 3.54, 3.80E-06, "Some Comment" ),
            "Warrabin Trough": unitProperty( 10, 2.3205, 1.20E-06, "Some Comment" ),
            "Granite": unitProperty( 11, 2.8, 3.80E-06, "Some Comment" ),
            "Granite Big Lake Suite": unitProperty( 12, 2.8, 8.70E-06, "Some Comment" ),
            "Granites Devonian": unitProperty( 13, 2.8, 3.80E-06, "Some Comment" ),
            "Basement NW": unitProperty( 14, 3.54, 5.30E-06, "Some Comment" ),
            "Basement SE": unitProperty( 15, 3.54, 4.60E-06, "Some Comment" ),
            "NoDataValue": unitProperty( -99999, '', '', ''),
}

dirName = "output_GAGocadGeothermal/"

def generateCSV(inputdata=[], filename="inputfile.csv"):
  try:
    directory = os.path.dirname(filename)
    if not os.path.exists(directory):
      os.makedirs(directory)
    newCSV=open(filename, 'wb')
    wr = csv.writer(newCSV)
    wr.writerow(cvsFormat)
    for key,item in sorted(csvKeyFile.items(), key=lambda x: abs(x[1].geoID)):
      wr.writerow([key]+item.getList())
    newCSV.close()
    print "filename %s created: OK" % (filename)
  except Exception, e:
      print "could not generate the file"
      print e.message

def findFiles(starting_dir='.', pattern='Temp*h5'):
    '''look for files to process'''
    matches = []
    for root, dirnames, filenames in os.walk(starting_dir):
        for filename in fnmatch.filter(filenames, pattern):
            matches.append(os.path.join(root, filename))
    return matches


import h5py, os
import numpy as np

def calcStats(filelist, outputfilename='results.h5'):
    ''' open up the hdf5 files and grab the 'data' sets, 
    in memory... 
    and then try to run some calcs '''
    try:
        outputfile = h5py.File(outputfilename, 'w')

        bigdata = None

        i=0
        for currFile in filelist:
            f = h5py.File(currFile, 'r')
            dataset = f['/data']
            mydata = dataset[...]
            
            if bigdata is None:
                bigdata = np.zeros([mydata.shape[0], len(filelist)])
            bigdata[:,i]=np.squeeze(mydata)
            i+=1

        print np.std(bigdata, axis=1)
        print np.average(bigdata, axis=1)
        print np.min(bigdata, axis=1)
        print np.max(bigdata, axis=1)

        outputfile.create_dataset('std-dev', data=np.std(bigdata, axis=1))
        outputfile.create_dataset('average', data=np.average(bigdata, axis=1))
        outputfile.create_dataset('min', data=np.min(bigdata, axis=1))
        outputfile.create_dataset('max', data=np.max(bigdata, axis=1))
        outputfile.close()
        print "outputfile created [ok]: %s" % outputfilename

    except Exception, e:
        print e.message


def main():
  ''' Later on this may end up inside the loop to change things (like lower flux....)'''
  #Build the default list of arguments
  argList = ["uwGocadGAMaterialsModelGenerator.sh", "--voxetFilename=../Cooper_Basin_3D_Map_geology.vo", "--materialsPropName=Geology", "--keyFilename=KeyToVoxSet.csv", "--run"]
  
  #Add optional arguments
  lowerBoundaryFlux = "0.04"
  lowerBoundaryTemp = ""
  if (len(lowerBoundaryFlux) > 0):
      argList.append("--useLowerFluxBC")
      argList.append("--lowerBoundaryFlux=" + lowerBoundaryFlux)
  
  if (len(lowerBoundaryTemp) > 0):
      argList.append("--lowerBoundaryTemp=" + lowerBoundaryTemp)

  ''' We are treating each element in the array as a run - count one of them for the number of runs'''
  runCount = len(arrayRunner["Basement"]['heat_prod'])
  
  for arr_index in xrange(0,runCount):
      newcsvKeyFile=csvKeyFile
  
      for geounit in arrayRunner:
        ''' heat prod first'''
        if arrayRunner[geounit].has_key('heat_prod'):
          newVal = arrayRunner[geounit]['heat_prod'][arr_index]
          print 'altering %s %s: orig:%s => new:%s' % (geounit, 'heat_prod',
                   newcsvKeyFile[geounit].heat_prod, newVal)
          newcsvKeyFile[geounit].heat_prod = newVal
        if arrayRunner[geounit].has_key('thermal_cond'):
          newVal = arrayRunner[geounit]['thermal_cond'][arr_index]
          print 'altering %s %s: orig: %s => new: %s' % (geounit, 'thermal_cond',
                   newcsvKeyFile[geounit].thermal_cond, newVal)
          newcsvKeyFile[geounit].thermal_cond = arrayRunner[geounit]['thermal_cond'][arr_index]
  
      generateCSV(newcsvKeyFile, 'run%s/KeyToVoxSet.csv' % arr_index)
  
      startdir=os.getcwd()
      try:
          os.chdir('run%s' % arr_index)
  
          #Begin processing
          print " ".join(argList)
          retcode = subprocess.call(argList)
          print "result: " + str(retcode)
  
          os.chdir(startdir)
      except Exception, e:
          print "broken!"
          print e.message
          os.chdir(startdir)

  # create output dirs
  os.mkdir(dirName)

  filelist = findFiles()
  calcStats(filelist, dirName+'/stats.h5')

  #Zip up the outputs...
  arglist = ['zip', '-9', '-r', dirName+'/outputs.zip'] + glob.glob('run*')
  retcode = subprocess.call(arglist)
  
  #Upload results directory
  print "Fetching output files"
  outputFiles =  glob.glob(dirName + "*")
  print outputFiles
  for invFile in outputFiles:
      cloudUpload(invFile, invFile.replace(dirName, ""))
  print "Done"

if __name__ == "__main__":
  main()
