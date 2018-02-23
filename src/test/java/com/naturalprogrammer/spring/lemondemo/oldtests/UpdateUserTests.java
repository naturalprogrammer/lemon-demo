package com.naturalprogrammer.spring.lemondemo.oldtests;

import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.hasErrors;
import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.restDocFilters;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import java.io.IOException;

import javax.validation.ConstraintViolationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser.Role;
import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;
import com.naturalprogrammer.spring.lemon.exceptions.VersionException;
import com.naturalprogrammer.spring.lemondemo.entities.User;
import com.naturalprogrammer.spring.lemondemo.repositories.UserRepository;
import com.naturalprogrammer.spring.lemondemo.services.MyService;
import com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Test cases for updating user
 * 
 * @author Sanjay Patel
 */
public class UpdateUserTests extends AbstractTests {

	private static final String UPDATED_NAME = "Edited name";
			
    private String userPatch1;
    private String userPatchRevokeAdmin;
    private String userPatchNullName;
    private String userPatchLongName;
	
	@Value("classpath:/update-user/patch-1.json")
	public void setUserPatch1(Resource patch) throws IOException {
		this.userPatch1 = MyTestUtil.toString(patch);
	}
	
	@Value("classpath:/update-user/patch-revoke-admin.json")
	public void setUserPatchRevokeAdmin(Resource patch) throws IOException {
		this.userPatchRevokeAdmin = MyTestUtil.toString(patch);;
	}

	@Value("classpath:/update-user/patch-null-name.json")
	public void setUserPatchNullName(Resource patch) throws IOException {
		this.userPatchNullName = MyTestUtil.toString(patch);;
	}

	@Value("classpath:/update-user/patch-long-name.json")
	public void setUserPatchLongName(Resource patch) throws IOException {
		this.userPatchLongName = MyTestUtil.toString(patch);;
	}

	@Autowired
	private UserRepository userRepository;
	
	private long user1Id;
	private long adminId;
	
	
	/**
     * Sign up User 1 first
     */
	@Before
    public void setUp() {
		
    	User user1 = SignupTests.newUser1();
		
    	BasicTests.pingSession(filters);
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
		
		// Update the User
    	update(user1Id, userPatch1)
		.then()
			.statusCode(200)
			.body("name", equalTo(UPDATED_NAME)); // name of the principal has changed
    	
    	// Fetch the user and check
    	FetchUserTests.fetchById(filters, user1Id)
		.then()
    		.body("name", equalTo(UPDATED_NAME))
    		.body("roles", hasItem(Role.UNVERIFIED)) // roles haven't changed
    		.body("roles", not(hasItem(Role.ADMIN)))
    		.body("email", equalTo(SignupTests.USER1_EMAIL)) // email hasn't changed
    		.body("version", equalTo(1)); // version has incremented

    	// Ensure again that the name of logged in user has changed
    	BasicTests.getContext(filters)
    	.then()
    		.body("user.name", equalTo(UPDATED_NAME)) // name of the principal has changed
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
		User user1 = userRepository.getOne(user1Id);
		//Assert.assertNotNull(user1.getVerificationCode());
    			
		BasicTests.adminLogin(filters);
    	
		// Update the User
		given()
			.spec(restDocFilters(restDocs, "update-user"))
			.spec(filters)
			.pathParam("id", user1Id)
			.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
		.body(userPatch1)
		.patch("/api/core/users/{id}")
 		.then()
			.statusCode(200)
			.body("name", equalTo(MyService.ADMIN_NAME)); // name of the principal shouldn't change

    	// Fetch the user and check
    	FetchUserTests.fetchById(filters, user1Id)
		.then()
    		.body("name", equalTo(UPDATED_NAME))
    		.body("roles", not(hasItem(Role.UNVERIFIED))) // roles have changed
    		.body("roles", hasItem(Role.ADMIN));
    	
    	// Fetch user1 from database and ensure that
		// his verificationCode is now null
		user1 = userRepository.getOne(user1Id);
		//Assert.assertNull(user1.getVerificationCode());
		
		// Re-update the user, making him unverified again
    	update(user1Id, MyTestUtil.getUserPatch2())
		.then()
			.statusCode(200);

    	// Fetch the user and check
    	FetchUserTests.fetchById(filters, user1Id)
		.then()
    		.body("roles", hasItem(Role.UNVERIFIED)); // now UNVERIFIED again
    	
       	// Fetch user1 from database and ensure that
    	// his verificationCode is not null
    	user1 = userRepository.getOne(user1Id);
    	//Assert.assertNotNull(user1.getVerificationCode());    	
    }	

	
	/**
	 * Providing an unknown id should throw exception.
	 */
	@Test
    public void unknownId() {
    	
		// Update the User
    	update(user1Id + 1, userPatch1)
		.then()
			.statusCode(422)
			.body("exception", equalTo(MultiErrorException.class.getName()))
			.body("errors", hasErrors("id",	"com.naturalprogrammer.spring.userNotFound"));
    }
	
