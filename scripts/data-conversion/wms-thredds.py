# -*- coding: utf-8 -*-
import glob, os, fnmatch, uuid, netCDF4, logging, csv, xml.sax.saxutils, re, traceback, numpy

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


xmlVariableFormat = """
<variable id="%s">
    <defaultColorScaleRange>%s %s</defaultColorScaleRange>
    <defaultNumColorBands>254</defaultNumColorBands>
</variable>
"""

# Parse the data files
print 'Parsing data files'
fileset = _glob('/projects/r17/GA/', '*demg.isi', '*tmig.isi', '*doseg.isi', '*pctkg.isi', '*ppmthg.isi', '*ppmug.isi')
with open('/projects/r17/shared/wmsthredds/variables.xml', 'w') as xmlOut:
    xmlOut.write("<variables>\n")
    for currentfile in sorted(fileset):
        try:
            currentFileBasePath = os.path.basename(currentfile)
            filename, extension = os.path.splitext(currentFileBasePath)
            if '.ers.' not in filename and os.path.exists('/projects/r17/shared/netCDF/'+filename+'.nc4'):
                data = netCDF4.Dataset('/projects/r17/shared/netCDF/'+filename+'.nc4','r', format='NETCDF4')
                variableData = numpy.ma.masked_invalid(data.variables[filename][:], copy=False)
                upperBound = variableData.max()
                lowerBound = variableData.min()
                data.close()
            
                xmlOut.write(xmlVariableFormat % (filename, lowerBound, upperBound))
        except Exception, e:
            print 'Exception:', e
            print traceback.format_exc()
            pass
    xmlOut.write("</variables>\n")

