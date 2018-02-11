//package com.naturalprogrammer.spring.lemondemo;
//
//import static com.naturalprogrammer.spring.lemon.domain.AbstractUser.UUID_LENGTH;
//import static com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil.restDocFilters;
//import static io.restassured.RestAssured.given;
//import static org.hamcrest.Matchers.equalTo;
//import static org.hamcrest.Matchers.hasKey;
//import static org.hamcrest.Matchers.isEmptyOrNullString;
//import static org.hamcrest.Matchers.not;
//import static org.junit.Assert.assertTrue;
//import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
//import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
//
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.security.access.AccessDeniedException;
//
//import com.naturalprogrammer.spring.lemondemo.entities.User;
//import com.naturalprogrammer.spring.lemondemo.repositories.UserRepository;
//import com.naturalprogrammer.spring.lemondemo.testutil.JsonPrefixFilter;
//import com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil;
//
//import io.restassured.filter.Filter;
//
///**
// * Tests token management
// * 
// * @author Sanjay Patel
// *
// */
//public class ApiKeyTests extends AbstractTests {
//	
//	@Autowired
//	private UserRepository userRepository;
//	
//	@Test
//	public void testApiKey() {
//		
//		// Retrieve the first ADMIN
//    	User admin = userRepository.findByEmail(lemonProperties.getAdmin().getUsername()).get();
//    	
//    	// ensure that there's no API Key
//    	assertTrue(admin.getApiKey() == null);
//    	
//    	// Obtain CSRF token
//    	BasicTests.pingSession(filters);
//    	
//    	// try to create API key without logging in
//    	given()
//    		.spec(filters)
//    		.pathParam("id", admin.getId())
//	    .post("/api/core/users/{id}/api-key")
//		.then()
//			.statusCode(403)
//			.body("exception", equalTo(AccessDeniedException.class.getName()))
//			.body("$", not(hasKey("apiKey"))); // token should not be present
//
//    	// Login
//    	BasicTests.adminLogin(filters);
//    
//    	// create API key
//    	String apiKey = 
//    	given()
//			.spec(restDocFilters(restDocs, "create-api-key"))
//    		.spec(filters)
//    		.pathParam("id", admin.getId())
//	    .post("/api/core/users/{id}/api-key")
//		.then()
//			.statusCode(200)
//			.body("apiKey", not(isEmptyOrNullString())) // a token should be present
//			.extract().path("apiKey");
//    	
//    	// API key should be a UUID
//    	assertTrue(apiKey.length() == UUID_LENGTH);
//    	
//    	// API key should have been saved, encoded
//    	admin = userRepository.getOne(admin.getId());
//    	assertTrue(admin.getApiKey() != null && admin.getApiKey().length() != UUID_LENGTH);
//    	
//    	// Abandon old session
//    	filters = MyTestUtil.configureFilters();
//    	BasicTests.pingSession(filters);
//    	
//    	// Trying a restricted operation should throw 403
//    	UpdateUserTests
//	    	.update(filters, admin.getId(), MyTestUtil.getUserPatch2())
//	    	.then()
//				.statusCode(403)
//				.body("exception", equalTo(AccessDeniedException.class.getName()));
//    	
//    	// Won't need session and XSRF for API key authenticated calls
//    	// So, just create a JsonPrefixFilter
//    	Filter jsonPrefixFilter = new JsonPrefixFilter();
//    	filters = null; // in case it's used below by mistake
//    	
//    	// Trying with a wrong API key should throw 401
//    	given()
//    		.filter(jsonPrefixFilter)
//			.pathParam("id", admin.getId())
//			.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
//			.header("Authorization", "Bearer " + admin.getId() + ":" + "a-wrong-API-key")
//			.body(MyTestUtil.getUserPatch2())
//		.patch("/api/core/users/{id}")
//		.then()
//			.statusCode(401)
//			.body("message", equalTo("Wrong authentication token. Was it changed or removed?"));
//    	
//    	// Trying with a wrong user id should throw 401
//    	given()
//			.filter(jsonPrefixFilter)
//			.pathParam("id", admin.getId())
//			.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
//			.header("Authorization", "Bearer " + "9811:" + apiKey)
//			.body(MyTestUtil.getUserPatch2())
//		.patch("/api/core/users/{id}")
//		.then()
//			.statusCode(401)
//			.body("message", equalTo("User not found"));
//    	
//    	// Using correct API key should work
//    	given()
//			.spec(restDocFilters(restDocs, "use-api-key", requestHeaders( 
//					headerWithName("Authorization").description(
//						"Custom token authentication - of the format _userId:api-key_"))))
//    		.filter(jsonPrefixFilter)
//			.pathParam("id", admin.getId())
//			.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
//			.header("Authorization", "Bearer " + admin.getId() + ":" + apiKey)
//			.body(MyTestUtil.getUserPatch2())
//		.patch("/api/core/users/{id}")
//		.then()
//			.statusCode(200)
//			.body("name", equalTo("Edited name"))
//			.body("unverified", equalTo(false)); // an Admin shouldn't be able to change his own roles    	
//
//    	// Create a new session
//    	filters = MyTestUtil.configureFilters();
//    	BasicTests.pingSession(filters);
//
//    	// Try removing API key without logging in
//    	given()
//    		.spec(filters)
//    		.pathParam("id", admin.getId())
//	    .delete("/api/core/users/{id}/api-key")
//		.then()
//			.statusCode(403)
//			.body("exception", equalTo(AccessDeniedException.class.getName()));
//    	
//    	// API key should not have been removed
//    	admin = userRepository.getOne(admin.getId());
//    	assertTrue(admin.getApiKey() != null);
//
//    	// Login
//    	BasicTests.adminLogin(filters);
//    
//    	// Remove API key
//    	given()
//			.spec(restDocFilters(restDocs, "remove-api-key"))
//    		.spec(filters)
//    		.pathParam("id", admin.getId())
//	    .delete("/api/core/users/{id}/api-key")
//		.then()
//			.statusCode(204);
//
//    	// API key should have been removed
//    	admin = userRepository.getOne(admin.getId());
//    	assertTrue(admin.getApiKey() == null);
//	}
//}
