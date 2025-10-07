package com.zybooks.voronova_option1_final;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MessageDao {

    // Save a new system/user message
    @Insert
    long insert(Message message);

    // Inbox: all messages where THIS user is the receiver, newest first
    @Query("SELECT * FROM message_table WHERE receiver = :username ORDER BY timestamp DESC")
    List<Message> getInbox(String username);

    // Get the most recent single message (for quick badge or preview)
    @Query("SELECT * FROM message_table WHERE receiver = :username ORDER BY timestamp DESC LIMIT 1")
    Message getLatestForUser(String username);

    // Clear a user's inbox (handy in tests)
    @Query("DELETE FROM message_table WHERE receiver = :username")
    int clearInbox(String username);

    // Count all messages in the system (sanity check / tests)
    @Query("SELECT COUNT(*) FROM message_table")
    int count();
}
