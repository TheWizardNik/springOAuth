package com.example.oauth.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Data
@Entity
@Table(name = "App_User", //
        uniqueConstraints = { //
                @UniqueConstraint(name = "APP_USER_UK", columnNames = "User_Name"),
                @UniqueConstraint(name = "APP_USER_UK2", columnNames = "Email") })
public class AppUser {

    @Id
    @GeneratedValue
    @Column(name = "User_Id", nullable = false)
    private Long userId;

    @Column(name = "User_Name", length = 36, nullable = false)
    private String userName;

    @Column(name = "Email", length = 128, nullable = false)
    private String email;

    @Column(name = "First_Name", length = 36, nullable = true)
    private String firstName;

    @Column(name = "Last_Name", length = 36, nullable = true)
    private String lastName;

    @Column(name = "Encrypted_Password", length = 128, nullable = false)
    private String encryptedPassword;

    @Column(name = "Enabled", length = 1, nullable = false)
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }
}