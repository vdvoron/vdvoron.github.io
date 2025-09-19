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

@RunWith(AndroidJUnit4.class)
public class UserDaoAndroidTest {

    private AppDatabase db;
    private UserDao userDao;

    @Before
    public void setup() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        userDao = db.userDao();
    }

    @After
    public void tearDown() { db.close(); }

    @Test
    public void login_withCorrectPassword_succeeds() {
        User u = new User("u1", "pw1");
        userDao.insert(u);

        User found = userDao.getUser("u1", "pw1"); // or whatever your DAO exposes
        assertNotNull(found);
        assertEquals("u1", found.username);
    }

    @Test
    public void login_withWrongPassword_fails() {
        userDao.insert(new User("u1", "pw1"));
        User notFound = userDao.getUser("u1", "wrong");
        assertNull(notFound);
    }

    @Test
    public void createAccount_thenLogin_works() {
        int before = userDao.count(); // if you have it; else skip
        userDao.insert(new User("newUser", "secret"));
        User logged = userDao.getUser("newUser", "secret");
        assertNotNull(logged);
        // Optional: assertEquals(before + 1, userDao.count());
    }
}