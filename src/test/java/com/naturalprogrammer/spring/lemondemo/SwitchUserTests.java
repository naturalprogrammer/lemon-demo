package com.naturalprogrammer.spring.lemondemo;

import static com.jayway.restassured.RestAssured.given;
import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.restDocFilters;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;

import org.junit.Test;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.naturalprogrammer.spring.lemondemo.entities.User;
import com.naturalprogrammer.spring.lemondemo.services.MyService;

/**
 * Test cases for switching user
 */
public class SwitchUserTests extends AbstractTests {
	
    @Test
    public void canSwitchUser() {
    	
    	User user1 = SignupTests.signupUser1(filters); 
    	
    	// login as an admin
    	BasicTests.adminLogin(filters);
    	
    	// Switch to the new user
    	given()
    		.spec(restDocFilters(restDocs, "switch-user", requestParameters( 
				parameterWithName("username").description("The login id of the user to switch to"))))
    		.spec(filters)
    		.param("username", user1.getEmail())
    	.post("/login/impersonate")
		.then()
			// name of the logged in user should now be "User 1"
	    	.body("name", equalTo(user1.getName()));
    	
    	// Doubly ensure that it worked
    	BasicTests.getContext(filters)
    	.then()
    		// name of the logged in user should be "User 1"
    		.body("user.name", equalTo(user1.getName()));
    	
    	// switch back
    	given()
			.spec(restDocFilters(restDocs, "switch-back"))
    		.spec(filters)
		.post("/logout/impersonate")
		.then()
			// name of the logged in user should now be "Administrator"
	    	.body("name", equalTo(MyService.ADMIN_NAME));
	
    	// Doubly ensure that it worked
    	BasicTests.getContext(filters)
    	.then()
    		// name of the logged in user should be "User 1"
    		.body("user.name", equalTo(MyService.ADMIN_NAME));
    }
    
    
    /**
     * non-admins (User1) can't switch
     */
    @Test
    public void nonAdminSwitch() {
    	
    	SignupTests.signupUser1(filters); 
    	tryUnauthorizedSwitch();
    	
    }
    
    
    /**
     * Unauthenticated users can't switch.
     */
    @Test
    public void tryUnauthorizedSwitch() {
    	
    	switchUser(filters, lemonProperties.getAdmin().getUsername())
    	.then()
	    	.statusCode(403) // should be a 403 Forbidden
	    	.body("error", equalTo("Forbidden"));
    }
    
    /**
     * Bad ADMINs can't switch 
     */
    @Test
    public void badAdminsCantSwitch() {
    	
    	User user1 = SignupTests.signupUser1(filters); 
    			
    	// Make user1 a bad-admin
    	UpdateUserTests.makeUser1BadAdmin(filters, user1.getId());
    	
    	// Login as User1
    	BasicTests.login(filters, user1.getEmail(), user1.getPassword());
    	
    	// try switching to another user
    	switchUser(filters, lemonProperties.getAdmin().getUsername())
    	.then()
	    	// should be a 401 Unauthorized
	    	.statusCode(403)
	    	.body("error", equalTo("Forbidden"));

    }


	/**
	 * Utility for switching user
	 */
    public static Response switchUser(RequestSpecification filters, String email) {
    	return given().spec(filters)
    			.param("username", email)
    			.post("/login/impersonate");
	}

}
