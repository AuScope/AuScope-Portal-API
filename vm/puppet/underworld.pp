# To Setup 
# puppet module install stahnma/epel
# ln -s /usr/bin/pip-python /usr/bin/pip

#Get the common VGL components
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

# Setup some default exec environment variables (they will eventually point to directories)
Exec {environment => ['PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/lib64/openmpi/bin:/usr/local/underworld/build/bin:', 
                      'LD_LIBRARY_PATH=/usr/lib64/openmpi/lib:$PETSC_DIR/lib:$HDF5_DIR/lib', 
                      'PETSC_DIR=/usr/local/petsc-3.1-p8-openmpi-1.6.1-opt', 
                      'HDF5_DIR=/usr/local/hdf5-1.8.10']}

#Install Underworld specific packages...
class uw_packages {
    package { ["openmpi", "openmpi-devel", "blas", "blas-devel", "lapack", "lapack-devel", "libxml2", "libxml2-devel", "mesa-libOSMesa", "mesa-libOSMesa-devel", "mesa-libGL", "mesa-libGL-devel", "mesa-libGLU", "mesa-libGLU-devel"]: 
        ensure => installed,
        require => Class["epel"],
    }
}
class {"uw_packages": }

# Download, build and install PETSc
puppi::netinstall { 'petsc':
    url => 'http://ftp.mcs.anl.gov/pub/petsc/release-snapshots/petsc-3.1-p8.tar.gz',
    extracted_dir => 'petsc-3.1-p8',
    destination_dir => '/usr/local',
    postextract_command => '/usr/local/petsc-3.1-p8/config/configure.py --prefix=/usr/local/petsc-3.1-p8-openmpi-1.6.1-opt --with-debugging=0 --with-shared=1 --with-hypre=1 --download-hypre=ifneeded --with-ml=1 --download-ml=ifneeded --with-mumps=1 --download-mumps=ifneeded --with-parmetis=1 --download-parmetis=ifneeded --with-scalapack=1 --download-scalapack=ifneeded --with-blacs --download-blacs=ifneeded --with-mpi-dir=/usr/lib64/openmpi && make PETSC_DIR=/usr/local/petsc-3.1-p8 PETSC_ARCH=linux-gnu-c-opt all && make PETSC_DIR=/usr/local/petsc-3.1-p8 PETSC_ARCH=linux-gnu-c-opt install && make PETSC_DIR=/usr/local/petsc-3.1-p8-openmpi-1.6.1-opt test',
    environment => ['PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/lib64/openmpi/bin', 
                    'LD_LIBRARY_PATH=/usr/lib64/openmpi/lib:'],
    before => Puppi::Netinstall['hdf5'],
    require => [Class["uw_packages"], Class["vgl_common"]],
}



# Download, build and install HDF5
puppi::netinstall { 'hdf5':
    url => 'http://www.hdfgroup.org/ftp/HDF5/current/src/hdf5-1.8.10.tar.gz',
    extracted_dir => 'hdf5-1.8.10',
    destination_dir => '/usr/local',
    environment => ['PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/lib64/openmpi/bin', 
                    'LD_LIBRARY_PATH=/usr/lib64/openmpi/lib:$PETSC_DIR/lib:', 
                    'PETSC_DIR=/usr/local/petsc-3.1-p8-openmpi-1.6.1-opt'],
    postextract_command => '/usr/local/hdf5-1.8.10/configure --prefix=/usr/local/hdf5-1.8.10 --enable-cxx --enable-fortran && make all && make install',
}

# Checkout, build and install Underworld
$uwBuildDir='/usr/local/underworld'
exec {'uw-checkout':
    command => "/usr/bin/hg clone https://www.underworldproject.org/hg/stgUnderworld $uwBuildDir",
    require => [Puppi::Netinstall['petsc'], Puppi::Netinstall['hdf5']],
    before => Exec['uw-obtainrepos'],
    creates => $uwBuildDir,
    logoutput => on_failure,
}
exec {'uw-obtainrepos':
    cwd => "$uwBuildDir",
    creates => "$uwBuildDir/GocadToolbox",
    command => "$uwBuildDir/obtainRepositories.py --with-gocadtoolbox=1",
    before => Exec['uw-configure'],
    logoutput => on_failure,
    timeout => 0,
}
exec {'uw-configure':
    cwd => "$uwBuildDir",
    creates => "$uwBuildDir/config.cfg",
    command => "$uwBuildDir/configure.py",
    before => Exec['uw-scons'],
    logoutput => on_failure,
    timeout => 0,
}
exec {'uw-scons':
    cwd => "$uwBuildDir",
    creates => "$uwBuildDir/build/bin",
    command => "$uwBuildDir/scons.py",
    logoutput => on_failure,
    timeout => 0,
}

#Setup environment info in profile
$mpiShContent= '# Environment for MPI
export PATH=/usr/lib64/openmpi/bin:$PATH
export LD_LIBRARY_PATH=/usr/lib64/openmpi/lib:/opt/intel64:$LD_LIBRARY_PATH'
file {"mpi-profile-env":
    path => "/etc/profile.d/mpi.sh",
    ensure => present,
    content => $mpiShContent,
}

$underworldShContent= '# Environment for Underworld
export PETSC_DIR=/usr/local/petsc-3.1-p8-openmpi-1.6.1-opt
export HDF5_DIR=/usr/local/hdf5-1.8.10
export LD_LIBRARY_PATH=$HDF5_DIR/lib:$PETSC_DIR/lib:$LD_LIBRARY_PATH
export UNDERWORLD_HOME=/usr/local/underworld
export PATH=$UNDERWORLD_HOME/build/bin:$UNDERWORLD_HOME/GocadToolbox/script/:$PATH'
file {"underworld-profile-env":
    path => "/etc/profile.d/underworld.sh",
    ensure => present,
    content => $underworldShContent,
}
