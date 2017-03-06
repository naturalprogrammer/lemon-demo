package com.naturalprogrammer.spring.lemondemo;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import java.io.FileNotFoundException;

import org.junit.Test;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;
import com.naturalprogrammer.spring.lemondemo.services.MyService;
import com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil;

/**
 * Tests Ping, Login, Logout etc.
 * 
 * @author Sanjay Patel
 *
 */
public class BasicTests extends AbstractTests {

    /**
     * Pinging should give you the CSRF cookie
     */
    @Test
	public void canPing() {
    	
    	// ping
    	ping(filters)
    	.then()
    		// CSRF cookie should be returned
    		.cookie(LemonSecurityConfig.XSRF_TOKEN_COOKIE_NAME);    	
	}
    
    /**
     * Ping utility
     */
    public static Response ping(RequestSpecification filters) {
    	return given().spec(filters)
    		.get("/api/core/ping");
    }
    
    /**
     * Getting the context returns properties meant for
     * the client side, and the current user.
     */
    @Test
	public void canGetContext() {
    	
    	// Get the context
    	getContext(filters)
	    .then()
	    	// body should have the ReCaptcha site key 
    		.body("context.reCaptchaSiteKey",
    			equalTo(lemonProperties.getRecaptcha().getSitekey()))
    		// and all the lemon.shared.* properties
	    	.body("context.shared.fooBar", equalTo("123..."))
	    	// logged in user should be null 
	    	.body("user", equalTo(null));
    	
	}
    
    /**
     * Utility to get the context
     */
    public static Response getContext(RequestSpecification filters) {
    	return given().spec(filters)
    		.get("/api/core/context");    	
    }
    
    /**
     * Getting the context after logging in.
      */
    @Test
	public void canGetContextAfterLogin() throws FileNotFoundException {
    	
    	// obtain the CSRF cookie
    	getContext(filters);
    	
    	// login as the first admin
    	adminLogin(filters);
    	
    	// get the context
    	getContext(filters)
    	.then()
    		.root("user")
    		
    		// name of the logged in user should be "Administrator"
	        .body("name", equalTo(MyService.ADMIN_NAME))
			
	        // check the email and username of the logged in user
			.body("email", equalTo(lemonProperties.getAdmin().getUsername()))    		
			.body("username", equalTo(lemonProperties.getAdmin().getUsername()))
			
			// check roles
			.body("roles", hasItem(AbstractUser.Role.ADMIN))
			.body("unverified", equalTo(false))    		
			.body("blocked", equalTo(false))    		
			.body("admin", equalTo(true))    		
			.body("goodUser", equalTo(true))    		
			.body("goodAdmin", equalTo(true))
			
			// shouldn't receive createdDate, lastModifiedDate, password, verificationCode and forgotPasswordCode
			.body("createdDate", equalTo(null))    		
			.body("lastModifiedDate", equalTo(null))    		
			.body("password", equalTo(null))    		
			.body("verificationCode", equalTo(null))    		
			.body("forgotPasswordCode", equalTo(null));
	}
    
    /**
     * Ensure that CSRF is enabled
     */
    @Test
    public void csrfEnabled() {
    	login(filters, "some-login-id", "some-password")
    	.then()
    		.statusCode(403)
    		.body("message", equalTo("Could not verify the provided CSRF token because your session was not found."));
    }
    
    /**
     * Logging in
     */
    @Test
	public void canLogin() {
    	
    	// obtain the CSRF cookie
    	getContext(filters); 
    	
    	// login as first admin
    	adminLogin(filters);
    }
    
