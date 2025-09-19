package com.zybooks.voronova_option1_final;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Covers:
 * - log in with correct password → works
 * - log in with wrong password → fails
 * - create new account → then login works
 *
 * Uses an in-memory fake "repo" scoped to each test (fresh state).
 */
public class AccountFlowTest {

    // Very small in-memory account service just for tests
    static class FakeAccountRepo {
        private final Map<String, String> creds = new HashMap<>();
        boolean exists(String u) { return creds.containsKey(u); }
        void put(String u, String p) { creds.put(u, p); }
        boolean verify(String u, String p) { return p != null && p.equals(creds.get(u)); }
        void clear() { creds.clear(); }
    }

    static class AccountService {
        private final FakeAccountRepo repo;
        AccountService(FakeAccountRepo repo) { this.repo = repo; }

        boolean create(String user, String pass) {
            if (user == null || user.trim().isEmpty()) return false;
            if (pass == null || pass.length() < 6) return false; // simple rule
            if (repo.exists(user)) return false;
            repo.put(user, pass);
            return true;
        }

        boolean login(String user, String pass) {
            return repo.verify(user, pass);
        }
    }

    private FakeAccountRepo repo;
    private AccountService accounts;

    @Before
    public void setUp() {
        repo = new FakeAccountRepo();
        accounts = new AccountService(repo);
        // seed two users
        assertTrue(accounts.create("user1", "password1"));
        assertTrue(accounts.create("user2", "password2"));
    }

    @Test
    public void login_correctPassword_succeeds() {
        assertTrue(accounts.login("user1", "password1"));
    }

    @Test
    public void login_wrongPassword_fails() {
        assertFalse(accounts.login("user1", "nope"));
    }

    @Test
    public void create_thenLogin_works() {
        assertTrue(accounts.create("newbie", "secret!"));
        assertTrue(accounts.login("newbie", "secret!"));
    }
}