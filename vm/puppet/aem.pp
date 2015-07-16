class {["epel", "python_pip", "vgl_common"]:}

# Disable fsck on boot
class { autofsck:
  ensure => present, # default
}


#Install aem specific packages...
class aem_packages {
    package { ["unzip","fftw3","libfftw3-bin","libfftw3-mpi3","fftw3-dev","libfftw3-dev","libfftw3-mpi-dev","openmpi-bin","openmpi-common","libopenmpi1.6","libopenmpi-dev"]:
        ensure => installed,
        require => Class["epel"],
    }
}


class {"aem_packages": }



file { 'clean_and_remove_gaaem_dir':
  path => '/tmp/gaaem-1.0.beta/',
  ensure => absent,
  recurse => true,
  purge => true,
  force => true,
  require => [Class["aem_packages"], Class["vgl_common"]],
}



puppi::netinstall { 'gaaem-install':
        url => 'https://dl.dropboxusercontent.com/u/9399103/gaaem-1.0.beta.zip',
        extracted_dir => 'gaaem-1.0.beta',
        destination_dir => '/tmp',
        require => File["clean_and_remove_gaaem_dir"],
}

file { 'update_galeisbstdem_make':
  path => '/tmp/gaaem-1.0.beta/makefiles/galeisbstdem.make',
  ensure => present,
  require => Puppi::Netinstall['gaaem-install']
}->
file_line { 'Change_exedir':
  path => '/tmp/gaaem-1.0.beta/makefiles/galeisbstdem.make',
  line => 'exedir     = /usr/bin/gaaem',
  match   => "^exedir",
}


exec { "build_aem" :
        environment => ["FFTW_DIR=/usr/lib/x86_64-linux-gnu/"],
        cwd => "/tmp/gaaem-1.0.beta/makefiles",
        path => "/bin:/usr/bin:/usr/local/bin",
        command => "make -f /tmp/gaaem-1.0.beta/makefiles/galeisbstdem.make allclean",
        require => File["update_galeisbstdem_make"],
        timeout => 0,
}
