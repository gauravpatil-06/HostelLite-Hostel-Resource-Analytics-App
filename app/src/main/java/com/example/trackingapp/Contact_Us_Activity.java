package com.example.trackingapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Contact_Us_Activity extends AppCompatActivity {

    private TextInputEditText etMessage;
    private TextInputLayout tilMessage;
    private MaterialButton btnSendMessage;
    private ConstraintLayout rowCallUs, rowMailUs;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private FirebaseFirestore db;
    private static final String PHONE_NUMBER = "7875335539";
    private static final String EMAIL_ADDRESS = "gp949958@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        toolbar = findViewById(R.id.toolbar);
        etMessage = findViewById(R.id.et_message);
        tilMessage = findViewById(R.id.til_message);
        btnSendMessage = findViewById(R.id.btn_send_message);
        rowCallUs = findViewById(R.id.row_call_us);
        rowMailUs = findViewById(R.id.row_mail_us);
        progressBar = findViewById(R.id.progress);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup Click Listeners
        rowCallUs.setOnClickListener(v -> makePhoneCall());
        rowMailUs.setOnClickListener(v -> sendEmail());
        btnSendMessage.setOnClickListener(v -> validateAndSendMessage());
    }

    private void validateAndSendMessage() {
        String message = etMessage.getText().toString().trim();
        tilMessage.setError(null);

        if (message.isEmpty()) {
            tilMessage.setError("Please enter a message before sending");
            return;
        }
        sendMessageToFirestore(message);
    }

    private void sendMessageToFirestore(String message) {
        setLoadingState(true);

        Map<String, Object> contactMessage = new HashMap<>();
        contactMessage.put("message", message);
        contactMessage.put("timestamp", System.currentTimeMillis());

        db.collection("contact_messages")
                .add(contactMessage)
                .addOnSuccessListener(documentReference -> {
                    setLoadingState(false);
                    Toast.makeText(this, "Message sent successfully!", Toast.LENGTH_LONG).show();
                    etMessage.setText(""); // Clear the text field
                    // Optionally navigate away
                    // startActivity(new Intent(Contact_Us_Activity.this, Home_Page.class));
                    // finish();
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(this, "Error sending message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSendMessage.setEnabled(false);
            btnSendMessage.setText("Sending...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnSendMessage.setEnabled(true);
            btnSendMessage.setText("Send Message");
        }
    }

    private void makePhoneCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + PHONE_NUMBER));
        startActivity(intent);
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", EMAIL_ADDRESS, null));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry from TrackingApp");
        startActivity(Intent.createChooser(intent, "Choose an Email client :"));
    }
}