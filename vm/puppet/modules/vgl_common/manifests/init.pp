# Installs common VGL dependencies for Centos
# Depends on the stahnma/epel module and python_pip module

class vgl_common {

    # Install default packages
    package { ["wget", "subversion", "mercurial", "ftp", "bzip2", "elfutils", "ntp", "ntpdate", "gcc", "gcc-c++", "make", "openssh", "openssh-clients", "swig", "libpng-devel", "freetype-devel", "atlas", "atlas-devel", "libffi-devel", "numpy", "swift", "scipy","python-keystoneclient"]: 
        ensure => installed,
        require => Class["epel"],
    }
    
    # Install default pip packages
    package {  ["boto", "pyproj"]:
        ensure => installed,
        provider => "pip",
        require => Class["python_pip"],
    }  
    
    # Install startup bootstrap
    $curl_cmd = "/usr/bin/curl"
    $bootstrapLocation = "/etc/rc.d/rc.local"
    exec { "get-bootstrap":
        before => File[$bootstrapLocation],
        command => "$curl_cmd -L https://svn.auscope.org/subversion/AuScopePortal/VEGL-Portal/trunk/vm/ec2-run-user-data.sh > $bootstrapLocation",
    }
    file { $bootstrapLocation: 
        ensure => present,
        mode => "a=rwx",
    }
}
