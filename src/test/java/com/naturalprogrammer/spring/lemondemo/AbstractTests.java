package com.naturalprogrammer.spring.lemondemo;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.specification.RequestSpecification;
import com.naturalprogrammer.spring.lemon.LemonProperties;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;
import com.naturalprogrammer.spring.lemondemo.services.MyService;
import com.naturalprogrammer.spring.lemondemo.testutil.MyTestUtil;

/**
 * Inherit concrete test classes from this class.
 * 
 * @author Sanjay Patel
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT)
@ActiveProfiles("itest")
public abstract class AbstractTests {
	
    /**
     * Let RestAssured use our object mapper
     */
    static {
        RestAssured.config()
            .objectMapperConfig(new ObjectMapperConfig()
                .jackson2ObjectMapperFactory(
                    (clazz, str) -> LemonUtil.getMapper()));
        
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();    	
    }


	@Autowired
	protected LemonProperties lemonProperties;
	
	@Autowired
	protected DataSource dataSource;
	
	@Autowired
	protected MyService service;
	
	@LocalServerPort
	public void setPort(int port) {
    	RestAssured.port = port;   	
    }
	
	@Rule
	public JUnitRestDocumentation restDocs =
			new JUnitRestDocumentation("target/generated-snippets");
	
//    @BeforeClass
//    public static void init() {
//    	RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();    	    	    	
//    }
    
    /**
     * For isolating "filters", and for truncating the database, we need to force execute
     * test cases sequentially.
     * See http://stackoverflow.com/questions/545194/force-junit-to-run-one-test-case-at-a-time
     */
    private static Lock sequential = new ReentrantLock();
    
    protected RequestSpecification filters;
    
    /**
     * Executes before any test case and any other @Before method in the subclass
     */
    @Before
    public final void baseSetUp() {
    	
        // Start a test case and do a lock so that
    	// any other test test case doesn't start
    	// before it is unlocked
    	sequential.lock();
        
    	// adds the first Admin user
    	service.onStartup();
    	
    	// Set up filters for authentication, CORS, JSON prefixing etc.
    	filters = MyTestUtil.configureFilters();
    }
	
	/**
	 * Executes after any test case and any other @After method in the subclass
	 * 
	 * @throws SQLException
	 */
    @After
    public final void baseTearDown() throws SQLException {
		
		// Reset filters
    	filters = null;

		// Truncate the database after each test case
		MyTestUtil.truncateDb(dataSource);
		
		// Unlock, so that another test case can start
		sequential.unlock();
    }
}
