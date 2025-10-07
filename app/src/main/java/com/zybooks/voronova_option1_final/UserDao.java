package com.zybooks.voronova_option1_final;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {

    // Insert a new user, ignore if username already exists
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(User user);

    // Login: check if user with matching username and password exists
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    // Find a user by their username
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    // Check if a username already exists
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int countByUsername(String username);
}