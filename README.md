# AuScope-Portal-API

Future back-end API server for AuScope Portal (http://portal.auscope.org) and AuScope Virtual Geophysics Portal (https://vgl.auscope.org/)

### To build and test:

mvn clean install

### To build and run:

mvn clean spring-boot:run


### IMPORTANT NOTES:

1) When using for AuScope Portal development, comment out the "cswGAECat" entry in https://github.com/AuScope/AuScope-Portal-API/blob/master/src/main/resources/application-registries.yaml for a faster startup time
2) Before deploying into development/production server, please fill out stackdriver entries in application.yaml with:
https://bitbucket.csiro.au/projects/GAP/repos/auscopeportalconfig/browse/stackdriver_entries_for_application.yaml
3) In 'pom.xml', there are two profiles 'prod' and 'test'. These are for production and test builds, and control the set of map layers available to Auscope Portal
4) Configuration files are in https://github.com/AuScope/AuScope-Portal-API/tree/master/src/main/resources
5) Vocabularies, known layers, registries etc. are defined in https://github.com/AuScope/AuScope-Portal-API/tree/master/src/main/java/org/auscope/portal/server/config

