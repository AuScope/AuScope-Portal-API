package org.auscope.portal.server.web.security;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A data access object for VEGLJob
 * @author Josh Vote
 *
 */
public class ANVGLUserDao extends HibernateDaoSupport {
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Retrieves ANVGLUser that has the specified UD
     *
     * @param id the ID of the user
     */
    public ANVGLUser getById(final String id) {
        return (ANVGLUser) getHibernateTemplate().get(ANVGLUser.class, id);
    }

    /**
     * Retrieves ANVGLUser that has the specified UD
     *
     * @param id the ID of the user
     */
    public ANVGLUser getByEmail(final String email) {
        List<?> resList = getHibernateTemplate().findByNamedParam("from ANVGLUser u where u.email =:p", "p", email);
        if(resList.isEmpty()) return null;
        return (ANVGLUser) resList.get(0);
    }
    
    /**
     * Deletes the given user.
     */
    public void deleteUser(final ANVGLUser user) {
        getHibernateTemplate().delete(user);
    }

    /**
     * Saves or updates the given user.
     */
    public void save(final ANVGLUser user) {
        getHibernateTemplate().saveOrUpdate(user);
    }
}
