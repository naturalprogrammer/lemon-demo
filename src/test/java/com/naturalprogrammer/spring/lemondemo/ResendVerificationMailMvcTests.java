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

@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
public class ResendVerificationMailMvcTests extends AbstractMvcTests {
	
	private String adminToken;
	
	private User userForm = new User("user1@example.com", "user123", "User 1");
	private String user1Token;
	private User user;
		
	@Before
	public void signup() throws Exception {
		
		adminToken = login("admin@example.com", "admin!");

		MvcResult result = mvc.perform(post("/api/core/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(LemonUtils.toJson(userForm)))
				.andExpect(status().is(201))
				.andReturn();
		
		MockHttpServletResponse response = result.getResponse();
		user1Token = response.getHeader(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME);
		user = LemonUtils.fromJson(response.getContentAsString(), User.class);
	}
	
	@Test
	public void testResendVerificationMail() throws Exception {
		
		mvc.perform(get("/api/core/users/{id}/resend-verification-mail", user.getId())
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, user1Token))
			.andExpect(status().is(204));
	}

	@Test
	public void testResendVerificationMailUnauthenticated() throws Exception {
		
		mvc.perform(get("/api/core/users/{id}/resend-verification-mail", user.getId()))
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
