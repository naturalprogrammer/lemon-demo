package com.naturalprogrammer.spring.lemondemo;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;

public class FetchNewTokenMvcTests extends AbstractMvcTests {
	
	@Test
	public void testFetchNewToken() throws Exception {
		
		MvcResult result = mvc.perform(post("/api/core/fetch-new-token")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER_NAME, tokens.get(UNVERIFIED_USER_ID))
                .header("contentType",  MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is(204))
				.andExpect(header().string(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME, containsString(".")))
				.andReturn();

		String newToken = result.getResponse().getHeader(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME);
		
		Assert.assertNotEquals(tokens.get(UNVERIFIED_USER_ID), newToken);
		ensureTokenWorks(newToken);
	}
	
	@Test
	public void testFetchNewTokenExpiration() throws Exception {
		
		MvcResult result = mvc.perform(post("/api/core/fetch-new-token")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER_NAME, tokens.get(UNVERIFIED_USER_ID))
		        .param("expirationMillis", "1000")
                .header("contentType",  MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is(204))
				.andReturn();

		String newToken = result.getResponse().getHeader(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME);
		ensureTokenWorks(newToken);

		Thread.sleep(1001L);
		mvc.perform(get("/api/core/context")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER_NAME, newToken))
				.andExpect(status().is(401));
		
	}

	@Test
	public void testFetchNewTokenByAdminForAnotherUser() throws Exception {
		
		MvcResult result = mvc.perform(post("/api/core/fetch-new-token")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER_NAME, tokens.get(ADMIN_ID))
		        .param("username", UNVERIFIED_USER_EMAIL)
                .header("contentType",  MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is(204))
				.andReturn();

		String newToken = result.getResponse().getHeader(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME);
		
		ensureTokenWorks(newToken);		
	}
	
	@Test
	public void testFetchNewTokenByNonAdminForAnotherUser() throws Exception {
		
		mvc.perform(post("/api/core/fetch-new-token")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER_NAME, tokens.get(UNVERIFIED_USER_ID))
		        .param("username", ADMIN_EMAIL)
                .header("contentType",  MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is(403));
	}
}
