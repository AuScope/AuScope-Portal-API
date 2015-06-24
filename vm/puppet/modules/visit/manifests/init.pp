# Installs VisIt binaries and libraries: https://wci.llnl.gov/codes/visit
class visit {

    package {"xutils-dev":
        ensure => installed,
    }

    #Get build_visit script
    exec { "visit-dl":
        cwd => "/tmp",
        command => "/usr/bin/wget http://portal.nersc.gov/project/visit/releases/2.9.2/build_visit2_9_2",
        creates => "/tmp/build_visit2_9_2",
        require => Package["xutils-dev"],
        timeout => 0,
    }
    
    #Strip out any console questions
    exec { "visit-strip":
        cwd => "/tmp",
        command => "/bin/sed 's/read RESPONSE/RESPONSE=\"yes\"/g' build_visit2_9_2 > build_visit_stripped",
        creates => "/tmp/build_visit_stripped",
        timeout => 0,
        require => Exec["visit-dl"],
    }
    
    exec { "visit-build":
        cwd => "/tmp",
        command => "/bin/bash build_visit_stripped --mesa --console --silo",
        timeout => 0,
        require => Exec["visit-dl"],
    }


    $visitShContent= '# Environment for visit
export VISITINSTALL=/usr/local/visit2_9_1.linux-x86_64
export PATH=$VISITINSTALL/bin:$PATH
export PYTHONPATH=$VISITINSTALL/2.9.1/linux-x86_64/lib/site-packages:$PYTHONPATH
'
    file {"visit-profile-env":
        path => "/etc/profile.d/visit.sh",
        ensure => present,
        content => $visitShContent,
        require => Exec["visit-build"],
    }
}
