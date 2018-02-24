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
public class AbstractUser1MvcTests extends AbstractMvcTests {
	
	private User userForm = new User("user1@example.com", "user123", "User 1");

	protected String adminToken;
	protected String user1Token;
	protected User user1;
		
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
		user1 = LemonUtils.fromJson(response.getContentAsString(), User.class);
	}
}
