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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * InventoryActivity
 *
 * Displays and manages the current user's inventory list.
 * Features:
 *  - Reads the username passed from MainActivity
 *  - Shows only items that belong to this user
 *  - Allows adding, updating, deleting items
 *  - Sorting and searching via InventoryAlgorithms
 *  - Button to open the SMS settings screen
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

    // New controls (search + sort)
    private EditText searchInput;
    private Button searchBtn;
    private Button sortNameBtn;
    private Button sortQtyBtn;

    // ---- Data ----
    private InventoryDao inventoryDao;
    private String loggedInUsername;   // username passed from MainActivity

    // In-memory copy of what's shown (for sorting/searching)
    private final List<InventoryItem> currentItems = new ArrayList<>();

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

        // New search/sort views
        searchInput    = findViewById(R.id.searchInput);
        searchBtn      = findViewById(R.id.searchBtn);
        sortNameBtn    = findViewById(R.id.sortNameBtn);
        sortQtyBtn     = findViewById(R.id.sortQtyBtn);
    }

    /** Create the Room database and get the DAO. */
    /** Create or reuse the singleton Room database and get the DAO. */
    private void setupDatabase() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
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

        // Sort by name
        sortNameBtn.setOnClickListener(v -> {
            InventoryAlgorithms.sortByName(currentItems);
            refreshTable();
        });

        // Sort by quantity
        sortQtyBtn.setOnClickListener(v -> {
            InventoryAlgorithms.sortByQuantity(currentItems);
            refreshTable();
        });

        // Search by name (case-insensitive)
        searchBtn.setOnClickListener(v -> {
            String q = searchInput.getText().toString().trim();
            if (q.isEmpty()) {
                refreshTable(); // show all again
                return;
            }
            // Sort by name first, then binary search
            InventoryAlgorithms.sortByName(currentItems);
            InventoryItem found = InventoryAlgorithms.binarySearchByName(currentItems, q);
            if (found != null) {
                showSearchResult(Collections.singletonList(found));
            } else {
                Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Load all items for this user on a background thread. */
    private void loadInventoryItems() {
        new Thread(() -> {
            List<InventoryItem> fromDb = inventoryDao.getAllItems(loggedInUsername);
            currentItems.clear();
            currentItems.addAll(fromDb);
            runOnUiThread(this::refreshTable);
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
            // keep our in-memory list in sync
            currentItems.add(newItem);
            runOnUiThread(() -> {
                refreshTable();
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
        if (reason == QtyResult.Reason.TOO_LARGE) {
            // Only err_quantity_too_large should be formatted with MAX_QTY
            Toast.makeText(this, getString(R.string.err_quantity_too_large, MAX_QTY), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.err_quantity_invalid), Toast.LENGTH_SHORT).show();
        }
    }

    /* -----------------------------
     * Table helpers
     * --------------------------- */

    /** Rebuild the table from currentItems (keeps header row at index 0). */
    private void refreshTable() {
        // Remove everything after the header
        int childCount = inventoryTable.getChildCount();
        if (childCount > 1) {
            inventoryTable.removeViews(1, childCount - 1);
        }
        for (InventoryItem item : currentItems) {
            addTableRow(item);
        }
    }

    /** Temporarily show only a subset (used by search). */
    private void showSearchResult(List<InventoryItem> subset) {
        int childCount = inventoryTable.getChildCount();
        if (childCount > 1) {
            inventoryTable.removeViews(1, childCount - 1);
        }
        for (InventoryItem item : subset) {
            addTableRow(item);
        }
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
        deleteView.setOnClickListener(v -> confirmAndDelete(item));

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
                // Using literals so it compiles with your current strings.xml
                .setTitle("Update Quantity")
                .setView(input)
                .setPositiveButton("Update", (dialog, which) -> {
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
                            // Update view and keep table ordering as-is
                            qtyView.setText(String.valueOf(newQty));
                            Toast.makeText(this, getString(R.string.item_updated), Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** Confirm deletion, then remove from DB, list, and refresh UI. */
    private void confirmAndDelete(InventoryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_msg, item.name))
                .setPositiveButton(R.string.delete_button, (dialog, which) -> {
                    new Thread(() -> {
                        inventoryDao.deleteItem(item);
                        currentItems.remove(item);
                        runOnUiThread(() -> {
                            refreshTable();
                            Toast.makeText(this, getString(R.string.item_deleted), Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}