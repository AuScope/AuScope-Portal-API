package org.auscope.portal.server.web.service;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.web.repositories.VEGLSeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class VEGLSeriesService {
	
	@Autowired
	private VEGLSeriesRepository seriesRepository;
	
	
	/**
     * Queries for series matching the given criteria. Some but not all of
     * the parameters may be <code>null</code>.
     */
    public List<VEGLSeries> query(final String user, final String name,
                                 final String desc) {
    	VEGLSeries series = new VEGLSeries();
    	if (StringUtils.isNotEmpty(user)) {
        	series.setUser(user);
        } else {
        	series.setUser(null);
        }
        if (StringUtils.isNotEmpty(name)) {
        	series.setName(name);
        } else {
        	series.setName(null);
        }
        if (StringUtils.isNotEmpty(desc)) {
        	series.setDescription(desc);
        } else {
        	series.setDescription(null);
        }
        Example<VEGLSeries> example = Example.of(series);
        return seriesRepository.findAll(example);
    }

    /**
     * Retrieves the series with given ID.
     * @param user 
     */
    public VEGLSeries get(final int id, String userEmail) {
    	VEGLSeries res = seriesRepository.findById(id).orElse(null);
        if( (res!=null) && (! res.getUser().equalsIgnoreCase(userEmail))) {
            throw new AccessDeniedException("User not authorized to access series: "+id);
        }
        return res;
    }

    /**
     * Saves or updates the given series.
     */
    public void save(final VEGLSeries series) {
    	seriesRepository.save(series);
    }

    /**
     * Delete the given series.
     */
    public void delete(final VEGLSeries series) {
    	seriesRepository.delete(series);
    }
}
