package org.auscope.portal.server.web.repositories;

import java.util.List;

import org.auscope.portal.server.state.PortalState;
import org.auscope.portal.server.web.security.PortalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortalStateRepository extends JpaRepository<PortalState, Integer> {

	List<PortalState> findByParent(PortalUser user);	
	PortalState findById(String stateId);
}
