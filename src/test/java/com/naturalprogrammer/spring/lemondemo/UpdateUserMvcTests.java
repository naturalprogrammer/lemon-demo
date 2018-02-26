package com.naturalprogrammer.spring.lemondemo;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser.Role;
import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemondemo.entities.User;

@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
public class UpdateUserMvcTests extends AbstractMvcTests {
	
	private static final String UPDATED_NAME = "Edited name";
	
    private String userPatch1;
    private String userPatchRevokeAdmin;
    private String userPatchNullName;
    private String userPatchLongName;
	
	@Value("classpath:/update-user/patch-1.json")
	public void setUserPatch1(Resource patch) throws IOException {
		this.userPatch1 = LemonUtils.toString(patch);
	}
	
	@Value("classpath:/update-user/patch-revoke-admin.json")
	public void setUserPatchRevokeAdmin(Resource patch) throws IOException {
		this.userPatchRevokeAdmin = LemonUtils.toString(patch);;
	}

	@Value("classpath:/update-user/patch-null-name.json")
	public void setUserPatchNullName(Resource patch) throws IOException {
		this.userPatchNullName = LemonUtils.toString(patch);;
	}

	@Value("classpath:/update-user/patch-long-name.json")
	public void setUserPatchLongName(Resource patch) throws IOException {
		this.userPatchLongName = LemonUtils.toString(patch);;
	}

	/**
	 * A non-admin user should be able to update his own name,
	 * but changes in roles should be skipped.
	 * The name of security principal object should also
	 * change in the process.
	 * @throws Exception 
	 */
	@Test
    public void testUpdateSelf() throws Exception {
		
		mvc.perform(patch("/api/core/users/{id}", UNVERIFIED_USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, tokens.get(UNVERIFIED_USER_ID))
				.content(userPatch1))
				.andExpect(status().is(200))
				.andExpect(header().string(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME, containsString(".")))
				.andExpect(jsonPath("$.tag.name").value(UPDATED_NAME))
				.andExpect(jsonPath("$.roles").value(hasSize(1)))
				.andExpect(jsonPath("$.roles[0]").value("UNVERIFIED"))
				.andExpect(jsonPath("$.username").value(UNVERIFIED_USER_EMAIL))
				.andExpect(jsonPath("$.unverified").value(true))
				.andExpect(jsonPath("$.admin").value(false));
		
		User user = userRepository.findById(UNVERIFIED_USER_ID).get();
		
		// Ensure that data changed properly
		Assert.assertEquals(UNVERIFIED_USER_EMAIL, user.getEmail());
		Assert.assertEquals(1, user.getRoles().size());
		Assert.assertTrue(user.getRoles().contains(Role.UNVERIFIED));
		Assert.assertEquals(2L, user.getVersion().longValue());
    }

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
	public void testUpdateUser() throws Exception {
		
		User user = new User("user.foo@example.com", "user123", "User Foo");

		mvc.perform(patch("/api/core/users/{id}", UNVERIFIED_USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(LemonUtils.toJson(user)))
				.andExpect(status().is(201))
				.andExpect(header().string(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME, containsString(".")))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.nonce").doesNotExist())
				.andExpect(jsonPath("$.username").value("user.foo@example.com"))
				.andExpect(jsonPath("$.roles").value(hasSize(1)))
				.andExpect(jsonPath("$.roles[0]").value("UNVERIFIED"))
				.andExpect(jsonPath("$.tag.name").value("User Foo"))
				.andExpect(jsonPath("$.unverified").value(true))
				.andExpect(jsonPath("$.blocked").value(false))
				.andExpect(jsonPath("$.admin").value(false))
				.andExpect(jsonPath("$.goodUser").value(false))
				.andExpect(jsonPath("$.goodAdmin").value(false));
		
		// Ensure that password got encrypted
		Assert.assertNotEquals("user123", userRepository.findByEmail("user.foo@example.com").get().getPassword());
	}
	
//	@Test
//	public void testSignupLoggedIn() throws Exception {
//		
//		String adminToken = login("admin@example.com", "admin!");
//
//		User user = new User("user1@example.com", "user123", "User 1");
//
//		mvc.perform(post("/api/core/users")
//				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER, adminToken)
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(LemonUtils.toJson(user)))
//				.andExpect(status().is(403));
//	}
//	
	@Test
	public void testSignupDuplicateEmail() throws Exception {
		
		User user = new User("user@example.com", "user123", "User");

		mvc.perform(post("/api/core/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(LemonUtils.toJson(user)))
				.andExpect(status().is(422));
	}
}
