#! /usr/bin/python2.7
import csv, sys, os, subprocess, glob, time, datetime
import xml.etree.ElementTree as ET

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

                XComponentPrimary = ${column-xcomponentprimary}
                YComponentPrimary = ${column-ycomponentprimary}
                ZComponentPrimary = ${column-zcomponentprimary}

                XComponentSecondary = ${column-xcomponentsecondary}
                YComponentSecondary = ${column-ycomponentsecondary}
                ZComponentSecondary = ${column-zcomponentsecondary}

                StdDevXComponentWindows = ${column-stddevxwindows}
                StdDevYComponentWindows = ${column-stddevywindows}
                StdDevZComponentWindows = ${column-stddevzwindows}
        EMSystem1 End

        Earth Begin
                NumberOfLayers = 30
        Earth End

        Options Begin
                SolveConductivity = ${solve-conductivity}
                SolveThickness    = ${solve-thickness}

                SolveTX_Height = ${solve-txheight}
                SolveTX_Roll = ${solve-txroll}
                SolveTX_Pitch = ${solve-txpitch}
                SolveTX_Yaw = ${solve-txyaw}
                SolveTXRX_DX = ${solve-txrxdx}
                SolveTXRX_DY = ${solve-txrxdy}
                SolveTXRX_DZ = ${solve-txrxdz}
                SolveRX_Roll = ${solve-rxroll}
                SolveRX_Pitch = ${solve-rxpitch}
                SolveRX_Yaw = ${solve-rxyaw}

                AlphaConductivity = ${alpha-conductivity}
                AlphaThickness    = ${alpha-thickness}
                AlphaGeometry     = ${alpha-geometry}
                AlphaSmoothness   = ${alpha-smoothness}

                MinimumPhiD = ${min-phi-d}
                MinimumPercentageImprovement = ${min-percentage-imp}
                MaximumIterations = ${max-iterations}
        Options End

        InputOutput Begin
                InputFile   = aemInput.dat
                HeaderLines = 0
                Subsample   = 1

                OutputDataFile   = inversion.output.asc
                OutputHeaderFile = inversion.output.hdr

                Columns Begin
                        SurveyNumber    = ${column-surveynumber}
                        DateNumber      = ${column-datenumber}
                        FlightNumber    = ${column-flightnumber}
                        LineNumber      = ${column-linenumber}
                        FidNumber       = ${column-fidnumber}
                        Easting         = ${column-easting}
                        Northing        = ${column-northing}
                        GroundElevation = ${column-groundelevation}
                        Altimeter       = ${column-altimeter}

                        TX_Height       = ${column-txheight}
                        TX_Roll         = ${column-txroll}
                        TX_Pitch        = ${column-txpitch}
                        TX_Yaw          = ${column-txyaw}
                        TXRX_DX         = ${column-txrxdx}
                        TXRX_DY         = ${column-txrxdy}
                        TXRX_DZ         = ${column-txrxdz}
                        RX_Roll         = ${column-rxroll}
                        RX_Pitch        = ${column-rxpitch}
                        RX_Yaw          = ${column-rxyaw}

                        TotalFieldReconstruction Begin
                                TXRX_DX = ${column-txrxdx}
                                TXRX_DY = ${column-txrxdy}
                                TXRX_DZ = ${column-txrxdz}
                        TotalFieldReconstruction End

                        ReferenceModel Begin
                                TXRX_DX      = ${column-txrxdx}
                                TXRX_DY      = ${column-txrxdy}
                                TXRX_DZ      = ${column-txrxdz}
                                RX_Pitch     = ${column-rxpitch}
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
subprocess.call(["mpirun", "-n", "${n-threads}", "/root/code/bin/galeisbs.exe", "galeisbs.con"])

# Upload results
inversionFiles = glob.glob('inversion.output.*')
print 'About to upload the following files:'
print inversionFiles
for fn in inversionFiles:
    cloudUpload(fn, fn)
