package com.naturalprogrammer.spring.lemondemo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;

@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
public class LoginMvcTests extends AbstractMvcTests {
	
	@Test
	public void testLogin() throws Exception {
		
		mvc.perform(post("/login")
                .param("username", "admin@example.com")
                .param("password", "admin!")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(200))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.nonce").doesNotExist())
				.andExpect(jsonPath("$.username").value("admin@example.com"))
				.andExpect(jsonPath("$.roles").value(hasSize(1)))
				.andExpect(jsonPath("$.roles[0]").value("ADMIN"))
				.andExpect(jsonPath("$.tag.name").value("Admin 1"))
				.andExpect(jsonPath("$.unverified").value(false))
				.andExpect(jsonPath("$.blocked").value(false))
				.andExpect(jsonPath("$.admin").value(true))
				.andExpect(jsonPath("$.goodUser").value(true))
				.andExpect(jsonPath("$.goodAdmin").value(true));
	}

	@Test
	public void testLoginWrongPassword() throws Exception {
		
		mvc.perform(post("/login")
                .param("username", "admin@example.com")
                .param("password", "wrong-password")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(401));
	}

	@Test
	public void testLoginBlankPassword() throws Exception {
		
		mvc.perform(post("/login")
                .param("username", "admin@example.com")
                .param("password", "")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(401));
	}

	@Test
	public void testTokenLogin() throws Exception {
		
		String adminToken = login("admin@example.com", "admin!");
		
		mvc.perform(get("/api/core/context")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, adminToken))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.user.id").value(1));
	}

	@Test
	public void testTokenLoginWrongToken() throws Exception {
		
		mvc.perform(get("/api/core/context")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, "Bearer a-wrong-token"))
				.andExpect(status().is(401));
	}
	
	@Test
	public void testLogout() throws Exception {
		
		mvc.perform(post("/logout"))
                .andExpect(status().is(404));
	}
}
