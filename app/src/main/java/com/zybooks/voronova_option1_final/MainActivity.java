package com.zybooks.voronova_option1_final;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // One place to define the extra key used between activities
    public static final String EXTRA_USERNAME = "username";

    private EditText usernameEditText;
    private EditText passwordEditText;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect input fields and buttons from layout
        usernameEditText = findViewById(R.id.usernameField);
        passwordEditText = findViewById(R.id.passwordField);
        Button loginButton = findViewById(R.id.loginButton);
        Button createAccountButton = findViewById(R.id.createAccountButton);

        // Get access to database
        UserDatabase db = UserDatabase.getInstance(this);
        userDao = db.userDao();

        // Button actions
        loginButton.setOnClickListener(v -> handleLogin());
        createAccountButton.setOnClickListener(v -> handleCreateAccount());
    }

    // Try to log in with entered username and password
    private void handleLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        // Show message if any field is empty
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user exists in database
        User user = userDao.login(username, password);
        if (user != null) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

            // Pass the username to InventoryActivity (use constant key)
            Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
            intent.putExtra(EXTRA_USERNAME, username);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Invalid login. Try again or create an account.", Toast.LENGTH_SHORT).show();
        }
    }

    // Try to create a new account with the entered info
    private void handleCreateAccount() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        // Show message if fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter a username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Make sure the username is not already taken
        if (userDao.getUserByUsername(username) != null) {
            Toast.makeText(this, "Username already exists. Choose another.", Toast.LENGTH_SHORT).show();
        } else {
            // Add user to database and move to inventory screen
            userDao.insert(new User(username, password));
            Toast.makeText(this, "Account created! Logging in...", Toast.LENGTH_SHORT).show();

            // Pass the username to InventoryActivity (use constant key)
            Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
            intent.putExtra(EXTRA_USERNAME, username);
            startActivity(intent);
        }
    }
}