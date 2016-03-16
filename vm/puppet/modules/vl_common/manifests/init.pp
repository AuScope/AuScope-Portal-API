# Installs common VL dependencies for Debian and Redhat based distros
# Depends on the stahnma/epel module and python_pip module

include epel
include puppi
include python_pip

class vl_common {

    # Install default packages (curl/wget declared in puppi)
  package { ["subversion", "mercurial", "ftp", "bzip2", "bzip2-devel",
             "elfutils", "ntp", "ntpdate", "gcc", "make", "swig", "mlocate",
             "expect-dev", "gfortran", "build-essential"]:
    ensure => installed,
    require => Class["epel"],
  }

  case $::osfamily {
    'redhat': {
      package { ["atlas", "atlas-devel", "gcc-c++", "openssh",
                 "openssh-clients", "libpng-devel", "freetype-devel",
                 "libffi-devel"]:
        ensure => installed,
        require => Class["epel"],
      }
    }
    default: {
      package { ["libatlas-dev", "libatlas-base-dev", "openssh-server",
                 "openssh-client", "libpng-dev", "libfreetype6-dev",
                 "libffi-dev"]:
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
  package {  ["boto", "pyproj", "swift", "python-keystoneclient"]:
    ensure => installed,
    provider => "pip",
    require => Class["python_pip"],
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
    command => "$curl_cmd -L https://raw.githubusercontent.com/AuScope/ANVGL-Portal/master/vm/ec2-run-user-data.sh > $bootstrapLocation",
  }
  file { $bootstrapLocation:
    ensure => present,
    mode => "a=rwx",
  }
}
