package org.auscope.portal.server.vegl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.access.AccessDeniedException;

/**
 * A Hibernate-backed VEGLSeries data object
 *
 * @author Cihan Altinay
 * @author Josh Vote  -- Modified for VEGL
 */
public class VEGLSeriesDao extends HibernateDaoSupport {

    /**
     * Queries for series matching the given criteria. Some but not all of
     * the parameters may be <code>null</code>.
     */
    public List<VEGLSeries> query(final String user, final String name,
                                 final String desc) {
        String queryString = "from VEGLSeries s where s.user=:email";
        ArrayList<String> paramKeys = new ArrayList<>();
        ArrayList<Object> paramVals = new ArrayList<>();
        
        paramKeys.add("email");
        paramVals.add(user);
        
        if (StringUtils.isNotEmpty(name)) {
            queryString += " and s.name like '%:name%'";
            paramKeys.add("name");
            paramVals.add(name);
        }   

        if (StringUtils.isNotEmpty(desc)) {
            queryString += " and s.description like '%:desc%'";
            paramKeys.add("desc");
            paramVals.add(name);
        }

        @SuppressWarnings("unchecked")
        List<VEGLSeries> res = (List<VEGLSeries>) getHibernateTemplate()
                .findByNamedParam(queryString,
                        paramKeys.toArray(new String[0]), paramVals.toArray());
        return res;
    }

    /**
     * Retrieves the series with given ID.
     * @param user 
     */
    public VEGLSeries get(final int id, String userEmail) {
        VEGLSeries res = getHibernateTemplate().get(VEGLSeries.class, id);
        if( (res!=null) && (! res.getUser().equalsIgnoreCase(userEmail))) {
            throw new AccessDeniedException("User not authorized to access series: "+id);
        }
        return res;
    }

    /**
     * Saves or updates the given series.
     */
    public void save(final VEGLSeries series) {
        getHibernateTemplate().saveOrUpdate(series);
    }

    /**
     * Delete the given series.
     */
    public void delete(final VEGLSeries series) {
        getHibernateTemplate().delete(series);
    }
}
