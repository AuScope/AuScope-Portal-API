FROM tomcat:8.5

MAINTAINER geoffrey.squire@data61.csiro.au

ARG cert=https://qv.csiro.au/QuoVadis_Global_SSL_ICA_G3.crt
ARG war=target/VGL-Portal-0.0.1-SNAPSHOT.war

# Include the QuoVadis root certificate for CSIRO certs
ADD ${cert} /cacert
RUN keytool -noprompt -importcert -alias startssl -keystore \
  /docker-java-home/jre/lib/security/cacerts -storepass changeit -file /cacert \
  && rm /cacert

ADD ${war} /usr/local/tomcat/webapps/VGL-Portal.war

RUN rm -rf /usr/local/tomcat/webapps/ROOT \
  && unzip /usr/local/tomcat/webapps/VGL-Portal.war -d /usr/local/tomcat/webapps/ROOT \
  && rm /usr/local/tomcat/webapps/VGL-Portal.war

# ADD application.yaml /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/
