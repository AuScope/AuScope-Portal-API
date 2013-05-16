# Installs VisIt binaries and libraries: https://wci.llnl.gov/codes/visit
class visit {

    puppi::netinstall { 'visit':
        url => 'http://portal.nersc.gov/svn/visit/trunk/releases/2.6.1/visit2_6_1.linux-x86_64-rhel5.tar.gz',
        extracted_dir => 'visit2_6_1.linux-x86_64',
        destination_dir => '/usr/local',
    }
    
    $visitShContent= '# Environment for visit
export VISITINSTALL=/usr/local/visit2_6_1.linux-x86_64
export PATH=$VISITINSTALL/bin:$PATH
export PYTHONPATH=$VISITINSTALL/2.6.1/linux-x86_64/lib/site-packages:$PYTHONPATH
'
    file {"visit-profile-env":
        path => "/etc/profile.d/visit.sh",
        ensure => present,
        content => $visitShContent,
        require => Puppi::Netinstall["visit"],
    }
}
