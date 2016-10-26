# Builds VisIt binaries and libraries: https://wci.llnl.gov/codes/visit
# Also installs a number of dependencies
class visit {

    package {["xutils-dev", "python-libxml2", "libglu-dev", "libglu1", "libglu1-mesa-dev", "libxt-dev", "libqt4-dev", "libqt4-opengl-dev", "cmake"]:
        ensure => installed,
    }
    
    # Copy across compiled visit libs
    file { ["/usr/local/visit"]:
        ensure => "directory",
    }
    
    #Get build_visit script
    exec { "visit-dl":
        cwd => "/mnt",
        command => "/usr/bin/wget http://portal.nersc.gov/project/visit/releases/2.9.2/build_visit2_9_2",
        creates => "/mnt/build_visit2_9_2",
        require => [File["/usr/local/visit"], Package["python-libxml2"], Package["xutils-dev"], Package["libglu-dev"], Package["libglu1"], Package["libglu1-mesa-dev"], Package["libxt-dev"], Package["libqt4-dev"], Package["libqt4-opengl-dev"]],
        timeout => 0,
    }
    
    # The build_visit script is pretty fussy about where libpython can exist
    # It will be *somewhere* so just fake this file if it DNE
    file { '/usr/lib/libpython2.7.so':
        ensure => 'present',
        require => Exec["visit-dl"],
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
        command => "/bin/bash build_visit_stripped --mesa --console --silo --visit --system-python --system-qt --system-cmake --system-mesa --prefix /usr/local/visit",
        timeout => 0,
        require => Exec["visit-strip"],
    }

    $visitShContent= '# Environment for visit
export VISITINSTALL=/usr/local/visit/current
export PATH=$VISITINSTALL/bin:/usr/local/visit/bin:$PATH
export PYTHONPATH=$VISITINSTALL/linux-x86_64/lib/site-packages:$PYTHONPATH
'
    file {"visit-profile-env":
        path => "/etc/profile.d/visit.sh",
        ensure => present,
        content => $visitShContent,
        require => [Exec["visit-build"]],
    }
}
