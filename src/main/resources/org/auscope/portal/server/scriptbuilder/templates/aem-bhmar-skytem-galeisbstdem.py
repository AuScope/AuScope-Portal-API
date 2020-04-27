#! /usr/bin/python2.7
import csv, sys, os, subprocess, glob, time, datetime
import xml.etree.ElementTree as ET

controlFileString = """
Control Begin

    NumberOfSystems = 2
    
    EMSystem1 Begin
        SystemFile      = SkytemLM.stm
        UseXComponent   = no
        UseYComponent   = no
        UseZComponent   = yes
        InvertTotalField = no
        ReconstructPrimaryFieldFromInputGeometry = no
        EstimateNoiseFromModel = no
        ZComponentSecondary    = -Column 33
        ZComponentNoise        = Column 72
    EMSystem1 End
    
    EMSystem2 Begin
        SystemFile      = SkytemHM.stm
        UseXComponent   = no
        UseYComponent   = no
        UseZComponent   = yes
        InvertTotalField = no
        ReconstructPrimaryFieldFromInputGeometry = no
        EstimateNoiseFromModel = no
        ZComponentSecondary    = -Column 51
        ZComponentNoise        = Column 90
    EMSystem2 End

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
        AlphaSmoothness   = 333 //Set to 0 for no vertical conductivity smoothing
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
            SurveyNumber    = Column 1
            DateNumber      = Column 4
            FlightNumber    = Column 5
            LineNumber      = Column 6
            FidNumber       = Column 7
            Easting         = Column 12
            Northing        = Column 13
            GroundElevation = Column 21
            Altimeter       = Column 19

            TX_Height       = Column 19
            TX_Roll         = Column 24
            TX_Pitch        = Column 25
            TX_Yaw          = Column 26
            TXRX_DX         = Column 27
            TXRX_DY         = Column 28
            TXRX_DZ         = Column 29
            RX_Roll         = Column 24
            RX_Pitch        = Column 25
            RX_Yaw          = Column 26

            ReferenceModel Begin
                Conductivity   = 0.001
                Thickness      = 1.50  1.65  1.81  2.00  2.20  2.42  2.66  2.92  3.21  3.54  3.89  4.28  4.71  5.18  5.70  6.27  6.89  7.58  8.34  9.17 10.09 11.10 12.21 13.43 14.77 16.25 17.88 19.66 21.63
            ReferenceModel End

            StdDevReferenceModel Begin
                Conductivity   = 3.0
            StdDevReferenceModel End
        Columns End
    Input End

    Output Begin
        DataFile = inversion.output.asc
        LogFile  = inversion.output.log

        PositiveLayerBottomDepths = no
        NegativeLayerBottomDepths = yes
        InterfaceElevations       = no
        ParameterSensitivity      = yes
        ParameterUncertainty      = yes
        PredictedData             = no
    Output End

Control End
"""

stmSkytemLMFileString = """
System Begin
    Name = SkyTem-Low-Moment
    Type = Time Domain

    Transmitter Begin
        NumberOfTurns = 1
        PeakCurrent   = 1
        LoopArea      = 1
        BaseFrequency = 222.22222222222222222
        WaveformDigitisingFrequency = 3640888.888888889
        WaveFormCurrent Begin
            -1.000E-03 0.000E+00
            -9.146E-04 6.264E-01
            -7.879E-04 9.132E-01
            -5.964E-04 9.905E-01
            0.000E+00 1.000E+00
            4.629E-07 9.891E-01
            8.751E-07 9.426E-01
            1.354E-06 8.545E-01
            2.540E-06 6.053E-01
            3.972E-06 3.030E-01
            5.404E-06 4.077E-02
            5.721E-06 1.632E-02
            6.113E-06 4.419E-03
            6.663E-06 6.323E-04
            8.068E-06 0.000E+00
            1.250E-03 0.000E+00
        WaveFormCurrent End

    Transmitter End

    Receiver Begin
        NumberOfWindows = 18
        WindowWeightingScheme = AreaUnderCurve

        //Gate04 (0.00001139 0.00001500) was removed as too close to 11.5us front gate
        WindowTimes Begin
            0.00001539 0.00001900
            0.00001939 0.00002400
            0.00002439 0.00003100
            0.00003139 0.00003900
            0.00003939 0.00004900
            0.00004939 0.00006200
            0.00006239 0.00007800
            0.00007839 0.00009900
            0.00009939 0.00012500
            0.00012539 0.00015700
            0.00015739 0.00019900
            0.00019939 0.00025000
            0.00025039 0.00031500
            0.00031539 0.00039700
            0.00039739 0.00050000
            0.00050039 0.00063000
            0.00063039 0.00079300
            0.00079339 0.00099900
        WindowTimes End

        LowPassFilter Begin
            CutOffFrequency = 300000 450000
            Order           = 1       1
        LowPassFilter End

    Receiver End

    ForwardModelling Begin

        OutputType = dB/dt

        SaveDiagnosticFiles = no

        XOutputScaling = 1
        YOutputScaling = 1
        ZOutputScaling = 1
        SecondaryFieldNormalisation  =  none

        FrequenciesPerDecade = 5
        NumberOfAbsiccaInHankelTransformEvaluation = 21

    ForwardModelling End

System End
"""

