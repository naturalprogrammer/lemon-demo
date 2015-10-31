package com.naturalprogrammer.spring.lemondemo;

import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.hasErrors;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.jayway.restassured.response.Response;
import com.naturalprogrammer.spring.lemon.domain.ChangePasswordForm;
import com.naturalprogrammer.spring.lemondemo.entities.User;
import com.naturalprogrammer.spring.lemondemo.repositories.UserRepository;

/**
 * Test cases for changing password
 * 
 * @author Sanjay Patel
 *
 */
public class ChangePasswordTests extends AbstractTests {
	
	@Autowired
	private UserRepository userRepository;
	
	private User user1;
	private static final String NEW_PASSWORD = "new-password";
	
	private long user1Id;
	private long adminId;
	
    /**
     * Sign up and login User 1 first
     */
	@Before
    public void setUp() {
		
		user1 = SignupTests.newUser1();
		
		BasicTests.ping(filters);
    	SignupTests.signup(filters, user1);
    	
    	user1Id = userRepository
        		.findByEmail(user1.getEmail())
        		.get().getId();
    	
    	adminId = userRepository
        		.findByEmail(lemonProperties.getAdmin().getUsername())
        		.get().getId();  	
    }
    
	/**
	 * A non-admin user should be able to change his password.
	 */
	@Test
    public void canChangeSelfPassword() {
    	
		// change password
		changeUser1Password(new ChangePasswordForm(user1.getPassword(), NEW_PASSWORD, NEW_PASSWORD))
		.then()
			.statusCode(200);
    	
		// ensure he is logged out
    	BasicTests.getContext(filters)
    	.then()
    		.body("user", equalTo(null));
    	
		// Try logging in with new password
    	BasicTests.login(filters, user1.getEmail(), NEW_PASSWORD)
    	.then()
			.statusCode(200)
			.body("name", equalTo(user1.getName()));    	
    }
	
	/**
	 * An good admin user should be able to change the password of another user.
	 */
	@Test
    public void adminCanChangeOthersPassword() {
    	
    	BasicTests.adminLogin(filters);

		// Change password of others
		changeUser1Password(new ChangePasswordForm(user1.getPassword(), NEW_PASSWORD, NEW_PASSWORD))
		.then()
			.statusCode(200);
    	
    	BasicTests.login(filters, user1.getEmail(), NEW_PASSWORD)
    	.then()
			.statusCode(200);
    }
	
	/**
	 * Helper method for changing password of User 1
	 * 
	 * @param form
	 * @return
	 */
	private Response changeUser1Password(ChangePasswordForm form) {
		// Change password of self
    	return given().spec(filters)
    		.pathParam("id", user1Id)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(form)
		.post("/api/core/users/{id}/change-password");	
	}
	
	/**
	 * Providing an unknown id should throw exception.
	 */
	@Test
    public void unknownId() {
    	
		given().spec(filters)
    		.pathParam("id", user1Id + 986) // give an unknown id
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(new ChangePasswordForm(user1.getPassword(), NEW_PASSWORD, NEW_PASSWORD))
		.post("/api/core/users/{id}/change-password")	
		.then()
			.statusCode(400)
			.body("exception", equalTo("MultiErrorException"))
			.body("errors", hasErrors("id", "com.naturalprogrammer.spring.userNotFound"));
    }
	
	/**
	 * A non-admin user should not be able to change others' password.
	 */
	@Test
    public void tryToUpdateAnother() {
    	
		// Already logged in as User1.
		// Try updating ADMIN's password
    	given().spec(filters)
    		.pathParam("id", adminId)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(new ChangePasswordForm(user1.getPassword(), NEW_PASSWORD, NEW_PASSWORD))
		.post("/api/core/users/{id}/change-password")	
		.then()
			.statusCode(403)
			.body("exception", equalTo("AccessDeniedException"))
			.body("message", equalTo("Access is denied"));
    }
	
	/**
	 * A bad ADMIN trying to change the password of another user should throw exception
	 */
	@Test
    public void badAdminTryingToUpdateAnother() {
		
		UpdateUserTests.makeUser1BadAdmin(filters, user1Id, 0);
    	
    	// Login as User 1, which is now a bad ADMIN 
    	BasicTests.login(filters, user1.getEmail(), user1.getPassword());
    	BasicTests.ping(filters);

    	// Try to update another should not succeed
    	tryToUpdateAnother();
    	
    }
	
	/**
	 * Trying with invalid passwords
	 */
	@Test
	public void invalidPasswords() {
		
		// Null passwords
		changeUser1Password(new ChangePasswordForm(null, null, null))
		.then()
		.statusCode(400)
		.body("errors", hasErrors(
			"changePasswordForm.oldPassword", "{com.naturalprogrammer.spring.blank.password}",
			"changePasswordForm.password", "{com.naturalprogrammer.spring.blank.password}",
			"changePasswordForm.retypePassword", "{com.naturalprogrammer.spring.blank.password}"				
		));
		
		// Blank passwords
		changeUser1Password(new ChangePasswordForm("", "", ""))
		.then()
		.statusCode(400)
		.body("errors", hasErrors(
			"changePasswordForm.oldPassword", "{com.naturalprogrammer.spring.blank.password}",
			"changePasswordForm.password", "{com.naturalprogrammer.spring.blank.password}",
			"changePasswordForm.retypePassword", "{com.naturalprogrammer.spring.blank.password}"				
		));
		
		// Short passwords
		changeUser1Password(new ChangePasswordForm("short", "short", "short"))
		.then()
		.statusCode(400)
		.body("errors", hasErrors(
			"changePasswordForm.oldPassword", "{com.naturalprogrammer.spring.invalid.password.size}",
			"changePasswordForm.password", "{com.naturalprogrammer.spring.invalid.password.size}",
			"changePasswordForm.retypePassword", "{com.naturalprogrammer.spring.invalid.password.size}"				
		));
		
		String longPassword = StringUtils.repeat('x', 31);
				
		// Long passwords
		changeUser1Password(new ChangePasswordForm(longPassword, longPassword, longPassword))
		.then()
		.statusCode(400)
		.body("errors", hasErrors(
			"changePasswordForm.oldPassword", "{com.naturalprogrammer.spring.invalid.password.size}",
			"changePasswordForm.password", "{com.naturalprogrammer.spring.invalid.password.size}",
			"changePasswordForm.retypePassword", "{com.naturalprogrammer.spring.invalid.password.size}"				
		));
				
	}
	
	/**
	 * Trying with wrong old password
	 */
	@Test
	public void wrongOldPassword() {
		
		// Wrong old password
		changeUser1Password(new ChangePasswordForm("a-wrong-password", NEW_PASSWORD, NEW_PASSWORD))
		.then()
		.statusCode(400)
		.body("errors", hasErrors(
			"changePasswordForm.oldPassword", "com.naturalprogrammer.spring.wrong.password"
		));
				
	}

	/**
	 * Password and retypePassword not same
	 */
	@Test
	public void differentRetypePassword() {
		
		// Password and retypePassword not same
		changeUser1Password(new ChangePasswordForm(user1.getPassword(), NEW_PASSWORD, NEW_PASSWORD + "1"))
		.then()
		.statusCode(400)
		.body("errors", hasErrors(
			"changePasswordForm.retypePassword", "{com.naturalprogrammer.spring.different.passwords}"
		));
				
	}

}
