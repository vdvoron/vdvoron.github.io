package com.zybooks.voronova_option1_final;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Defines a user for login and signup, saved in the database
@Entity(tableName = "user_table")
public class User {
    // Username will be used as the ID, must be unique
    @PrimaryKey
    @NonNull
    public String username;

    // Password for the user
    public String password;

    // Creates a new user with a username and password
    public User(@NonNull String username, String password) {
        this.username = username;
        this.password = password;
    }
}