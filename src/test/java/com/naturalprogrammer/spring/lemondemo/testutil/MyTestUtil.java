package com.naturalprogrammer.spring.lemondemo.testutil;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.filter.session.SessionFilter;
import com.jayway.restassured.specification.RequestSpecification;

public class MyTestUtil {
	
	public static RequestSpecification configureFilters() {
		
		return new RequestSpecBuilder()
			.addFilter(new SessionFilter())
			.addFilter(new XsrfFilter())
			.addFilter(new JsonPrefixFilter())
			.build();			
	}
	
	/**
	 * It's not possible to rollback transactions in such integration tests.
	 * See http://www.jayway.com/2014/07/04/integration-testing-a-spring-boot-application/
	 * 
	 * Hence this method for truncating the data.
	 * See https://blog.42.nl/articles/keeping-integration-tests-isolated/
	 */
	public static void truncateDb(DataSource dataSource) throws SQLException {
		
        try (
       		Connection connection = dataSource.getConnection();
       		Statement databaseTruncationStatement = connection.createStatement();
        ) {
    		Assert.assertTrue(
               	"This @After method wipes the entire database! Do not use this on anything other than an in-memory database!",
               	connection.getMetaData().getDriverName().equals("HSQL Database Engine Driver"));
        	
            databaseTruncationStatement.executeUpdate("TRUNCATE SCHEMA public AND COMMIT");
        }
	}
	
	/**
	 * Creates a matcher for the errors array
	 * in the response, which contains FieldErrors in the format
	 * [{field: "fieldName", code: "I18n message code", message: "The actual message"}, ...]
	 * 
	 * @param errorPairs	errors in pairs (field1, code2, field2, code2 ...) 
	 * @return	The matcher
	 */
	public static Matcher<Iterable<Map<String, String>>> hasErrors(String... errorPairs) {
		
	    // Passed arguments must be in pairs of field and code
		if(errorPairs.length % 2 != 0){
	        throw new IllegalArgumentException("Error field and code must be pairs.");
	    }

		// To hold the matchers
		List<Matcher<Map<String, String>>> itemMatchers =
			new ArrayList<Matcher<Map<String, String>>>();
		
	    for (int i = 0; i < errorPairs.length; i += 2) {
	    	
	    	// make a matcher for each error
	    	Matcher<Map<String, String>> matcher = Matchers
	    		.allOf(Matchers.hasEntry("field", errorPairs[i]),
	    		       Matchers.hasEntry("code", errorPairs[i + 1]));
	    	
	    	// add it to the list
	    	itemMatchers.add(matcher);
	    }

	    // return the assembled matcher
		return Matchers.hasItems( // needs an Array
			itemMatchers.toArray( // So, do the conversion
				(Matcher<Map<String, String>>[])
				Array.newInstance(itemMatchers.get(0).getClass(), itemMatchers.size())
			)
		);
		
	}
	


}
