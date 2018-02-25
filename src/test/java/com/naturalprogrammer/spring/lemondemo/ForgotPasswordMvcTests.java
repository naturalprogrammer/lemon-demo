package com.naturalprogrammer.spring.lemondemo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;

public class ForgotPasswordMvcTests extends AbstractMvcTests {
	
	@Test
	public void testForgorPassword() throws Exception {
		
		mvc.perform(post("/api/core/forgot-password")
                .param("email", "admin@example.com")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(204));
	}
	
	@Test
	public void testForgorPasswordUnknownEmail() throws Exception {
		
		mvc.perform(post("/api/core/forgot-password")
                .param("email", "unknown@example.com")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(404));
	}
	
	@Test
	public void testForgorPasswordInvalidEmail() throws Exception {
		
		// Null email
		mvc.perform(post("/api/core/forgot-password")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(400));

		// Blank email
		mvc.perform(post("/api/core/forgot-password")
                .param("email", "")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(422));
		
		// Wrong email format
		mvc.perform(post("/api/core/forgot-password")
                .param("email", "wrong-email-format")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(422));
	}
}
