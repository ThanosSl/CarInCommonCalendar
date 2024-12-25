package com.example.car_in_common_test2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StartScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startscreen);

        // Initialize Buttons
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        // Login Button Click Listener
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartScreenActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Register Button Click Listener
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartScreenActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
