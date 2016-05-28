package com.naturalprogrammer.spring.lemondemo;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;
import com.naturalprogrammer.spring.lemon.social.LemonSocialSecurityConfig;

@Configuration
public class MySecurityConfig extends LemonSocialSecurityConfig {

	@Override
	protected void authorizeRequests(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.antMatchers("/admin/**").hasRole("GOOD_ADMIN");
		super.authorizeRequests(http);
	}
}
