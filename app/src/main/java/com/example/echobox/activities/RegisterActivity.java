package com.example.echobox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echobox.R;
import com.example.echobox.database.DBHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * RegisterActivity handles the creation of new user accounts.
 * It takes user input (email and password), validates it, and 
 * saves the new user to the local SQLite database.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnRegister;
    private TextView btnGoLogin;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize DBHelper for database operations
        dbHelper = DBHelper.getInstance(this);

        // Bind UI components
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoLogin = findViewById(R.id.btnGoLogin);

        // Set up the registration process
        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            // Check if all fields are filled
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Attempt to register the user in the database
            boolean success = dbHelper.registerUser(email, password);

            if (success) {
                // If successful, notify the user and navigate to the Login screen
                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish(); // Close registration activity
            } else {
                // If registration fails (e.g., email already exists), show an error toast
                Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
            }
        });

        // Navigate back to Login screen if user already has an account
        btnGoLogin.setOnClickListener(v -> finish());
    }
}