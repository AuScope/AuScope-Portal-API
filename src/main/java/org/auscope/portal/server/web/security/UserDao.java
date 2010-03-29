package org.auscope.portal.server.web.security;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.gridjob.GeodesyJob;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A Hibernate-backed User data object
 *
 * @author Abdi Jama
 */
public class UserDao extends HibernateDaoSupport {
    protected final Log logger = LogFactory.getLog(getClass());
       
    /**
     * Retrieves the user with given username.
     *
     * @return <code>GeodesyJob</code> object with given ID.
     */
    public User get(final String username) {
        return (User) getHibernateTemplate().get(User.class, username);
    }
}
