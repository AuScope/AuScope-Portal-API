@ECHO OFF

IF "%1" == "" GOTO RUN_WAR
IF "%1" == "-b" GOTO BUILD
IF "%1" == "-r" GOTO RUN_WAR
GOTO END

:BUILD
mvn clean install
GOTO END

:RUN_WAR
mvn clean spring-boot:run
:END
