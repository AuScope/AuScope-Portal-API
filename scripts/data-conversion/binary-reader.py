# -*- coding: utf-8 -*-
"""
Created on Thu Jan 24 12:03:12 2013

@author: ran110
"""

staticLookupType = {"IEEE8ByteReal":'d',
                    "Signed32BitInteger":'i',
                    "IEEE4ByteReal":'f',
                    "Signed16BitInteger":'h',
                    }

import os, fnmatch, array, sys, numpy


def findFiles(starting_dir='.', pattern='*.pd.vec'):
    '''look for files to process'''
    matches = []
    for root, dirnames, filenames in os.walk(starting_dir):
        for filename in fnmatch.filter(filenames, pattern):
            matches.append(os.path.join(root, filename))
    return matches

def getDataType(filelist):
    dataTypes = {}
    for header in filelist:
        try:
            binaryType=""
            f=open(header,'r')
            data=f.readlines()
            print data
            for line in data:
                if 'CellType' in line:
                    binaryType = [item.strip() for item in line.split('=')][1]
                    break
            dataTypes[header] = staticLookupType[binaryType]
            f.close()
        except Exception, e:
            print e.message

    for items in dataTypes:
        print items, dataTypes[items]
        
    return dataTypes


def extractData(dataTypes):
    allData = {}
    for datafile in dataTypes.keys():
        res = array.array(dataTypes[datafile])
        try:
            print 
            with open(datafile.replace('.vec',''), 'r+b') as f:
                while True:
                    try: 
                        res.fromfile(f, 1024*1024)
                    except EOFError: 
                        break
            print datafile.replace('.vec',''), ':', res.buffer_info()
            allData[datafile] = res
        except Exception, e:
            print e.message

    return allData

def main():
    filesToProcess = findFiles('C:/Users/ran110/Downloads/GSWA_P1239RAD/')
    dataTypes = getDataType(filesToProcess)
    bigData = extractData(dataTypes)
    print bigData.keys()
    f = open('ascidump.txt', 'w')
    f.write(','. join(str(x) for x in bigData.pop('C:/Users/ran110/Downloads/GSWA_P1239RAD/easting.PD.vec')))
    f.write('\n')
    f.close()
    

if __name__ == '__main__':
    main()