package org.auscope.portal.server.web.service;

import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.admin.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VglAdminService extends AdminService {

    @Autowired
    public VglAdminService(HttpServiceCaller serviceCaller) {
        super(serviceCaller);
    }

    
}
