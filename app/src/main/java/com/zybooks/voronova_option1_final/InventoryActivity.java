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
 *  - Sends system messages for confirmations/alerts
 *  - Button to open the SMS settings screen
 */
public class InventoryActivity extends AppCompatActivity {

    private static final int MAX_QTY = 1_000_000;
    private static final int LOW_STOCK_THRESHOLD = 0;

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
    private MessageDao messageDao;     // NEW
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

    private void initViews() {
        itemNameInput  = findViewById(R.id.itemNameInput);
        itemQtyInput   = findViewById(R.id.itemQtyInput);
        inventoryTable = findViewById(R.id.inventoryTable);
        addItemButton  = findViewById(R.id.addItemButton);
        backButton     = findViewById(R.id.backArrow);
        smsButton      = findViewById(R.id.smsButton);

        searchInput    = findViewById(R.id.searchInput);
        searchBtn      = findViewById(R.id.searchBtn);
        sortNameBtn    = findViewById(R.id.sortNameBtn);
        sortQtyBtn     = findViewById(R.id.sortQtyBtn);
    }

    /** Create or reuse the singleton Room database and get the DAOs. */
    private void setupDatabase() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        inventoryDao = db.inventoryDao();
        messageDao   = db.messageDao();  // NEW
    }

    private void bindListeners() {
        smsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SmsActivity.class);
            intent.putExtra(MainActivity.EXTRA_USERNAME, loggedInUsername);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> finish());
        addItemButton.setOnClickListener(v -> onAddItemClicked());

        sortNameBtn.setOnClickListener(v -> {
            InventoryAlgorithms.sortByName(currentItems);
            refreshTable();
        });

        sortQtyBtn.setOnClickListener(v -> {
            InventoryAlgorithms.sortByQuantity(currentItems);
            refreshTable();
        });

        searchBtn.setOnClickListener(v -> {
            String q = searchInput.getText().toString().trim();
            if (q.isEmpty()) {
                refreshTable();
                return;
            }
            InventoryAlgorithms.sortByName(currentItems);
            InventoryItem found = InventoryAlgorithms.binarySearchByName(currentItems, q);
            if (found != null) {
                showSearchResult(Collections.singletonList(found));
            } else {
                Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadInventoryItems() {
        new Thread(() -> {
            List<InventoryItem> fromDb = inventoryDao.getAllItems(loggedInUsername);
            currentItems.clear();
            currentItems.addAll(fromDb);
            runOnUiThread(this::refreshTable);
        }).start();
    }

    private void onAddItemClicked() {
        String name   = itemNameInput.getText().toString().trim();
        String qtyStr = itemQtyInput.getText().toString().trim();

        if (!validateNewItem(name, qtyStr)) return;

        int quantity = readQuantity(qtyStr).value;
        InventoryItem newItem = new InventoryItem(name, quantity, loggedInUsername);

        new Thread(() -> {
            inventoryDao.insertItem(newItem);
            currentItems.add(newItem);
            runOnUiThread(() -> {
                refreshTable();
                itemNameInput.setText("");
                itemQtyInput.setText("");
                Toast.makeText(this, getString(R.string.item_added), Toast.LENGTH_SHORT).show();
                sendSystemMessage("Added item \"" + name + "\" with quantity " + quantity + ".");
            });
        }).start();
    }

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

    private static class QtyResult {
        final Integer value;
        final Reason reason;
        enum Reason { OK, EMPTY, NOT_A_NUMBER, NEGATIVE, TOO_LARGE }
        QtyResult(Integer value, Reason reason) { this.value = value; this.reason = reason; }
    }

    private QtyResult readQuantity(String qtyStr) {
        if (qtyStr == null || qtyStr.trim().isEmpty()) return new QtyResult(null, QtyResult.Reason.EMPTY);
        qtyStr = qtyStr.trim();
        if (qtyStr.length() > 10) return new QtyResult(null, QtyResult.Reason.TOO_LARGE);
        try {
            long asLong = Long.parseLong(qtyStr);
            if (asLong < 0) return new QtyResult(null, QtyResult.Reason.NEGATIVE);
            if (asLong > MAX_QTY) return new QtyResult(null, QtyResult.Reason.TOO_LARGE);
            return new QtyResult((int) asLong, QtyResult.Reason.OK);
        } catch (NumberFormatException e) {
            return new QtyResult(null, QtyResult.Reason.NOT_A_NUMBER);
        }
    }

    private void showQtyError(QtyResult.Reason reason) {
        if (reason == QtyResult.Reason.TOO_LARGE) {
            Toast.makeText(this, getString(R.string.err_quantity_too_large, MAX_QTY), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.err_quantity_invalid), Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshTable() {
        int childCount = inventoryTable.getChildCount();
        if (childCount > 1) inventoryTable.removeViews(1, childCount - 1);
        for (InventoryItem item : currentItems) addTableRow(item);
    }

    private void showSearchResult(List<InventoryItem> subset) {
        int childCount = inventoryTable.getChildCount();
        if (childCount > 1) inventoryTable.removeViews(1, childCount - 1);
        for (InventoryItem item : subset) addTableRow(item);
    }

    private void addTableRow(InventoryItem item) {
        TableRow row = new TableRow(this);

        TextView nameView = new TextView(this);
        nameView.setText(item.name);
        nameView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        nameView.setPadding(8, 8, 8, 8);

        TextView qtyView = new TextView(this);
        qtyView.setText(String.valueOf(item.quantity));
        qtyView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        qtyView.setPadding(8, 8, 8, 8);
        qtyView.setClickable(true);
        qtyView.setOnClickListener(v -> showUpdateQuantityDialog(item, qtyView));

        TextView deleteView = new TextView(this);
        deleteView.setText(getString(R.string.delete_button));
        deleteView.setTextColor(ContextCompat.getColor(this, R.color.light_blue));
        deleteView.setPadding(8, 8, 8, 8);
        deleteView.setClickable(true);
        deleteView.setOnClickListener(v -> confirmAndDelete(item));

        row.addView(nameView);
        row.addView(qtyView);
        row.addView(deleteView);
        inventoryTable.addView(row);
    }

    private void showUpdateQuantityDialog(InventoryItem item, TextView qtyView) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.quantity));

        new AlertDialog.Builder(this)
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
                            qtyView.setText(String.valueOf(newQty));
                            Toast.makeText(this, getString(R.string.item_updated), Toast.LENGTH_SHORT).show();
                            sendSystemMessage("Updated \"" + item.name + "\" to quantity " + newQty + ".");
                            if (newQty <= LOW_STOCK_THRESHOLD) {
                                sendSystemMessage("Alert: \"" + item.name + "\" is out of stock.");
                            }
                        });
                    }).start();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

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
                            // Optionally: sendSystemMessage("Deleted item \"" + item.name + "\".");
                        });
                    }).start();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /* -----------------------------
     * System messages (persisted inbox)
     * --------------------------- */

    /** Insert a system message to this user's inbox on a background thread. */
    private void sendSystemMessage(String text) {
        new Thread(() -> {
            if (messageDao == null) return;
            Message m = new Message(
                    loggedInUsername,         // receiver
                    text,                     // body
                    System.currentTimeMillis()
            );
            messageDao.insert(m);
        }).start();
    }
}