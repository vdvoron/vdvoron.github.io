package com.zybooks.voronova_option1_final;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "username";

    private EditText usernameEditText;
    private EditText passwordEditText;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = findViewById(R.id.usernameField);
        passwordEditText = findViewById(R.id.passwordField);
        Button loginButton = findViewById(R.id.loginButton);
        Button createAccountButton = findViewById(R.id.createAccountButton);

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        userDao = db.userDao();

        loginButton.setOnClickListener(v -> handleLogin());
        createAccountButton.setOnClickListener(v -> handleCreateAccount());
    }

    private void handleLogin() {
        final String username = usernameEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString(); // keep spaces if user wants

        if (username.isEmpty() || password.isEmpty()) {
            toast(getString(R.string.enter_username_password));
            return;
        }

        new Thread(() -> {
            User user = userDao.login(username, password);
            runOnUiThread(() -> {
                if (user != null) {
                    toast(getString(R.string.login_successful));
                    startInventory(username);
                } else {
                    toast(getString(R.string.login_failed));
                }
            });
        }).start();
    }

    private void handleCreateAccount() {
        final String username = usernameEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            toast(getString(R.string.enter_username_password));
            return;
        }
        // simple password rule just to avoid empty/1-char passwords
        if (password.length() < 4) {
            toast(getString(R.string.password_too_short));
            return;
        }

        new Thread(() -> {
            User existing = userDao.getUserByUsername(username);
            if (existing != null) {
                runOnUiThread(() -> toast(getString(R.string.account_exists)));
                return;
            }

            userDao.insert(new User(username, password));
            runOnUiThread(() -> {
                toast(getString(R.string.account_created));
                startInventory(username);
            });
        }).start();
    }

    private void startInventory(String username) {
        Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
        intent.putExtra(EXTRA_USERNAME, username);
        startActivity(intent);
    }

    private void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}