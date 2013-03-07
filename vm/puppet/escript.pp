import "vgl_common"
import "epel"
import "python_pip"
import "puppi"
import "autofsck"

class {["epel", "python_pip", "vgl_common"]:}

# Disable fsck on boot
class { autofsck:
  ensure => present, # default
}

#Install escript specific packages...
class escript_packages {
    package { ["atlas-devel", "boost-devel", "blas-devel", "netcdf-devel", "scons", "suitesparse-devel"]: 
        ensure => installed,
        require => Class["epel"],
    }
}
class {"escript_packages": }

# Note: At the time of writing the current OpenMPI package (openmpi-devel-1.5.4-1.el6.x86_64) is missing the necessary I/O component. 
# Parts of escript require the I/O functionality and will not work. A bug was filed with CentOS who will 
# hopefully fix the issue in an updated package (see http://bugs.centos.org/view.php?id=5931). 
# When that bug is fixed you should be able run yum install openmpi but until that time you will need to build from source: 
puppi::netinstall { 'openmpi':
    url => 'http://www.open-mpi.org/software/ompi/v1.6/downloads/openmpi-1.6.3.tar.gz',
    extracted_dir => 'openmpi-1.6.3',
    destination_dir => '/tmp',
    postextract_command => '/tmp/openmpi-1.6.3/configure --prefix=/usr/local && make install',
    require => [Class["escript_packages"], Class["vgl_common"]],
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
    require => [Class["escript_packages"], Class["vgl_common"]],
}

# Install GDAL
puppi::netinstall { 'gdal':
    url => 'http://download.osgeo.org/gdal/gdal-1.9.2.tar.gz',
    extracted_dir => 'gdal-1.9.2',
    destination_dir => '/tmp',
    postextract_command => '/tmp/gdal-1.9.2/configure && make install',
    require => [Class["escript_packages"], Class["vgl_common"]],
}

# Install SILO
puppi::netinstall { 'silo':
    url => 'https://wci.llnl.gov/codes/silo/silo-4.8/silo-4.8-bsd.tar.gz',
    extracted_dir => 'silo-4.8-bsd',
    destination_dir => '/tmp',
    postextract_command => '/tmp/silo-4.8-bsd/configure --prefix=/usr/local && make install',
    require => [Class["escript_packages"], Class["vgl_common"]],
}

# Install SymPy
puppi::netinstall { 'sympy':
    url => 'http://sympy.googlecode.com/files/sympy-0.7.2.tar.gz',
    extracted_dir => 'sympy-0.7.2',
    destination_dir => '/tmp',
    postextract_command => '/tmp/sympy-0.7.2/setup.py install',
    require => [Class["escript_packages"], Class["vgl_common"]],
}

#Checkout, configure and install escript
exec { "escript-co":
    cwd => "/tmp",
    command => "/usr/bin/svn co https://svn.esscc.uq.edu.au/svn/esys13/trunk escript_trunk",
    creates => "/tmp/escript_trunk",
    require => [Puppi::Netinstall["sympy"], Puppi::Netinstall["proj"], Puppi::Netinstall["gdal"], Puppi::Netinstall['openmpi'], Puppi::Netinstall['silo']],
    timeout => 0,
}
# Copy vm_options.py to <hostname>_options.py AND set the mpi prefix to correct values
exec { "escript-config":
    cwd => "/tmp/escript_trunk/scons",
    command => "/bin/sed \"s/^mpi_prefix.*$/mpi_prefix = ['\\/usr\\/local\\/include', '\\/usr\\/local\\/lib']/g\" vm_options.py > `/bin/hostname | /bin/sed s/[^a-zA-Z0-9]/_/g`_options.py",
    require => Exec["escript-co"],
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
