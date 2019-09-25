package org.auscope.portal.server.web;

import org.auscope.portal.core.server.http.HttpServiceCaller;

/**
 * A separate HttpServiceCaller for searches with a smaller timeout period.
 * 
 * @author woo392
 *
 */
public class SearchHttpServiceCaller extends HttpServiceCaller {

	public SearchHttpServiceCaller(int connectionTimeOut) {
		super(connectionTimeOut);
	}

}
