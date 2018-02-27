package com.naturalprogrammer.spring.lemondemo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemondemo.entities.User;

public class RequestEmailChangeMvcTests extends AbstractMvcTests {
	
	private static final String NEW_EMAIL = "new.email@example.com";
	
	private User form() {
		
		User user = new User();
		user.setPassword(USER_PASSWORD);
		user.setNewEmail(NEW_EMAIL);
		
		return user;
	}
	
	@Test
	public void testRequestEmailChange() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/email-change-request", UNVERIFIED_USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, tokens.get(UNVERIFIED_USER_ID))
				.content(LemonUtils.toJson(form())))
				.andExpect(status().is(204));
		
		User updatedUser = userRepository.findById(UNVERIFIED_USER_ID).get();
		Assert.assertEquals(NEW_EMAIL, updatedUser.getNewEmail());
		Assert.assertEquals(UNVERIFIED_USER_EMAIL, updatedUser.getEmail());
	}
}
