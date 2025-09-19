package com.zybooks.voronova_option1_final;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// Tells Room to create a database with InventoryItem as the table
@Database(entities = {InventoryItem.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    // Lets the app use InventoryDao to interact with the inventory table
    public abstract InventoryDao inventoryDao();
}