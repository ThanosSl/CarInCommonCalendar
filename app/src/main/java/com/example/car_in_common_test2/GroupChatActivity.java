package com.example.car_in_common_test2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {

    private Button createGroupButton, signOutButton;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupChatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        groupChatRef = FirebaseDatabase.getInstance().getReference("groupChats");

        // Initialize UI components
        createGroupButton = findViewById(R.id.createGroupButton);
        signOutButton = findViewById(R.id.disconnectButton);

        // Create Group Chat Button
        createGroupButton.setOnClickListener(v -> createGroupChat());

        signOutButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(GroupChatActivity.this)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        mAuth.signOut();
                        Toast.makeText(GroupChatActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupChatActivity.this, StartScreenActivity.class));
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss(); // Close the dialog
                    })
                    .show();
        });

    }

    private void createGroupChat() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            // Fetch current user's selectedCarId
            usersRef.child(currentUserId).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String selectedCarId = snapshot.child("selectedCarId").getValue(String.class);
                    if (selectedCarId != null) {
                        findUsersWithSameCarId(selectedCarId);
                    } else {
                        Toast.makeText(this, "No car assigned to your account.", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch your car ID.", Toast.LENGTH_SHORT).show());
        }
    }

    private void findUsersWithSameCarId(String carId) {
        usersRef.orderByChild("selectedCarId").equalTo(carId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        List<String> userIds = new ArrayList<>();
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            userIds.add(userSnapshot.getKey());
                        }

                        if (userIds.size() > 1) {
                            createChatInDatabase(userIds);
                        } else {
                            Toast.makeText(this, "No other users share your car ID.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No users found with this car ID.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createChatInDatabase(List<String> userIds) {
        // Query groupChats to check for an existing group with the same members
        groupChatRef.get().addOnSuccessListener(snapshot -> {
            boolean groupExists = false;
            String existingGroupId = null;

            // Check each existing group
            for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                GroupChat group = groupSnapshot.getValue(GroupChat.class);

                if (group != null && group.members.containsAll(userIds) && userIds.containsAll(group.members)) {
                    groupExists = true;
                    existingGroupId = group.groupId;
                    break;
                }
            }

            if (groupExists) {
                // Group already exists, navigate to ChatActivity
                Toast.makeText(this, "Group chat already exists!", Toast.LENGTH_SHORT).show();
                navigateToChatActivity(existingGroupId);
            } else {
                // Create a new group chat
                String groupId = groupChatRef.push().getKey();
                GroupChat newGroupChat = new GroupChat(groupId, userIds, System.currentTimeMillis());

                groupChatRef.child(groupId).setValue(newGroupChat)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Group chat created!", Toast.LENGTH_SHORT).show();
                            navigateToChatActivity(groupId);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to create group chat.", Toast.LENGTH_SHORT).show()
                        );
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error checking group chats: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void navigateToChatActivity(String groupId) {
        Intent intent = new Intent(GroupChatActivity.this, ChatActivity.class);
        intent.putExtra("groupId", groupId); // Pass the group ID
        startActivity(intent);
    }


    public static class GroupChat {
        public String groupId;
        public List<String> members;
        public long createdAt;

        public GroupChat() { }

        public GroupChat(String groupId, List<String> members, long createdAt) {
            this.groupId = groupId;
            this.members = members;
            this.createdAt = createdAt;
        }
    }
}
