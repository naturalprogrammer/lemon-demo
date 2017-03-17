package com.naturalprogrammer.spring.lemondemo;

import static com.jayway.restassured.RestAssured.given;
import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.hasErrors;
import static org.hamcrest.Matchers.equalTo;

import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;
import com.naturalprogrammer.spring.lemondemo.entities.User;
import com.naturalprogrammer.spring.lemondemo.repositories.UserRepository;
import com.naturalprogrammer.spring.lemondemo.services.MyService;

/**
 * Tests for fetchUsersByEmail and fetchUsersById 
 * 
 * @author Sanjay Patel
 *
 */
public class FetchUserTests extends AbstractTests {
	
	@Autowired
	private UserRepository userRepository;
	
	
	/**
	 * A user should be able to fetch his data, including the confidential
	 * email and username. But other confidential fields would be null.
	 */
	@Test
	public void canFetchSelf() {
		
    	// Sign up user 1
		User user1 = signupUser1AndForgetPassword();
    	
    	// Login
    	BasicTests.login(filters, user1.getEmail(), user1.getPassword());
    	
    	// Fetch and test
    	fetchByEmail(user1.getEmail())
		.then()
			.statusCode(200)
			.body("name", equalTo(user1.getName()))
			
			// email and username are visible to self
			.body("email", equalTo(user1.getEmail()))
			.body("username", equalTo(user1.getEmail()))
			
			// other confidential fields are null
			.body("createdDate", equalTo(null))
			.body("lastModifiedDate", equalTo(null))
			.body("password", equalTo(null))
			.body("verificationCode", equalTo(null))
			.body("forgotPasswordCode", equalTo(null));
	}

	
	/**
	 * A good ADMIN should be able to fetch another user's data,
	 * including the confidential email and username.
	 * But other confidential fields would be null
	 */
	@Test
	public void goodAdminFetchOthers() {
		
    	// sign up User 1
		User user1 = signupUser1AndForgetPassword();
    	
    	// Login as Admin
    	BasicTests.adminLogin(filters);
    	
    	// Fetch and test
    	fetchByEmail(user1.getEmail())
		.then()
			.statusCode(200)
			.body("name", equalTo(user1.getName()))
			
			// email and username are visible to a good Admin
			.body("email", equalTo(user1.getEmail()))
			.body("username", equalTo(user1.getEmail()))
			
			// other confidential fields are null
			.body("createdDate", equalTo(null))
			.body("lastModifiedDate", equalTo(null))
			.body("password", equalTo(null))
			.body("verificationCode", equalTo(null))
			.body("forgotPasswordCode", equalTo(null));
	}

	
	/**
	 * A bad ADMIN should be able to fetch another user's data.
	 * But all the confidential fields would be null.
	 */
	@Test
	public void badAdminFetchOther() {
		
		// Sign up User 1
		User user1 = signupUser1();
		
		// Fetch its Id from database
		long user1Id = userRepository.findByEmail(user1.getEmail()).get().getId();
		
		// Make User1 a bad Admin
		UpdateUserTests.makeUser1BadAdmin(filters, user1Id, 0);
		
		// Log User1 in
		BasicTests.login(filters, user1.getEmail(), user1.getPassword());
    	
    	// Fetch another user and test that all confidential fields are null
    	fetchOther();
	}

	
	/**
	 * A non ADMIN should be able to fetch another user's data.
	 * But all the confidential fields would be null.
	 */
	@Test
	public void nonAdminFetchOther() {
		
		// Sign up User 1
		User user1 = signupUser1AndForgetPassword();
		
		// Log User1 in
		BasicTests.login(filters, user1.getEmail(), user1.getPassword());
    	
    	// Fetch another user and test that all confidential fields are null
    	fetchOther();
	}

	
	/**
	 * An anonymous user should be able to fetch another user's data.
	 * But all the confidential fields would be null.
	 */
	@Test
	public void fetchOther() {
		
		// Fetch other's data
		fetchByEmail(lemonProperties.getAdmin().getUsername())
		.then()
			.statusCode(200)
			.body("name", equalTo(MyService.ADMIN_NAME))
			
			// All confidential fields should be null
			.body("email", equalTo(null))
			.body("username", equalTo(null))
			.body("createdDate", equalTo(null))
			.body("lastModifiedDate", equalTo(null))
			.body("password", equalTo(null))
			.body("verificationCode", equalTo(null))
			.body("forgotPasswordCode", equalTo(null));
	}
	
	
	/**
	 * Providing blank or non-well-formed email
	 */
	@Test
	public void badEmailId() {

		// Fetch with an blank email id
		fetchByEmail("")
		.then()
			.statusCode(422)
    		.body("exception", equalTo(ConstraintViolationException.class.getName()))
    		.body("errors", hasErrors("email", "{org.hibernate.validator.constraints.NotBlank.message}"));    		

		// Fetch with a bad email id
		fetchByEmail("bad.email.id")
		.then()
			.statusCode(422)
    		.body("exception", equalTo(ConstraintViolationException.class.getName()))
    		.body("errors", hasErrors("email", "{org.hibernate.validator.constraints.Email.message}"));		
	}
	

