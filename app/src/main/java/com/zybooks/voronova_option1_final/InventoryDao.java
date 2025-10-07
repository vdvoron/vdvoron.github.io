package com.zybooks.voronova_option1_final;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InventoryDao {

    // Insert a new item; return new row ID.
    // Use ABORT so duplicates (if you later add a UNIQUE constraint) will fail instead of overwrite.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertItem(InventoryItem item);

    // Update an existing item; returns number of rows updated.
    @Update
    int updateItem(InventoryItem item);

    // Delete an item; returns number of rows deleted.
    @Delete
    int deleteItem(InventoryItem item);

    // Get all items for a user (sorted for a stable UI order).
    @Query("SELECT * FROM InventoryItem WHERE username = :username ORDER BY name ASC")
    List<InventoryItem> getAllItems(String username);

    // Does a name already exist for this user?
    @Query("SELECT COUNT(*) FROM InventoryItem WHERE username = :username AND name = :name")
    int countByNameForUser(String username, String name);

    // Find a single item by name for this user.
    @Query("SELECT * FROM InventoryItem WHERE username = :username AND name = :name LIMIT 1")
    InventoryItem findByNameForUser(String username, String name);
}