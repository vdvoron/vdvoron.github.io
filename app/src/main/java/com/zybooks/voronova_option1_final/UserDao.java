package com.zybooks.voronova_option1_final;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

    // Add a new user to the database
    @Insert
    void insert(User user);

    // Check if user with matching username and password exists
    @Query("SELECT * FROM user_table WHERE username = :username AND password = :password")
    User login(String username, String password);

    // Find a user by their username
    @Query("SELECT * FROM user_table WHERE username = :username")
    User getUserByUsername(String username);
}