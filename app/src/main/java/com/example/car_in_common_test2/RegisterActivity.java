package com.example.car_in_common_test2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;  // Firebase Authentication
    private DatabaseReference mDatabase;  // Firebase Database

    private EditText emailEditText, passwordEditText, nameEditText, surnameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users"); // Users node

        nameEditText = findViewById(R.id.editTextName);
        surnameEditText = findViewById(R.id.editTextLastName);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        Button registerButton = findViewById(R.id.buttonRegister);

        // Register Button
        registerButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String surname = surnameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(surname) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(RegisterActivity.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(email, password, name, surname);
            }
        });
    }

    // Register User in Firebase Authentication
    private void registerUser(String email, String password, String name, String surname) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser(); // Get current user
                        if (user != null) {
                            saveUserToDatabase(user.getUid(), name, surname, email);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Save additional user details in the Realtime Database
    private void saveUserToDatabase(String userId, String name, String surname, String email) {
        User newUser = new User(name, surname, email);

        mDatabase.child(userId).setValue(newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        // Navigate to CarDetailsActivity
                        Intent intent = new Intent(RegisterActivity.this, CarDetailsActivity.class);
                        startActivity(intent);
                        finish(); // Close RegisterActivity
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to save user data: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // User class for saving data
    static class User {
        public String name;
        public String surname;
        public String email;

        public User() {} // Default constructor for Firebase

        public User(String name, String surname, String email) {
            this.name = name;
            this.surname = surname;
            this.email = email;
        }
    }
}
