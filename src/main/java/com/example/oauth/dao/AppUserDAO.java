package com.example.oauth.dao;

import com.example.oauth.entity.AppRole;
import com.example.oauth.entity.AppUser;
import com.example.oauth.form.AppUserForm;
import com.example.oauth.utils.EncryptedPasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.UserProfile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class AppUserDAO {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AppRoleDAO appRoleDAO;

    public AppUser findAppUserByUserId(Long userId) {
        try {
            String sql = "Select e from " + AppUser.class.getName() + " e " //
                    + " Where e.userId = :userId ";

            Query query = entityManager.createQuery(sql, AppUser.class);
            query.setParameter("userId", userId);

            return (AppUser) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public AppUser findAppUserByUserName(String userName) {
        try {
            String sql = "Select e from " + AppUser.class.getName() + " e " //
                    + " Where e.userName = :userName ";

            Query query = entityManager.createQuery(sql, AppUser.class);
            query.setParameter("userName", userName);

            return (AppUser) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public AppUser findByEmail(String email) {
        try {
            String sql = "Select e from " + AppUser.class.getName() + " e " //
                    + " Where e.email = :email ";

            Query query = entityManager.createQuery(sql, AppUser.class);
            query.setParameter("email", email);

            return (AppUser) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private String findAvailableUserName(String userName_prefix) {
        AppUser account = this.findAppUserByUserName(userName_prefix);
        if (account == null) {
            return userName_prefix;
        }
        int i = 0;
        while (true) {
            String userName = userName_prefix + "_" + i++;
            account = this.findAppUserByUserName(userName);
            if (account == null) {
                return userName;
            }
        }
    }

    public AppUser createAppUser(Connection<?> connection) {

        ConnectionKey key = connection.getKey();

        System.out.println("key= (" + key.getProviderId() + "," + key.getProviderUserId() + ")");

        UserProfile userProfile = connection.fetchUserProfile();

        String email = userProfile.getEmail();
        AppUser appUser = this.findByEmail(email);
        if (appUser != null) {
            return appUser;
        }
        String userName_prefix = userProfile.getFirstName().trim().toLowerCase()//
                + "_" + userProfile.getLastName().trim().toLowerCase();

        String userName = this.findAvailableUserName(userName_prefix);

        String randomPassword = UUID.randomUUID().toString().substring(0, 5);
        String encrytedPassword = EncryptedPasswordUtils.encryptePassword(randomPassword);

        appUser = new AppUser();
        appUser.setEnabled(true);
        appUser.setEncryptedPassword(encrytedPassword);
        appUser.setUserName(userName);
        appUser.setEmail(email);
        appUser.setFirstName(userProfile.getFirstName());
        appUser.setLastName(userProfile.getLastName());

        this.entityManager.persist(appUser);

        List<String> roleNames = new ArrayList<String>();
        roleNames.add(AppRole.ROLE_USER);
        this.appRoleDAO.createRoleFor(appUser, roleNames);

        return appUser;
    }

    public AppUser registerNewUserAccount(AppUserForm appUserForm, List<String> roleNames) {
        AppUser appUser = new AppUser();
        appUser.setUserName(appUserForm.getUserName());
        appUser.setEmail(appUserForm.getEmail());
        appUser.setFirstName(appUserForm.getFirstName());
        appUser.setLastName(appUserForm.getLastName());
        appUser.setEnabled(true);
        String encrytedPassword = EncryptedPasswordUtils.encryptePassword(appUserForm.getPassword());
        appUser.setEncryptedPassword(encrytedPassword);
        this.entityManager.persist(appUser);
        this.entityManager.flush();

        this.appRoleDAO.createRoleFor(appUser, roleNames);

        return appUser;
    }

}