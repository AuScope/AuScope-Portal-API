package org.auscope.portal.server.vegl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class VGLSignatureDao extends HibernateDaoSupport {
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Retrieves the signature of a given user.
     *
     * @param user
     */
    public VGLSignature getSignatureOfUser(final String user) {
        return (VGLSignature) getSession()
                .createQuery("from VGLSignature s where s.user=:user")
                .setParameter("user", user).uniqueResult();
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