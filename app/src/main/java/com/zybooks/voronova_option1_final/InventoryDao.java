package com.zybooks.voronova_option1_final;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InventoryDao {

    // Insert a new item; returns the new row ID (or -1 on conflict if you add constraints later)
    @Insert
    long insertItem(InventoryItem item);

    // Update an existing item; returns how many rows were updated (0 means not found)
    @Update
    int updateItem(InventoryItem item);

    // Delete an item; returns how many rows were deleted
    @Delete
    int deleteItem(InventoryItem item);

    // Get all items for a user (sorted for a stable UI order)
    @Query("SELECT * FROM InventoryItem WHERE username = :username ORDER BY name ASC")
    List<InventoryItem> getAllItems(String username);

    // Check if a name already exists for this user (use before insert/rename)
    @Query("SELECT COUNT(*) FROM InventoryItem WHERE username = :username AND name = :name")
    int countByNameForUser(String username, String name);

    // Optional: find one by name for this user (handy for editing flows)
    @Query("SELECT * FROM InventoryItem WHERE username = :username AND name = :name LIMIT 1")
    InventoryItem findByNameForUser(String username, String name);
}