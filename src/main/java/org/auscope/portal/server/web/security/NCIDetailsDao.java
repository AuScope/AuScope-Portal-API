package org.auscope.portal.server.web.security;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class NCIDetailsDao extends HibernateDaoSupport {

    public NCIDetails getByUser(ANVGLUser user) {
        List<?> resList = getHibernateTemplate().findByNamedParam("from NCIDetails d where d.user =:p", "p", user);
        if(resList.isEmpty()) return null;
        return (NCIDetails) resList.get(0);
    }
    
    public void save(NCIDetails details) {
        getHibernateTemplate().saveOrUpdate(details);
    }
}
