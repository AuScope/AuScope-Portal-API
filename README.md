![Java CI with Maven](https://github.com/AuScope/AuScope-Portal-API/workflows/build-dev/badge.svg?branch=master)
[![Coverage](.github/badges/jacoco.svg)](.github/coverage/jacoco.csv)

# AuScope-Portal-API

Back-end API server for AuScope Portal (http://portal.auscope.org.au)

### To build and test:

```
mvn clean install
```

### To run:

```
java -jar --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED .\target\auscope-portal-api-6.6.1-SNAPSHOT.war
```

### IMPORTANT NOTES:

1) Configuration files are in https://github.com/AuScope/AuScope-Portal-API/tree/master/src/main/resources. You must create a valid 'application.yaml' file before starting. 'application.yaml.default' is a suitable template.
2) Elastic Search: A separate elastic search instance is now required to run AuScope Portal API.  Configure its connection details in the application.yaml
3) In 'application.yaml', there are two profiles 'prod' and 'test'. These are for production and test builds, and control the set of map layers available to Auscope Portal
4) Vocabularies, known layers, registries etc. are defined in https://github.com/AuScope/AuScope-Portal-API/tree/master/src/main/resources

