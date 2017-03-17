package com.naturalprogrammer.spring.lemondemo;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;

@Component
public class MySecurityConfig extends LemonSecurityConfig {

	@Override
	protected void authorizeRequests(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.mvcMatchers("/admin/**").hasRole("GOOD_ADMIN");
		super.authorizeRequests(http);
	}
}
