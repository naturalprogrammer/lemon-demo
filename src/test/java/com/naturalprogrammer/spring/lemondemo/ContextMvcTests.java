package com.naturalprogrammer.spring.lemondemo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.test.context.jdbc.Sql;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;

@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
public class ContextMvcTests extends AbstractMvcTests {
	
	@Test
	public void testGetContextLoggedIn() throws Exception {
		
		String adminToken = login("admin@example.com", "admin!");
		
		mvc.perform(get("/api/core/context")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, adminToken))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.context.reCaptchaSiteKey").isString())
				.andExpect(jsonPath("$.user.id").value(1))
				.andExpect(jsonPath("$.user.roles[0]").value("ADMIN"));
	}
	
	@Test
	public void testGetContextWithoutLoggedIn() throws Exception {
		
		mvc.perform(get("/api/core/context"))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.context.reCaptchaSiteKey").isString())
				.andExpect(jsonPath("$.user").doesNotExist());
	}	
}
