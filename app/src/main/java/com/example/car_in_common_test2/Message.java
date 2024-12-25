package com.example.car_in_common_test2;

public class Message {
    private String text;
    private String senderEmail;
    private long timestamp;

    public Message() {
        // Required empty constructor for Firebase
    }

    public Message(String text, String senderEmail, long timestamp) {
        this.text = text;
        this.senderEmail = senderEmail;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
