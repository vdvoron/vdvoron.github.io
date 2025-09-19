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

import java.io.IOException;
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
                .allowMainThreadQueries()   // fine for tests
                .build();
        dao = db.inventoryDao();
    }

    @After
    public void tearDown() throws IOException {
        db.close();
    }

    @Test
    public void insertAndQuery_filtersByUsername() {
        dao.insertItem(new InventoryItem("Apples", 5, "alice"));
        dao.insertItem(new InventoryItem("Bananas", 3, "bob"));
        dao.insertItem(new InventoryItem("Milk", 2, "alice"));

        List<InventoryItem> alice = dao.getAllItems("alice");
        assertEquals(2, alice.size());
        assertTrue(alice.stream().allMatch(i -> "alice".equals(i.username)));
    }

    @Test
    public void updateItem_persistsQuantity() {
        dao.insertItem(new InventoryItem("Water", 1, "alice"));
        InventoryItem water = dao.findByNameForUser("alice", "Water");
        assertNotNull(water);

        water.quantity = 7;
        dao.updateItem(water);

        InventoryItem after = dao.findByNameForUser("alice", "Water");
        assertNotNull(after);
        assertEquals(7, after.quantity);
    }

    @Test
    public void deleteItem_removesRow() {
        dao.insertItem(new InventoryItem("Juice", 2, "alice"));
        InventoryItem juice = dao.findByNameForUser("alice", "Juice");
        assertNotNull(juice);

        dao.deleteItem(juice);
        assertNull(dao.findByNameForUser("alice", "Juice"));
    }
}