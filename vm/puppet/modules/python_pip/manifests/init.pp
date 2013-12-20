# Installs python AND pip (python install program) for Centos
# Depends on the stahnma/epel module
class python_pip {

    # Install Python
    package { ["python", "python-devel"]: 
        ensure => installed,
    }
    
    # Install Python-Pip
    package { ["python-pip"]: 
        ensure => installed,
        require => Class["epel"],
    }
}