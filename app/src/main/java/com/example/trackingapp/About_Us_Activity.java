package com.example.trackingapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class About_Us_Activity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private AboutAdapter adapter;
    private List<AboutItem> aboutItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView_about);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Prepare data and setup adapter
        prepareAboutData();
        adapter = new AboutAdapter(aboutItems);
        recyclerView.setAdapter(adapter);
    }

    private void prepareAboutData() {
        // Here we add all 10 points to our list.
        aboutItems.add(new AboutItem(R.drawable.ic_overview, "1. Project Overview", "• Predictive analysis of hostel water & electricity usage.\n• Helps in planning daily, weekly, and monthly resources."));
        aboutItems.add(new AboutItem(R.drawable.ic_purpose, "2. App Purpose", "• Forecast usage trends.\n• Highlight peaks and possible wastage.\n• Provide alerts to hostel admins."));
        aboutItems.add(new AboutItem(R.drawable.ic_data_collection, "3. Data Collection", "• Historical water consumption (liters).\n• Electricity usage (kWh).\n• Hostel occupancy (number of students).\n• Weather data (temperature, season)."));
        aboutItems.add(new AboutItem(R.drawable.ic_vision, "4. Vision", "• Ensure efficient and sustainable hostel resource usage."));
        aboutItems.add(new AboutItem(R.drawable.ic_mission, "5. Mission", "• Provide actionable insights to hostel admins.\n• Optimize daily and monthly operations.\n• Encourage eco-friendly practices through predictions and alerts."));
        aboutItems.add(new AboutItem(R.drawable.ic_benefits, "6. User Benefits", "• Efficient resource management.\n• Reduced wastage and costs.\n• Improved hostel operations."));
        aboutItems.add(new AboutItem(R.drawable.ic_reporting, "7. Reporting", "• Graphical display of trends.\n• Weekly and monthly reports.\n• Alerts for abnormal usage."));
        aboutItems.add(new AboutItem(R.drawable.ic_sustainability, "8. Sustainability", "• Promote conscious usage of water & electricity.\n• Minimize environmental impact.\n• Encourage responsible hostel living."));
        aboutItems.add(new AboutItem(R.drawable.ic_notifications, "9. Alerts & Notifications", "• Instant alerts for high consumption.\n• Suggestions for corrective actions.\n• Notifications for maintenance schedules."));
        aboutItems.add(new AboutItem(R.drawable.ic_future, "10. Future Plans", "• Integrate AI for better predictions.\n• Add more hostel facilities tracking.\n• Expand to multiple hostels for benchmarking."));
    }
}