package org.auscope.portal.server.vegl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A Hibernate-backed VEGLSeries data object
 *
 * @author Cihan Altinay
 * @author Josh Vote  -- Modified for VEGL
 */
public class VEGLSeriesDao extends HibernateDaoSupport {
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Queries for series matching the given criteria. Some but not all of
     * the parameters may be <code>null</code>.
     */
    public List<VEGLSeries> query(final String user, final String name,
                                 final String desc) {
        String queryString = new String("from VEGLSeries s where");
        boolean first = true;

        if (StringUtils.isNotEmpty(user)) {
            queryString += " s.user = '" + user + "'";
            first = false;
        }

        if (StringUtils.isNotEmpty(name)) {
            if (!first) {
                queryString += " and";
            }

            queryString += " s.name like '%"+name+"%'";
            first = false;
        }

        if (StringUtils.isNotEmpty(desc)) {
            if (!first) {
                queryString += " and";
            }

            queryString += " s.description like '%"+desc+"%'";
            first = false;
        }

        if (first) {
            logger.warn("All parameters were null!");
            return null;
        }

        return (List<VEGLSeries>) getHibernateTemplate().find(queryString);
    }

    /**
     * Retrieves the series with given ID.
     */
    public VEGLSeries get(final int id) {
        return (VEGLSeries) getHibernateTemplate().get(VEGLSeries.class, id);
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
