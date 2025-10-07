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
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        userDao = db.userDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void insertAndLogin_success() {
        userDao.insert(new User("alice", "pw"));
        User found = userDao.login("alice", "pw");
        assertNotNull(found);
        assertEquals("alice", found.username);
    }

    @Test
    public void login_wrongPassword_returnsNull() {
        userDao.insert(new User("bob", "secret"));
        assertNull(userDao.login("bob", "wrong"));
    }

    @Test
    public void getUserByUsername_findsSavedUser() {
        userDao.insert(new User("carol", "1234"));
        User u = userDao.getUserByUsername("carol");
        assertNotNull(u);
        assertEquals("carol", u.username);
    }
}