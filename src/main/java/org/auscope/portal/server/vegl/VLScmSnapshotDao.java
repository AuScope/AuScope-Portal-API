package org.auscope.portal.server.vegl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A data access object for VLSCMSnapshot
 *
 * @author Geoff Squire
 *
 */
public class VLScmSnapshotDao extends HibernateDaoSupport {
    protected final Log logger = LogFactory.getLog(getClass());

    public List<VLScmSnapshot> getSnapshotsForEntry(final String scmEntryId) {
        String query = "from VLScmSnapshot s where s.entryId=:scmEntryId";
        return (List<VLScmSnapshot>)getHibernateTemplate()
            .findByNamedParam(query, "scmEntryId", scmEntryId);
    }

    public VLScmSnapshot getSnapshotForEntryAndProvider(final String scmEntryId,
                                                        final String computeServiceId) {
        String query = "from VLScmSnapshot s where s.entryId=:scmEntryId and s.computeServiceId=:computeServiceId";
        return (VLScmSnapshot)getHibernateTemplate()
            .findByNamedParam(query,
                              new String[] {"scmEntryId", "computeServiceId"},
                              new String[] {scmEntryId, computeServiceId});
    }
}
