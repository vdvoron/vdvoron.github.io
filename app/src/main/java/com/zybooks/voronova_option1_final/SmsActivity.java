package com.zybooks.voronova_option1_final;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SmsActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;

    private SwitchCompat enableNotificationsSwitch;
    private EditText phoneNumberInput;
    private TextView permissionStatus;
    private Button savePhoneNumberButton;

    private boolean isSmsAllowed = false;
    private SharedPreferences sharedPreferences;

    private String currentUsername = "default"; // fallback if username not passed

    // persist confirmation/alerts to inbox
    private MessageDao messageDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        Intent intent = getIntent();
        currentUsername = intent.getStringExtra(MainActivity.EXTRA_USERNAME);
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "default";
        }

        enableNotificationsSwitch = findViewById(R.id.enableNotificationsSwitch);
        phoneNumberInput          = findViewById(R.id.phoneNumberInput);
        permissionStatus          = findViewById(R.id.permissionStatus);
        savePhoneNumberButton     = findViewById(R.id.savePhoneNumberButton);
        ImageButton backButton    = findViewById(R.id.backArrow);

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        messageDao = db.messageDao();

        sharedPreferences = getSharedPreferences("sms_prefs_" + currentUsername, MODE_PRIVATE);

        String savedPhone = sharedPreferences.getString("saved_phone", "");
        if (!savedPhone.isEmpty()) {
            phoneNumberInput.setText(savedPhone);
        }

        checkSmsPermission();

        backButton.setOnClickListener(v -> finish());

        savePhoneNumberButton.setOnClickListener(v -> {
            String phone = phoneNumberInput.getText().toString().trim();

            if (phone.isEmpty()) {
                Toast.makeText(this, getString(R.string.empty_phone), Toast.LENGTH_SHORT).show();
                return;
            }
            if (phone.length() != 10 || !phone.matches("\\d+")) {
                Toast.makeText(this, getString(R.string.invalid_phone), Toast.LENGTH_SHORT).show();
                return;
            }

            sharedPreferences.edit().putString("saved_phone", phone).apply();
            Toast.makeText(this, getString(R.string.save), Toast.LENGTH_SHORT).show();

            // inbox log
            sendSystemMessage("Phone number saved: " + phone + ".");
        });

        enableNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                sendSystemMessage("SMS notifications turned OFF.");
                return;
            }
            if (isSmsAllowed) {
                sendSmsDemo();
                sendSystemMessage("SMS notifications turned ON.");
            } else {
                requestSmsPermission();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSmsPermission();
    }

    private void checkSmsPermission() {
        boolean granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;

        isSmsAllowed = granted;
        permissionStatus.setText(granted
                ? getString(R.string.permission_granted)
                : getString(R.string.permission_denied));
    }

    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_CODE
        );
    }

    private void sendSmsDemo() {
        String phoneNumber = phoneNumberInput.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_phone), Toast.LENGTH_SHORT).show();
            return;
        }
        if (phoneNumber.length() != 10 || !phoneNumber.matches("\\d+")) {
            Toast.makeText(this, getString(R.string.invalid_phone), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isSmsAllowed) {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    "InventoryApp: notifications enabled. You'll receive alerts here.",
                    null,
                    null
            );
            Toast.makeText(this, getString(R.string.sms_sent), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.sms_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;

            isSmsAllowed = granted;
            permissionStatus.setText(granted
                    ? getString(R.string.permission_granted)
                    : getString(R.string.permission_denied));

            if (!granted) {
                enableNotificationsSwitch.setChecked(false);
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                sendSystemMessage("SMS permission denied. Notifications are OFF.");
                return;
            }

            if (enableNotificationsSwitch.isChecked()) {
                sendSmsDemo();
                sendSystemMessage("SMS permission granted. Notifications are ON.");
            }
        }
    }

    /* -----------------------------
     * System messages (persisted inbox)
     * --------------------------- */
    private void sendSystemMessage(String text) {
        new Thread(() -> {
            if (messageDao == null) return;
            // Message(receiver, body, timestamp) — matches your simplified Message model
            Message m = new Message(
                    currentUsername,
                    text,
                    System.currentTimeMillis()
            );
            messageDao.insert(m);
        }).start();
    }
}