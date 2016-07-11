package org.auscope.portal.server.vegl;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A data access object for VLSCMSnapshot
 *
 * @author Geoff Squire
 *
 */
public class VLScmSnapshotDao extends HibernateDaoSupport {
    @SuppressWarnings("unchecked")
    public List<VLScmSnapshot> getSnapshotsForEntry(final String scmEntryId) {
        final String query = "from VLScmSnapshot s where s.entryId=:scmEntryId";
        return (List<VLScmSnapshot>)getHibernateTemplate()
                .findByNamedParam(query, "scmEntryId", scmEntryId);
    }

    public VLScmSnapshot getSnapshotForEntryAndProvider(final String scmEntryId,
            final String computeServiceId) {
        final String query = "from VLScmSnapshot s where s.entryId=:scmEntryId and s.computeServiceId=:computeServiceId";
        return (VLScmSnapshot)getHibernateTemplate()
                .findByNamedParam(query,
                        new String[] {"scmEntryId", "computeServiceId"},
                        new String[] {scmEntryId, computeServiceId});
    }
}
