package org.auscope.portal.server.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Class for redirecting a user that isn't setup correctly to the user setup page.
 * @author Josh Vote (CSIRO)
 *
 */
public class RedirectUnconfiguredUserHandler implements AuthenticationSuccessHandler {

    protected CloudStorageService[] cloudStorageServices;
    protected CloudComputeService[] cloudComputeServices;
    protected NCIDetailsDao nciDetailsDao;
    private String frontEndUrl;

    public NCIDetailsDao getNciDetailsDao() {
        return nciDetailsDao;
    }

    public void setNciDetailsDao(NCIDetailsDao nciDetailsDao) {
        this.nciDetailsDao = nciDetailsDao;
    }

    public CloudStorageService[] getCloudStorageServices() {
        return cloudStorageServices;
    }

    public void setCloudStorageServices(CloudStorageService[] cloudStorageServices) {
        this.cloudStorageServices = cloudStorageServices;
    }

    public CloudComputeService[] getCloudComputeServices() {
        return cloudComputeServices;
    }

    public void setCloudComputeServices(CloudComputeService[] cloudComputeServices) {
        this.cloudComputeServices = cloudComputeServices;
    }

    public String getFrontEndUrl() {
		return frontEndUrl;
	}

	public void setFrontEndUrl(String frontEndUrl) {
		this.frontEndUrl = frontEndUrl;
	}

	@SuppressWarnings("unused")
    private final Log log = LogFactory.getLog(getClass());

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth)
            throws IOException, ServletException {
        response.sendRedirect(frontEndUrl + "/login/loggedIn");
    }
}