	/**
	 * A non-admin trying to update the name and roles of another user should throw exception
	 */
	@Test
    public void tryToUpdateAnother() {
    	
		// Update Admin
    	update(adminId, userPatch1)
		.then()
			.statusCode(403)
			.body("exception", equalTo(AccessDeniedException.class.getName()))
			.body("message", equalTo("Access is denied"));
    }
	
	/**
	 * A bad ADMIN trying to update the name and roles of another user should throw exception
	 */
	@Test
    public void badAdminTryingToUpdateAnother() {
		
		User user1 = SignupTests.newUser1();
				
		makeUser1BadAdmin(filters, user1Id);
    	
    	// Login as User 1, which is now a bad ADMIN 
    	BasicTests.login(filters, user1.getEmail(), user1.getPassword());

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
	public static void makeUser1BadAdmin(RequestSpecification filters, long user1Id) {
				
    	BasicTests.adminLogin(filters);
		
		// Update User 1
    	update(filters, user1Id, MyTestUtil.getUserPatchBadAdmin())
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
		
    	// Update the User
    	update(adminId, userPatchRevokeAdmin)
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
    	update(user1Id, userPatchNullName)
		.then()
			.statusCode(422)
			.body("exception", equalTo(ConstraintViolationException.class.getName()))
			.body("errors", hasErrors("updatedUser.name", "{blank.name}"));
    	
    	// Update the User with a long name   	
    	update(user1Id, userPatchLongName)
		.then()
			.statusCode(422)
			.body("exception", equalTo(ConstraintViolationException.class.getName()))
			.body("errors", hasErrors(
				"updatedUser.name", "{javax.validation.constraints.Size.message}"));    	
    }

	
	/**
	 * Repeated update with version as 0 should fail
	 * Due to optimistic locking check
	 */
	@Test
    public void versionMismatch() {
    	
		// Update the User
    	update(user1Id, userPatch1)
		.then()
			.statusCode(200)
			.body("name", equalTo(UPDATED_NAME)); // name of the principal has changed
    	
		// Try update again, with the same version 0
    	update(user1Id, userPatch1)
		.then()
			.statusCode(409)
			.body("exception", equalTo(VersionException.class.getName()));
	}
	
	/**
	 * Helper method to update a user
	 * 
	 * @param userId		the id of the user to update
	 * @param userPatch		the data to be updated
	 * @return
	 */
	private Response update(long userId, String userPatch) {
		
		return update(filters, userId, userPatch);
	}
	
	/**
	 * Static utility method to update a user 
	 * 
	 * @param filters
	 * @param userId
	 * @param userPatch
	 * @return
	 */
	public static Response update(RequestSpecification filters, long userId, String userPatch) {
		
		return given().spec(filters)
	    		.pathParam("id", userId)
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.body(userPatch)
			.patch("/api/core/users/{id}");	
	}
}
