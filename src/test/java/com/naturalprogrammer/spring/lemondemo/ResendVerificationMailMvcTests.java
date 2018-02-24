package com.naturalprogrammer.spring.lemondemo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemondemo.entities.User;

public class ResendVerificationMailMvcTests extends AbstractUser1MvcTests {

	@Test
	public void testResendVerificationMail() throws Exception {
		
		mvc.perform(get("/api/core/users/{id}/resend-verification-mail", user1.getId())
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, user1Token))
			.andExpect(status().is(204));
	}

	@Test
	public void testResendVerificationMailUnauthenticated() throws Exception {
		
		mvc.perform(get("/api/core/users/{id}/resend-verification-mail", user1.getId()))
			.andExpect(status().is(403));
	}
	
	@Test
	public void testResendVerificationMailAlreadyVerified() throws Exception {
		
		mvc.perform(get("/api/core/users/1/resend-verification-mail")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, adminToken))
			.andExpect(status().is(422));
	}
	
	@Test
	public void testResendVerificationMailOtherUser() throws Exception {
		
		mvc.perform(get("/api/core/users/1/resend-verification-mail")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, user1Token))
			.andExpect(status().is(403));
	}
	
	@Test
	public void testResendVerificationMailNonExistingUser() throws Exception {
		
		mvc.perform(get("/api/core/users/99/resend-verification-mail")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, user1Token))
			.andExpect(status().is(404));
	}
}
