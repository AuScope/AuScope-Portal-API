#!/bin/bash
# Installs puppet on a Centos 6 machine
# Originally sourced from http://awaseroot.wordpress.com/2012/09/01/new-script-install-puppet-on-centos/
# Usage:
# installPuppet-centos.sh [svnUrl] [pathSuffix]
# gitUrl - The base VGL URL where additional puppet modules will be downloaded from. Defaults to "https://github.com/AuScope/ANVGL-Portal/raw/master"
# pathSuffix - Will be appended to gitUrl to form the base url that will be recursively downloaded for modules. Defaults to "vm/puppet/modules/"

# /////////////////////////////
# VGL Portal Custom Modules - download from user specified GIT (or default)
#
# Edit these variables if you need to download from a different git
# repo/branch.
#
# /////////////////////////////

# baseUrl -- git repository url
baseUrl="https://github.com/AuScope/ANVGL-Portal.git"

# branch -- branch in the git repo
branch="master"

# pathSuffix -- path to puppet modules in the repo
pathSuffix="/vm/puppet/modules/"

apt-get update
apt-get upgrade
apt-get install -y --force-yes puppet

#/////////////////////////////
#Install Additional Modules
#/////////////////////////////

# Puppet Forge Modules
puppet module install stahnma/epel
if [ $? -ne 0 ]
then
    echo "Failed to install puppet module stahnma/epel"
    exit 1
fi

puppet module install example42/puppi
if [ $? -ne 0 ]
then
    echo "Failed to install puppet module example42/puppi"
    exit 1
fi

puppet module install jhoblitt/autofsck
if [ $? -ne 0 ]
then
    echo "Failed to install puppet module jhoblitt/autofsck"
    exit 1
fi

#/////////////////////////////
# Clone specified git repository into $tmpModulesDir and install puppet modules.
#/////////////////////////////

apt-get install -y --force-yes wget git
tmpModulesDir="/tmp/modules/"
rm -rf "$tmpModulesDir"
if [ "$1" !=  "" ]
then
    baseUrl="$1"
fi
if [ "$2" !=  "" ]
then
    pathSuffix="$2"
fi

#Ensure suffix doesn't start with a '/'
if [ `head -c 2 <<< "$pathSuffix"` != "/" ]
then
    pathSuffix=`tail -c +2 <<< "$pathSuffix"`
fi

# Clone the git repository into $tmpModulesDir so we can extract the
# puppet modules.  Make sure to use the correct branch!
mkdir -p "$tmpModulesDir"
git clone "$baseUrl" "$tmpModulesDir"
cd "$tmpModulesDir"
git checkout "$branch"

#Now copy the modules to the puppet module install directory
moduleDir="/etc/puppet/modules"
find "$tmpModulesDir/$pathSuffix" -maxdepth 1 -mindepth 1 -type d -exec cp {} -r "$moduleDir" \;
if [ $? -ne 0 ]
then
    echo "Failed copying to puppet module directory - aborting"
    exit 2
fi

#Tidy up
rm -rf "$tmpModulesDir"
