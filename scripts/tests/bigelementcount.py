''' n Billion element problem..... 

YOU MUST HAVE OVER 32G of memory!

'''

import h5py, os
import numpy as np

''' one million x one thousand..... - yeah '''
bigdata = np.random.rand(1000000, 1000)
print bigdata

def calcStats(outputfilename='results.h5'):
    ''' open up the hdf5 files and grab the 'data' sets, 
    in memory... 
    and then try to run some calcs '''

    try:
        outputfile = h5py.File(outputfilename, 'w')

        outputfile.create_dataset('std-dev', data=np.std(bigdata, axis=1))
        outputfile.create_dataset('average', data=np.average(bigdata, axis=1))
        outputfile.create_dataset('min', data=np.min(bigdata, axis=1))
        outputfile.create_dataset('max', data=np.max(bigdata, axis=1))
        outputfile.close()
        print "outputfile created [ok]: %s" % outputfilename

    except Exception, e:
        print e.message

def main():
    calcStats()
    
if __name__ == '__main__':
    main()
