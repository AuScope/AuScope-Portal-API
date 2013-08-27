#! /usr/bin/python2.7
import csv, sys, os, subprocess, glob, time, datetime
import xml.etree.ElementTree as ET

class Vgl(file):

    def __init__(self, file):
        self.header =  ['line',
        'flight',
        'fid',
        'project_fas',
        'project_ga',
        'aircraft', 
        'date', 
        'time', 
        'bearing', 
        'latitude', 
        'longitude', 
        'easting', 
        'northing',
        'lidar',
        'radalt',
        'tx_elevation', # tx_gps_height
        'dtm', # elevation
        'mag',
        'tx_pitch',
        'tx_roll',
        'tx_height',
        'hsep_raw', # txrx_dx_pf 
        'vsep_raw', # txrx_dz_pf
        'txrx_dx_gps', # Unmapped
        'txrx_dy_gps', # Unmapped
        'txrx_dz_gps', # Unmapped
        'tx_height_std',
        'hsep_std',
        'vsep_std',
        'emx_nonhprg1','emx_nonhprg2','emx_nonhprg3','emx_nonhprg4','emx_nonhprg5','emx_nonhprg6','emx_nonhprg7','emx_nonhprg8','emx_nonhprg9','emx_nonhprg10','emx_nonhprg11','emx_nonhprg12','emx_nonhprg13','emx_nonhprg14','emx_nonhprg15',
        'emx_hprg1','emx_hprg2','emx_hprg3','emx_hprg4','emx_hprg5','emx_hprg6','emx_hprg7','emx_hprg8','emx_hprg9','emx_hprg10','emx_hprg11','emx_hprg12','emx_hprg13','emx_hprg14','emx_hprg15',
        'x_sferics',
        'x_lowfreq',
        'x_powerline',
        'x_vlf1','x_vlf2','x_vlf3','x_vlf4',
        'x_geofact',
        'emz_nonhprg1','emz_nonhprg2','emz_nonhprg3','emz_nonhprg4','emz_nonhprg5','emz_nonhprg6','emz_nonhprg7','emz_nonhprg8','emz_nonhprg9','emz_nonhprg10','emz_nonhprg11','emz_nonhprg12','emz_nonhprg13','emz_nonhprg14','emz_nonhprg15',
        'emz_hprg1','emz_hprg2','emz_hprg3','emz_hprg4','emz_hprg5','emz_hprg6','emz_hprg7','emz_hprg8','emz_hprg9','emz_hprg10','emz_hprg11','emz_hprg12','emz_hprg13','emz_hprg14','emz_hprg15',
        'z_sferics',
        'z_lowfreq',
        'z_powerline',
        'z_vlf1','z_vlf2','z_vlf3','z_vlf4',
        'z_geofact'];

        self.run(file);
 
    def run(self,file):
        dics = self.getVglXMLDict(file);
        self.writeToCSV(dics,"aemInput.dat");

    def replace_all(self,text, dic):
        for i, j in dic.iteritems():
            text = text.replace(i, j);
        return text;


    def writeToCSV(self,dictionaryData,filename):
        with open(filename,'w') as f:
            writer = csv.DictWriter(f,fieldnames=self.header, delimiter=' ');
            for d in dictionaryData:
                writer.writerow(d);


    def getVglXMLDict(self,filename):
        tree = ET.parse(filename);
        root = tree.getroot();
        csvArray=[];
        for featureMembers in root:
            for aemsurveys in featureMembers:
                dict={};
                dict['line'] = aemsurveys.find('{http://ga.gov.au}line').text;
                dict['flight'] = aemsurveys.find('{http://ga.gov.au}flight').text;
                dict['fid'] = aemsurveys.find('{http://ga.gov.au}fid').text;
                dict['project_fas'] = aemsurveys.find('{http://ga.gov.au}project_fas').text;
                dict['project_ga'] = aemsurveys.find('{http://ga.gov.au}project_ga').text;
                dict['aircraft'] = aemsurveys.find('{http://ga.gov.au}aircraft').text;
                
                # Parse timestamp into seperate date/time fields
                parsedDateTime = datetime.datetime.strptime(aemsurveys.find('{http://ga.gov.au}timestamp').text, "%Y-%m-%dT%H:%M:%SZ")
                timeSinceMidnight = (parsedDateTime - parsedDateTime.replace(hour=0, minute=0, second=0, microsecond=0))
                dict['date'] = parsedDateTime.strftime("%Y%m%d");
                dict['time'] = (timeSinceMidnight.microseconds + (timeSinceMidnight.seconds) * 10**6) / 10**6
                
                dict['bearing'] = aemsurveys.find('{http://ga.gov.au}bearing').text;
                dict['latitude'] = aemsurveys.find('{http://ga.gov.au}latitude').text;
                dict['longitude'] = aemsurveys.find('{http://ga.gov.au}longitude').text;
                dict['easting'] = aemsurveys.find('{http://ga.gov.au}easting').text;
                dict['northing'] = aemsurveys.find('{http://ga.gov.au}northing').text;
                dict['lidar'] = aemsurveys.find('{http://ga.gov.au}lidar').text;
                dict['radalt'] = aemsurveys.find('{http://ga.gov.au}radalt').text;
                dict['tx_elevation'] = aemsurveys.find('{http://ga.gov.au}tx_elevation').text;
                dict['dtm'] = aemsurveys.find('{http://ga.gov.au}dtm').text;
                dict['mag'] = aemsurveys.find('{http://ga.gov.au}mag').text;
                dict['tx_pitch'] = aemsurveys.find('{http://ga.gov.au}tx_pitch').text;
                dict['tx_roll'] = aemsurveys.find('{http://ga.gov.au}tx_roll').text;
                dict['tx_height'] = aemsurveys.find('{http://ga.gov.au}tx_height').text;
                dict['hsep_raw'] = aemsurveys.find('{http://ga.gov.au}hsep_raw').text;
                dict['vsep_raw'] = aemsurveys.find('{http://ga.gov.au}vsep_raw').text;
                dict['tx_height_std'] = aemsurveys.find('{http://ga.gov.au}tx_height_std').text;
                dict['hsep_std'] = aemsurveys.find('{http://ga.gov.au}hsep_std').text;
                dict['vsep_std'] = aemsurveys.find('{http://ga.gov.au}vsep_std').text;
                dict['txrx_dx_gps'] = 0 # Not yet mapped
                dict['txrx_dy_gps'] = 0 # Not yet mapped
                dict['txrx_dz_gps'] = 0 # Not yet mapped
                dict['emx_nonhprg1'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg1').text;
                dict['emx_nonhprg2'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg2').text;
                dict['emx_nonhprg3'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg3').text;
                dict['emx_nonhprg4'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg4').text;
                dict['emx_nonhprg5'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg5').text;
                dict['emx_nonhprg6'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg6').text;
                dict['emx_nonhprg7'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg7').text;
                dict['emx_nonhprg8'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg8').text;
                dict['emx_nonhprg9'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg9').text;
                dict['emx_nonhprg10'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg10').text;
                dict['emx_nonhprg11'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg11').text;
                dict['emx_nonhprg12'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg12').text;
                dict['emx_nonhprg13'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg13').text;
                dict['emx_nonhprg14'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg14').text;
                dict['emx_nonhprg15'] = aemsurveys.find('{http://ga.gov.au}emx_nonhprg15').text;
                dict['emx_hprg1'] = aemsurveys.find('{http://ga.gov.au}emx_hprg1').text;
                dict['emx_hprg2'] = aemsurveys.find('{http://ga.gov.au}emx_hprg2').text;
                dict['emx_hprg3'] = aemsurveys.find('{http://ga.gov.au}emx_hprg3').text;
                dict['emx_hprg4'] = aemsurveys.find('{http://ga.gov.au}emx_hprg4').text;
                dict['emx_hprg5'] = aemsurveys.find('{http://ga.gov.au}emx_hprg5').text;
                dict['emx_hprg6'] = aemsurveys.find('{http://ga.gov.au}emx_hprg6').text;
                dict['emx_hprg7'] = aemsurveys.find('{http://ga.gov.au}emx_hprg7').text;
                dict['emx_hprg8'] = aemsurveys.find('{http://ga.gov.au}emx_hprg8').text;
                dict['emx_hprg9'] = aemsurveys.find('{http://ga.gov.au}emx_hprg9').text;
                dict['emx_hprg10'] = aemsurveys.find('{http://ga.gov.au}emx_hprg10').text;
                dict['emx_hprg11'] = aemsurveys.find('{http://ga.gov.au}emx_hprg11').text;
                dict['emx_hprg12'] = aemsurveys.find('{http://ga.gov.au}emx_hprg12').text;
                dict['emx_hprg13'] = aemsurveys.find('{http://ga.gov.au}emx_hprg13').text;
                dict['emx_hprg14'] = aemsurveys.find('{http://ga.gov.au}emx_hprg14').text;
                dict['emx_hprg15'] = aemsurveys.find('{http://ga.gov.au}emx_hprg15').text;
                dict['x_sferics'] = aemsurveys.find('{http://ga.gov.au}x_sferics').text;
                dict['x_lowfreq'] = aemsurveys.find('{http://ga.gov.au}x_lowfreq').text;
                dict['x_powerline'] = aemsurveys.find('{http://ga.gov.au}x_powerline').text;
                dict['x_vlf1'] = aemsurveys.find('{http://ga.gov.au}x_vlf1').text;
                dict['x_vlf2'] = aemsurveys.find('{http://ga.gov.au}x_vlf2').text;
                dict['x_vlf3'] = aemsurveys.find('{http://ga.gov.au}x_vlf3').text;
                dict['x_vlf4'] = aemsurveys.find('{http://ga.gov.au}x_vlf4').text;
                dict['x_geofact'] = aemsurveys.find('{http://ga.gov.au}x_geofact').text;
                dict['emz_nonhprg1'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg1').text;
                dict['emz_nonhprg2'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg2').text;
                dict['emz_nonhprg3'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg3').text;
                dict['emz_nonhprg4'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg4').text;
                dict['emz_nonhprg5'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg5').text;
                dict['emz_nonhprg6'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg6').text;
                dict['emz_nonhprg7'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg7').text;
                dict['emz_nonhprg8'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg8').text;
                dict['emz_nonhprg9'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg9').text;
                dict['emz_nonhprg10'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg10').text;
                dict['emz_nonhprg11'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg11').text;
                dict['emz_nonhprg12'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg12').text;
                dict['emz_nonhprg13'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg13').text;
                dict['emz_nonhprg14'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg14').text;
                dict['emz_nonhprg15'] = aemsurveys.find('{http://ga.gov.au}emz_nonhprg15').text;
                dict['emz_hprg1'] = aemsurveys.find('{http://ga.gov.au}emz_hprg1').text;
                dict['emz_hprg2'] = aemsurveys.find('{http://ga.gov.au}emz_hprg2').text;
                dict['emz_hprg3'] = aemsurveys.find('{http://ga.gov.au}emz_hprg3').text;
                dict['emz_hprg4'] = aemsurveys.find('{http://ga.gov.au}emz_hprg4').text;
                dict['emz_hprg5'] = aemsurveys.find('{http://ga.gov.au}emz_hprg5').text;
                dict['emz_hprg6'] = aemsurveys.find('{http://ga.gov.au}emz_hprg6').text;
                dict['emz_hprg7'] = aemsurveys.find('{http://ga.gov.au}emz_hprg7').text;
                dict['emz_hprg8'] = aemsurveys.find('{http://ga.gov.au}emz_hprg8').text;
                dict['emz_hprg9'] = aemsurveys.find('{http://ga.gov.au}emz_hprg9').text;
                dict['emz_hprg10'] = aemsurveys.find('{http://ga.gov.au}emz_hprg10').text;
                dict['emz_hprg11'] = aemsurveys.find('{http://ga.gov.au}emz_hprg11').text;
                dict['emz_hprg12'] = aemsurveys.find('{http://ga.gov.au}emz_hprg12').text;
                dict['emz_hprg13'] = aemsurveys.find('{http://ga.gov.au}emz_hprg13').text;
                dict['emz_hprg14'] = aemsurveys.find('{http://ga.gov.au}emz_hprg14').text;
                dict['emz_hprg15'] = aemsurveys.find('{http://ga.gov.au}emz_hprg15').text;
                dict['z_sferics'] = aemsurveys.find('{http://ga.gov.au}z_sferics').text;
                dict['z_lowfreq'] = aemsurveys.find('{http://ga.gov.au}z_lowfreq').text;
                dict['z_powerline'] = aemsurveys.find('{http://ga.gov.au}z_powerline').text;
                dict['z_vlf1'] = aemsurveys.find('{http://ga.gov.au}z_vlf1').text;
                dict['z_vlf2'] = aemsurveys.find('{http://ga.gov.au}z_vlf2').text;
                dict['z_vlf3'] = aemsurveys.find('{http://ga.gov.au}z_vlf3').text;
                dict['z_vlf4'] = aemsurveys.find('{http://ga.gov.au}z_vlf4').text;
                dict['z_geofact'] = aemsurveys.find('{http://ga.gov.au}z_geofact').text;                   
                csvArray.append(dict);
        return csvArray;
    
controlFileString = """
Control Begin

        LogFile  = inversion.output.log
        NumberOfSystems = 1

        EMSystem1 Begin
                SystemFile = tempest.stm
                UseXComponent   = yes
                UseYComponent   = no
                UseZComponent   = yes

                InvertTotalField = yes
                ReconstructPrimaryFieldFromInputGeometry = yes

                EstimateNoiseFromModel = yes
                XMultiplicativeNoise   = 2.26
                XAdditiveNoise  = 0.0119 0.0117 0.0093 0.0061 0.0057 0.0054 0.0051 0.0048 0.0046 0.0044 0.0043 0.0040 0.0034 0.0026 0.0034
                ZMultiplicativeNoise   = 3.74
                ZAdditiveNoise  = 0.0094 0.0084 0.0067 0.0047 0.0045 0.0043 0.0041 0.0039 0.0036 0.0034 0.0033 0.0030 0.0024 0.0017 0.0019

                XComponentPrimary = UNAVAILABLE
                YComponentPrimary = UNAVAILABLE
                ZComponentPrimary = UNAVAILABLE

                XComponentSecondary = Column 27
                YComponentSecondary = UNAVAILABLE
                ZComponentSecondary = -Column 65

                StdDevXComponentWindows = UNAVAILABLE
                StdDevYComponentWindows = UNAVAILABLE
                StdDevZComponentWindows = UNAVAILABLE
        EMSystem1 End

        Earth Begin
                NumberOfLayers = 30
        Earth End

        Options Begin
                SolveConductivity = yes
                SolveThickness    = no

                SolveTX_Height = no
                SolveTX_Roll = no
                SolveTX_Pitch = no
                SolveTX_Yaw = no
                SolveTXRX_DX = yes
                SolveTXRX_DY = no
                SolveTXRX_DZ = yes
                SolveRX_Roll = no
                SolveRX_Pitch = yes
                SolveRX_Yaw = no

                AlphaConductivity = 1.0
                AlphaThickness    = 0.0
                AlphaGeometry     = 1.0
                AlphaSmoothness   = 1000000 //Set to 0 for no vertical conductivity smoothing

                MinimumPhiD = 1.0
                MinimumPercentageImprovement = 1.0
                MaximumIterations = 100
        Options End

        InputOutput Begin
                InputFile   = aemInput.dat
                HeaderLines = 0
                Subsample   = 1

                OutputDataFile   = inversion.output.asc
                OutputHeaderFile = inversion.output.hdr

                Columns Begin
                        SurveyNumber    = Column 5
                        DateNumber      = Column 7
                        FlightNumber    = Column 2
                        LineNumber      = Column 1
                        FidNumber       = Column 3
                        Easting         = Column 12
                        Northing        = Column 13
                        GroundElevation = Column 17
                        Altimeter       = Column 14

                        TX_Height       = Column 21
                        TX_Roll         = Column 20
                        TX_Pitch        = -Column 19
                        TX_Yaw          = 0
                        TXRX_DX         = Column 22
                        TXRX_DY         = 0
                        TXRX_DZ         = Column 23
                        RX_Roll         = 0
                        RX_Pitch        = 0
                        RX_Yaw          = 0

                        TotalFieldReconstruction Begin
                                TXRX_DX = Column 22
                                TXRX_DY = 0
                                TXRX_DZ = Column 23
                        TotalFieldReconstruction End

                        ReferenceModel Begin
                                TXRX_DX      = Column 22
                                TXRX_DY      = 0
                                TXRX_DZ      = Column 23
                                RX_Pitch     = 0
                                Conductivity   = 0.01
                                Thickness      = 4.00 4.40 4.84 5.32 5.86 6.44 7.09 7.79 8.57 9.43 10.37 11.41 12.55 13.81 15.19 16.71 18.38 20.22 22.24 24.46 26.91 29.60 32.56 35.82 39.40 43.34 47.67 52.44 57.68
                        ReferenceModel End

                        StdDevReferenceModel Begin
                                TXRX_DX      = 1.0
                                TXRX_DZ      = 1.0
                                RX_Pitch     = 1.0
                                Conductivity = 3.0
                        StdDevReferenceModel End
                Columns End
        InputOutput End
Control End
"""

stmFileString = """
System Begin
        Name = Tempest
        Type = Time Domain

        Transmitter Begin
                NumberOfTurns = 1
                PeakCurrent   = 0.5
                LoopArea      = 1
                BaseFrequency = 25
                WaveFormCurrent Begin
                        -0.0200000000000    0.0
                        -0.0199933333333    1.0
                        -0.0000066666667    1.0
                         0.0000000000000    0.0
                         0.0000066666667   -1.0
                         0.0199933333333   -1.0
                         0.0200000000000    0.0
                WaveFormCurrent End
                WaveformDigitisingFrequency = 600000
        Transmitter End

        Receiver Begin

                NumberOfWindows = 15
                WindowWeightingScheme = Boxcar

                WindowTimes Begin
                        0.0000066667    0.0000200000
                        0.0000333333    0.0000466667
                        0.0000600000    0.0000733333
                        0.0000866667    0.0001266667
                        0.0001400000    0.0002066667
                        0.0002200000    0.0003400000
                        0.0003533333    0.0005533333
                        0.0005666667    0.0008733333
                        0.0008866667    0.0013533333
                        0.0013666667    0.0021000000
                        0.0021133333    0.0032733333
                        0.0032866667    0.0051133333
                        0.0051266667    0.0079933333
                        0.0080066667    0.0123933333
                        0.0124066667    0.0199933333
                WindowTimes End

        Receiver End

        ForwardModelling Begin

                OutputType = B

                XOutputScaling = 1e15
                YOutputScaling = 1e15
                ZOutputScaling = 1e15
                SecondaryFieldNormalisation  =  none

                FrequenciesPerDecade = 5
                NumberOfAbsiccaInHankelTransformEvaluation = 21

        ForwardModelling End

System End
"""

def cloudUpload(inFilePath, cloudKey):
    cloudBucket = os.environ["STORAGE_BUCKET"]
    cloudDir = os.environ["STORAGE_BASE_KEY_PATH"]
    queryPath = (cloudBucket + "/" + cloudDir + "/" + cloudKey).replace("//", "/")
    retcode = subprocess.call(["cloud", "upload", cloudKey, inFilePath, "--set-acl=public-read"])
    print ("cloudUpload: " + inFilePath + " to " + queryPath + " returned " + str(retcode))

# downloads the specified key from bucket and writes it to outfile
def cloudDownload(cloudKey, outFilePath):
    cloudBucket = os.environ["STORAGE_BUCKET"]
    cloudDir = os.environ["STORAGE_BASE_KEY_PATH"]
    queryPath = (cloudBucket + "/" + cloudDir + "/" + cloudKey).replace("//", "/")
    retcode = subprocess.call(["cloud", "download",cloudBucket,cloudDir,cloudKey, outFilePath])
    print "cloudDownload: " + queryPath + " to " + outFilePath + " returned " + str(retcode)


# Write our control files
with open("galeisbs.con", "w") as f:
    f.write(controlFileString)
with open("tempest.stm", "w") as f:
    f.write(stmFileString)
cloudUpload("galeisbs.con", "galeisbs.con")
cloudUpload("tempest.stm", "tempest.stm")


Vgl("${wfs-input-xml}");
cloudUpload("aemInput.dat", "aemInput.dat")

subprocess.call(["mpirun", "-n", "${n-threads}", "/root/code/bin/galeisbs.exe", "galeisbs.con"])

inversionFiles = glob.glob('inversion.output.*')
print 'About to upload the following files:'
print inversionFiles
for fn in inversionFiles:
    cloudUpload(fn, fn)
