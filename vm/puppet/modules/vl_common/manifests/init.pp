# Installs common VL dependencies for Debian and Redhat based distros
# Depends on the stahnma/epel module and python_pip module

class vl_common {

  ensure_packages('curl')
  ensure_packages('wget')

    # Install default packages (curl/wget declared in puppi)
  package { ["subversion", "mercurial", "ftp", "bzip2",
             "elfutils", "ntp", "ntpdate", "gcc", "make", "swig", "mlocate",
             "expect-dev", "gfortran", "build-essential", "at", "python-swiftclient"]:
    ensure => installed,
    require => Class["epel"],
  }

  case $::osfamily {
    'redhat': {
      package { ["gcc-c++", "openssh", "openssh-clients",  "libffi-devel", "bzip2-devel"]:
        ensure => installed,
        require => Class["epel"],
      }
    }
    default: {
      package { ["openssh-server", "openssh-client", "libffi-dev", "libbz2-dev"]:
        ensure => installed,
        require => Class["epel"],
      }
    }
  }

  package {  ["numpy", "boto", "pyproj"]:
    ensure => installed,
    provider => "pip",
    require => Class["python_pip"],
  }

  package {["scipy"]:
    ensure => installed,
    provider => "pip",
    require => [Class["python_pip"], Package["numpy"]],
  }
}
