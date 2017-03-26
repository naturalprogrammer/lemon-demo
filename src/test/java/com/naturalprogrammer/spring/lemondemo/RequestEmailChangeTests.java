package com.naturalprogrammer.spring.lemondemo;

import static com.jayway.restassured.RestAssured.given;
import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.hasErrors;
import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.restDocFilters;
import static org.hamcrest.Matchers.equalTo;

import javax.validation.ConstraintViolationException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser.Role;
import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;
import com.naturalprogrammer.spring.lemondemo.entities.User;
import com.naturalprogrammer.spring.lemondemo.repositories.UserRepository;


/**
 * Test cases for requesting email change
 * 
 * @author Sanjay Patel
 *
 */
public class RequestEmailChangeTests extends AbstractTests {
	
	@Autowired
	private UserRepository userRepository;	
	
    /**
     * A non-admin user should be able to request changing his email.
     */
	@Test
	public void canRequestEmailChange() {
		
    	// sign a user up
    	User user1 = SignupTests.newUser1();
		User signedUp = SignupTests.signupUser1(filters);
    	
		// make sure he is unverified
		Assert.assertTrue(signedUp.hasRole(Role.UNVERIFIED));
    	
    	// check that the newEmail and changeEmailCode of
		// the user are null
    	Assert.assertNull(signedUp.getNewEmail());
    	Assert.assertNull(signedUp.getChangeEmailCode());

    	// request email change
    	User updatedUser = buildUpdatedUser("new@example.com", user1.getPassword());
    	
    	given()
			.spec(restDocFilters(restDocs, "request-email-change"))
    		.spec(filters)
    		.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
    		.body(updatedUser)
    		.pathParam("id", signedUp.getId())
    	.post("/api/core/users/{id}/request-email-change")
        .then()
        	.statusCode(204);
    	
    	// ensure that newEmail and changeEmailCode are now properly set 
    	assertEmailChangeRequested(signedUp.getId(), updatedUser.getNewEmail());
	}

	

	/**
     * A good admin should be able to request changing email of another user.
     */
	@Test
	public void goodAdminCanRequestEmailChangeOfOther() {
		
		// Sign a user up
		User signedUp = SignupTests.signupUser1(filters);
		
		// Login as ADMIN
		BasicTests.adminLogin(filters);
		
		// build data to post
    	User updatedUser = buildUpdatedUser("new@example.com",
    			lemonProperties.getAdmin().getPassword());

    	// request email change
    	requestEmailChange(signedUp.getId(), updatedUser)
        .then()
        	.statusCode(204);
    	
    	// ensure that newEmail and changeEmailCode are now properly set
    	assertEmailChangeRequested(signedUp.getId(), updatedUser.getNewEmail());
	}
	
	
	/*
	 * Trying with unknown user id
	 */
	@Test
	public void unknownId() {
		
    	// build data for submitting
		User user1 = SignupTests.newUser1();
		User updatedUser = buildUpdatedUser("new@example.com", user1.getPassword());    	
		
		// try requesting with an unknown user id
    	BasicTests.ping(filters);
		requestEmailChange(7867L, updatedUser)
        .then()
        	.statusCode(422)
			.body("exception", equalTo(MultiErrorException.class.getName()))
			.body("errors", hasErrors("id",	"com.naturalprogrammer.spring.userNotFound"));
	}
	

