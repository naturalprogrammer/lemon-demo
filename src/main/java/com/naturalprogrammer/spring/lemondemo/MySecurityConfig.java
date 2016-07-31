package com.naturalprogrammer.spring.lemondemo;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;

@Configuration
public class MySecurityConfig extends LemonSecurityConfig {

	@Override
	protected void authorizeRequests(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.mvcMatchers("/admin/**").hasRole("GOOD_ADMIN");
		super.authorizeRequests(http);
	}
}
