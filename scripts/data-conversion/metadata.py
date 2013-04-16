# -*- coding: utf-8 -*-
"""
Created on Tue Jan 29 21:09:09 2013

@author: ran110
@author: rmg599

"""
import glob, os, fnmatch, uuid, netCDF4, logging, csv, xml.sax.saxutils

formatter = logging.Formatter('%(asctime)s %(message)s')
errlogger = logging.getLogger('metadata_bad')
errloggerHdlr = logging.FileHandler('metadata.bad')
errloggerHdlr.setFormatter(formatter)
errlogger.addHandler(errloggerHdlr)
errlogger.setLevel(logging.WARNING)

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

additionalMetadata = {}

templateFiles = {'child': {
                    'filename':'/projects/r17/test/scripts/iso19139-child-template.xml',
                    'abstract':'{ABSTRACT}',
                    'uuid':'{UUID}',
                    'parent':'{PARENTUUID}',
                    'title':'{TITLE}',
                    'date':'{DATE}',
                    'theme':'{THEME}',
                    'west':'{WEST}',
                    'east':'{EAST}',
                    'north':'{NORTH}',
                    'south':'{SOUTH}',
                    'wms-address':'{ONLINEWMSRESOURCE}',
                    'wcs-address':'{ONLINEWCSRESOURCE}',
                    'layername':'{LAYERNAME}',
                    },
                 'parent': {
                    'filename':'/projects/r17/test/scripts/iso19139-parent-template.xml',
                    'parent':'{PARENTUUID}',
                    'title':'{TITLE}',
                    'date':'{DATE}',
                    'abstract':'{ABSTRACT}',
                    'theme':'{THEME}',
                    'west':'{WEST}',
                    'east':'{EAST}',
                    'north':'{NORTH}',
                    'south':'{SOUTH}',
                    }
                }

wmsServer="http://siss2.anu.edu.au/thredds/wms/ga/projects/"
wcsServer="http://siss2.anu.edu.au/thredds/wcs/ga/projects/"

'''build a list of files we care about....'''
'''fileset = findFiles('/projects/r17/GA', '*pctkg.isi') + \
          findFiles('/projects/r17/GA', '*demg.isi') + \
          findFiles('/projects/r17/GA', '*doseg.isi') + \
          findFiles('/projects/r17/GA', '*ppmthg.isi') + \
          findFiles('/projects/r17/GA', '*ppmug.isi') + \
          findFiles('/projects/r17/GA', '*timg.isi')'''
#fileset = glob.glob('/projects/r17/test/data/*.isi')

def isNumber(s):
    try:
        float(s)
        return True
    except ValueError:
        return False

def getMetadataOverride(surveyId, fieldName):
    if surveyId in additionalMetadata:
        mEntry = additionalMetadata[surveyId]
        possibleValues = mEntry[fieldName]
        if len(possibleValues):
            return possibleValues[0]
    
    return None
    
def getMetadataOverrideKeywords(surveyId):
    #Get every keyword
    allKeywords = []
    if surveyId in additionalMetadata:
        mEntry = additionalMetadata[surveyId]
        kw1Values = mEntry['keyword1']
        kw2Values = mEntry['keyword2']
        
        if kw1Values is not None and len(kw1Values) > 0:
            allKeywords = allKeywords + kw1Values
        
        if kw2Values is not None and len(kw2Values) > 0:
            allKeywords = allKeywords + kw2Values        
    
    #Remove duplicates before returning
    return list(set(allKeywords))

