# Installs VisIt binaries and libraries: https://wci.llnl.gov/codes/visit
class visit {

    package {["xutils-dev", "python-libxml2", "libglu-dev", "libglu1", "libglu1-mesa-dev", "libxt-dev"]:
        ensure => installed,
    }
    

    #Get build_visit script
    exec { "visit-dl":
        cwd => "/mnt",
        command => "/usr/bin/wget http://portal.nersc.gov/project/visit/releases/2.9.2/build_visit2_9_2",
        creates => "/mnt/build_visit2_9_2",
        require => [Package["python-libxml2"], Package["xutils-dev"], Package["libglu-dev"], Package["libglu1"], Package["libglu1-mesa-dev"], Package["libxt-dev"]],
        timeout => 0,
    }
    
    #Strip out any console questions
    exec { "visit-strip":
        cwd => "/mnt",
        command => "/bin/sed 's/read RESPONSE/RESPONSE=\"yes\"/g' build_visit2_9_2 > build_visit_stripped",
        creates => "/mnt/build_visit_stripped",
        timeout => 0,
        require => Exec["visit-dl"],
    }
    
    exec { "visit-build":
        cwd => "/mnt",
        command => "/bin/bash build_visit_stripped --mesa --console --silo",
        timeout => 0,
        require => Exec["visit-strip"],
    }
    
    file { ["/usr/local/visit", "/usr/local/visit/2.9.2"]:
        ensure => "directory",
        require => Exec["visit-build"],
    }
    
    file { '/usr/local/visit/current':
       ensure => 'link',
       target => '2.9.2',
       require => File["/usr/local/visit/2.9.2"],
    }
    
    exec { "visit-install-lib":
        cwd => "/mnt",
        command => "/bin/cp -R /mnt/visit2.9.2/src/lib /usr/local/visit/current",
        timeout => 0,
        creates => "/usr/local/visit/current/lib",
        require => File["/usr/local/visit/current"],
    }
    
    exec { "visit-install-bin":
        cwd => "/mnt",
        command => "/bin/cp -R /mnt/visit2.9.2/src/bin /usr/local/visit/current",
        timeout => 0,
        creates => "/usr/local/visit/current/bin",
        require => File["/usr/local/visit/current"],
    }
    
    exec { "visit-install-exe":
        cwd => "/mnt",
        command => "/bin/cp -R /mnt/visit2.9.2/src/exe /usr/local/visit/current",
        timeout => 0,
        creates => "/usr/local/visit/current/exe",
        require => File["/usr/local/visit/current"],
    }
    
    exec { "visit-install-plugins":
        cwd => "/mnt",
        command => "/bin/cp -R /mnt/visit2.9.2/src/plugins /usr/local/visit/current",
        timeout => 0,
        creates => "/usr/local/visit/current/plugins",
        require => File["/usr/local/visit/current"],
    }
    
    exec { "visit-install-version":
        cwd => "/mnt",
        command => "/bin/cp /mnt/visit2.9.2/src/VERSION /usr/local/visit/current/VERSION",
        timeout => 0,
        creates => "/usr/local/visit/current/VERSION",
        require => File["/usr/local/visit/current"],
    }


    $visitShContent= '# Environment for visit
export VISITINSTALL=/usr/local/visit/current
export PATH=$VISITINSTALL/bin:$PATH
export PYTHONPATH=$VISITINSTALL/lib/site-packages:$PYTHONPATH
'
    file {"visit-profile-env":
        path => "/etc/profile.d/visit.sh",
        ensure => present,
        content => $visitShContent,
        require => [Exec["visit-install-lib"], Exec["visit-install-bin"]],
    }
}
