package org.auscope.portal.server.vegl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class VGLSignatureDao extends HibernateDaoSupport {
    protected final Log logger = LogFactory.getLog(getClass());
    
    /**
     * Retrieves the signature of a given user.
     * 
     * @param user the user email address
     * @return user VGLSignature object. null if the user's exact and domain
     *         signature cannot be found
     */
    public VGLSignature getSignatureOfUser(final String user) {
        VGLSignature userSignature = null;

        // Look up user's signature from the database using exact match
        Query q = getSession().createQuery(
                "from VGLSignature s where s.user=:user");
        userSignature = (VGLSignature) q.setParameter("user", user)
                .uniqueResult();

        // Look up user's signature from the database using domain match
        if (userSignature == null) {
            String userDomain = user.substring(user.indexOf("@"));
            userSignature = (VGLSignature) q.setParameter("user", userDomain)
                    .uniqueResult();
            // This is needed to prevent the matching record from being replaced
            if (userSignature != null) {
                userSignature.setId(null);
            }
        }

        return userSignature;
    }

    /**
     * Retrieves the signature with given ID.
     */
    public VGLSignature get(final int id) {
        return (VGLSignature) getHibernateTemplate()
                .get(VGLSignature.class, id);
    }

    /**
     * Saves or updates the given signature.
     */
    public void save(final VGLSignature vglSignature) {
        getHibernateTemplate().saveOrUpdate(vglSignature);
    }
}