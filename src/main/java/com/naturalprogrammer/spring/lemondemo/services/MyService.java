package com.naturalprogrammer.spring.lemondemo.services;

import java.util.Map;

import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.stereotype.Service;

import com.naturalprogrammer.spring.lemon.LemonService;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemondemo.entities.User;

@Service
public class MyService extends LemonService<User, Long> {

	public static final String ADMIN_NAME = "Administrator";

	@Override
    public User newUser() {
        return new User();
    }

	@Override
	public Long parseId(String id) {
		return Long.valueOf(id);
	}

	@Override
    protected void updateUserFields(User user, User updatedUser, User currentUser) {

        super.updateUserFields(user, updatedUser, currentUser);

        user.setName(updatedUser.getName());

        LemonUtils.afterCommit(() -> {
            if (currentUser.equals(user))
                currentUser.setName(user.getName());
        });
    }
    
    @Override
    protected User createAdminUser() {
    	
    	User user = super.createAdminUser(); 
    	user.setName(ADMIN_NAME);
    	return user;
    }
    
    @Override
    protected User userForClient(User currentUser) {

        User user = super.userForClient(currentUser);
        if (user != null)
            user.setName(currentUser.getName());
        return user;
    }
    
    @Override
    public void fillAdditionalFields(User user, Map<String, Object> attributes) {
    	user.setName((String) attributes.get(StandardClaimNames.GIVEN_NAME));
    }
}