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


#Install aem specific packages...
class aem_packages {
    package { ["fftw-devel", "fftw", "openmpi", "openmpi-devel"]: 
        ensure => installed,
        require => Class["epel"],
    }
}


class {"aem_packages": }



# Todo : Install ga-aem code (awaiting GA paper work to be done to make the code public)