stmSkytemHMFileString = """
System Begin
    Name = SkyTem-HighMoment
    Type = Time Domain

    Transmitter Begin
        NumberOfTurns = 1
        PeakCurrent   = 1
        LoopArea      = 1
        BaseFrequency = 25
        WaveformDigitisingFrequency = 819200
        WaveFormCurrent Begin
            -1.000E-02 0.000E+00
            -8.386E-03 4.568E-01
            -6.380E-03 7.526E-01
            -3.783E-03 9.204E-01
            0.000E+00 1.000E+00
            3.960E-07 9.984E-01
            7.782E-07 9.914E-01
            1.212E-06 9.799E-01
            3.440E-06 9.175E-01
            1.981E-05 4.587E-01
            3.619E-05 7.675E-03
            3.664E-05 3.072E-03
            3.719E-05 8.319E-04
            3.798E-05 1.190E-04
            3.997E-05 0.000E+00
            1.000E-02 0.000E+00
        WaveFormCurrent End
    Transmitter End

    Receiver Begin
        NumberOfWindows = 21
        WindowWeightingScheme = AreaUnderCurve

        //Gate11 (5.93900E-05   7.50000E-05) was removed as too close to 59us front gate
        WindowTimes Begin
            7.53900E-05 9.60000E-05
            9.63900E-05 1.22000E-04
            1.22390E-04 1.54000E-04
            1.54390E-04 1.96000E-04
            1.96390E-04 2.47000E-04
            2.47390E-04 3.12000E-04
            3.12390E-04 3.94000E-04
            3.94390E-04 4.97000E-04
            4.97390E-04 6.27000E-04
            6.27390E-04 7.90000E-04
            7.90390E-04 9.96000E-04
            9.96390E-04 1.25500E-03
            1.25539E-03 1.58100E-03
            1.58139E-03 1.99100E-03
            1.99139E-03 2.50800E-03
            2.50839E-03 3.15800E-03
            3.15839E-03 3.97700E-03
            3.97739E-03 5.00800E-03
            5.00839E-03 6.30600E-03
            6.30639E-03 7.93900E-03
            7.93939E-03 9.73900E-03
        WindowTimes End

        LowPassFilter Begin
            CutOffFrequency = 300000 450000
            Order           = 1       1
        LowPassFilter End

    Receiver End

    ForwardModelling Begin

        OutputType = dB/dt

        SaveDiagnosticFiles = no

        XOutputScaling = 1
        YOutputScaling = 1
        ZOutputScaling = 1
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
with open("SkytemLM.stm", "w") as f:
    f.write(stmSkytemLMFileString)
with open("SkytemHM.stm", "w") as f:
    f.write(stmSkytemHMFileString)    
cloudUpload("galeisbs.con", "galeisbs.con")
cloudUpload("SkytemLM.stm", "SkytemLM.stm")
cloudUpload("SkytemHM.stm", "SkytemHM.stm")

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
