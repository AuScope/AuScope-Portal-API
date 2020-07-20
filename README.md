![Docker](https://github.com/AuScope/AuScope-Portal-API/workflows/Docker/badge.svg?branch=master)

# AuScope-Portal-API

Future back-end API server for AuScope Portal (http://portal.auscope.org) and AuScope Virtual Geophysics Portal (https://vgl.auscope.org/)

### To build and test:

mvn clean install

### To build and run:

mvn clean spring-boot:run


### IMPORTANT NOTES:

1) Configuration files are in https://github.com/AuScope/AuScope-Portal-API/tree/master/src/main/resources. You must create a valid 'application.yaml' file before starting. 'application.yaml.default' is a suitable template.
2) When using for AuScope Portal development, comment out the "cswGAECat" entry in https://github.com/AuScope/AuScope-Portal-API/blob/master/src/main/resources/application-registries.yaml for a faster startup time
3) In 'application.yaml', there are two profiles 'prod' and 'test'. These are for production and test builds, and control the set of map layers available to Auscope Portal
4) Vocabularies, known layers, registries etc. are defined in https://github.com/AuScope/AuScope-Portal-API/tree/master/src/main/java/org/auscope/portal/server/config
5) On Windows, if you get 'CreateProcess error=206, The filename or extension is too long' when you run with 'spring-boot:run' then try turning off forking by adding '-Dspring-boot.run.fork=false' to the command line
6) Before deploying into development/production server, please fill out stackdriver entries in application.yaml with:
https://bitbucket.csiro.au/projects/GAP/repos/auscopeportalconfig/browse/stackdriver_entries_for_application.yaml