    /**
     * Logging in with wrong credentials
     */
    @Test
	public void loginWithWrongCredentials() {
    	
    	// obtain the CSRF cookie
    	getContext(filters); 
    	
    	// login with wrong password
    	login(filters, "admin@example.com", "wrong-password")
	    .then()
	    	// should be a 401 Unauthorized
	    	.statusCode(401)
	    	.body("error", equalTo("Unauthorized"))
    		.body("message", equalTo("Authentication Failed: Bad credentials"));
    	 
    	// login with wrong email    	
    	login(filters, "wrong.email@example.com", "admin!")
	    .then()
	    	// should be a 401 Unauthorized
	    	.statusCode(401)
	    	.body("error", equalTo("Unauthorized"))
    		.body("message", equalTo("Authentication Failed: Bad credentials"));
	}
    
    
    /**
     * Utility for logging in the Admin user that was created
     * at application startup 
     */
    public static void adminLogin(RequestSpecification filters) {
    	
    	// Login
    	given().spec(filters)
    	    	.param("username", "admin@example.com")
    	    	.param("password", "admin!")
    	    .post("/login")
    	    .then()
    		// body should have name as "Administrator"
    		.body("name", equalTo(MyService.ADMIN_NAME))
    		// roles should include "ADMIN"
    		.body("roles", hasItem(AbstractUser.Role.ADMIN))
    		// should have been decorated
    		.body("goodAdmin", equalTo(true));
    }    
    
    /**
     * Utility for logging in the given user
     */
    public static Response login(RequestSpecification filters, String username, String password) {
    	return given().spec(filters)
    	    	.param("username", username)
    	    	.param("password", password)
    	    .post("/login");
    }
    
    /**
	 * The remember me feature
     */
    @Test
	public void canRememberMe() {
    	
    	// obtain the CSRF cookie
    	getContext(filters); 
    	
    	// login with remember-me
    	String rememberMeCookie =
    	given().spec(filters)
    	   	.param("username", "admin@example.com")
    	   	.param("password", "admin!")
    	   	.param(LemonSecurityConfig.REMEMBER_ME_PARAMETER, true)
    	.post("/login")
    	.then()
    		.cookie(LemonSecurityConfig.REMEMBER_ME_COOKIE)
    	// obtain the remember-me cookie
    	.extract().cookie(LemonSecurityConfig.REMEMBER_ME_COOKIE);
    	
    	// Now have a new session
    	filters = MyTestUtil.configureFilters();

    	// Without the cookie, the user isn't logged in
    	getContext(filters)
		.then()
			.body("user", equalTo(null));

    	// With the cookie. the user is automatically logged in
    	given()
    		.spec(filters)
    		.cookie(LemonSecurityConfig.REMEMBER_ME_COOKIE, rememberMeCookie)
		.get("/api/core/context")
		.then()
			.body("user.name", equalTo(MyService.ADMIN_NAME));
    	
    	// CSRF token would have changed because a login
    	// took place in the previous call.
    	// So, let's ping
    	given().spec(filters)
    		.cookie(LemonSecurityConfig.REMEMBER_ME_COOKIE, rememberMeCookie)
    		.get("/api/core/ping");
    	
    	// Then, log the user out
    	// and see that the cookie is reset
    	given().spec(filters)
    	.cookie(LemonSecurityConfig.REMEMBER_ME_COOKIE, rememberMeCookie)
		.post("/logout")
		.then()
			.statusCode(200)
			.cookie(LemonSecurityConfig.REMEMBER_ME_COOKIE, equalTo(""));
	}
    
    
    /**
     * Try remember me with wrong token
     */
    @Test
	public void wrongRememberMeToken() {
    	
    	// obtain the CSRF cookie
    	getContext(filters); 
    	
    	given()
    		.spec(filters)
    		.cookie(LemonSecurityConfig.REMEMBER_ME_COOKIE, "A wrong remember-me token")
		.get("/api/core/context")
		.then()
			.body("user", equalTo(null));
	}

    
    /**
     * POST /logout
     * 	should log a user out
     */
    @Test
	public void canLogout() {
    	
    	// Obtain the CSRF cookie
    	getContext(filters);
    	
    	// Login first
    	adminLogin(filters);
    	
    	// Doubly ensure that login worked
    	getContext(filters)
	    .then()
	    	.body("user.name", equalTo(MyService.ADMIN_NAME));

    	// Now logout
    	logout(filters);
	}
    
    /**
     * Logout utility.
     * 
     * @param filters
     */
    public static void logout(RequestSpecification filters) {
    	
    	given().spec(filters)
		.post("/logout")
		.then()
			.statusCode(200);
    	
    	// Doubly ensure
    	getContext(filters)
	    .then()
	    	.body("user", equalTo(null));    	
    }
    
}
