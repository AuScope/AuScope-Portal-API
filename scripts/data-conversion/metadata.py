# -*- coding: utf-8 -*-
"""
Created on Tue Jan 29 21:09:09 2013

@author: ran110

"""
import os, fnmatch, uuid, netCDF4


def walklevel(some_dir, level=1):
    some_dir = some_dir.rstrip(os.path.sep)
    assert os.path.isdir(some_dir)
    num_sep = some_dir.count(os.path.sep)
    for root, dirs, files in os.walk(some_dir):
        yield root, dirs, files
        num_sep_this = root.count(os.path.sep)
        if num_sep + level <= num_sep_this:
            del dirs[:]


def findFiles(starting_dir='.', pattern='*'):
    '''look for files to process'''
    matches = []
    for root, dirnames, filenames in walklevel(starting_dir, 1):
        for filename in fnmatch.filter(filenames, pattern):
            matches.append(os.path.join(root, filename))
    return matches



templateFiles = {'child' : {
                    'filename':'/projects/r17/scripts/iso19139-child-template.xml',
                    'abstract':'{ABSTRACT}',
                    'uuid':'{UUID}',
                    'parent':'{PARENTUUID}',
                    'title':'{TITLE}',
                    'date':'{DATE}',
                    'abstract':'{ABSTRACT}',
                    'theme':'{THEME}',
                    'west':'{WEST}',
                    'east':'{EAST}',
                    'north':'{NORTH}',
                    'south':'{SOUTH}',
                    'wms-address':'{ONLINEWMSRESOURCE}',
                    'layername':'{LAYERNAME}',
                    },
                 'parent':'iso19139-parent-template.xml'}


wmsServer="http://siss2.anu.edu.au/thredds/wms/ga/projects/"
'''build a list of files we care about....'''
'''fileset = findFiles('/projects/r17/GA', '*pctkg.isi') + \
          findFiles('/projects/r17/GA', '*demg.isi') + \
          findFiles('/projects/r17/GA', '*doseg.isi') + \
          findFiles('/projects/r17/GA', '*ppmthg.isi') + \
          findFiles('/projects/r17/GA', '*ppmug.isi') + \
          findFiles('/projects/r17/GA', '*timg.isi') '''

import glob
fileset = glob.glob('/projects/r17/GA/*.isi')

for currentfile in sorted(fileset):
    try:
        filename, extension = os.path.splitext(os.path.basename(currentfile))
        if '.ers.' not in filename and os.path.exists('/projects/r17/shared/netCDF/'+filename+'.warp.nc4'):
            fileuuid=uuid.uuid4()
            mydata={}
            f=open(currentfile, 'r')
            outputTemplate = open(templateFiles['child']['filename'], 'r').read()
            
            for line in f.readlines():
                if '=' in line:
                    mydata[line.split('=')[0].strip().lower()] = line.split('=')[1].strip().replace('"','')
            print currentfile
            f.close()
    
    
            replaceVars = templateFiles['child']
            if mydata.has_key('label'):
                outputTemplate = outputTemplate.replace(replaceVars['title'], mydata['label'].replace('_', ' '))
            else:
                print currentfile, 'title unknown'
                outputTemplate = outputTemplate.replace(replaceVars['title'], filename)
            outputTemplate = outputTemplate.replace(replaceVars['abstract'], str(mydata))
            outputTemplate = outputTemplate.replace(replaceVars['uuid'], str(fileuuid))
            if mydata.has_key('date'):
                outputTemplate = outputTemplate.replace(replaceVars['date'], mydata['date'].split('_')[0])
            else:
                print currentfile, 'date unknown'
                outputTemplate = outputTemplate.replace(replaceVars['date'], 'unknown')
            if mydata.has_key('theme'):
                outputTemplate = outputTemplate.replace(replaceVars['theme'], mydata['theme'])
            else:
                print currentfile, 'theme unknown'
                outputTemplate = outputTemplate.replace(replaceVars['theme'], 'unknown')
            outputTemplate = outputTemplate.replace(replaceVars['wms-address'], wmsServer+filename+'.warp.nc4')
            outputTemplate = outputTemplate.replace(replaceVars['layername'], filename)
            
            data = netCDF4.Dataset('/projects/r17/shared/netCDF/'+filename+'.warp.nc4','r', format='NETCDF4')
            north = str(data.variables['lat'][-1:][0])
            west = str(data.variables['lon'][0])
            south = str(data.variables['lat'][0])
            east = str(data.variables['lon'][-1:][0])
            print north, west, south, east
            outputTemplate = outputTemplate.replace(replaceVars['north'], north)
            outputTemplate = outputTemplate.replace(replaceVars['west'], west)
            outputTemplate = outputTemplate.replace(replaceVars['south'], south)
            outputTemplate = outputTemplate.replace(replaceVars['east'], east)
    
            
            f = open(currentfile[:-4].replace('r17/GA/','r17/shared/metadata/')+'.xml', 'w')
            f.write(outputTemplate)
            f.close()
        
        
    except Exception, e:
        print e
        pass
