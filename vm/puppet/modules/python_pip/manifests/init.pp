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

    # Centos packages call the 'pip' executable 'pip-python' which can cause problems.
    # This is only a workaround until the below issue is fixed
    # Issue - http://projects.puppetlabs.com/issues/18236
    # Patch - https://github.com/puppetlabs/puppet/pull/1345
    case $operatingsystem {
        centos, redhat: { 
            file { '/usr/bin/pip':
                ensure => 'link',
                target => '/usr/bin/pip-python',
            }
        }
    }
}