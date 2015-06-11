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
            
            # Install Python-Pip
            package { ["python-pip"]: 
                ensure => installed,
                require => Class["epel"],
            }    
        }
        default: {
            package { "python-dev": 
                ensure => installed,
            }
            
            # Cant use python-pip package while https://bugs.launchpad.net/ubuntu/+source/python-pip/+bug/1306991 is active
            # Workaround by using easy_install
            exec { "easy-install-pip":
                cwd => "/tmp",
                command => "/usr/bin/easy_install pip",
                creates => "/usr/local/bin/pip",
                require => [Package["python"]],
                timeout => 0,
            }
        }
    }
}
