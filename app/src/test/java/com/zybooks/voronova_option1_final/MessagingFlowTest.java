package com.zybooks.voronova_option1_final;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Covers:
 * - user1 sends message to user2 → appears in user2 inbox
 * - send to non-existing user → fails
 *
 * All in-memory; no Android framework needed.
 */
public class MessagingFlowTest {

    static class FakeAccountRepo {
        private final Set<String> users = new HashSet<>();
        void add(String u) { users.add(u); }
        boolean exists(String u) { return users.contains(u); }
    }

    static class FakeMessageRepo {
        // inbox per user
        private final Map<String, List<String>> inbox = new HashMap<>();
        void deliver(String to, String msg) {
            inbox.computeIfAbsent(to, k -> new ArrayList<>()).add(msg);
        }
        List<String> inboxOf(String user) { return inbox.getOrDefault(user, Collections.emptyList()); }
    }

    static class MessageService {
        private final FakeAccountRepo accounts;
        private final FakeMessageRepo messages;
        MessageService(FakeAccountRepo a, FakeMessageRepo m) { accounts = a; messages = m; }

        boolean send(String from, String to, String body) {
            if (from == null || to == null || body == null || body.trim().isEmpty()) return false;
            if (!accounts.exists(from) || !accounts.exists(to)) return false;
            messages.deliver(to, String.format("From %s: %s", from, body));
            return true;
        }
    }

    private FakeAccountRepo accounts;
    private FakeMessageRepo messages;
    private MessageService svc;

    @Before
    public void setUp() {
        accounts = new FakeAccountRepo();
        messages = new FakeMessageRepo();
        svc = new MessageService(accounts, messages);

        accounts.add("user1");
        accounts.add("user2");
    }

    @Test
    public void send_toExistingUser_appearsInInbox() {
        assertTrue(svc.send("user1", "user2", "hello"));
        List<String> inbox = messages.inboxOf("user2");
        assertEquals(1, inbox.size());
        assertTrue(inbox.get(0).contains("hello"));
    }

    @Test
    public void send_toUnknownUser_fails() {
        assertFalse(svc.send("user1", "ghost", "hi"));
        assertTrue(messages.inboxOf("ghost").isEmpty());
    }
}