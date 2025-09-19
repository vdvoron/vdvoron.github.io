package com.zybooks.voronova_option1_final;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

/**
 * InventoryActivity
 *
 * Displays and manages the current user's inventory list.
 * Features:
 *  - Reads the username passed from MainActivity
 *  - Shows only items that belong to this user
 *  - Allows adding, updating, and deleting items
 *  - Provides a button to open the SMS settings screen
 */
public class InventoryActivity extends AppCompatActivity {

    // Business limit to avoid overflow or nonsense values
    private static final int MAX_QTY = 1_000_000;

    // ---- UI ----
    private EditText itemNameInput;
    private EditText itemQtyInput;
    private TableLayout inventoryTable;
    private Button addItemButton;
    private ImageButton backButton;
    private LinearLayout smsButton;

    // ---- Data ----
    private InventoryDao inventoryDao;
    private String loggedInUsername;   // username passed from MainActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // 1) Get the username from the intent extras
        loggedInUsername = getIntent().getStringExtra(MainActivity.EXTRA_USERNAME);
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            Toast.makeText(this, getString(R.string.err_missing_username), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2) Set up views, database, and listeners
        initViews();
        setupDatabase();
        bindListeners();

        // 3) Load this user's inventory items
        loadInventoryItems();
    }

    /** Find views from the XML layout. */
    private void initViews() {
        itemNameInput  = findViewById(R.id.itemNameInput);
        itemQtyInput   = findViewById(R.id.itemQtyInput);
        inventoryTable = findViewById(R.id.inventoryTable);
        addItemButton  = findViewById(R.id.addItemButton);
        backButton     = findViewById(R.id.backArrow);
        smsButton      = findViewById(R.id.smsButton);
    }

    /** Create the Room database and get the DAO. */
    private void setupDatabase() {
        AppDatabase db = Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
                        "inventory-db"
                )
                // For development only: reset DB if schema version changes
                .fallbackToDestructiveMigration()
                .build();
        inventoryDao = db.inventoryDao();
    }

    /** Set up button click actions. */
    private void bindListeners() {
        // Navigate to the SMS settings screen
        smsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SmsActivity.class);
            intent.putExtra(MainActivity.EXTRA_USERNAME, loggedInUsername);
            startActivity(intent);
        });

        // Return to the previous screen
        backButton.setOnClickListener(v -> finish());

        // Add a new inventory item
        addItemButton.setOnClickListener(v -> onAddItemClicked());
    }

    /** Load all items for this user on a background thread. */
    private void loadInventoryItems() {
        new Thread(() -> {
            for (InventoryItem item : inventoryDao.getAllItems(loggedInUsername)) {
                runOnUiThread(() -> addTableRow(item));
            }
        }).start();
    }

    /* -----------------------------
     * Add Item
     * --------------------------- */

    /** Called when the Add button is clicked: validate → insert → update UI. */
    private void onAddItemClicked() {
        String name   = itemNameInput.getText().toString().trim();
        String qtyStr = itemQtyInput.getText().toString().trim();

        if (!validateNewItem(name, qtyStr)) return;

        // safe: validateNewItem already checked and parsed
        int quantity = readQuantity(qtyStr).value;
        InventoryItem newItem = new InventoryItem(name, quantity, loggedInUsername);

        new Thread(() -> {
            inventoryDao.insertItem(newItem);
            runOnUiThread(() -> {
                addTableRow(newItem);
                itemNameInput.setText("");
                itemQtyInput.setText("");
                Toast.makeText(this, getString(R.string.item_added), Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    /** Ensure name is not empty and quantity is valid. */
    private boolean validateNewItem(String name, String qtyStr) {
        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.err_item_name_empty), Toast.LENGTH_SHORT).show();
            return false;
        }
        QtyResult r = readQuantity(qtyStr);
        if (r.reason != QtyResult.Reason.OK) {
            showQtyError(r.reason);
            return false;
        }
        return true;
    }

    /* -----------------------------
     * Quantity parsing with reasons
     * --------------------------- */

    /** Holder for parsed quantity and error reason. */
    private static class QtyResult {
        final Integer value;   // null if invalid
        final Reason reason;   // NEVER null
        enum Reason { OK, EMPTY, NOT_A_NUMBER, NEGATIVE, TOO_LARGE }
        QtyResult(Integer value, Reason reason) {
            this.value = value;
            this.reason = reason;
        }
    }

    /** Parse a non-negative quantity and detect overflow or bad input. */
    private QtyResult readQuantity(String qtyStr) {
        if (qtyStr == null || qtyStr.trim().isEmpty()) {
            return new QtyResult(null, QtyResult.Reason.EMPTY);
        }
        qtyStr = qtyStr.trim();

        // Quick length guard to prevent integer overflow
        if (qtyStr.length() > 10) {
            return new QtyResult(null, QtyResult.Reason.TOO_LARGE);
        }

        try {
            long asLong = Long.parseLong(qtyStr); // use long to catch large input safely
            if (asLong < 0) return new QtyResult(null, QtyResult.Reason.NEGATIVE);
            if (asLong > MAX_QTY) return new QtyResult(null, QtyResult.Reason.TOO_LARGE);
            return new QtyResult((int) asLong, QtyResult.Reason.OK);
        } catch (NumberFormatException e) {
            return new QtyResult(null, QtyResult.Reason.NOT_A_NUMBER);
        }
    }

    /** Show the correct toast message for a quantity error. */
    private void showQtyError(QtyResult.Reason reason) {
        int msgId;
        switch (reason) {
            case EMPTY:
            case NOT_A_NUMBER:
            case NEGATIVE:
                msgId = R.string.err_quantity_invalid;
                break;
            case TOO_LARGE:
                msgId = R.string.err_quantity_too_large;
                break;
            default:
                msgId = R.string.err_quantity_invalid;
        }
        Toast.makeText(this, getString(msgId, MAX_QTY), Toast.LENGTH_SHORT).show();
    }

    /* -----------------------------
     * Table rows (name, qty, delete)
     * --------------------------- */

    /** Add a row to the table for one item (Name, Quantity, Delete). */
    private void addTableRow(InventoryItem item) {
        TableRow row = new TableRow(this);

        // Item name
        TextView nameView = new TextView(this);
        nameView.setText(item.name);
        nameView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        nameView.setPadding(8, 8, 8, 8);

        // Quantity (tap to edit)
        TextView qtyView = new TextView(this);
        qtyView.setText(String.valueOf(item.quantity));
        qtyView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        qtyView.setPadding(8, 8, 8, 8);
        qtyView.setClickable(true);
        qtyView.setOnClickListener(v -> showUpdateQuantityDialog(item, qtyView));

        // Delete button (tap to confirm and delete)
        TextView deleteView = new TextView(this);
        deleteView.setText(getString(R.string.delete_button));
        deleteView.setTextColor(ContextCompat.getColor(this, R.color.light_blue));
        deleteView.setPadding(8, 8, 8, 8);
        deleteView.setClickable(true);
        deleteView.setOnClickListener(v -> confirmAndDelete(item, row));

        // Add views to the row, then to the table
        row.addView(nameView);
        row.addView(qtyView);
        row.addView(deleteView);
        inventoryTable.addView(row);
    }

    /** Show a dialog to update quantity with validation. */
    private void showUpdateQuantityDialog(InventoryItem item, TextView qtyView) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.quantity));

        new AlertDialog.Builder(this)
                .setTitle(R.string.update_quantity_title)
                .setView(input)
                .setPositiveButton(R.string.action_update, (dialog, which) -> {
                    String newQtyStr = input.getText().toString().trim();
                    QtyResult r = readQuantity(newQtyStr);
                    if (r.reason != QtyResult.Reason.OK) {
                        showQtyError(r.reason);
                        return;
                    }
                    int newQty = r.value;
                    item.quantity = newQty;

                    new Thread(() -> {
                        inventoryDao.updateItem(item);
                        runOnUiThread(() -> {
                            qtyView.setText(String.valueOf(newQty));
                            Toast.makeText(this, getString(R.string.item_updated), Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** Confirm deletion, then remove from database and UI. */
    private void confirmAndDelete(InventoryItem item, TableRow row) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_msg, item.name))
                .setPositiveButton(R.string.delete_button, (dialog, which) -> {
                    new Thread(() -> {
                        inventoryDao.deleteItem(item);
                        runOnUiThread(() -> {
                            inventoryTable.removeView(row);
                            Toast.makeText(this, getString(R.string.item_deleted), Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}