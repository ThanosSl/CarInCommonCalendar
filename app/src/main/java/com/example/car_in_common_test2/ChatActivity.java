package com.example.car_in_common_test2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private Button buttonSend;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Retrieve the groupId passed from GroupChatActivity
        String groupId = getIntent().getStringExtra("groupId");
        if (groupId == null || groupId.isEmpty()) {
            Toast.makeText(this, "Error: Group ID is missing.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if groupId is missing
            return;
        }

        // Initialize Firebase Auth and Database for this specific group chat
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("groupChats")
                .child(groupId)
                .child("messages");

        // Initialize UI elements
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        // Set up RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);

        // Send Message Button
        buttonSend.setOnClickListener(v -> sendMessage());

        // Start listening for messages in this group chat
        listenForMessages();
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                String senderEmail = currentUser.getEmail();
                long timestamp = System.currentTimeMillis();

                // Create message object
                Message message = new Message(messageText, senderEmail, timestamp);

                // Push message to Firebase under the group's messages
                databaseReference.push().setValue(message)
                        .addOnSuccessListener(aVoid -> editTextMessage.setText("")) // Clear input field
                        .addOnFailureListener(e ->
                                Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show());
            }
        } else {
            Toast.makeText(this, "Cannot send an empty message", Toast.LENGTH_SHORT).show();
        }
    }


    private void listenForMessages() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Handle updates if needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Handle message deletion if needed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Handle message reordering if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