def createParentMetadataRecord(mydata):
    """This creates a parent metadata record
    
    Title - Surveyid from corresponding isi metadata file.

    """
    parentfile = '/projects/r17/test/outputs/metadata/P' + mydata['surveyid'] + '.xml'
    if not os.path.exists(parentfile):
        fileuuid=uuid.uuid4()
        
        outputTemplate = open(templateFiles['parent']['filename'], 'r').read()
        replaceVars = templateFiles['parent']
        
        #FileIdentifier
        outputTemplate = outputTemplate.replace(replaceVars['parent'], str(fileuuid))
        
        #Title
        title = getMetadataOverride(mydata['surveyid'], 'title')
        if title is None:
            title = mydata['title']
        title = xml.sax.saxutils.escape(title)
        outputTemplate = outputTemplate.replace(replaceVars['title'], title)
        
        #Date
        date = getMetadataOverride(mydata['surveyid'], 'date')
        if date is None and mydata.has_key('date'):
            date = mydata['date'].split('_')[0]
        elif date is None:
            date = 'unknown'
        title = xml.sax.saxutils.escape(date)
        outputTemplate = outputTemplate.replace(replaceVars['date'], date)
        
        #Abstract
        abstract = getMetadataOverride(mydata['surveyid'], 'abstract')
        if abstract is None:
            abstract = str(mydata)
        title = xml.sax.saxutils.escape(abstract)
        outputTemplate = outputTemplate.replace(replaceVars['abstract'], abstract)
        
        #Keywords
        keywordList = getMetadataOverrideKeywords(mydata['surveyid'])
        if not keywordList:
            keywordList = [mydata['theme']] # If we have no overrides, use the read theme
        if len(keywordList) < 5:
            keywordList += ['']*(5-len(keywordList)) # If we have less than 5 entries, pad it out to empty strings
        for x in range(0,5):
            key = '{KEYWORD%s}' % (x + 1)
            value = xml.sax.saxutils.escape(keywordList[x])
            outputTemplate = outputTemplate.replace(key, value)
        
        #GeographicBoundingBox
        north = mydata['north']
        if not isNumber(north):
            north = '90'
        south = mydata['south']
        if not isNumber(south):
            south = '-90'
        east = mydata['east']
        if not isNumber(east):
            east = '180'
        west = mydata['west']
        if not isNumber(west):
            west = '-180'
        outputTemplate = outputTemplate.replace(replaceVars['north'], xml.sax.saxutils.escape(north))
        outputTemplate = outputTemplate.replace(replaceVars['west'], xml.sax.saxutils.escape(west))
        outputTemplate = outputTemplate.replace(replaceVars['south'], xml.sax.saxutils.escape(south))
        outputTemplate = outputTemplate.replace(replaceVars['east'], xml.sax.saxutils.escape(east))
        
        f = open(parentfile, 'w')
        f.write(outputTemplate)
        f.close()
        
        return fileuuid
    else:
        print parentfile, 'already exists'
        return -1

def createChildMetadataRecord(mydata):
    """This creates a child metadata record"""
    fileuuid=uuid.uuid4()
    
    outputTemplate = open(templateFiles['child']['filename'], 'r').read()
    replaceVars = templateFiles['child']
    
    #FileIdentifier
    outputTemplate = outputTemplate.replace(replaceVars['uuid'], str(fileuuid))
    
    #ParentIdentifier
    outputTemplate = outputTemplate.replace(replaceVars['parent'], str(mydata['parentuuid']))   
    
    #Title
    if mydata.has_key('label'):
        outputTemplate = outputTemplate.replace(replaceVars['title'], mydata['label'].replace('_', ' '))
    else:
        outputTemplate = outputTemplate.replace(replaceVars['title'], mydata['filename'])
        
    #Abstract
    outputTemplate = outputTemplate.replace(replaceVars['abstract'], str(mydata))
    
    #Date
    if mydata.has_key('date'):
        outputTemplate = outputTemplate.replace(replaceVars['date'], mydata['date'].split('_')[0])
    else:
        outputTemplate = outputTemplate.replace(replaceVars['date'], 'unknown')
    
    #Keyword
    if mydata.has_key('theme'):
        outputTemplate = outputTemplate.replace(replaceVars['theme'], mydata['theme'])
    else:
        outputTemplate = outputTemplate.replace(replaceVars['theme'], 'unknown')
        
    outputTemplate = outputTemplate.replace(replaceVars['wcs-address'], wcsServer+mydata['filename']+'.nc4')
    outputTemplate = outputTemplate.replace(replaceVars['wms-address'], wmsServer+mydata['filename']+'.nc4')
    outputTemplate = outputTemplate.replace(replaceVars['layername'], mydata['filename'])
    
    #GeographicBoundingBox
    outputTemplate = outputTemplate.replace(replaceVars['north'], mydata['north'])
    outputTemplate = outputTemplate.replace(replaceVars['west'], mydata['west'])
    outputTemplate = outputTemplate.replace(replaceVars['south'], mydata['south'])
    outputTemplate = outputTemplate.replace(replaceVars['east'], mydata['east'])
    
    f = open('/projects/r17/test/outputs/metadata/' + mydata['filename'] + '.xml', 'w')
    f.write(outputTemplate)
    f.close()
    
    return

curSurveyId=""
parentUUID=""

