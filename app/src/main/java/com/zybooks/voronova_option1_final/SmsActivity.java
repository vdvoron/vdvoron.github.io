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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        // Get username from previous screen (use same key as MainActivity)
        Intent intent = getIntent();
        String extra = MainActivity.EXTRA_USERNAME; // keeps it consistent everywhere
        currentUsername = intent.getStringExtra(extra);
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "default";
        }

        // Match UI elements from layout
        enableNotificationsSwitch = findViewById(R.id.enableNotificationsSwitch);
        phoneNumberInput          = findViewById(R.id.phoneNumberInput);
        permissionStatus          = findViewById(R.id.permissionStatus);
        savePhoneNumberButton     = findViewById(R.id.savePhoneNumberButton);
        ImageButton backButton    = findViewById(R.id.backArrow);

        // Store phone number per user (simple local storage)
        sharedPreferences = getSharedPreferences("sms_prefs_" + currentUsername, MODE_PRIVATE);

        // Load saved number for this user
        String savedPhone = sharedPreferences.getString("saved_phone", "");
        if (!savedPhone.isEmpty()) {
            phoneNumberInput.setText(savedPhone);
        }

        // Initial permission check
        checkSmsPermission();

        // Back to previous screen
        backButton.setOnClickListener(v -> finish());

        // Save phone number
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
        });

        // When the switch is turned on, send only if already granted; otherwise ask first
        enableNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) return;              // user turned it off — nothing to do
            if (isSmsAllowed) {
                sendSmsDemo();
            } else {
                requestSmsPermission();          // ask first; do NOT send yet
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-check in case user changed permission in system settings
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
            smsManager.sendTextMessage(phoneNumber, null, "InventoryApp: Test alert!", null, null);
            Toast.makeText(this, getString(R.string.sms_sent), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.sms_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    // When user responds to the runtime permission dialog
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
                // Switch back off because we can’t send without permission
                enableNotificationsSwitch.setChecked(false);
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                return;
            }

            // If user still wants notifications ON, now it’s safe to send the test SMS
            if (enableNotificationsSwitch.isChecked()) {
                sendSmsDemo();
            }
        }
    }
}