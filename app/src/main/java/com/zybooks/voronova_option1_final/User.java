package com.zybooks.voronova_option1_final;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * User entity
 *
 * Represents a user account stored in the database.
 * The username is the unique key so no duplicates are allowed.
 */
@Entity(tableName = "users")
public class User {

    /** Username acts as the primary key, must be unique */
    @PrimaryKey
    @NonNull
    public String username;

    /** Password for the account (plain text for now – fine for a class project) */
    @NonNull
    public String password;

    /** Constructor */
    public User(@NonNull String username, @NonNull String password) {
        this.username = username;
        this.password = password;
    }

    // --- Getters ---
    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    // --- Setter for password (in case user changes it) ---
    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    @Override
    @NonNull
    public String toString() {
        return "User{username='" + username + "', password='" + password + "'}";
    }
}