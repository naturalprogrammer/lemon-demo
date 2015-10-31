package com.naturalprogrammer.spring.lemondemo;

import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.hasErrors;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.naturalprogrammer.spring.lemondemo.entities.User;
import com.naturalprogrammer.spring.lemondemo.repositories.UserRepository;

/**
 * Tests for forgot password
 * 
 * @author Sanjay Patel
 *
 */
public class ForgotPasswordTests extends AbstractTests {
	
	@Autowired
	private UserRepository userRepository;
	
    /**
     * For simplicity, we have tested all the business rules
     * in this single test case.
     */
	@Test
    public void canForgotPassword() {
    	
    	User user1 = SignupTests.newUser1();
    			
    	// Obtain CSRF cookie
    	BasicTests.getContext(filters);
    	
    	// Sign up User 1
    	SignupTests.signup(filters, user1);
    	
    	// Logout
    	BasicTests.logout(filters);

    	// Forgot password with wrong email
    	forgotPassword("wrong.email@example.com")
		.then()
			.statusCode(400)
    		.body("exception", equalTo("MultiErrorException"))
    		.body("errors", hasErrors(null, "com.naturalprogrammer.spring.userNotFound"));

    	// Forgot password
    	forgotPassword(user1.getEmail())
    	.then()
			.statusCode(200);   	
    	
    	String forgotPasswordCode = userRepository
    		.findByEmail(user1.getEmail())
    		.get().getForgotPasswordCode();

    	String newPassword = "a-new-password";
    	
    	// Try resetting with a wrong forgot password code
    	resetPassword("wrong-code", newPassword)
		.then()
			.statusCode(400)   	
			.body("exception", equalTo("MultiErrorException"))
			.body("errors", hasErrors(null, "com.naturalprogrammer.spring.invalidLink"));		
    	
    	// Try resetting with a blank password
    	resetPassword(forgotPasswordCode, "")
		.then()
			.statusCode(400)
			.body("exception", equalTo("ConstraintViolationException"))
			.body("errors", hasErrors("newPassword", "{com.naturalprogrammer.spring.blank.password}"));

		// Try resetting with a short password
    	resetPassword(forgotPasswordCode, "short")
		.then()
			.statusCode(400)
			.body("exception", equalTo("ConstraintViolationException"))
			.body("errors", hasErrors("newPassword", "{com.naturalprogrammer.spring.invalid.password.size}"));

		// Try resetting with a long password
    	resetPassword(forgotPasswordCode, StringUtils.repeat('x', 31))
		.then()
			.statusCode(400)
			.body("exception", equalTo("ConstraintViolationException"))
			.body("errors", hasErrors("newPassword", "{com.naturalprogrammer.spring.invalid.password.size}"));
		
    	// Try resetting with a proper password
    	resetPassword(forgotPasswordCode, newPassword)
		.then()
			.statusCode(200);
    	
    	// Try resetting again
    	resetPassword(forgotPasswordCode, newPassword)
		.then()
			.statusCode(400)   	
			.body("exception", equalTo("MultiErrorException"))
			.body("errors", hasErrors(null,	"com.naturalprogrammer.spring.invalidLink"));		
    	
    	// Try logging in with the new password
    	BasicTests.login(filters, user1.getEmail(), newPassword)
    	.then()
    		.body("email", equalTo(user1.getEmail()));
    }

	
	/**
	 * Helper for forgot-password
	 */
	private Response forgotPassword(String email) {

		return forgotPassword(filters, email);
	}

	
	/**
	 * Utility for calling forgot-password
	 * 
	 * @param filters
	 * @param email
	 */
	public static Response forgotPassword(RequestSpecification filters, String email) {
    	return given().spec(filters)
    				.param("email", email)
    		   .post("/api/core/forgot-password");	
	}
	
	
	/**
	 * Helper for reset-password
	 */
	private Response resetPassword(String forgotPasswordCode, String newPassword) {
    	return given().spec(filters)
				.pathParam("forgotPasswordCode", forgotPasswordCode)
				.param("newPassword", newPassword)
			  .post("/api/core/users/{forgotPasswordCode}/reset-password");	
	}
}
