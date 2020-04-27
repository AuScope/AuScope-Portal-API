include "vl_common"
include "epel"
include "python_pip"
include "puppi"
include "autofsck"

#Install escript specific packages...
class escript_packages {

    #Install easy packages
    case $::osfamily {
        'redhat': {
            package { ["atlas", "atlas-devel", "blas-devel", "netcdf-devel", "suitesparse-devel", "boost-devel", "libpng-devel", "freetype-devel"]: 
                ensure => installed,
                require => Class["epel"],
            }    
        }
        default: {
            package { ["libatlas-dev", "libatlas-base-dev", "python-liblas", "liblas-dev", "libnetcdf-dev", "libsuitesparse-dev", "libboost-all-dev", "libboost-python-dev", "libboost-dev", "libpng-dev", "libfreetype6-dev",]: 
                ensure => installed,
                require => Class["epel"],
            }
        }
    }

    package { ["scons"]: 
        ensure => installed,
        require => Class["epel"],
    }
    
    package {"matplotlib":
        ensure => installed,
        provider => "pip",
        require => [Class["python_pip"], Package["numpy"]],
    }
    
    #Custom compile things we need that may not be packaged for this distro
    #Install easy packages
    case $::osfamily {
        'redhat': {
            # Note: At the time of writing the current OpenMPI package (openmpi-devel-1.5.4-1.el6.x86_64) is missing the necessary I/O component. 
            # Parts of escript require the I/O functionality and will not work. A bug was filed with CentOS who will 
            # hopefully fix the issue in an updated package (see http://bugs.centos.org/view.php?id=5931). 
            # When that bug is fixed you should be able run yum install openmpi but until that time you will need to build from source: 
            puppi::netinstall { 'openmpi':
                url => 'http://www.open-mpi.org/software/ompi/v1.6/downloads/openmpi-1.6.3.tar.gz',
                extracted_dir => 'openmpi-1.6.3',
                destination_dir => '/tmp',
                postextract_command => '/tmp/openmpi-1.6.3/configure --prefix=/usr/local && make install',
                require => [Class["escript_packages"], Class["vl_common"]],
            }
            $mpiShContent= '# Environment for MPI
            export PATH=/usr/local/bin:$PATH
            export LD_LIBRARY_PATH=/usr/local/lib/openmpi:/usr/local/lib/:$LD_LIBRARY_PATH'
            file {"mpi-profile-env":
                path => "/etc/profile.d/mpi.sh",
                ensure => present,
                content => $mpiShContent,
                require => Puppi::Netinstall['openmpi'],
            }
            
            # Install cartographic projection library
            puppi::netinstall { 'proj':
                url => 'http://download.osgeo.org/proj/proj-4.8.0.tar.gz',
                extracted_dir => 'proj-4.8.0',
                destination_dir => '/tmp',
                postextract_command => '/tmp/proj-4.8.0/configure && make install',
                require => [Class["escript_packages"], Class["vl_common"]],
            }
            
            # Install GDAL
            puppi::netinstall { 'gdal':
                url => 'http://download.osgeo.org/gdal/gdal-1.9.2.tar.gz',
                extracted_dir => 'gdal-1.9.2',
                destination_dir => '/tmp',
                postextract_command => '/tmp/gdal-1.9.2/configure && make install',
                require => [Class["escript_packages"], Class["vl_common"]],
            }
            
            # Install SILO
            puppi::netinstall { 'silo':
                url => 'https://wci.llnl.gov/codes/silo/silo-4.8/silo-4.8-bsd.tar.gz',
                extracted_dir => 'silo-4.8-bsd',
                destination_dir => '/tmp',
                postextract_command => '/tmp/silo-4.8-bsd/configure --prefix=/usr/local && make install',
                require => [Class["escript_packages"], Class["vl_common"]],
            }
            
            # Install SymPy
            puppi::netinstall { 'sympy':
                url => 'http://sympy.googlecode.com/files/sympy-0.7.2.tar.gz',
                extracted_dir => 'sympy-0.7.2',
                destination_dir => '/tmp',
                postextract_command => '/tmp/sympy-0.7.2/setup.py install',
                require => [Class["escript_packages"], Class["vl_common"]],
            }    
        }
        default: {
            #Non redhat instances have these packages ready to go
            package { ["libopenmpi-dev", "openmpi-bin", "libproj-dev", "gdal-bin", "libgdal-dev", "libsilo-dev", "python-silo", "python-sympy"]: 
                ensure => installed,
            }
        }
    }
}

class {"escript_packages": }


#Checkout, configure and install escript
exec { "escript-co":
    cwd => "/tmp",
    command => "/usr/bin/svn co --non-interactive --trust-server-cert https://svn.geocomp.uq.edu.au/svn/esys13/trunk escript_trunk",
    creates => "/tmp/escript_trunk",
    require => [Class["escript_packages"]],
    timeout => 0,
}
# Copy vm_options.py to <hostname>_options.py AND set the mpi prefix to correct values
case $::osfamily {
    'redhat': {
        exec { "escript-config":
            cwd => "/tmp/escript_trunk/scons",
            command => "/bin/sed \"s/^mpi_prefix.*$/mpi_prefix = ['\\/usr\\/local\\/include', '\\/usr\\/local\\/lib']/g\" vm_options.py > `/bin/hostname | /bin/sed s/[^a-zA-Z0-9]/_/g`_options.py",
            require => Exec["escript-co"],
        }    
    }
    default: {
        $scons_cfg_contents = '# Puppet generated SCONS config for debian
from templates.jessie_options import *
prefix = \'/opt/escript\'
werror = False 
verbose = True
openmp = True
mpi = \'OPENMPI\' 
netcdf = True
netcdf_prefix = [\'/usr/include/\', \'/usr/lib\']
umfpack = True
lapack = True
silo = True
'
        file {"debian-scons-cfg-env":
            path => "/tmp/escript-scons-debian.py",
            ensure => present,
            content => $scons_cfg_contents,
            require => Exec["escript-co"],
        }
        exec { "escript-config":
            cwd => "/tmp/escript_trunk/scons",
            command => "/bin/cp /tmp/escript-scons-debian.py `/bin/hostname | /bin/sed s/[^a-zA-Z0-9]/_/g`_options.py",
            require => File["debian-scons-cfg-env"],
        }
    }
}
exec { "escript-install":
    cwd => "/tmp/escript_trunk",
    command => "/usr/bin/scons",
    require => Exec["escript-config"],
    timeout => 0,
}
$escriptShContent= '# Environment for escript
export PATH=/opt/escript/bin:$PATH'
file {"escript-profile-env":
    path => "/etc/profile.d/escript.sh",
    ensure => present,
    content => $escriptShContent,
    require => Exec['escript-install'],
}

# Install VisIt
class {"visit":
    require => [Class["vl_common"], Exec['escript-install']],
}
