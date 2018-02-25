package com.naturalprogrammer.spring.lemondemo;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.naturalprogrammer.spring.lemon.security.JwtService;
import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;

public class VerificationMvcTests extends AbstractMvcTests {
	
	private String verificationCode;
	
	@Autowired
	private JwtService jwtService;
	
	@Before
	public void setUp() {
		
		verificationCode = jwtService.createToken(JwtService.VERIFY_AUDIENCE,
				Long.toString(UNVERIFIED_USER_ID), 60000L,
				LemonUtils.mapOf("email", "unverifieduser@example.com"));
	}
	
	@Test
	public void testEmailVerification() throws Exception {
		
		mvc.perform(post("/api/core/users/{userId}/verification", UNVERIFIED_USER_ID)
                .param("code", verificationCode)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(200))
				.andExpect(header().string(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME, containsString(".")))
				.andExpect(jsonPath("$.id").value(UNVERIFIED_USER_ID))
				.andExpect(jsonPath("$.roles").value(hasSize(0)))
				.andExpect(jsonPath("$.unverified").value(false))
				.andExpect(jsonPath("$.goodUser").value(true));
		
		// Already verified
		mvc.perform(post("/api/core/users/{userId}/verification", UNVERIFIED_USER_ID)
                .param("code", verificationCode)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED)
		.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, tokens.get(UNVERIFIED_USER_ID)))
                .andExpect(status().is(422));
	}
	
	@Test
	public void testEmailVerificationNonExistingUser() throws Exception {
		
		mvc.perform(post("/api/core/users/99/verification")
                .param("code", verificationCode)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED)
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, tokens.get(UNVERIFIED_USER_ID)))
                .andExpect(status().is(404));
	}

}
