# Shell script template for provisioning a portal vm if required
#
# Content taken from installPuppet-{centos,debian}.sh.
#
#
# /////////////////////////////
# ANVGL Portal Custom Modules - download from user specified GIT (or default)
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

# Install puppet itself if not already available
if hash puppet 2>/dev/null; then
    echo "Puppet version $(puppet --version ) already installed."
    if [ -f /etc/debian_version ]; then
        sudo apt-get update
        sudo apt-get install -y --force-yes at
    else
        sudo rpm -ivh http://yum.puppetlabs.com/el/6/products/x86_64/puppetlabs-release-6-7.noarch.rpm
        sudo yum install -y at
    fi
else
    # Determine what OS we're using so we install appropriately
    # Checks for a debian based system, or assumes rpm based
    if [ -f /etc/debian_version ]; then
        sudo apt-get update
        sudo apt-get install -y --force-yes puppet at
    else
        sudo rpm -ivh http://yum.puppetlabs.com/el/6/products/x86_64/puppetlabs-release-6-7.noarch.rpm
        yum install -y puppet at
    fi
fi

# Pip
if hash pip 2>/dev/null; then
    echo "Pip already installed."
else
    # Determine what OS we're using so we install appropriately
    # Checks for a debian based system, or assumes rpm based
    if [ -f /etc/debian_version ]; then
        sudo apt-get install -y python-pip
    else
        yum install -y python-pip
    fi
fi

# Swift
if hash swift 2>/dev/null; then
    echo "Swift already installed."
else
    # Determine what OS we're using so we install appropriately
    # Checks for a debian based system, or assumes rpm based
    if [ -f /etc/debian_version ]; then
        sudo apt-get install -y python-swiftclient
    else
        yum install -y python-swiftclient
    fi
fi

#sudo sh -c 'echo "    server = master.local" >> /etc/puppet/puppet.conf'
#sudo service puppet restart
#sudo chkconfig puppet on

#/////////////////////////////
#Install Additional Modules
#/////////////////////////////
# Puppet simply reports already installed modules, so this is safe
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
#
# First checks whether the vl modules are already available.
#/////////////////////////////

# Directory where vl modules will be installed
# Should we install into the "code" subdir?
moduleDirPrefix="/etc/puppet/code"
if [ ! -d "$moduleDirPrefix" ]; then
    # Ensure /etc/puppet exists, and try installing modules into that
    moduleDirPrefix="/etc/puppet"
    if [ ! -d "$moduleDirPrefix" ]; then
        echo "/etc/puppet directory does not exist, puppet install not found."
        exit 1
    fi
fi

# Ensure the modules subdirectory exists
moduleDir="$moduleDirPrefix/modules"
if [ ! -d "$moduleDir" ]; then
    echo "Creating the 'modules' subdirectory for puppet."
    sudo mkdir -p "$moduleDir"
fi

if [ ! -d "$moduleDir/vl_common" ]; then
    echo "Installing vl common modules into $moduleDir/vl_common"
    if [ -f /etc/debian_version ]; then
        sudo apt-get install -y --force-yes wget git
    else
        sudo yum install -y wget git
    fi

    # Assumes our temp dir does not already have content!
    tmpModulesDir="/opt/vgl/modules"
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
    cp -r "$tmpModulesDir/$pathSuffix/vl_common" "$moduleDir"
    if [ $? -ne 0 ]
    then
        echo "Failed copying to puppet module directory - aborting"
        exit 2
    fi

    # Don't tidy up until we're sure this approach works with cloud-init
    # # Tidy up
    # rm -rf "$tmpModulesDir"
else
    echo "Common vl modules found in $moduleDir/vl_common"
fi

# /////////////////////////////
# Make sure we are provisioned
# /////////////////////////////

# cd back out of the deleted directory to avoid issues with puppet application
cd "${WORKING_DIR}"

# Apply puppet modules
# TODO: template this so the portal can pass in provisioning from SSC
puppet apply <<EOF
include epel
include vl_common
EOF
