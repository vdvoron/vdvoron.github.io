package com.zybooks.voronova_option1_final;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * InventoryItem
 *
 * Represents a single record in the inventory database.
 * Each record belongs to one user and contains:
 *  - a unique auto-generated ID
 *  - the item name
 *  - the item quantity
 *  - the username of the owner
 */
@Entity
public class InventoryItem {

    /** Auto-generated unique ID for each item (Room creates this). */
    @PrimaryKey(autoGenerate = true)
    public int id;

    /** Descriptive name of the item. */
    @NonNull
    public String name;

    /** Quantity of this item (must be non-negative). */
    public int quantity;

    /** Username of the user who owns this item. */
    @NonNull
    public String username;

    /** Constructor used by Room and by code when inserting a new item. */
    public InventoryItem(@NonNull String name, int quantity, @NonNull String username) {
        this.name = name;
        this.quantity = quantity;
        this.username = username;
    }

    // ---- Getters / setter ----
    public int getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    /** Helpful for debugging/logging. */
    @Override
    @NonNull
    public String toString() {
        return "InventoryItem{id=" + id +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", username='" + username + '\'' +
                '}';
    }
}