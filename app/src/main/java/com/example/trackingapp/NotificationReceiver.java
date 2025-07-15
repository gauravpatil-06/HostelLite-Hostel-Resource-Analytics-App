package com.example.trackingapp;
import com.example.trackingapp.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.trackingapp.R;


public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle the notification click event here
        if (intent != null && intent.hasExtra("message")) {
            String message = intent.getStringExtra("message");

            // Show a Toast message when the notification is clicked
            Toast.makeText(context, "Notification clicked: " + message, Toast.LENGTH_LONG).show();

            // Optionally, you can start a new Activity when the notification is clicked
            Intent homeIntent = new Intent(context, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Important for launching an Activity from a BroadcastReceiver
            context.startActivity(homeIntent);
        }
    }
}
