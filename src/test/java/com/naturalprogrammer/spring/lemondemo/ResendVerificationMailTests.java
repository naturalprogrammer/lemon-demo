package com.naturalprogrammer.spring.lemondemo;

import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.hasErrors;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.jayway.restassured.response.Response;
import com.naturalprogrammer.spring.lemondemo.entities.User;
import com.naturalprogrammer.spring.lemondemo.repositories.UserRepository;

/**
 * Test cases for verifying user
 * 
 * @author Sanjay Patel
 *
 */
public class ResendVerificationMailTests extends AbstractTests {

	@Autowired
	private UserRepository userRepository;

	/**
	 * A user can resend verification mail
	 */
	@Test
	public void canResendVerificationMail() {

		// Sign a user up
		User signedUp = SignupTests.signupUser1(filters);
		String oldVerificationCode = signedUp.getVerificationCode();

		resendVerificationMail(signedUp.getId())
		.then()
			.statusCode(200);

		signedUp = userRepository.findOne(signedUp.getId());

		Assert.assertEquals(oldVerificationCode, signedUp.getVerificationCode());

	}

	/**
	 * A good Admin can resend verification mail for another user
	 */
	@Test
	public void goodAdminCanResendVerificationMail() {

		// Sign a user up
		User signedUp = SignupTests.signupUser1(filters);

		BasicTests.adminLogin(filters);
		resendVerificationMail(signedUp.getId()).then().statusCode(200);
	}

	/**
	 * Providing unknown user id
	 */
	@Test
	public void unknownId() {

		BasicTests.ping(filters);
		BasicTests.adminLogin(filters);
		
		resendVerificationMail(9780)
		.then()
			.statusCode(400)
			.body("exception", equalTo("MultiErrorException"))
			.body("errors", hasErrors(
				"id", "com.naturalprogrammer.spring.userNotFound"
			));
	}

	/**
	 * Trying without logging in
	 */
	@Test
	public void unauthorizedResendVerificationMail() {

		long adminId = userRepository
				.findByEmail(lemonProperties.getAdmin().getUsername())
				.get().getId(); 
		
		unauthorizedResendVerificationMail(adminId);
	}

	/**
	 * Helper method for attempting re-sending verification mail without proper
	 * permission
	 * 
	 * @param userId
	 */
	private void unauthorizedResendVerificationMail(long userId) {
		
		// Update Admin
		resendVerificationMail(userId)
		.then().statusCode(403)
			.body("exception", equalTo("AccessDeniedException"));
	}

	/**
	 * Non-admin trying to resend verification mail for another user
	 */
	@Test
	public void resendVerificationMailAnotherUser() {

		// Sign a user up
		User user1 = SignupTests.signupUser1(filters);
		
		// Sign up as another User
		BasicTests.logout(filters);
		SignupTests.signup(filters, new User("user99@example.com", "user99", "User 99"));

		unauthorizedResendVerificationMail(user1.getId());
	}

	
	/**
	 * Bad admin trying to resend verification mail for another user
	 */
	@Test
	public void badAdminResendVerificationMailAnotherUser() {

		// Sign a user up
		User user1 = SignupTests.signupUser1(filters);
		
    	// Make user1 a bad-admin
    	UpdateUserTests.makeUser1BadAdmin(filters, user1.getId(), 0);
		BasicTests.logout(filters);
		
    	// Sign up another User
		SignupTests.signup(filters, new User("user99@example.com", "user99", "User 99"));
		User anotherUser = userRepository.findByEmail("user99@example.com").get();
		
    	// Login as User1
    	BasicTests.login(filters, user1.getEmail(), user1.getPassword());
    	BasicTests.ping(filters);

		unauthorizedResendVerificationMail(anotherUser.getId());
	}

	
	/**
	 * Trying while already verified
	 */
	@Test
	public void alreadyVerified() {

		BasicTests.ping(filters);
		BasicTests.adminLogin(filters);
		
		long adminId = userRepository
				.findByEmail(lemonProperties.getAdmin().getUsername())
				.get().getId(); 
				
		resendVerificationMail(adminId)
		.then()
			.statusCode(400)
			.body("exception", equalTo("MultiErrorException"))
			.body("errors", hasErrors(
				null, "com.naturalprogrammer.spring.alreadyVerified"
			));
	}
	
	
	/**
	 * Helper method for verifying
	 * 
	 * @param userId
	 * @return the Response
	 */
	private Response resendVerificationMail(long userId) {
		return given().spec(filters).pathParam("id", userId)
				.get("/api/core/users/{id}/resend-verification-mail");
	}

}
