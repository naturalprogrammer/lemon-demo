package com.naturalprogrammer.spring.lemondemo;

import static com.jayway.restassured.RestAssured.given;
import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.hasErrors;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.jayway.restassured.response.Response;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser.Role;
import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;
import com.naturalprogrammer.spring.lemondemo.entities.User;
import com.naturalprogrammer.spring.lemondemo.repositories.UserRepository;

/**
 * Test cases for changing email
 * 
 * @author Sanjay Patel
 *
 */
public class ChangeEmailTests extends AbstractTests {
	
	private static final String NEW_EMAIL = "new@example.com"; 
	
	@Autowired
	private UserRepository userRepository;
	
	
    /**
     * A non-admin user should be able to change his email.
     */
	@Test
	public void canChangeEmail() {
		
    	// request for email change of User 1
		User signedUp = requestEmailChangeUser1(); 
    			
    	// change email
		changeEmail(signedUp.getChangeEmailCode())
	    .then()
	    	.statusCode(200);
		
		// ensure that user is logged out
    	BasicTests.getContext(filters)
    	.then()
    		.body("user", equalTo(null));
		
    	// ensure that the email was indeed changed
		assertEmailChanged(signedUp.getId(), signedUp.getNewEmail());
	}
	
	
	/**
     * A good admin should not be able to change email of another user.
     */
	@Test
	public void goodAdminTryingEmailChangeOfOther() {
		
    	// signup and request email change for User 1
		User signedUp = requestEmailChangeUser1(); 
		
		// Login as ADMIN
		BasicTests.adminLogin(filters);

    	// try to change the email
		changeEmail(signedUp.getChangeEmailCode())
	    .then()
        	.statusCode(422)
			.body("exception", equalTo(MultiErrorException.class.getName()))
			.body("errors", hasErrors(
				null, "com.naturalprogrammer.spring.wrong.changeEmailCode"
			));
	}

	
    /**
     * Providing a wrong changeEmailCode shouldn't work.
     */
	@Test
	public void wrongChangeEmailCode() {
		
    	// signup and request email change for User 1
    	requestEmailChangeUser1(); 
    			
    	// try to change the email
    	changeEmail("wrong-change-email-code")
	    .then()
        	.statusCode(422)
			.body("exception", equalTo(MultiErrorException.class.getName()))
			.body("errors", hasErrors(
				null, "com.naturalprogrammer.spring.wrong.changeEmailCode"
			));
	}
	
	
	/**
     * Trying the operation without having requested first.
     */
	@Test
	public void tryingWithoutRequested() {
		
		// Login as ADMIN
		BasicTests.ping(filters);
		BasicTests.adminLogin(filters);

    	// Try changing email without first requesting
		changeEmail("some-random-code")
	    .then()
        	.statusCode(422)
			.body("exception", equalTo(MultiErrorException.class.getName()))
			.body("errors", hasErrors(
				null, "com.naturalprogrammer.spring.wrong.changeEmailCode"
			));
	}
	
	
    /**
     * Trying after some user registers the newEmail, leaving it non unique.
     */
	@Test
	public void nonUniqueEmail() {
		
    	// signup and request email change for User 1
    	User signedUp = requestEmailChangeUser1();
    	
    	// Some new user registers the newEmail
    	BasicTests.logout(filters);
    	SignupTests.signup(filters, new User(NEW_EMAIL, "password", "A new user"))
    	.then()
    		.statusCode(201);
    	
    	// User logs in again
    	BasicTests.login(filters, signedUp.getUsername(), SignupTests.newUser1().getPassword())
    	.then()
    		.statusCode(200)
    		.body("id", equalTo(signedUp.getId().intValue()));
    	
    	// try to change email
    	changeEmail(signedUp.getChangeEmailCode())
	    .then()
	    	.statusCode(422)
			.body("exception", equalTo(MultiErrorException.class.getName()))
			.body("errors", hasErrors(
				null, "com.naturalprogrammer.spring.duplicate.email"
			));
	}
	

	/**
	 * Helper for changing email 
	 */
	private Response changeEmail(String changeEmailCode) {
		
		return given().spec(filters)
				.pathParam("changeEmailCode", changeEmailCode)
			   .post("/api/core/users/{changeEmailCode}/change-email");
	}
	

	/**
	 * Helper method for sign up User 1 and
	 * request change email
	 */
	private User requestEmailChangeUser1() {
		
    	// Sign a user up
    	User user1 = SignupTests.newUser1();
		User signedUp = SignupTests.signupUser1(filters);
    	
    	// build data to submit
		User updatedUser = RequestEmailChangeTests
    		.buildUpdatedUser(NEW_EMAIL, user1.getPassword());    	

    	// request email change
		RequestEmailChangeTests
    	.requestEmailChange(filters, signedUp.getId(), updatedUser);
    	
    	// return the user from database
		return userRepository.findOne(signedUp.getId()); 
	}
	
	
	/**
	 * Helper method for testing whether email change request
	 * set the newEmail and changeEmailCode 
	 * 
	 * @param id
	 */
	private void assertEmailChanged(Long id, String newEmail) {
    	
		User user = userRepository.findOne(id);
    	
		// email has changed
		Assert.assertEquals(newEmail, user.getEmail());
    	
		// newEmail and changeEmailCode are set null
		Assert.assertNull(user.getNewEmail());
    	Assert.assertNull(user.getChangeEmailCode());
    	
    	// user is made verified
    	Assert.assertNull(user.getVerificationCode());
    	Assert.assertFalse(user.hasRole(Role.UNVERIFIED));
	}
}
