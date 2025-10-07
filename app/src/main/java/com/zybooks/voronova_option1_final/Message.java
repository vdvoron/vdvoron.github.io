package com.zybooks.voronova_option1_final;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Message
 *
 * Represents a system notification for a user.
 * Examples: confirming phone number, alerting low stock, or out-of-stock items.
 */
@Entity(tableName = "message_table")
public class Message {

    /** Auto-generated unique ID for each message */
    @PrimaryKey(autoGenerate = true)
    public int id;

    /** The username who receives this system message */
    @NonNull
    public String receiver;

    /** The message content */
    @NonNull
    public String body;

    /** Unix time (milliseconds) when the message was created */
    public long timestamp;

    /** Constructor to create a new system message */
    public Message(@NonNull String receiver,
                   @NonNull String body,
                   long timestamp) {
        this.receiver = receiver;
        this.body = body;
        this.timestamp = timestamp;
    }

    // --- Getters ---
    @NonNull
    public String getReceiver() { return receiver; }

    @NonNull
    public String getBody() { return body; }

    public long getTimestamp() { return timestamp; }

    @NonNull
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", receiver='" + receiver + '\'' +
                ", body='" + body + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}