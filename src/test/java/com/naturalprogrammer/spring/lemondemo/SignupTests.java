package com.naturalprogrammer.spring.lemondemo;

import static com.jayway.restassured.RestAssured.given;
import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.hasErrors;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.http.MediaType;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;
import com.naturalprogrammer.spring.lemondemo.entities.User;
import com.naturalprogrammer.spring.lemondemo.repositories.UserRepository;

/**
 * Test cases for Sign up
 * 
 * @author Sanjay Patel
 *
 */
public class SignupTests extends AbstractTests {

	/**
	 * Utility class for creating a new User
	 * 
	 * @return User 1
	 */
	public static final User newUser1() {
		
		return new User("user1@example.com", "user1!", "User 1");
	}

	/**
	 * Utility for sign up 
	 */
    public static Response signup(RequestSpecification filters, User user) {
		return given().spec(filters)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(user)
		.post("/api/core/signup");
	}
    
    /**
     * Utility for signing up User 1
     */
    public static User signupUser1(RequestSpecification filters) {
		
		User user1 = newUser1();
		
    	BasicTests.ping(filters);
    	signup(filters, user1);
    	
    	return LemonUtil
    			.getBean(UserRepository.class)
    			.findByEmail(user1.getEmail()).get();
    }

    
    /**
     * Signing up
     */
    @Test
	public void canSignup() {
    	
    	// Obtain CSRF cookie
    	BasicTests.getContext(filters);
    	
    	User user1 = newUser1();
    	
    	// Sign up User 1
    	signup(filters, user1)
     	.then()
    		// name of the logged in user should now be "User 1"
    		.body("name", equalTo(user1.getName()))
    		// check the email of the logged in user
    		.body("email", equalTo(user1.getEmail()))    		
    		// roles should include "UNVERIFIED"
    		.body("roles", hasItem(AbstractUser.Role.UNVERIFIED));    	
    	
    	// Re-check the current user as User 1 with UNVERIFIED role
    	BasicTests.getContext(filters)
    	.then()
    		// check for correct name
    		.body("user.name", equalTo(user1.getName()))
    		// roles for correct roles
    		.body("user.roles", hasItem(AbstractUser.Role.UNVERIFIED));    	
    	
    	// Logout
    	BasicTests.logout(filters);
    	
    	// Login as User 1
    	BasicTests.login(filters, user1.getEmail(), user1.getPassword())
    	.then()
			// User id should be greater than 0
			.body("id", greaterThan(0))
			// email should match
			.body("email", equalTo(user1.getEmail()))
			// name should match
			.body("name", equalTo(user1.getName()))
			// roles should include "UNVERIFIED"
			.body("roles", hasItem(AbstractUser.Role.UNVERIFIED));
	}
    
    /**
     * Sign up with duplicate email
     */
    @Test
	public void signupDuplicateEmail() {
    	
    	// Obtain CSRF cookie
    	BasicTests.getContext(filters);
    	
    	User user1 = newUser1();

    	// Sign up User 1
    	signup(filters, user1);
    	
    	// Can't signup when logged in
    	BasicTests.logout(filters);
    	
    	// Try signing up with the same email id
    	signup(filters, user1)
    	.then()
    		.statusCode(400)
    		.body("exception", equalTo("ConstraintViolationException"))
    		.body("errors", hasErrors("user.email",
    				"{com.naturalprogrammer.spring.duplicate.email}"));
    }
    
	/**
	 * Signup with null data
	 */
    @Test
	public void signupBlank() {
    	
    	// Obtain CSRF cookie
    	BasicTests.getContext(filters);
    	
    	// Try signing up with null data
    	signup(filters, new User())
    	.then()
    		.statusCode(400)
    		.body("exception", equalTo("ConstraintViolationException"))
    		.body("errors", hasErrors(
    			"user.email", "{com.naturalprogrammer.spring.blank.email}",    					
    			"user.name", "{blank.name}",
    			"user.password", "{com.naturalprogrammer.spring.blank.password}"));
    }
    

	/**
	 * Signup with short data
	 */
    @Test
	public void signupShort() {
    	
    	// Obtain CSRF cookie
    	BasicTests.getContext(filters);
    	
    	User user = new User("x", "x", "");
    	
    	// Try signing up with null data
    	signup(filters, user)
    	.then()
    		.statusCode(400)
    		.body("exception", equalTo("ConstraintViolationException"))
    		.body("errors", hasErrors(
    			"user.email", "{com.naturalprogrammer.spring.invalid.email}",    					    				
    			"user.email", "{com.naturalprogrammer.spring.invalid.email.size}",    					    				
    			"user.name", "{blank.name}",
    			"user.password", "{com.naturalprogrammer.spring.invalid.password.size}"));
    }
    
	/**
	 * Signup with long data
	 */	
    @Test
	public void signupLong() {
    	
    	// Obtain CSRF cookie
    	BasicTests.getContext(filters);
    	
    	String longString = StringUtils.repeat("x", 250);
    	
    	User user = new User(longString + "@example.com", longString, longString);
    	
    	// Try signing up with null data
    	signup(filters, user)
    	.then()
    		.statusCode(400)
    		.body("exception", equalTo("ConstraintViolationException"))
    		.body("errors", hasErrors(
    			"user.email", "{com.naturalprogrammer.spring.invalid.email.size}",    					    				
    			"user.name", "{javax.validation.constraints.Size.message}", 					    				
    			"user.password", "{com.naturalprogrammer.spring.invalid.password.size}"));
    }

    /**
     * A already logged in user should not be able to sign up
     */
    @Test
    public void signupWhileLoggedIn() {

    	// Obtain CSRF cookie
    	BasicTests.getContext(filters);

    	// Login as admin
    	BasicTests.adminLogin(filters);

    	// Try signing up as User 1
    	signup(filters, newUser1())
     	.then()
     		.statusCode(403)
    		// name of the logged in user should now be "User 1"
    		.body("exception", equalTo("AccessDeniedException"))
    		.body("message", equalTo("Access is denied"));    	
    }
    
}
