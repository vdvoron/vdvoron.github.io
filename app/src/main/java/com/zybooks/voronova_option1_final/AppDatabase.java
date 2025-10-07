package com.zybooks.voronova_option1_final;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Bump version if you change schema. exportSchema=false to avoid schema folder warnings.
@Database(
        entities = { InventoryItem.class },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract InventoryDao inventoryDao();

    // Simple thread-safe singleton
    private static volatile AppDatabase INSTANCE;

    public static @NonNull AppDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "inventory-db"
                            )
                            .fallbackToDestructiveMigration() // fine for class project
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}