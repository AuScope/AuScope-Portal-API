#! /usr/bin/python2.7
import csv
import xml.etree.ElementTree as ET
import sys

class Vgl(file):

    def __init__(self, file):
        self.header = ['FID','id','line','flight','fid','project_fas','project_ga','aircraft','timestamp','bearing','location','lidar','radalt','tx_elevation','dtm','mag','tx_pitch','tx_roll','tx_height','hsep_raw','vsep_raw','tx_height_std','hsep_std','vsep_std','emx_nonhprg1','emx_nonhprg2','emx_nonhprg3','emx_nonhprg4','emx_nonhprg5','emx_nonhprg6','emx_nonhprg7','emx_nonhprg8','emx_nonhprg9','emx_nonhprg10','emx_nonhprg11','emx_nonhprg12','emx_nonhprg13','emx_nonhprg14','emx_nonhprg15','emx_hprg1','emx_hprg2','emx_hprg3','emx_hprg4','emx_hprg5','emx_hprg6','emx_hprg7','emx_hprg8','emx_hprg9','emx_hprg10','emx_hprg11','emx_hprg12','emx_hprg13','emx_hprg14','emx_hprg15','x_sferics','x_lowfreq','x_powerline','x_vlf1','x_vlf2','x_vlf3','x_vlf4','x_geofact','emz_nonhprg1','emz_nonhprg2','emz_nonhprg3','emz_nonhprg4','emz_nonhprg5','emz_nonhprg6','emz_nonhprg7','emz_nonhprg8','emz_nonhprg9','emz_nonhprg10','emz_nonhprg11','emz_nonhprg12','emz_nonhprg13','emz_nonhprg14','emz_nonhprg15','emz_hprg1','emz_hprg2','emz_hprg3','emz_hprg4','emz_hprg5','emz_hprg6','emz_hprg7','emz_hprg8','emz_hprg9','emz_hprg10','emz_hprg11','emz_hprg12','emz_hprg13','emz_hprg14','emz_hprg15','z_sferics','z_lowfreq','z_powerline','z_vlf1','z_vlf2','z_vlf3','z_vlf4','z_geofact'];
        self.run(file);

 
    def run(self,file):
        dics = self.getVglXMLDict(file);
        self.writeToCSV(dics,"out.csv");

    def replace_all(self,text, dic):
        for i, j in dic.iteritems():
            text = text.replace(i, j);
        return text;


    def writeToCSV(self,dictionaryData,filename):
        with open(filename,'w') as f:
            writer = csv.DictWriter(f,fieldnames=self.header);
            writer.writeheader();
            for d in dictionaryData:
                writer.writerow(d);


    def getVglXMLDict(self,filename):
        tree = ET.parse(filename);
        root = tree.getroot();
        csvArray=[];
        for featureMembers in root:
            for aemsurveys in featureMembers:
                dict={};
                dict['FID'] = aemsurveys.get('{http://www.opengis.net/gml}id');
                dict['id'] = aemsurveys.find('{http://ga.gov.au}id').text;
                dict['line'] = aemsurveys.find('{http://ga.gov.au}line').text;
                dict['flight'] = aemsurveys.find('{http://ga.gov.au}flight').text;
                dict['fid'] = aemsurveys.find('{http://ga.gov.au}fid').text;
                dict['project_fas'] = aemsurveys.find('{http://ga.gov.au}project_fas').text;
                dict['project_ga'] = aemsurveys.find('{http://ga.gov.au}project_ga').text;
                dict['aircraft'] = aemsurveys.find('{http://ga.gov.au}aircraft').text;
                dict['timestamp'] = aemsurveys.find('{http://ga.gov.au}timestamp').text;
                dict['bearing'] = aemsurveys.find('{http://ga.gov.au}bearing').text;
                dict['location'] = 'POINT(' + aemsurveys.find('{http://www.opengis.net/gml}location/{http://www.opengis.net/gml}Point/{http://www.opengis.net/gml}pos').text + ')';
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
    


class Configurator(file):

    def __init__(self,file):
        self.run(file);
        self.LogFile  = '${LogFile}'
        self.SystemFile = '${SystemFile}'
        self.InputFile   = ${InputFile}
        self.OutputDataFile  = '${OutputDataFile}'
        self.OutputHeaderFile = '${OutputHeaderFile}'
        self.run(file);
        
    def run(self,file):
        replaceList = {'${gogo}':"come",'${lala}':"baba"};
        out = open("out.txt","w");
        for line in open("test.txt"):
            out.write(self.replace_all(line,replaceList));
        out.close();

    def replace_all(self,text, dic):
        for i, j in dic.iteritems():
            text = text.replace(i, j);
        return text;



class Cloud():

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




def main(args):
    Configurator('test.txt');
    Vgl('example-wfs-gml.xml');



if __name__ == '__main__':
    main(sys.argv)

    
