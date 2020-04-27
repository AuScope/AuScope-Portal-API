#! /usr/bin/python2.7
import csv, sys, os, subprocess, glob, time, datetime
import xml.etree.ElementTree as ET

controlFileString = """
Control Begin

    NumberOfSystems = 1

    EMSystem1 Begin
        SystemFile = tempestStandard.stm
        UseXComponent   = yes
        UseYComponent   = no
        UseZComponent   = yes

        InvertTotalField = no
        ReconstructPrimaryFieldFromInputGeometry = no

        EstimateNoiseFromModel = yes

        XMultiplicativeNoise   = 2.26
        XAdditiveNoise         = 0.0119 0.0117 0.0093 0.0061 0.0057 0.0054 0.0051 0.0048 0.0046 0.0044 0.0043 0.0040 0.0034 0.0026 0.0034
        XComponentSecondary    = Column 30
        
        ZMultiplicativeNoise   = 3.74
        ZAdditiveNoise         = 0.0094 0.0084 0.0067 0.0047 0.0045 0.0043 0.0041 0.0039 0.0036 0.0034 0.0033 0.0030 0.0024 0.0017 0.0019
        ZComponentSecondary    = -Column 68
    EMSystem1 End

    Earth Begin
        NumberOfLayers = 30
    Earth End

    Options Begin
        SolveConductivity = yes
        SolveThickness    = no

        SolveTX_Height = no
        SolveTX_Roll   = no
        SolveTX_Pitch  = no
        SolveTX_Yaw    = no
        SolveTXRX_DX   = no
        SolveTXRX_DY   = no
        SolveTXRX_DZ   = no
        SolveRX_Roll   = no
        SolveRX_Pitch  = no
        SolveRX_Yaw    = no

        AlphaConductivity = 1.0
        AlphaThickness    = 0.0
        AlphaGeometry     = 1.0
        AlphaSmoothness   = 100000 //Set to 0 for no vertical conductivity smoothing
        SmoothnessMethod  = Minimise2ndDerivatives

        MinimumPhiD = 1.0
        MinimumPercentageImprovement = 1.0
        MaximumIterations = 100
    Options End

    Input Begin
        DataFile   = aemInput.dat
        HeaderLines = 0
        Subsample   = 1

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

            ReferenceModel Begin
                Conductivity = 0.001
                Thickness    = 4.00 4.40 4.84 5.32 5.86 6.44 7.09 7.79 8.57 9.43 10.37 11.41 12.55 13.81 15.19 16.71 18.38 20.22 22.24 24.46 26.91 29.60 32.56 35.82 39.40 43.34 47.67 52.44 57.68
            ReferenceModel End

            StdDevReferenceModel Begin
                Conductivity = 3.0
            StdDevReferenceModel End
        Columns End
    Input End

    Output Begin
        DataFile = inversion.output.asc
        LogFile  = inversion.output.log

        PositiveLayerBottomDepths = no
        NegativeLayerBottomDepths = yes
        InterfaceElevations       = no
        ParameterSensitivity      = no
        ParameterUncertainty      = no
        PredictedData             = no
    Output End

Control End
"""

tempestStandardFileString = """
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
        WaveformDigitisingFrequency = 75000
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

        FrequenciesPerDecade = 6
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
with open("tempestStandard.stm", "w") as f:
    f.write(tempestStandardFileString)   
cloudUpload("galeisbs.con", "galeisbs.con")
cloudUpload("tempestStandard.stm", "tempestStandard.stm")

# Read the WFS Input data into a CSV format
tree = ET.parse("${wfs-input-xml}");
root = tree.getroot();
csvArray=[];
for featureMembers in root:
    for aemsurveys in featureMembers:
        row = []
        for field in aemsurveys:
            #Non simple properties are ignored
            if len(field) > 0:
                continue
            row.append(field.text)
        csvArray.append(row)    
with open("aemInput.dat",'w') as f:
    writer = csv.writer(f, delimiter=' ', lineterminator='\n')
    for row in csvArray:
        writer.writerow(row)        
cloudUpload("aemInput.dat", "aemInput.dat")

# Execute AEM Process via MPI
subprocess.call(["mpirun", "-n", "${n-threads}", "/usr/bin/gaaem/galeisbstdem.exe", "galeisbs.con"])

# Upload results
inversionFiles = glob.glob('inversion.output.*')
print 'About to upload the following files:'
print inversionFiles
for fn in inversionFiles:
    cloudUpload(fn, fn)
    
# Concatenate output files for easier parsing
ascFiles = sorted(glob.glob('inversion.output.*.asc'))
with open('inversion.output.asc.combined', 'w') as outfile:
    for fname in ascFiles:
        with open(fname) as infile:
            for line in infile:
                outfile.write(line)
cloudUpload('inversion.output.asc.combined', 'inversion.output.asc.combined')
logFiles = sorted(glob.glob('inversion.output.*.log'))
with open('inversion.output.log.combined', 'w') as outfile:
    for fname in logFiles:
        with open(fname) as infile:
            for line in infile:
                outfile.write(line)
cloudUpload('inversion.output.log.combined', 'inversion.output.log.combined')
