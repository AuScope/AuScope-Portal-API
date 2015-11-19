# Installs common VL dependencies for Centos
# Depends on the stahnma/epel module and python_pip module

class vgl_common {

    # Install default packages
    package { ["wget", "subversion", "mercurial", "ftp", "bzip2", "elfutils", "ntp", "ntpdate", "gcc", "make", "swig", "expect-dev", "gfortran", "build-essential", "curl"]: 
        ensure => installed,
        require => Class["epel"],
    }
    
    case $::osfamily {
        'redhat': {
            package { ["atlas", "atlas-devel", "gcc-c++", "openssh", "openssh-clients", "libpng-devel", "freetype-devel", "libffi-devel"]: 
                ensure => installed,
                require => Class["epel"],
            }
        }
        default: {
            package { ["libatlas-dev", "libatlas-base-dev", "openssh-server", "openssh-client", "libpng-dev", "libfreetype6-dev", "libffi-dev"]: 
                ensure => installed,
                require => Class["epel"],
            }
            
            package {  ["python-swiftclient"]:
                ensure => installed,
                provider => "pip",
                require => Class["python_pip"],
            }  
        }
    }
    
    # Install default pip packages
    package {  ["numpy", "boto", "pyproj", "swift", "python-keystoneclient"]:
        ensure => installed,
        provider => "pip",
        require => Class["python_pip"],
    }  
    package {["scipy"]:
        ensure => installed,
        provider => "pip",
        require => [Class["python_pip"], Package["numpy"]],
    }

    
    
    # Install startup bootstrap
    $curl_cmd = "/usr/bin/curl"
    $bootstrapLocation = "/etc/rc.local"
    case $::osfamily {
        'redhat': {
            $bootstrapLocation = "/etc/rc.d/rc.local"
        }
    }
    
    exec { "get-bootstrap":
        before => File[$bootstrapLocation],
        command => "$curl_cmd -L https://raw.githubusercontent.com/AuScope/VEGL-Portal/master/vm/ec2-run-user-data.sh > $bootstrapLocation",
    }
    file { $bootstrapLocation: 
        ensure => present,
        mode => "a=rwx",
    }
}
