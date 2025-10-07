package com.zybooks.voronova_option1_final;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Instrumented tests for InventoryDao using an in-memory Room DB.
 * Runs on the emulator/device (androidTest).
 */
@RunWith(AndroidJUnit4.class)
public class InventoryDaoAndroidTest {

    private AppDatabase db;
    private InventoryDao dao;

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class)
                .allowMainThreadQueries()   // OK for tests
                .build();
        dao = db.inventoryDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void insertAndQuery_filtersByUsername() {
        dao.insertItem(new InventoryItem("Apples", 5, "alice"));
        dao.insertItem(new InventoryItem("Bananas", 3, "bob"));
        dao.insertItem(new InventoryItem("Milk", 2, "alice"));

        List<InventoryItem> alice = dao.getAllItems("alice");
        assertEquals(2, alice.size());

        // No streams: verify every row belongs to alice
        for (InventoryItem it : alice) {
            assertEquals("alice", it.username);
        }
    }

    @Test
    public void updateItem_persistsQuantity() {
        dao.insertItem(new InventoryItem("Water", 1, "alice"));

        InventoryItem water = dao.findByNameForUser("alice", "Water");
        assertNotNull(water);

        water.quantity = 7;
        int rows = dao.updateItem(water);
        assertEquals(1, rows);

        InventoryItem after = dao.findByNameForUser("alice", "Water");
        assertNotNull(after);
        assertEquals(7, after.quantity);
    }

    @Test
    public void deleteItem_removesRow() {
        dao.insertItem(new InventoryItem("Juice", 2, "alice"));
        InventoryItem juice = dao.findByNameForUser("alice", "Juice");
        assertNotNull(juice);

        int rows = dao.deleteItem(juice);
        assertEquals(1, rows);

        assertNull(dao.findByNameForUser("alice", "Juice"));
    }

    @Test
    public void getAllItems_returnsSortedByNameAsc() {
        dao.insertItem(new InventoryItem("Zucchini", 1, "alice"));
        dao.insertItem(new InventoryItem("Apple", 1, "alice"));
        dao.insertItem(new InventoryItem("Mango", 1, "alice"));

        List<InventoryItem> list = dao.getAllItems("alice");
        assertEquals(3, list.size());

        // Because DAO query is ORDER BY name ASC
        assertEquals("Apple", list.get(0).name);
        assertEquals("Mango", list.get(1).name);
        assertEquals("Zucchini", list.get(2).name);
    }
}