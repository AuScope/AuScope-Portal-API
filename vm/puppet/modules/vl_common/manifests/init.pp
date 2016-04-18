# Installs common VL dependencies for Debian and Redhat based distros
# Depends on the stahnma/epel module and python_pip module

include epel
include puppi
include python_pip

class vl_common {

    # Install default packages (curl/wget declared in puppi)
  package { ["subversion", "mercurial", "ftp", "bzip2", "bzip2-devel",
             "elfutils", "ntp", "ntpdate", "gcc", "make", "swig", "mlocate",
             "expect-dev", "gfortran", "build-essential", "curl", "wget"]:
    ensure => installed,
    require => Class["epel"],
  }

  case $::osfamily {
    'redhat': {
      package { ["gcc-c++", "openssh", "openssh-clients",  "libffi-devel"]:
        ensure => installed,
        require => Class["epel"],
      }
    }
    default: {
      package { ["openssh-server", "openssh-client", "libffi-dev"]:
        ensure => installed,
        require => Class["epel"],
      }
    }
  }
}
