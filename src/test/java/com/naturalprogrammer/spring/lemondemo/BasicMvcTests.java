package com.naturalprogrammer.spring.lemondemo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.test.context.jdbc.Sql;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;

@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql", })
public class BasicMvcTests extends AbstractMvcTests {
	
	@Test
	public void testPing() throws Exception {
		
		mvc.perform(get("/api/core/ping"))
				.andExpect(status().is(204));
	}
	
	@Test
	public void testGetContextLoggedIn() throws Exception {
		
		mvc.perform(get("/api/core/context")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, tokens.get(ADMIN_ID)))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.context.reCaptchaSiteKey").isString())
				.andExpect(jsonPath("$.user.id").value(ADMIN_ID))
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