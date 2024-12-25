package com.example.car_in_common_test2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView usernameText, emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get UI components
        usernameText = findViewById(R.id.username);
        emailText = findViewById(R.id.email);
        ImageView profilePicture = findViewById(R.id.profilePicture);
        Button signOutButton = findViewById(R.id.signOutButton);

        // Set email of the logged-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            emailText.setText(currentUser.getEmail()); // Display email
            fetchAndDisplayUsername(currentUser.getUid()); // Fetch username from database
        } else {
            emailText.setText("No email available");
            usernameText.setText("No username available");
        }

        // Sign-out button logic
        signOutButton.setOnClickListener(v -> {
            // Confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Sign out from Firebase
                        mAuth.signOut();
                        // Navigate back to StartScreenActivity
                        Intent intent = new Intent(ProfileActivity.this, StartScreenActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    /**
     * Fetches the username from the Firebase Realtime Database
     * and displays it in the TextView.
     */
    private void fetchAndDisplayUsername(String userId) {
        mDatabase.child("users").child(userId).child("name").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String username = task.getResult().getValue(String.class);
                        usernameText.setText(username != null ? username : "No username available");
                    } else {
                        usernameText.setText("Failed to fetch username");
                    }
                })
                .addOnFailureListener(e -> {
                    usernameText.setText("Error: " + e.getMessage());
                });
    }
}
