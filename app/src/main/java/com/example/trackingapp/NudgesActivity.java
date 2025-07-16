package com.example.trackingapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NudgesActivity extends AppCompatActivity {

    private LinearLayout suggestionsContainer;
    private FirebaseFirestore firestore;
    private final String userId = "user123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nudges);

        suggestionsContainer = findViewById(R.id.suggestionsContainer);
        firestore = FirebaseFirestore.getInstance();

        fetchUsageAndSuggest();
    }

    private void fetchUsageAndSuggest() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DocumentReference usageRef = firestore.collection("UsageData").document(userId).collection("daily").document(todayDate);
        DocumentReference goalRef = firestore.collection("users").document(userId).collection("goals").document("data");

        goalRef.get().addOnCompleteListener(goalTask -> {
            if (goalTask.isSuccessful()) {
                DocumentSnapshot goalDoc = goalTask.getResult();
                long waterGoal = 1000; // Default goal
                long electricityGoal = 1000; // Default goal

                if (goalDoc != null && goalDoc.exists()) {
                    Long wg = goalDoc.getLong("idealWaterGoal");
                    Long eg = goalDoc.getLong("idealElectricityGoal");
                    if (wg != null && wg > 0) waterGoal = wg;
                    if (eg != null && eg > 0) electricityGoal = eg;
                }

                long finalWaterGoal = waterGoal;
                long finalElectricityGoal = electricityGoal;

                usageRef.get().addOnCompleteListener(usageTask -> {
                    if (usageTask.isSuccessful()) {
                        DocumentSnapshot usageDoc = usageTask.getResult();
                        long waterUsed = 0;
                        long electricityUsed = 0;

                        if (usageDoc != null && usageDoc.exists()) {
                            Long wu = usageDoc.getLong("waterUsed");
                            Long eu = usageDoc.getLong("electricityUsed");
                            if (wu != null) waterUsed = wu;
                            if (eu != null) electricityUsed = eu;
                        }

                        displaySuggestions(waterUsed, finalWaterGoal, electricityUsed, finalElectricityGoal);

                    } else {
                        Toast.makeText(this, "Failed to fetch usage data", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Failed to fetch goals", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displaySuggestions(long waterUsed, long waterGoal, long electricityUsed, long electricityGoal) {
        suggestionsContainer.removeAllViews();
        int delay = 100;

        // Water Card
        boolean isWaterExceeded = waterUsed > waterGoal;
        List<String> waterTips = isWaterExceeded ?
                Arrays.asList("Turn off the tap while brushing.", "Prefer bucket baths over showers.", "Fix any leaking taps immediately.") :
                Arrays.asList("Great job on saving water!", "Your efforts are making a difference.", "Keep up the excellent work!");
        addUsageCard("💧 Water Usage", waterUsed, waterGoal, "L", waterTips, isWaterExceeded, delay);

        delay += 200;

        // Electricity Card
        boolean isElecExceeded = electricityUsed > electricityGoal;
        List<String> elecTips = isElecExceeded ?
                Arrays.asList("Turn off lights when leaving a room.", "Unplug devices when not in use.", "Use natural light during the day.") :
                Arrays.asList("Excellent electricity management!", "You are an energy saver!", "Continue your smart usage habits.");
        addUsageCard("⚡️ Electricity Usage", electricityUsed, electricityGoal, "kWh", elecTips, isElecExceeded, delay);
    }

    private void addUsageCard(String title, long usage, long goal, String unit, List<String> tips, boolean isExceeded, int delay) {
        // --- Main CardView ---
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) (8 * getResources().getDisplayMetrics().density);
        cardParams.setMargins(margin, margin, margin, margin);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(15 * getResources().getDisplayMetrics().density);
        cardView.setCardElevation(8 * getResources().getDisplayMetrics().density);
        cardView.setContentPadding(24, 24, 24, 24);

        // --- Vertical LinearLayout inside Card ---
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // --- Title TextView ---
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(20);
        titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        titleView.setTextColor(Color.parseColor("#111827"));

        // --- Usage Stats TextView (e.g., Usage: 500L / Goal: 1000L) ---
        TextView statsView = new TextView(this);
        String statsText = String.format(Locale.getDefault(), "Usage: %d%s / Goal: %d%s", usage, unit, goal, unit);
        statsView.setText(statsText);
        statsView.setTextSize(14);
        statsView.setTextColor(Color.parseColor("#4B5563"));
        LinearLayout.LayoutParams statsParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statsParams.setMargins(0, 8, 0, 8);
        statsView.setLayoutParams(statsParams);

        // --- ProgressBar ---
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax((int) goal);
        progressBar.setProgress((int) Math.min(usage, goal)); // Cap progress at max
        ColorStateList progressColor = ColorStateList.valueOf(isExceeded ? Color.parseColor("#EF4444") : Color.parseColor("#3B82F6"));
        progressBar.setProgressTintList(progressColor);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressParams.setMargins(0, 4, 0, 12);
        progressBar.setLayoutParams(progressParams);

        // --- Summary TextView (e.g., 50L over goal) ---
        TextView summaryView = new TextView(this);
        summaryView.setTextSize(16);
        summaryView.setTypeface(null, Typeface.BOLD);
        if (isExceeded) {
            long difference = usage - goal;
            long percentOver = (goal > 0) ? (difference * 100) / goal : 0;
            summaryView.setText(String.format(Locale.getDefault(), "%d%s (%d%%) over your goal!", difference, unit, percentOver));
            summaryView.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            long difference = goal - usage;
            summaryView.setText(String.format(Locale.getDefault(), "%d%s under your goal! ✅", difference, unit));
            summaryView.setTextColor(Color.parseColor("#2E7D32"));
        }

        // --- Divider Line ---
        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (1 * getResources().getDisplayMetrics().density));
        dividerParams.setMargins(0, 24, 0, 16);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.parseColor("#E5E7EB"));

        // --- Tips TextView ---
        TextView tipsView = new TextView(this);
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append("Suggestions:\n");
        ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        for (String tip : tips) {
            int start = ssb.length();
            ssb.append("\n").append(tip);
            ssb.setSpan(new BulletSpan(20, Color.parseColor("#6B7280")), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tipsView.setText(ssb);
        tipsView.setTextSize(15);
        tipsView.setTextColor(Color.parseColor("#374151"));
        tipsView.setLineSpacing(0f, 1.3f);

        // --- Add all views to the layout ---
        mainLayout.addView(titleView);
        mainLayout.addView(statsView);
        mainLayout.addView(progressBar);
        mainLayout.addView(summaryView);
        mainLayout.addView(divider);
        mainLayout.addView(tipsView);

        cardView.addView(mainLayout);
        suggestionsContainer.addView(cardView);

        // --- Animation ---
        cardView.setAlpha(0f);
        cardView.setTranslationY(50);
        cardView.animate().alpha(1f).translationY(0f).setStartDelay(delay).setDuration(400).start();
    }
}