package com.example.echobox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echobox.R;
import com.example.echobox.database.DBHelper;
import com.example.echobox.models.User;
import com.example.echobox.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * LoginActivity handles the user login process.
 * It checks for an existing session, validates credentials against the local database,
 * and manages transitions to the Home and Register screens.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView btnGoRegister;
    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SessionManager to manage user login state
        sessionManager = new SessionManager(this);

        // Check if a user is already logged in; if so, skip the login screen
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        // Initialize DBHelper for database queries
        dbHelper = new DBHelper(this);

        // Bind UI components to their respective views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        // Handle the login button click event
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            // Validate that both fields are filled
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Attempt to authenticate the user and retrieve their profile
            User user = dbHelper.getUserByEmailAndPassword(email, password);

            if (user != null) {
                // Save user details to session and navigate to HomeActivity
                sessionManager.createLoginSession(user.getUsername(), user.getEmail());
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            } else {
                // Notify user of incorrect credentials
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });

        // Navigate to the RegisterActivity for new account creation
        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }
}