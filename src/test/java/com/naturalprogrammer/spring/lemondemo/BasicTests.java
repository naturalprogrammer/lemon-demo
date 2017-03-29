package com.naturalprogrammer.spring.lemondemo;

import static com.jayway.restassured.RestAssured.given;
import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.restDocFilters;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;

import org.junit.Test;
import org.springframework.http.MediaType;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.naturalprogrammer.spring.lemon.LemonAutoConfiguration;
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
    	given()
			.spec(filters)
			.spec(restDocFilters(restDocs, "ping"))
		.get("/api/core/ping")
		.then()
    		// CSRF cookie should be returned
    		.statusCode(204)
    		.cookie(LemonSecurityConfig.XSRF_TOKEN_COOKIE_NAME, not(isEmptyOrNullString()));    	
	}
   
    
    /**
     * Pinging the session should give you the JSESSIONID
     */
    @Test
	public void canPingSession() {
    	
    	// ping
    	given()
			.spec(filters)
			.spec(restDocFilters(restDocs, "ping"))
		.get("/api/core/ping-session")
		.then()
    		// CSRF cookie should be returned
    		.statusCode(204)
    		.cookies("JSESSIONID", not(isEmptyOrNullString()),
    			LemonSecurityConfig.XSRF_TOKEN_COOKIE_NAME, not(isEmptyOrNullString()));    	
	}
   
    
    /**
     * Ping and create session if not there
     */
    public static Response pingSession(RequestSpecification filters) {
    	return given()
    			.spec(filters)
    			.get("/api/core/ping-session");
    }
    
    
    /**
     * Ping to get the CSRF cookie
     */
    public static Response ping(RequestSpecification filters) {
    	return given()
    			.spec(filters)
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
	    	.body("$", not(hasKey("user"))); 	
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
	public void canGetContextAfterLogin() {
    	
    	// login as the first admin
    	adminLogin(filters);
    	
    	// Get the context
    	given()
    		.spec(restDocFilters(restDocs, "context", relaxedResponseFields( 
    				fieldWithPath("context").description("Context object containing attributes like _reCaptchaSiteKey_"),
    				fieldWithPath("context.shared").description("All the _lemon.shared.\\*_ properties that are defined in _application\\*.yml_"),    				
    				fieldWithPath("user").description("Logged-in user details"))))
    		.spec(filters)
    		.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
    	.get("/api/core/context")
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
			
			// shouldn't receive createdDate, lastModifiedDate, password, verificationCode, forgotPasswordCode, apiKey
			.body("$", not(hasKey("createdDate")))    		
			.body("$", not(hasKey("lastModifiedDate")))    		
			.body("$", not(hasKey("password")))    		
			.body("$", not(hasKey("verificationCode")))    		
			.body("$", not(hasKey("forgotPasswordCode")))
			.body("$", not(hasKey("apiKey")));
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
    	
    	// login as first admin
    	adminLogin(filters)
    	.then()
			// body should have name as "Administrator"
			.body("name", equalTo(MyService.ADMIN_NAME))
			// roles should include "ADMIN"
			.body("roles", hasItem(AbstractUser.Role.ADMIN))
			// should have been decorated
			.body("goodAdmin", equalTo(true));
    	
    	
    }

    
    /**
     * Logging in with wrong credentials
     */
    @Test
	public void loginWithWrongCredentials() {
    	
    	// obtain the CSRF cookie
    	pingSession(filters); 
    	
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
     * Logging in without creating session
     */
    @Test
	public void loginWithoutCreatingSession() {
    	
    	// obtain the CSRF cookie
    	ping(filters); 
    	
    	// login should succeed
    	login(filters, "admin@example.com", "admin!")
    	.then()
			// body should have name as "Administrator"
			.body("name", equalTo(MyService.ADMIN_NAME))
			// roles should include "ADMIN"
			.body("roles", hasItem(AbstractUser.Role.ADMIN))
			// should have been decorated
			.body("goodAdmin", equalTo(true));
    	
    	// But getting the context shouldn't identify the user
    	getContext(filters)
	    .then()
	    	// logged in user should be null 
	    	.body("$", not(hasKey("user"))); 	    	
	}

    
    /**
     * Utility for logging in the Admin user that was created
     * at application startup 
     * @return 
     */
    public static Response adminLogin(RequestSpecification filters) {
    	
    	pingSession(filters);
    	
    	// Login
    	return given().spec(filters)
    	    	.param("username", "admin@example.com")
    	    	.param("password", "admin!")
    	    .post("/login");
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
    	pingSession(filters); 
    	
    	// login with remember-me
    	String rememberMeCookie =
    	given()
			.spec(restDocFilters(restDocs, "login", requestParameters( 
					parameterWithName("username").description("The login id"), 
					parameterWithName("password").description("Password"), 
					parameterWithName(LemonAutoConfiguration.REMEMBER_ME_PARAMETER)
						.description("Whether to remember the login even after session expires"))))
			.spec(filters)
    	   	.param("username", "admin@example.com")
    	   	.param("password", "admin!")
    	   	.param(LemonAutoConfiguration.REMEMBER_ME_PARAMETER, true)
    	.post("/login")
    	.then()
    		.cookie(LemonAutoConfiguration.REMEMBER_ME_COOKIE)
    	// obtain the remember-me cookie
    	.extract().cookie(LemonAutoConfiguration.REMEMBER_ME_COOKIE);
    	
    	// Now have a new session
    	filters = MyTestUtil.configureFilters();

    	// Without the cookie, the user isn't logged in
    	getContext(filters)
		.then()
			.body("$", not(hasKey("user")));
    	
    	// With the cookie. the user is automatically logged in
    	given()
    		.spec(filters)
    		.cookie(LemonAutoConfiguration.REMEMBER_ME_COOKIE, rememberMeCookie)
		.get("/api/core/context")
		.then()
			.body("user.name", equalTo(MyService.ADMIN_NAME));
    	
    	// CSRF token would have changed because a login
    	// took place in the previous call.
    	// So, let's ping
    	given().spec(filters)
    		.cookie(LemonAutoConfiguration.REMEMBER_ME_COOKIE, rememberMeCookie)
    		.get("/api/core/ping");
    	
    	// Then, log the user out
    	// and see that the cookie is reset
    	given().spec(filters)
    	.cookie(LemonAutoConfiguration.REMEMBER_ME_COOKIE, rememberMeCookie)
		.post("/logout")
		.then()
			.statusCode(200)
			.cookie(LemonAutoConfiguration.REMEMBER_ME_COOKIE, equalTo(""));
	}
    
    
    /**
     * Try remember me with wrong token
     */
    @Test
	public void wrongRememberMeToken() {
    	
    	// obtain the CSRF cookie
    	pingSession(filters); 
    	
    	given()
    		.spec(filters)
    		.cookie(LemonAutoConfiguration.REMEMBER_ME_COOKIE, "A wrong remember-me token")
		.get("/api/core/context")
		.then()
			.body("$", not(hasKey("user")));
	}

    
    /**
     * POST /logout
     * 	should log a user out
     */
    @Test
	public void canLogout() {
    	
    	// Login first
    	adminLogin(filters);
    	
    	// Doubly ensure that login worked
    	getContext(filters)
	    .then()
	    	.body("user.name", equalTo(MyService.ADMIN_NAME));

    	// Now logout
    	given()
    		.spec(restDocFilters(restDocs, "logout"))
    		.spec(filters)    		
		.post("/logout")
		.then()
			.statusCode(200);
    	
    	// Doubly ensure
    	getContext(filters)
	    .then()
			.body("$", not(hasKey("user")));
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
    }
    
}
