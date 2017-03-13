package com.naturalprogrammer.spring.lemondemo;

import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.security.LemonTokenAuthenticationFilter;
import com.naturalprogrammer.spring.lemondemo.entities.User;

@Component
public class MyTokenAuthenticationFilter extends LemonTokenAuthenticationFilter<User, Long>{

	@Override
	protected Long parseId(String id) {
		return Long.parseLong(id);
	}
}
