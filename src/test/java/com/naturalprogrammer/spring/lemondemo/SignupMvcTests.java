package com.naturalprogrammer.spring.lemondemo;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemondemo.entities.User;
import com.naturalprogrammer.spring.lemondemo.repositories.UserRepository;

@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
public class SignupMvcTests extends AbstractMvcTests {
	
	@Autowired
	private UserRepository userRepository;
		
	@Test
	public void testSignupWithInvalidData() throws Exception {
		
		User invalidUser = new User("abc", "user1", null);

		mvc.perform(post("/api/core/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(LemonUtils.toJson(invalidUser)))
				.andExpect(status().is(422))
				.andExpect(jsonPath("$.errors[*].field").value(allOf(hasSize(4),
					hasItems("user.email", "user.password", "user.name"))));
	}

	@Test
	public void testSignup() throws Exception {
		
		User user = new User("user1@example.com", "user123", "User 1");

		mvc.perform(post("/api/core/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(LemonUtils.toJson(user)))
				.andExpect(status().is(201))
				.andExpect(header().string(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME, containsString(".")))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.username").value("user1@example.com"))
				.andExpect(jsonPath("$.roles").value(hasSize(1)))
				.andExpect(jsonPath("$.roles[0]").value("UNVERIFIED"))
				.andExpect(jsonPath("$.tag.name").value("User 1"))
				.andExpect(jsonPath("$.unverified").value(true))
				.andExpect(jsonPath("$.blocked").value(false))
				.andExpect(jsonPath("$.admin").value(false))
				.andExpect(jsonPath("$.goodUser").value(false))
				.andExpect(jsonPath("$.goodAdmin").value(false));
		
		Assert.assertNotEquals("user123", userRepository.findById(2L).get().getPassword());
	}
	
	@Test
	public void testSignupLoggedIn() throws Exception {
		
		String adminToken = login("admin@example.com", "admin!");

		User user = new User("user1@example.com", "user123", "User 1");

		mvc.perform(post("/api/core/users")
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(LemonUtils.toJson(user)))
				.andExpect(status().is(403));
	}
	
	@Test
	public void testSignupDuplicateEmail() throws Exception {
		
		User user = new User("user1@example.com", "user123", "User 1");

		mvc.perform(post("/api/core/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(LemonUtils.toJson(user)))
				.andExpect(status().is(201));
		
		mvc.perform(post("/api/core/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(LemonUtils.toJson(user)))
				.andExpect(status().is(422));
	}
}
