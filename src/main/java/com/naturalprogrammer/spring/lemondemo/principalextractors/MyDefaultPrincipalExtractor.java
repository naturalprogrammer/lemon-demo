//package com.naturalprogrammer.spring.lemondemo.principalextractors;
//
//import java.util.Map;
//
//import org.springframework.stereotype.Component;
//
//import com.naturalprogrammer.spring.lemon.security.principalextractors.DefaultPrincipalExtractor;
//import com.naturalprogrammer.spring.lemondemo.entities.User;
//
//@Component
//public class MyDefaultPrincipalExtractor extends DefaultPrincipalExtractor<User> {
//
//	protected void fillAdditionalFields(User user, Map<String, Object> map) {
//	    user.setName((String) map.get("name"));		
//	}
//}
