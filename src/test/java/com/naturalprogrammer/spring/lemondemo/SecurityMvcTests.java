package com.naturalprogrammer.spring.lemondemo;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.jdbc.Sql;

@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
public class SecurityMvcTests extends AbstractMvcTests {
	
	@Test
	public void testLogin() throws Exception {
		
		String adminToken = login("admin@example.com", "admin!");
		Assert.assertNotNull(adminToken);
	}
	
	@Test
	public void testGetContext() throws Exception {
		
		
		String adminToken = login("admin@example.com", "admin!");
		Assert.assertNotNull(adminToken);
	}	

}
