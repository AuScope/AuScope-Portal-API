# -*- coding: utf-8 -*-
"""
Created on Tue Jan 29 15:09:25 2013

@author: ran110
"""
import fnmatch, os, subprocess

'''
- demg: elevation grid
- tmig: total magnetic intensity grid
- doseg: Radiometrics Dose Rate grid 
- pctkg: Radiometrics Potassium Percentage grid 
- ppmthg: Radiometrics Thorium parts per million grid
- ppmug: Radiometrics Uranium parts per million grid 
'''
changes = {'ppmug':{'long_name':'Radiometrics Uranium parts per million grid'},
           'ppmthg':{'long_name':'Radiometrics Thorium parts per million grid'},
           'pctkg':{'long_name':'Radiometrics Potassium Percentage grid'},
           'doseg':{'long_name':'Radiometrics Dose Rate grid'},
           'tmig':{'long_name':'Total magnetic intensity grid'},
           'demg':{'long_name':'Digital elevation grid'},
            }
            
import glob

for filetype in changes.keys():
    for currfile in glob.glob('/projects/r17/shared/netCDF/*%s.warp.nc4' % filetype):
        print filetype, currfile
        try:
            command = ['ncrename', '-h', '-v', 'Band1,%s' % filetype, currfile]
            stdout, stderr = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
            if stdout.strip() is not None:
                print stdout
            if stderr.strip() is not None:
                print stderr
        except Exception, e:
            print e
            pass

        try:
            command = ['ncatted', '-a',
                       'long_name,%s,o,c,%s' % (filetype, changes[filetype]['long_name']),
                       currfile ]
            stdout, stderr = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
            if stdout.strip() is not None:
                print stdout
            if stderr.strip() is not None:
                print stderr            
        except Exception, e:
            print e
            pass