def _glob(path, *exts):
    """Glob for multiple file extensions

    Parameters
    ----------
    path : str
        A file name without extension, or directory name
    exts : tuple
        File extensions to glob for

    Returns
    -------
    files : list
        list of files matching extensions in exts in path

    """
    path = os.path.join(path, "*") 
    if not os.path.isdir(path):
        path + "*"
    return [f for files in [glob.glob(path + ext) for ext in exts] for f in files]


# Parse the supporting metadata dictionaries. These provide 'override' information for various metadata fields keyed by survey id.
print 'Parsing overriding-metadata.csv'
csvfile = open('/projects/r17/test/scripts/overriding-metadata.csv', 'rb')
csvReader = csv.DictReader(csvfile)
for row in csvReader:
    
    if row['SURVEYID'] in additionalMetadata:
        entry = additionalMetadata[row['SURVEYID']]
    else:
        entry = {}
        entry['title'] = []
        entry['abstract'] = []
        entry['bbox'] = []
        entry['keyword1'] = []
        entry['keyword2'] = []
        entry['date'] = []
        additionalMetadata[row['SURVEYID']] = entry
        
    if row['TITLE']:
        entry['title'].append(row['TITLE'])
    if row['ABSTRACT']:
        entry['abstract'].append(row['ABSTRACT'])    
    if row['MINLON']:
        bbox = {}
        bbox['eastBoundLongitude'] = row['MAXLON']
        bbox['westBoundLongitude'] = row['MINLON']
        bbox['northBoundLatitude'] = row['MAXLAT']
        bbox['southBoundLatitude'] = row['MINLAT']
        entry['bbox'].append(bbox)
    if row['KEYWORD1']:
        entry['keyword1'].append(row['KEYWORD1'])    
    if row['KEYWORD2']:
        entry['keyword2'].append(row['KEYWORD2'])
    if row['DATE']:
        entry['date'].append(row['DATE'])
csvfile.close()
    
    
# Parse the data files
print 'Parsing data files'
fileset = _glob('/projects/r17/test/data/', '*demg.isi', '*tmig.isi', '*doseg.isi', '*pctkg.isi', '*ppmthg.isi', '*ppmug.isi')

for currentfile in sorted(fileset):
    try:
        filename, extension = os.path.splitext(os.path.basename(currentfile))
        if '.ers.' not in filename and os.path.exists('/projects/r17/shared/netCDF/'+filename+'.nc4'):
            '''
                Harvest currentfile's metadata into a "mydata" hashtable
            '''
            mydata={}
            f=open(currentfile, 'r')
            for line in f.readlines():
                if '=' in line:
                    mydata[line.split('=')[0].strip().lower()] = line.split('=')[1].strip().replace('"','')
            f.close()

            if "surveyid" in mydata:
                if mydata.get("surveyid") == "" or mydata.get("surveyid").lower() == "unknown":
                    print currentfile, 'skipped as surveyid is unknown or empty'
                    errlogger.error(currentfile + ' - skipped as surveyid is unknown or empty')
	        else:
                    print currentfile
                    '''
                    Harvest geographic bounding box coordinates from currentfile's 
                    corresponding netCDF file and store them in "mydata" hastable
                    ''' 
                    data = netCDF4.Dataset('/projects/r17/shared/netCDF/'+filename+'.nc4','r', format='NETCDF4')
                    mydata['north'] = str(data.variables['lat'][-1:][0])
                    mydata['west'] = str(data.variables['lon'][0])
                    mydata['south'] = str(data.variables['lat'][0])
                    mydata['east'] = str(data.variables['lon'][-1:][0])
                    data.close()
            
                    '''
                    Create parent metadata record for currentfile, 
                    skip if it is already created
                    '''
                    if curSurveyId == "":
                        curSurveyId = mydata['surveyid']
                        parentUUID = createParentMetadataRecord(mydata)
                    elif curSurveyId != mydata['surveyid']:
                        curSurveyId = mydata['surveyid']
                        parentUUID = createParentMetadataRecord(mydata)
            
                    '''
                    Create child metadata record for currentfile
		    '''
                    if parentUUID == -1:
                        print currentfile, 'skipped as surveyid already exists'
                        errlogger.error(currentfile + ' - skipped as surveyid already exists')
		    else:
                        mydata['parentuuid'] = parentUUID
                        mydata['filename'] = filename
                        createChildMetadataRecord(mydata)
            else:
                    print currentfile, 'skipped as surveyid does not exist'
                    errlogger.error(currentfile + ' - skiped as surveyid does not exist')
                    
    except Exception, e:
        print e
        pass
