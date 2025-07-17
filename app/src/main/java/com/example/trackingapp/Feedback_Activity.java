package com.example.trackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Feedback_Activity extends AppCompatActivity {

    private TextInputLayout tilFeedbackTitle, tilFeedbackDescription;
    private TextInputEditText etFeedbackTitle, etFeedbackDescription;
    private MaterialButton btnSubmitFeedback;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    // Firebase Firestore instance
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        toolbar = findViewById(R.id.toolbar);
        tilFeedbackTitle = findViewById(R.id.til_feedback_title);
        etFeedbackTitle = findViewById(R.id.et_feedback_title);
        tilFeedbackDescription = findViewById(R.id.til_feedback_description);
        etFeedbackDescription = findViewById(R.id.et_feedback_description);
        btnSubmitFeedback = findViewById(R.id.btn_submit_feedback);
        progressBar = findViewById(R.id.progress);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        btnSubmitFeedback.setOnClickListener(v -> {
            String title = etFeedbackTitle.getText().toString().trim();
            String description = etFeedbackDescription.getText().toString().trim();

            if (validateInput(title, description)) {
                sendFeedbackToFirestore(title, description);
            }
        });
    }

    private boolean validateInput(String title, String description) {
        // Clear previous errors
        tilFeedbackTitle.setError(null);
        tilFeedbackDescription.setError(null);

        if (title.isEmpty()) {
            tilFeedbackTitle.setError("Please enter a feedback title");
            return false;
        }

        if (description.isEmpty()) {
            tilFeedbackDescription.setError("Please provide a detailed description");
            return false;
        }

        if (description.length() < 10) {
            tilFeedbackDescription.setError("Description should be at least 10 characters long");
            return false;
        }

        return true;
    }

    private void sendFeedbackToFirestore(String title, String description) {
        // Show progress and disable button
        setLoadingState(true);

        // Create a new feedback map
        Map<String, Object> feedback = new HashMap<>();
        feedback.put("title", title);
        feedback.put("description", description);
        feedback.put("timestamp", System.currentTimeMillis()); // To know when feedback was sent

        // Add a new document with a generated ID to the "feedback" collection
        db.collection("feedback")
                .add(feedback)
                .addOnSuccessListener(documentReference -> {
                    // On Success
                    setLoadingState(false);
                    Toast.makeText(Feedback_Activity.this, "Feedback submitted successfully!", Toast.LENGTH_LONG).show();

                    // Go back to Home page after success
                    startActivity(new Intent(Feedback_Activity.this, Home_Page.class));
                    finish(); // Finish this activity
                })
                .addOnFailureListener(e -> {
                    // On Failure
                    setLoadingState(false);
                    Toast.makeText(Feedback_Activity.this, "Failed to submit feedback. Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSubmitFeedback.setEnabled(false);
            btnSubmitFeedback.setText("Submitting...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnSubmitFeedback.setEnabled(true);
            btnSubmitFeedback.setText("Submit Feedback");
        }
    }
}