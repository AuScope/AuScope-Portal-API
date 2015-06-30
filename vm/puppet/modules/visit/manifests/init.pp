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
    
    exec { "visit-install":
        cwd => "/mnt",
        command => "/bin/cp -R /mnt/visit2.9.2/src/lib /usr/lib/visit2_9_2",
        timeout => 0,
        require => Exec["visit-build"],
    }


    $visitShContent= '# Environment for visit
export VISITINSTALL=/usr/lib/visit2_9_2
export PYTHONPATH=$VISITINSTALL/site-packages:$PYTHONPATH
'
    file {"visit-profile-env":
        path => "/etc/profile.d/visit.sh",
        ensure => present,
        content => $visitShContent,
        require => Exec["visit-install"],
    }
}
