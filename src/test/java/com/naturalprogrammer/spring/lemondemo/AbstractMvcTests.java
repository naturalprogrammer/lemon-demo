package com.naturalprogrammer.spring.lemondemo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;
import com.naturalprogrammer.spring.lemondemo.repositories.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest({
	"lemon.recaptcha.sitekey="
})
@AutoConfigureMockMvc(secure=false)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.HSQL)
@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
public class AbstractMvcTests {
	
	protected static final long ADMIN_ID = 101L;
	protected static final long UNVERIFIED_ADMIN_ID = 102L;
	protected static final long BLOCKED_ADMIN_ID = 103L;
	
	protected static final long USER_ID = 104L;
	protected static final long UNVERIFIED_USER_ID = 105L;
	protected static final long BLOCKED_USER_ID = 106L;
	
	protected static final String ADMIN_EMAIL = "admin@example.com";
	protected static final String ADMIN_PASSWORD = "admin!";
	
	protected static final String UNVERIFIED_USER_EMAIL = "unverifieduser@example.com";
	
	protected Map<Long, String> tokens = new HashMap<>(6);
	
    @Autowired
    protected MockMvc mvc;
    
    @Autowired
    protected UserRepository userRepository;
    
    protected String login(String userName, String password) throws Exception {

        MvcResult result = mvc.perform(post("/login")
                .param("username", userName)
                .param("password", password)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(200))
                .andReturn();

        return result.getResponse().getHeader(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME);
    }
    
    @Before
    public void baseSetUp() throws Exception {
    	
		tokens.put(ADMIN_ID, login(ADMIN_EMAIL, ADMIN_PASSWORD));
		tokens.put(UNVERIFIED_ADMIN_ID, login("unverifiedadmin@example.com", "admin!"));
		tokens.put(BLOCKED_ADMIN_ID, login("blockedadmin@example.com", "admin!"));
		tokens.put(USER_ID, login("user@example.com", "admin!"));
		tokens.put(UNVERIFIED_USER_ID, login(UNVERIFIED_USER_EMAIL, "admin!"));
		tokens.put(BLOCKED_USER_ID, login("blockeduser@example.com", "admin!"));
    }
}
