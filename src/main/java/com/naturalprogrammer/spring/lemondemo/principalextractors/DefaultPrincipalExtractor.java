package com.naturalprogrammer.spring.lemondemo.principalextractors;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.security.AbstractPrincipalExtractor;
import com.naturalprogrammer.spring.lemondemo.entities.User;

@Component
public class DefaultPrincipalExtractor extends AbstractPrincipalExtractor<User> {
	
    private static final Log log = LogFactory.getLog(DefaultPrincipalExtractor.class);
	
	public DefaultPrincipalExtractor() {
		log.info("Created");
	}

	
	@Override
	protected User newUserWithAdditionalProperties(Map<String, Object> principalMap) {
		
		User user = new User();
		user.setName((String) principalMap.get("name"));
		
		return user;
	}
}
