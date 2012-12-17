#!/bin/bash
# Installs puppet on a Centos 6 machine
# Originally sourced from http://awaseroot.wordpress.com/2012/09/01/new-script-install-puppet-on-centos/
# Usage:
# installPuppet-centos.sh [svnUrl] [pathSuffix]
# svnUrl - The base VGL URL where additional puppet modules will be downloaded from. Defaults to "https://svn.auscope.org/subversion/AuScopePortal/VEGL-Portal/trunk"
# pathSuffix - Will be appended to svnUrl to form the base url that will be recursively downloaded for modules. Defaults to "vm/puppet/modules/"

sudo sh -c \
'sudo cat > /etc/yum.repos.d/puppet.repo << EOF
[puppetlabs]
name=Puppet Labs Packages
baseurl=http://yum.puppetlabs.com/el/\$releasever/products/\$basearch/
enabled=1
gpgcheck=1
gpgkey=http://yum.puppetlabs.com/RPM-GPG-KEY-puppetlabs
EOF'

sudo sh -c \
'sudo cat > /etc/yum.repos.d/ruby.repo << EOF
[ruby]
name=ruby
baseurl=http://repo.premiumhelp.eu/ruby/
gpgcheck=0
enabled=0
EOF'

sudo yum install -y ruby
sudo yum --enablerepo="ruby" update ruby

sudo rpm -Uvh http://download.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-7.noarch.rpm
#sudo yum update

sudo yum --enablerepo=epel install -y puppet

#yum --enablerepo=epel,epel-puppet install -y puppet-server
#sudo yum install -y puppet-server

sudo sh -c 'echo "    server = master.local" >> /etc/puppet/puppet.conf'
sudo service puppet restart
sudo chkconfig puppet on

#/////////////////////////////
#Install Additional Modules
#/////////////////////////////

# Puppet Forge Modules
puppet module install stahnma/epel
puppet module install example42/puppi

# VGL Portal Custom Modules - download from user specified SVN (or default)
yum install -y wget 
baseUrl="https://svn.auscope.org/subversion/AuScopePortal/VEGL-Portal/trunk/"
pathSuffix="/vm/puppet/modules/"
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

#Ensure base url ends with a '/'
if [ `tail -c 2 <<< "$baseUrl"` != "/" ]
then
    baseUrl="$baseUrl/"
fi

#Ensure suffix doesn't start with a '/'
if [ `head -c 2 <<< "$pathSuffix"` != "/" ]
then
    pathSuffix=`tail -c +2 <<< "$pathSuffix"`
fi

#When we download we don't want a long tree of directories, we just want the modules directory. Therefore we need to cut directories
#back by the number of slashes in the URL. Don't forget there will be a trailing '/' and a http://
baseUrlSlashes=`grep -o "/" <<<"$baseUrl" | wc -l`
pathSuffixSlashes=`grep -o "/" <<<"$pathSuffix" | wc -l`
cutDirs=`expr $baseUrlSlashes - 3 + $pathSuffixSlashes`
wget -r "$baseUrl$pathSuffix" -P "$tmpModulesDir" -R htm,html -nH -np --cut-dirs $cutDirs
if [ $? -ne 0 ]
then
    echo "Failed download - aborting"
    exit 1
fi

#Now copy the modules to the puppet module install directory
moduleDir="/etc/puppet/modules"
find "$tmpModulesDir" -maxdepth 1 -mindepth 1 -type d -exec cp {} -r "$moduleDir" \;
if [ $? -ne 0 ]
then
    echo "Failed copying to puppet module directory - aborting"
    exit 2
fi

#Tidy up
rm -rf "$tmpModulesDir"

