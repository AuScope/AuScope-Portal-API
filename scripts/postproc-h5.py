# -*- coding: utf-8 -*-
"""
Post processing.... take 1
Created on Thu Jan 17 14:32:06 2013

@author: ran110
"""

'''
import subprocess
subprocess.call(['pip', 'install', 'h5py'])
'''

import h5py, os
import numpy as np

import fnmatch



def findFiles(starting_dir='.', pattern='Temp*h5'):
    '''look for files to process'''
    matches = []
    for root, dirnames, filenames in os.walk(starting_dir):
        for filename in fnmatch.filter(filenames, pattern):
            matches.append(os.path.join(root, filename))
    return matches
    
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
    filelist = findFiles()
    calcStats(filelist)
    
if __name__ == '__main__':
    main()