	/**
	 * A non-admin should not be able to request changing
	 * the email id of another user
	 */
	@Test
	public void nonAdminTryingAnother() {
		
    	// Sign a user up
    	User user1 = SignupTests.newUser1();
		SignupTests.signupUser1(filters);
    	
		// try requesting the change for another user
		wronglyTryAnother(user1.getPassword());
	}
	
	
	/**
	 * A bad admin trying to change the email id
	 * of another user
	 */
	@Test
	public void badAdminTryingAnother() {
		
    	// Sign a user up
    	User user1 = SignupTests.newUser1();
		User signedUp = SignupTests.signupUser1(filters);
		
		// Make him a bad admin
		UpdateUserTests.makeUser1BadAdmin(filters, signedUp.getId());
		BasicTests.login(filters, user1.getEmail(), user1.getPassword());

		// try requesting the change for another user
		wronglyTryAnother(user1.getPassword());
	}
	
	
	/**
     * Trying with invalid data.
     */
	@Test
	public void tryingWithInvalidData() {
		
		// Login as ADMIN
		BasicTests.adminLogin(filters);
		
    	long adminId = userRepository.findByEmail(
        		lemonProperties.getAdmin().getUsername()).get().getId();

    	// try with null newEmail and password
    	requestEmailChange(adminId, buildUpdatedUser(null, null))
        .then()
        	.statusCode(422)
			.body("exception", equalTo(ConstraintViolationException.class.getName()))
			.body("errors", hasErrors(
					"updatedUser.newEmail",	"{com.naturalprogrammer.spring.blank.email}",
					"updatedUser.password", "{com.naturalprogrammer.spring.blank.password}"
			));
    	
    	// try with blank newEmail and password
    	requestEmailChange(adminId, buildUpdatedUser("", ""))
        .then()
        	.statusCode(422)
			.body("exception", equalTo(ConstraintViolationException.class.getName()))
			.body("errors", hasErrors(
					"updatedUser.newEmail",	"{com.naturalprogrammer.spring.blank.email}",
					"updatedUser.password", "{com.naturalprogrammer.spring.blank.password}"
			));
    	
    	// try with blank newEmail and password
    	requestEmailChange(adminId, buildUpdatedUser("an-invalid.email", "password"))
        .then()
        	.statusCode(422)
			.body("exception", equalTo(ConstraintViolationException.class.getName()))
			.body("errors", hasErrors(
					"updatedUser.newEmail",	"{com.naturalprogrammer.spring.invalid.email}"
			));

    	// try with wrong password
    	requestEmailChange(adminId, buildUpdatedUser("new@example.com", "wrong-password"))
        .then()
        	.statusCode(422)
			.body("exception", equalTo(MultiErrorException.class.getName()))
			.body("errors", hasErrors(
					"updatedUser.password",	"com.naturalprogrammer.spring.wrong.password"
			));

    	// try with non-unique email
    	requestEmailChange(adminId, buildUpdatedUser(
    		lemonProperties.getAdmin().getUsername(),
    		lemonProperties.getAdmin().getPassword()))
        .then()
        	.statusCode(422)
			.body("exception", equalTo(ConstraintViolationException.class.getName()))
			.body("errors", hasErrors(
					"updatedUser.newEmail",	"{com.naturalprogrammer.spring.duplicate.email}"
			));

	}

	
	/**
	 * Helper method for trying another without
	 * sufficient rights
	 * 
	 * @param password
	 */
	private void wronglyTryAnother(String password) {
		
		User updatedUser = buildUpdatedUser("new@example.com", password);    	

    	long adminId = userRepository.findByEmail(
    		lemonProperties.getAdmin().getUsername()).get().getId();
    	
    	// a user trying to request email change of Admin
    	requestEmailChange(adminId, updatedUser)
        .then()
			.statusCode(403)
			.body("exception", equalTo(AccessDeniedException.class.getName()));
    	
    	// check that newEmail and changeEmailCode fields aren't set
    	assertEmailChangeNotRequested(adminId);
	}



	/**
	 * Utility method to build data
	 * for posting newEmail
	 * 
	 * @param newEmail
	 * @param password
	 * @return
	 */
	public static User buildUpdatedUser(String newEmail, String password) {
		
    	User updatedUser = new User();
    	updatedUser.setNewEmail(newEmail);
    	updatedUser.setPassword(password);
    	
    	return updatedUser;
	}

	
	/**
	 * Helper method for requesting email change
	 * 
	 * @param userId
	 * @param updatedUser
	 * @return
	 */
	private Response requestEmailChange(Long userId, User updatedUser) {
		return requestEmailChange(filters, userId, updatedUser);
	}
	
	
	/**
	 * Utility method for requesting email change 
	 */
	public static Response requestEmailChange(RequestSpecification filters, Long userId, User updatedUser) {
		return given().spec(filters)
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.body(updatedUser)
	        	.pathParam("id", userId)
	          .post("/api/core/users/{id}/request-email-change");
	}

	/**
	 * Helper method for testing whether email change request
	 * set the newEmail and changeEmailCode 
	 * 
	 * @param id
	 */
	private void assertEmailChangeRequested(Long id, String newEmail) {
    	User user = userRepository.findOne(id);
    	Assert.assertEquals(newEmail, user.getNewEmail());
    	Assert.assertNotNull(user.getChangeEmailCode());
	}
	
	/**
	 * Helper method for ensuring that email change request
	 * failed 
	 * 
	 * @param id
	 */	
	private void assertEmailChangeNotRequested(long id) {
    	User user = userRepository.findOne(id);
    	Assert.assertNull(user.getNewEmail());
    	Assert.assertNull(user.getChangeEmailCode());
	}
}
