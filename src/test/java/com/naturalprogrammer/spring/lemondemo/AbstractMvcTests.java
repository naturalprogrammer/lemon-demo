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

@RunWith(SpringRunner.class)
@SpringBootTest({
	"lemon.recaptcha.sitekey="
})
@AutoConfigureMockMvc(secure=false)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.HSQL)
@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
public class AbstractMvcTests {
	
	protected static final long ADMIN_ID = 1L;
	protected static final long UNVERIFIED_ADMIN_ID = 2L;
	protected static final long BLOCKED_ADMIN_ID = 3L;
	
	protected static final long USER1_ID = 4L;
	protected static final long UNVERIFIED_USER1_ID = 5L;
	protected static final long BLOCKED_USER1_ID = 6L;
	
	protected static final long NEXT_USER_ID = 7L;

	protected Map<Long, String> tokens = new HashMap<>(6);
	
    @Autowired
    protected MockMvc mvc;
    
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
    public void setUp() throws Exception {
    	
		tokens.put(ADMIN_ID, login("admin@example.com", "admin!"));
		tokens.put(UNVERIFIED_ADMIN_ID, login("unverifiedadmin@example.com", "admin!"));
		tokens.put(BLOCKED_ADMIN_ID, login("blockedadmin@example.com", "admin!"));
		tokens.put(USER1_ID, login("user1@example.com", "admin!"));
		tokens.put(UNVERIFIED_USER1_ID, login("unverifieduser@example.com", "admin!"));
		tokens.put(BLOCKED_USER1_ID, login("blockeduser@example.com", "admin!"));
    }
}
