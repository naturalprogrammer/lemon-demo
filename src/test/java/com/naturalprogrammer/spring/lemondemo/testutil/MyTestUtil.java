//package com.naturalprogrammer.spring.lemondemo.testutil;
//
//import static org.hamcrest.Matchers.allOf;
//import static org.hamcrest.Matchers.hasEntry;
//import static org.hamcrest.Matchers.hasItems;
//import static org.hamcrest.Matchers.hasKey;
//import static org.hamcrest.Matchers.not;
//import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
//import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
//import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
//
//import java.io.IOException;
//import java.lang.reflect.Array;
//import java.nio.charset.StandardCharsets;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import javax.sql.DataSource;
//
//import org.apache.commons.io.IOUtils;
//import org.hamcrest.Matcher;
//import org.junit.Assert;
//import org.springframework.core.io.Resource;
//import org.springframework.restdocs.JUnitRestDocumentation;
//import org.springframework.restdocs.restassured3.RestAssuredRestDocumentation;
//import org.springframework.restdocs.restassured3.operation.preprocess.RestAssuredPreprocessors;
//import org.springframework.restdocs.snippet.Snippet;
//import org.springframework.stereotype.Component;
//
//import io.restassured.builder.RequestSpecBuilder;
//import io.restassured.filter.session.SessionFilter;
//import io.restassured.specification.RequestSpecification;
//
//@Component
//public class MyTestUtil {
//	
////    private static String userPatchBadAdmin;
////    private static String userPatch2;
////    
////	public static String getUserPatchBadAdmin() {
////		return userPatchBadAdmin;
////	}
////	
////	public static String getUserPatch2() {
////		return userPatch2;
////	}
////
////	@Value("classpath:/update-user/patch-bad-admin.json")
////	public void setUserPatchBadAdmin(Resource patch) throws IOException {
////		MyTestUtil.userPatchBadAdmin = MyTestUtil.toString(patch);;
////	}	
////	
////	@Value("classpath:/update-user/patch-2.json")
////	public void setUserPatch2(Resource patch) throws IOException {
////		this.userPatch2 = MyTestUtil.toString(patch);;
////	}
//
//	public static RequestSpecification configureFilters() {
//		
//		return new RequestSpecBuilder()
//			.addFilter(new SessionFilter())
//			.addFilter(new XsrfFilter())
//			.addFilter(new JsonPrefixFilter())
//			.build();			
//	}
//	
//	
//	public static RequestSpecification restDocFilters(
//			JUnitRestDocumentation restDocumentation,
//			String identifier, Snippet... snippets) {
//		
//		return new RequestSpecBuilder()
//			.addFilter(RestAssuredRestDocumentation.documentationConfiguration(restDocumentation))
//			.addFilter(RestAssuredRestDocumentation.document(identifier,
//					preprocessRequest(
//							RestAssuredPreprocessors.modifyUris()
//								.scheme("https")
//								.host("www.example.com")
//								.removePort(),
//						prettyPrint()),
//					preprocessResponse(
//							RestAssuredPreprocessors.modifyUris()
//									.scheme("https")
//									.host("www.example.com")
//									.removePort(),
//							prettyPrint()),
//				snippets))
//			.build();			
//	}
//
//	
//	/**
//	 * It's not possible to rollback transactions in such integration tests.
//	 * See http://www.jayway.com/2014/07/04/integration-testing-a-spring-boot-application/
//	 * 
//	 * Hence this method for truncating the data.
//	 * See https://blog.42.nl/articles/keeping-integration-tests-isolated/
//	 */
//	public static void truncateDb(DataSource dataSource) throws SQLException {
//		
//        try (
//       		Connection connection = dataSource.getConnection();
//       		Statement databaseTruncationStatement = connection.createStatement();
//        ) {
//    		Assert.assertTrue(
//               	"This @After method wipes the entire database! Do not use this on anything other than an in-memory database!",
//               	connection.getMetaData().getDriverName().equals("HSQL Database Engine Driver"));
//        	
//            databaseTruncationStatement.executeUpdate("TRUNCATE SCHEMA public AND COMMIT");
//        }
//	}
//	
//	/**
//	 * Creates a matcher for the errors array
//	 * in the response, which contains FieldErrors in the format
//	 * [{field: "fieldName", code: "I18n message code", message: "The actual message"}, ...]
//	 * 
//	 * @param errorPairs	errors in pairs (field1, code2, field2, code2 ...) 
//	 * @return	The matcher
//	 */
//	@SuppressWarnings("unchecked")
//	public static Matcher<Iterable<Map<String, String>>> hasErrors(String... errorPairs) {
//		
//	    // Passed arguments must be in pairs of field and code
//		if(errorPairs.length % 2 != 0){
//	        throw new IllegalArgumentException("Error field and code must be pairs.");
//	    }
//
//		// To hold the matchers
//		List<Matcher<Map<String, String>>> itemMatchers =
//			new ArrayList<Matcher<Map<String, String>>>();
//		
//	    for (int i = 0; i < errorPairs.length; i += 2) {
//	    	
//	    	// make a matcher for each error
//	    	String field = errorPairs[i];
//	    	String error = errorPairs[i + 1];
//	    	
//	    	Matcher<Map<String, String>> matcher = 
//	    		allOf(
//	    			field == null ? not(hasKey("field")) : hasEntry("field", field),
//	    		    hasEntry("code", error));
//	    	
//	    	// add it to the list
//	    	itemMatchers.add(matcher);
//	    }
//
//	    // return the assembled matcher
//		return hasItems( // needs an Array
//			itemMatchers.toArray( // So, do the conversion
//				(Matcher<Map<String, String>>[])
//				Array.newInstance(itemMatchers.get(0).getClass(), itemMatchers.size())
//			)
//		);
//	}
//	
//
//	public static String toString(Resource resource) throws IOException {
//		return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8.name());
//	}
//}
