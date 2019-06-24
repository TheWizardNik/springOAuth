package com.example.oauth.social;

import com.example.oauth.dao.AppUserDAO;
import com.example.oauth.entity.AppUser;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.Connection;

public class ConnectionSignUpImpl implements ConnectionSignUp {

    private AppUserDAO appUserDAO;

    public ConnectionSignUpImpl(AppUserDAO appUserDAO) {
        this.appUserDAO = appUserDAO;
    }

    @Override
    public String execute(Connection<?> connection) {

        AppUser account = appUserDAO.createAppUser(connection);
        return account.getUserName();
    }

}