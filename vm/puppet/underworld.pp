# To Setup 
# puppet module install stahnma/epel
# ln -s /usr/bin/pip-python /usr/bin/pip

import "vgl_common"
class {"vgl_common":}

#Install pip-libs
package {"GDAL":
    ensure => installed,
    provider => "pip",
    require => Class["python_pip"],
}

