package com.naturalprogrammer.spring.lemondemo.principalextractors;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.security.AbstractPrincipalExtractor;
import com.naturalprogrammer.spring.lemondemo.entities.User;

@Component
public class DefaultPrincipalExtractor extends AbstractPrincipalExtractor<User> {
	
	public DefaultPrincipalExtractor() {
		this.provider = "default";
	}

	@Override
	protected User newUser(Map<String, Object> principalMap) {
		
		User user = new User();
		user.setName((String) principalMap.get("name"));
		
		return user;
	}
}
