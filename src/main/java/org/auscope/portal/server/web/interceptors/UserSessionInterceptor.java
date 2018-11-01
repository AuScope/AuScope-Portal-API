package org.auscope.portal.server.web.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

/**
 * Interceptor for handling user session expiry. Please refer to
 * applicationContext.xml file for a list of VL paths this interceptor apply to.
 *
 * Important: Do not include VL home page and its controller request mapping paths
 * to the above interceptor mapping list as doing so will prevent the spatial layers
 * from loading.
 *
 * @author Richard Goh
 */
public class UserSessionInterceptor extends HandlerInterceptorAdapter {
    private final Log LOG = LogFactory.getLog(getClass());

    /**
     * Check user's principal to ensure session is still valid
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {

        Authentication principal = (Authentication) request.getUserPrincipal();
        if (principal == null || principal.getPrincipal() == null) {
            LOG.warn("Failed to retrieve user principal.");
            ModelMap model = new ModelMap();
            model.put("success", false);
            model.put("msg", "Your session has timed out or login credentails are no longer valid.");
            model.put("debugInfo", "Please refresh this page or go to <a href='/VGL-Portal'>VGL Home Page</a> to start again.");
            MappingJackson2JsonView view = new MappingJackson2JsonView();
            view.render(model, request, response);
            return false;
        }

        return true;
    }
}