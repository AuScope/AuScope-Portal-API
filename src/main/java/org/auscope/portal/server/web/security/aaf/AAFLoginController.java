package org.auscope.portal.server.web.security.aaf;

import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;


//@Controller
//@SessionAttributes
public class AAFLoginController {
/*
    protected final Log logger = LogFactory.getLog(getClass());
    
    //@Resource(name="properties")
    //private Properties properties;
    @Value("${HOST.aafLoginUrl}")
    private String aafLoginUrl;
    
    @RequestMapping(value="/aaf/login", method = RequestMethod.GET)
    public String login(ModelMap model, HttpServletRequest request, @RequestParam Map<String,String> allRequestParams) {
        logger.debug(allRequestParams.toString());
        //if (allRequestParams.containsKey(GALAXY_PARAM)) {
        //    request.getSession().setAttribute(GALAXY_PARAM, allRequestParams.get(GALAXY_PARAM));
        //}
        //HostPrecedingPropertyPlaceholderConfigurer hppp = new HostPrecedingPropertyPlaceholderConfigurer();
        //String  key = "HOST.aafLoginUrl";
        //String url = hppp.resolvePlaceholder(key, properties);
        //model.put("aafUrl",url);
        
        model.put("aafUrl", aafLoginUrl);
        return "login";
    }
*/
}
