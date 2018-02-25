package com.naturalprogrammer.spring.lemondemo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;

public class ResendVerificationMailMvcTests extends AbstractMvcTests {

	@Test
	public void testResendVerificationMail() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/resend-verification-mail", UNVERIFIED_USER_ID)
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, tokens.get(UNVERIFIED_USER_ID)))
			.andExpect(status().is(204));
	}

	@Test
	public void testAdminResendVerificationMailOtherUser() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/resend-verification-mail", UNVERIFIED_USER_ID)
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, tokens.get(ADMIN_ID)))
			.andExpect(status().is(204));
	}

	@Test
	public void testBadAdminResendVerificationMailOtherUser() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/resend-verification-mail", UNVERIFIED_USER_ID)
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, tokens.get(UNVERIFIED_ADMIN_ID)))
			.andExpect(status().is(403));
		
		mvc.perform(post("/api/core/users/{id}/resend-verification-mail", UNVERIFIED_USER_ID)
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, tokens.get(BLOCKED_ADMIN_ID)))
			.andExpect(status().is(403));
	}

	@Test
	public void testResendVerificationMailUnauthenticated() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/resend-verification-mail", UNVERIFIED_USER_ID))
			.andExpect(status().is(403));
	}
	
	@Test
	public void testResendVerificationMailAlreadyVerified() throws Exception {
		
		mvc.perform(get("/api/core/users/{id}/resend-verification-mail", USER_ID)
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, tokens.get(USER_ID)))
			.andExpect(status().is(422));
	}
	
	@Test
	public void testResendVerificationMailOtherUser() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/resend-verification-mail", UNVERIFIED_USER_ID)
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, tokens.get(USER_ID)))
			.andExpect(status().is(403));
	}
	
	@Test
	public void testResendVerificationMailNonExistingUser() throws Exception {
		
		mvc.perform(post("/api/core/users/99/resend-verification-mail")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, tokens.get(ADMIN_ID)))
			.andExpect(status().is(404));
	}
}
