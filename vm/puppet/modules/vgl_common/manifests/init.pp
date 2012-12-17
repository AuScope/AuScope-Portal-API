# Installs common VGL dependencies for Centos
# Depends on the stahnma/epel module and python_pip module
class vgl_common {

    import "epel"
    class {"epel":}

    # Install default packages
    package { ["wget", "subversion", "mercurial", "ftp", "bzip2", "elfutils", "ntp", "ntpdate", "gcc", "gcc-c++", "make", "openssh", "openssh-clients", "swig", "libpng-devel", "freetype-devel"]: 
        ensure => installed,
    }
    
    import "python_pip"
    class {"python_pip":}
    
    package {  ["numpy", "scipy", "boto", "GDAL", "pyproj", "matplotlib"]:
        ensure => installed,
        provider => "pip",
        require => Class["python_pip"],
    }
}
