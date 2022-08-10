#!/bin/bash
percent=
if [ -e /Users/pet22a/eclipse-workspace/auscope-portal-api/target/site/jacoco/jacoco.csv2 ]
then
    percent=`awk -F, '/AuScope-Portal-API/{sumIM+= $4; sumIC+=$5} END {print sumIC/(sumIM+sumIC)*100}' jacoco.csv`
else
    percent="coverage file [jacoco.csv] not found"
fi

echo $percent
