package com.zybooks.voronova_option1_final;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Single Room database for the whole app:
 *  - User (accounts/login)
 *  - InventoryItem (items per user)
 *  - Message (simple inbox/outbox)
 *
 * NOTE: version bumped to 2 because schema changed from inventory-only to 3 entities.
 */
@Database(
        entities = { User.class, InventoryItem.class, Message.class },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // DAOs
    public abstract UserDao userDao();
    public abstract InventoryDao inventoryDao();
    public abstract MessageDao messageDao();

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
                            // OK for a class project; wipes data when version changes
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}