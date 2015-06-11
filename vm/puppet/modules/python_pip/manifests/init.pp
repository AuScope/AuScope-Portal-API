# Installs python AND pip (python install program) for Centos
# Depends on the stahnma/epel module
class python_pip {

    # Install Python
    package { "python": 
        ensure => installed,
    }
    case $::osfamily {
        'redhat': {
            package { "python-devel": 
                ensure => installed,
            }    
        }
        default: {
            package { "python-dev": 
                ensure => installed,
            }
        }
    }
    
    # Install Python-Pip
    package { ["python-pip"]: 
        ensure => installed,
        require => Class["epel"],
    }
}
