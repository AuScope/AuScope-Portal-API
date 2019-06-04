package org.auscope.portal.server.web.service;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.auscope.portal.server.web.repositories.ANVGLUserRepository;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.aaf.AAFAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;


@Service
public class ANVGLUserService {

	@Autowired
	private ANVGLUserRepository userRepository;
	
	public ANVGLUser getLoggedInUser() {
		Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
		String userEmail = "";
		// Google OAuth2
		if(userAuth instanceof OAuth2Authentication) {
			@SuppressWarnings("unchecked")
			HashMap<String, String> userDetails = (LinkedHashMap<String, String>)((OAuth2Authentication)userAuth).getUserAuthentication().getDetails();
			userEmail = (String)userDetails.get("email");
		}
		// Only other supported Authentication is AAFAuthentication, where
		// unique name has been set to user's email
		else {
			userEmail = userAuth.getName();
		}
		return getByEmail(userEmail);
	}
	
	public ANVGLUser getById(String id) {
		ANVGLUser user = userRepository.findById(id).orElse(null);
		return user;
	}
	
	public ANVGLUser getByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	public ANVGLUser getByFullName(String fullName) {
		return userRepository.findByFullName(fullName);
	}
	
	public void saveUser(ANVGLUser user) {
		userRepository.save(user);
	}

	public void deleteUser(ANVGLUser user) {
		userRepository.delete(user);
	}

	/*
	@Transactional
    public ANVGLUser getById(String id) {
        return getHibernateTemplate().get(ANVGLUser.class, id);
    }

	@Transactional
    public ANVGLUser getByEmail(String email) {
        List<?> resList = getHibernateTemplate().findByNamedParam("from ANVGLUser u where u.email =:p", "p", email);
        if(resList.isEmpty()) return null;
        return (ANVGLUser) resList.get(0);
    }
    
	@Transactional
    public void deleteUser(ANVGLUser user) {
        getHibernateTemplate().delete(user);
    }

	@Transactional
    public void save(ANVGLUser user) {
        getHibernateTemplate().saveOrUpdate(user);
    }
	*/
}
