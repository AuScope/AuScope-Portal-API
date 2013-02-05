# -*- coding: utf-8 -*-
"""
Created on Fri Jan 25 11:24:25 2013

@author: ran110
"""

import os, fnmatch, subprocess
modules = ['gdal/1.9.1_netcdf4',
           'proj/4.7.0',
           ]
          

def findFiles(starting_dir='.', pattern='*'):
    '''look for files to process'''
    matches = []
    for root, dirnames, filenames in os.walk(starting_dir):
        for filename in fnmatch.filter(filenames, pattern):
            matches.append(os.path.join(root, filename))
    return matches


fileset = findFiles('/projects/r17/GA', '*.ers')

for currFile in fileset:
    '''gdalwarp -of ERS -s_srs "+proj=latlong +datum=GDA94" -t_srs "+proj=latlong +datum=WGS84" -r cubic /projects/r17/GA/GSQ_P1247tmig.ers reproj.ers -dstnodata -99999
       gdal_translate -of netCDF -co "FORMAT=NC4" -co "COMPRESS=DEFLATE" -co "ZLEVEL=9" -stats reproj.ers reproj.nc4
    '''
    warp_command = ['gdalwarp',
               '-s_srs', '+proj=latlong +datum=GDA94',
               '-t_srs', '+proj=latlong +datum=WGS84',
               '-of', 'ERS',
               '-dstnodata', '-99999',
               '-r', 'cubic',
               currFile,
               currFile.replace('r17','p36').replace('.ers', '.warp.ers')
              ]
    translate_command = ['gdal_translate',
               '-of', 'netCDF',
               '-co', 'FORMAT=NC4',
               '-co', 'COMPRESS=DEFLATE',
               '-co', 'ZLEVEL=9',
               currFile.replace('r17','p36').replace('.ers', '.warp.ers'),
               currFile.replace('r17','p36').replace('.ers', '.warp.nc4')
               ]

    print " ".join(warp_command)
    stdout, stderr = subprocess.Popen(warp_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
    if stdout.strip() is not None:
        print stdout
    if stderr.strip() is not None:
        print stderr
    print " ".join(translate_command)
    stdout, stderr = subprocess.Popen(translate_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
    if stdout.strip() is not None:
        print stdout
    if stderr.strip() is not None:
        print stderr