	/**
	 * Providing an unknown email id
	 */
	@Test
	public void wrongEmailId() {

		// Fetch with an unknown email id
    	fetchByEmail("wrong.email@example.com")
		.then()
			.statusCode(422)
    		.body("exception", equalTo(MultiErrorException.class.getName()))
    		.body("errors", hasErrors("email", "com.naturalprogrammer.spring.userNotFound"));    		
	}
	
	/**
	 * Helper method for fetching by email
	 * 
	 * @param email
	 * @return
	 */
	private Response fetchByEmail(String email) {
    	return given().spec(filters)
    				.param("email", email)
    		   .get("/api/core/users/fetch-by-email");	
	}	

    /**
     * Testing for fetching by id is made concise, because
     * most business rules are common with fetching by email 
     */
    @Test
    public void canFetchById() {
    	
    	User admin = userRepository.findByEmail(lemonProperties.getAdmin().getUsername()).get();
    			
    	// Fetch with an unknown id
    	fetchById(admin.getId() + 99999)
		.then()
			.statusCode(422)
    		.body("exception", equalTo(MultiErrorException.class.getName()))
    		.body("errors", hasErrors("id", "com.naturalprogrammer.spring.userNotFound"));
    	
    	// Fetch Admin while not logged in
    	fetchById(admin.getId())
		.then()
			.statusCode(200)
    		.body("name", equalTo(MyService.ADMIN_NAME)) // name should be Administrator
    		.body("email", equalTo(null)); // email should be null
    	
    	// Fetch while logged in as ADMIN
    	BasicTests.adminLogin(filters);
    	fetchById(admin.getId())
		.then()
			.statusCode(200)
			.body("name", equalTo(MyService.ADMIN_NAME)) // name should be Administrator
			.body("email", equalTo(lemonProperties.getAdmin().getUsername())); // email be proper
    	
    	// Logout
    	BasicTests.logout(filters);
    	
    	// Fetch Admin after signing up as User1
    	// and see that email is null
    	SignupTests.signup(filters, SignupTests.newUser1());
    	
    	fetchById(admin.getId())
		.then()
			.statusCode(200)
			.body("name", equalTo(MyService.ADMIN_NAME))
			.body("email", equalTo(null));    	
    }
    
	/**
	 * Helper for fetching by Id
	 */
    private Response fetchById(long id) {
    	
    	return fetchById(filters, id);	
	}
    
    /**
     * Utility method for fetching by Id
     */
    public static Response fetchById(RequestSpecification filters, long id) {
    	
    	return given().spec(filters)
				   .pathParam("id", id)
			   .get("/api/core/users/{id}");	
    }

	
	/**
	 * Helper method for creating a new user with
	 * verificationCode and forgotPasswordCode set
	 */
	private User signupUser1AndForgetPassword() {
		
		User user1 = signupUser1();

    	// Set a forgotPasswordCode
		BasicTests.logout(filters);
    	ForgotPasswordTests.forgotPassword(filters, user1.getEmail());

    	return user1;
	}
	
	
	/**
	 * Helper method for creating a new user with
	 * verificationCode and forgotPasswordCode set
	 */
	private User signupUser1() {
		
		User user1 = SignupTests.newUser1(); 
				
    	// Sign up as User 1 - it will set a verificationCode
		BasicTests.ping(filters);
		SignupTests.signup(filters, user1);

    	return user1;
	}	
}
