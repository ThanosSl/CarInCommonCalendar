package com.example.car_in_common_test2;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if the user is logged in
        if (mAuth.getCurrentUser() != null) {
            // User is logged in, navigate to MainMenuActivity
            Intent intent = new Intent(SplashActivity.this, MainMenuActivity.class);
            startActivity(intent);
        } else {
            // User is not logged in, navigate to StartScreenActivity
            Intent intent = new Intent(SplashActivity.this, StartScreenActivity.class);
            startActivity(intent);
        }

        // Close SplashActivity
        finish();
    }
}
