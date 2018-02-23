package com.naturalprogrammer.spring.lemondemo;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemondemo.entities.User;

@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
public class SignupMvcTests extends AbstractMvcTests {
		
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
				.andExpect(status().is(403))
				.andExpect(jsonPath("$.errors[*].field").value(allOf(hasSize(4),
					hasItems("user.email", "user.password", "user.name"))));
	}
}
