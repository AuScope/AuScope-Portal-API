# Python classes for interpreting the startup parameters for a 
# Virtual Exploration Geophysics Laboratory Portal cloud VM.
#
# Compiled with Python 3.1
#

# Represents a simple spatial bounding box in a particular spatial reference system
class VEGLBBox:
    _srs = None
    _maxNorth = None
    _minNorth = None
    _maxEast = None
    _minEast = None
    
    def __init__(self, srs, maxNorth, minNorth, maxEast, minEast):
        self._srs = srs
        self._maxNorth = float(maxNorth)
        self._minNorth = float(minNorth)
        self._maxEast = float(maxEast)
        self._minEast = float(minEast)
        
    # Gets the spatial reference system of these bounds as a string
    def getSpatialReferenceSystem(self):
        return self._mgaZone
    
    # Gets the maximum northing of these bounds as a float  
    def getMaxNorthing(self):
        return self._maxNorth
    
    # Gets the minimum northing of these bounds as a float
    def getMinNorthing(self):
        return self._minNorth
    
    # Gets the maximum easting of these bounds as a float
    def getMaxEasting(self):
        return self._maxEast
    
    # Gets the minimum easting of these bounds as a float
    def getMinEasting(self):
        return self._minEast
    
    # Returns true if the specified northing/easting (assumed to be in the same SRS)
    # lies within the spatial area represented by this bounding box. 
    def isPointInsideArea(self, northing, easting):
        return ((easting >= self._minEast) and (easting <= self._maxEast) and (northing >= self._minNorth) and (northing <= self._maxNorth))


# The interface for python scripts to access the various input parameters made available to a VM 
# started by the VEGL Portal
class VEGLParameters:
    _paddedBounds = None
    _selectedBounds = None
    _fileName = ''
    _csvName = ''
    _cellX = 10000
    _cellY = 10000
    _cellZ = 2500
    _invDepth = 25000.0
    
    def __init__(self):
        #TODO: Actually load this state from the VM instead of using hardcoded parameters
        self._paddedBounds = VEGLBBox('52', 7210000.0, 7100000.0, 670000.0, 590000.0)
        self._selectedBounds = VEGLBBox('EPSG:4326', -25.75, -26.25, 130.7, 129.9)
        self._file_name = 'onshore_Bouguer_offshore_freeair_gravity_geodetic_June_2009.ers'
        self._csv_name = 'synth_test.csv'
        self._cellX = 10000
        self._cellY = 10000
        self._cellZ = 2500
        self._invDepth = 25000.0

    def getPaddedBounds(self):
        return self._paddedBounds
    
    def getSelectedBounds(self):
        return self_selectedBounds

    # Returns a dictionary in the following format {
    #    x : Integer : The size of the cell in the X direction 
    #    y : Integer : The size of the cell in the Y direction
    #    z : Integer : The size of the cell in the Z direction
    # }
    def getCellSize(self):
        return {x : self._cellX, y : self._cellY, z : self._cellZ}
    
    def getInversionDepth(self):
        return self_invDepth
    
    def getAWSSecretKey(self):
        return ''
    
    def getAWSAccessKey(self):
        return ''
    
    def getS3BaseKeyPath(self):
        return ''
    
    def getS3OutputBucket(self):
        return ''
    
    def getWorkingDirectory(self):
        return ''
    
    def getLogFilePath(self):
        return ''
