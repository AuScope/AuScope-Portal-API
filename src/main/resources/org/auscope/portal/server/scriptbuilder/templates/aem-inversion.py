#! /usr/bin/python2.7
import csv
import xml.etree.ElementTree as ET
import sys

class Vgl(file):
			
    def __init__(self, file):
        self.run(file);
        self.header = ['FID','id','line','flight','fid','project_fas','project_ga','aircraft','timestamp','bearing','location','lidar','radalt','tx_elevation','dtm','mag','tx_pitch','tx_roll','tx_height','hsep_raw','vsep_raw','tx_height_std','hsep_std','vsep_std','emx_nonhprg1','emx_nonhprg2','emx_nonhprg3','emx_nonhprg4','emx_nonhprg5','emx_nonhprg6','emx_nonhprg7','emx_nonhprg8','emx_nonhprg9','emx_nonhprg10','emx_nonhprg11','emx_nonhprg12','emx_nonhprg13','emx_nonhprg14','emx_nonhprg15','emx_hprg1','emx_hprg2','emx_hprg3','emx_hprg4','emx_hprg5','emx_hprg6','emx_hprg7','emx_hprg8','emx_hprg9','emx_hprg10','emx_hprg11','emx_hprg12','emx_hprg13','emx_hprg14','emx_hprg15','x_sferics','x_lowfreq','x_powerline','x_vlf1','x_vlf2','x_vlf3','x_vlf4','x_geofact','emz_nonhprg1','emz_nonhprg2','emz_nonhprg3','emz_nonhprg4','emz_nonhprg5','emz_nonhprg6','emz_nonhprg7','emz_nonhprg8','emz_nonhprg9','emz_nonhprg10','emz_nonhprg11','emz_nonhprg12','emz_nonhprg13','emz_nonhprg14','emz_nonhprg15','emz_hprg1','emz_hprg2','emz_hprg3','emz_hprg4','emz_hprg5','emz_hprg6','emz_hprg7','emz_hprg8','emz_hprg9','emz_hprg10','emz_hprg11','emz_hprg12','emz_hprg13','emz_hprg14','emz_hprg15','z_sferics','z_lowfreq','z_powerline','z_vlf1','z_vlf2','z_vlf3','z_vlf4','z_geofact']
 
    def run(self,file):
        dics = self.getVglXMLDict(file);
        self.writeToCSV(dics,"out.csv");

    def replace_all(self,text, dic):
        for i, j in dic.iteritems():
            text = text.replace(i, j);
        return text;


    def writeToCSV(self,dictionaryData,filename):
        with open(filename,'w') as f:
            writer = csv.DictWriter(f,fieldnames=['id','line'] );
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
                dict['id'] = aemsurveys.find('{http://ga.gov.au}id').text;
                dict['line'] = aemsurveys.find('{http://ga.gov.au}line').text;
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

    
