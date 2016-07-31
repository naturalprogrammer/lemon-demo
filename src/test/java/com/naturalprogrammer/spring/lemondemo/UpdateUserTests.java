package com.naturalprogrammer.spring.lemondemo;

import static com.jayway.restassured.RestAssured.given;
import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.hasErrors;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser.Role;
import com.naturalprogrammer.spring.lemondemo.entities.User;
import com.naturalprogrammer.spring.lemondemo.repositories.UserRepository;
import com.naturalprogrammer.spring.lemondemo.services.MyService;

/**
 * Test cases for updating user
 * 
 * @author Sanjay Patel
 */
public class UpdateUserTests extends AbstractTests {
	
	@Autowired
	private UserRepository userRepository;
	
	private long user1Id;
	private long adminId;
	
	/**
	 * Helper for creating update data
	 * 
	 * @return
	 */
	private User getUpdateData() {
		
		User data = new User();
		data.setName("Edited name");
		data.setUnverified(false);
		data.setAdmin(true);
		
		return data;
	}
	
	
	/**
     * Sign up User 1 first
     */
	@Before
    public void setUp() {
		
    	User user1 = SignupTests.newUser1();
		
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
	 * A non-admin user should be able to update his own name,
	 * but changes in roles should be skipped.
	 * The name of security principal object should also
	 * change in the process.
	 */
	@Test
    public void canUpdateSelf() {
		
		User updateData = getUpdateData();
    	
		// Update the User
    	update(user1Id, updateData)
		.then()
			.statusCode(200)
			.body("name", equalTo(updateData.getName())); // name of the principal has changed
    	
    	// Fetch the user and check
    	FetchUserTests.fetchById(filters, user1Id)
		.then()
    		.body("name", equalTo(updateData.getName()))
    		.body("roles", hasItem(Role.UNVERIFIED)) // roles haven't changed
    		.body("roles", not(hasItem(Role.ADMIN)))
    		.body("version", equalTo(1)); // version has incremented

    	// Ensure again that the name of logged in user has changed
    	BasicTests.getContext(filters)
    	.then()
    		.body("user.name", equalTo(updateData.getName())) // name of the principal has changed
    		.body("user.roles", hasItem(Role.UNVERIFIED)) // roles haven't changed
    		.body("user.roles", not(hasItem(Role.ADMIN)));    	
    }
	
	
	/**
	 * A good ADMIN should be able to update another user's name and roles.
	 * The name of security principal object should NOT change in the process,
	 * and the verification code should get set/unset on addition/deletion of
	 * the UNVERIFIED role. 
	 */
	@Test
    public void goodAdminCanUpdateOther() {
    	
    	// Fetch user1 from database and ensure that
		// his verificationCode isn't null
		User user1 = userRepository.findOne(user1Id);
		Assert.assertNotNull(user1.getVerificationCode());
    			
		BasicTests.adminLogin(filters);
    	
    	User updateData = getUpdateData();

		// Update the User
    	update(user1Id, updateData)
		.then()
			.statusCode(200)
			.body("name", equalTo(MyService.ADMIN_NAME)); // name of the principal shouldn't change

    	// Fetch the user and check
    	FetchUserTests.fetchById(filters, user1Id)
		.then()
    		.body("name", equalTo(updateData.getName()))
    		.body("roles", not(hasItem(Role.UNVERIFIED))) // roles have changed
    		.body("roles", hasItem(Role.ADMIN));
    	
    	// Fetch user1 from database and ensure that
		// his verificationCode is now null
		user1 = userRepository.findOne(user1Id);
		Assert.assertNull(user1.getVerificationCode());
		
		// Re-update the user, making him unverified again
		updateData.setUnverified(true);
		updateData.setVersion(1); // version got incremented on last update

		// Update the User
    	update(user1Id, updateData)
		.then()
			.statusCode(200);

    	// Fetch the user and check
    	FetchUserTests.fetchById(filters, user1Id)
		.then()
    		.body("roles", hasItem(Role.UNVERIFIED)); // now UNVERIFIED again
    	
       	// Fetch user1 from database and ensure that
    	// his verificationCode is not null
    	user1 = userRepository.findOne(user1Id);
    	Assert.assertNotNull(user1.getVerificationCode());    	
    }	

	
	/**
	 * Providing an unknown id should throw exception.
	 */
	@Test
    public void unknownId() {
    	
		// Update the User
    	update(user1Id + 1, getUpdateData())
		.then()
			.statusCode(400)
			.body("exception", equalTo("MultiErrorException"))
			.body("errors", hasErrors("id",	"com.naturalprogrammer.spring.userNotFound"));
    }
	
	/**
	 * A non-admin trying to update the name and roles of another user should throw exception
	 */
	@Test
    public void tryToUpdateAnother() {
    	
		// Update Admin
    	update(adminId, getUpdateData())
		.then()
			.statusCode(403)
			.body("exception", equalTo("AccessDeniedException"))
			.body("message", equalTo("Access is denied"));
    }
	
	/**
	 * A bad ADMIN trying to update the name and roles of another user should throw exception
	 */
	@Test
    public void badAdminTryingToUpdateAnother() {
		
		User user1 = SignupTests.newUser1();
				
		makeUser1BadAdmin(filters, user1Id, 0);
    	
    	// Login as User 1, which is now a bad ADMIN 
    	BasicTests.login(filters, user1.getEmail(), user1.getPassword());
    	BasicTests.ping(filters);

    	// Try to update another should not succeed
    	tryToUpdateAnother();
    }

	
	/**
	 * Utility for making User 1 A bad ADMIN
	 * 
	 * @param filters
	 * @param user1Id
	 * @param version
	 */
	public static void makeUser1BadAdmin(RequestSpecification filters, long user1Id, long version) {
				
		// Let's make User 1 a bad ADMIN
		User badAdmin = new User();
		badAdmin.setName("A bad ADMIN");
		badAdmin.setAdmin(true);
		badAdmin.setUnverified(true); // hence bad
		badAdmin.setVersion(version);
		
    	BasicTests.adminLogin(filters);
		
		// Update User 1
    	update(filters, user1Id, badAdmin)
		.then()
			.statusCode(200);
    	
    	// Fetch the user and check
    	FetchUserTests.fetchById(filters, user1Id)
		.then()
    		.body("roles", hasItem(Role.UNVERIFIED)) // roles have changed
    		.body("roles", hasItem(Role.ADMIN));
	}

	
	/**
	 * A good ADMIN should not be able to change his own roles
	 */
	@Test
    public void goodAdminCanNotUpdateSelfRoles() {
    	
    	BasicTests.adminLogin(filters);
    	
		final String NEW_NAME = "An old ADMIN";
		
    	// Let's make User 1 a bad ADMIN
		User revokeAdmin = new User();
		revokeAdmin.setName(NEW_NAME);
		revokeAdmin.setAdmin(false);
		revokeAdmin.setUnverified(true);

    	// Update the User
    	update(adminId, revokeAdmin)
		.then()
			.statusCode(200)
			.body("name", equalTo(NEW_NAME))
			.body("roles", hasItem("ADMIN")); // roles shouldn't change

    	// Fetch the user and check
    	FetchUserTests.fetchById(filters, adminId)
		.then()
    		.body("name", equalTo(NEW_NAME))
    		.body("roles", not(hasItem(Role.UNVERIFIED))) // roles have changed
    		.body("roles", hasItem(Role.ADMIN));
    }
	
	/**
	 * Invalid name
	 */
	@Test
    public void invalidNewName() {
    	
    	// Update the User with a null name
		User updatedName = new User();
    	update(user1Id, updatedName)
		.then()
			.statusCode(400)
			.body("exception", equalTo("ConstraintViolationException"))
			.body("errors", hasErrors("updatedUser.name", "{blank.name}"));
    	
    	// Update the User with a long name   	
		updatedName.setName(StringUtils.repeat('x', 51));
    	update(user1Id, updatedName)
		.then()
			.statusCode(400)
			.body("exception", equalTo("ConstraintViolationException"))
			.body("errors", hasErrors(
				"updatedUser.name", "{javax.validation.constraints.Size.message}"));    	
    }

	
	/**
	 * Repeated update with version as 0 should fail
	 * Due to optimistic locking check
	 */
	@Test
    public void versionMismatch() {
    	
		User updateData = getUpdateData();
		
		// Update the User
    	update(user1Id, updateData)
		.then()
			.statusCode(200)
			.body("name", equalTo(updateData.getName())); // name of the principal has changed
    	
		// Try update again, with the same version 0
    	update(user1Id, updateData)
		.then()
			.statusCode(409)
			.body("exception", equalTo("VersionException"));
	}
	
	/**
	 * Helper method to update a user
	 * 
	 * @param userId		the id of the user to update
	 * @param updateData	the data to be updated
	 * @return
	 */
	private Response update(long userId, User updateData) {
		
		return update(filters, userId, updateData);
	}
	
	/**
	 * Static utility method to update a user 
	 * 
	 * @param filters
	 * @param userId
	 * @param updateData
	 * @return
	 */
	public static Response update(RequestSpecification filters, long userId, User updateData) {
		
		return given().spec(filters)
	    		.pathParam("id", userId)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.body(updateData)
			.post("/api/core/users/{id}/update");	
	}

}
