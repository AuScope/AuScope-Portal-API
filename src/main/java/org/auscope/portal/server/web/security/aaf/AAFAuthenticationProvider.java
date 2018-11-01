package org.auscope.portal.server.web.security.aaf;

import org.auscope.portal.server.web.security.aaf.JWTManagement;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.util.Assert;

/**
 * Created by wis056 on 8/04/2015.
 */
public class AAFAuthenticationProvider implements AuthenticationProvider, InitializingBean, MessageSourceAware {

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private JWTManagement jwtManagement;


    public final void afterPropertiesSet() throws Exception {
        Assert.notNull(this.jwtManagement, "A JWT management system must be set");
        Assert.notNull(this.messages, "A message source must be set");
        doAfterPropertiesSet();
    }

    protected void doAfterPropertiesSet() {
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    public void setJwtManagement(JWTManagement jwtManagement) {
        this.jwtManagement = jwtManagement;
    }
    
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        Assert.isInstanceOf(AAFAuthenticationToken.class, authentication, messages.getMessage(
                "AAFAuthenticationProvider.onlySupports",
                "Only AAFAuthenticationToken is supported"));

        return this.jwtManagement.parseJWT(((AAFAuthenticationToken) authentication).getCredentials());
    }

    public boolean supports(Class<?> authentication) {
        return (AAFAuthenticationToken.class.isAssignableFrom(authentication));
    }
    
}
