/**
 * 
 */
package org.auscope.portal.server.vegl;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Geoff Squire
 *
 */
public class VglDownloadDao extends HibernateDaoSupport {

	/**
	 * Delete the specified VglDownload object.
	 * 
	 * @param download The VglDownload to delete
	 */
	public void deleteDownload(final VglDownload download) {
		if (download != null) {
			getHibernateTemplate().delete(download);
		}
	}
}
