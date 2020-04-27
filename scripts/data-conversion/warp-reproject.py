# -*- coding: utf-8 -*-
"""
Created on Fri Jan 25 11:24:25 2013

@author: ran110
"""

import os, glob, subprocess


fileset = glob.glob('/projects/r17/test/data/*.ers')

''' 
put the temp stuff in the temp dir!
final stuff into the shared dir
'''

for currFile in fileset:
    '''gdalwarp -of ERS -s_srs "+proj=latlong +datum=GDA94" -t_srs "+proj=latlong +datum=WGS84" -r cubic /projects/r17/test/data/GSQ_P1247tmig.ers reproj.ers -dstnodata -99999
       gdal_translate -of netCDF -co "FORMAT=NC4" -co "COMPRESS=DEFLATE" -co "ZLEVEL=9" -stats reproj.ers reproj.nc4
    '''

    
    if not os.path.exists(currFile.replace('r17/test/data/','r17/test/outputs/netCDF/').replace('.ers', '.nc4')):
        warp_command = ['gdalwarp',
                   '-s_srs', '+proj=latlong +datum=GDA94',
                   '-t_srs', '+proj=latlong +datum=WGS84',
                   '-of', 'ERS',
                   '-dstnodata', '-99999',
                   '-r', 'cubic',
                   currFile,
                   currFile.replace('r17/test/data/','r17/test/temp/').replace('.ers', '.warp.ers')
                  ]
        translate_command = ['gdal_translate',
                   '-of', 'netCDF',
                   '-co', 'FORMAT=NC4',
                   '-co', 'COMPRESS=DEFLATE',
                   '-co', 'ZLEVEL=9',
                   currFile.replace('r17/test/data/','r17/test/temp/').replace('.ers', '.warp.ers'),
                   currFile.replace('r17/test/data/','r17/test/outputs/netCDF/').replace('.ers', '.nc4')
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

        
    else:
        print currFile.replace('r17/test/data/','r17/test/outputs/netCDF/').replace('.ers', '.nc4'), 'already exists'
