package com.example.trackingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import android.view.MenuItem;

public class Home_Page extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    // Fragments for Bottom Navigation
    Fragment homeFragment = new HomeFragment();
    Fragment electricityusageFragment = new Electricity_Usage_Fragment();
    Fragment waterusageFragment = new Water_Usage_Fragment();
    Fragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // SharedPreferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // DrawerLayout & NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Drawer listener
        navigationView.setNavigationItemSelectedListener(this);

        // Bottom Navigation listener
        bottomNavigationView = findViewById(R.id.homeBottomNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.menuBottomNavHome); // Default selected

        // Load default fragment
        loadFragment(homeFragment);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.homeFrameLayout, fragment);
        transaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        // Bottom Navigation
        if (id == R.id.menuBottomNavHome) {
            loadFragment(homeFragment);
        } else if (id == R.id.menuBottomNavElectricityUsage) {
            loadFragment(electricityusageFragment);
        } else if (id == R.id.menuBottomNavWaterUsage) {
            loadFragment(waterusageFragment);
        } else if (id == R.id.menuBottomNavProfile) {
            loadFragment(profileFragment);
        }

        // Drawer Navigation
        else if (id == R.id.nav_occupancy) {
            startActivity(new Intent(this, occupancyActivity.class));
        } else if (id == R.id.nav_temperature) {
            startActivity(new Intent(this, temperatureActivity.class));
        } else if (id == R.id.nav_nudges) {
            startActivity(new Intent(this, NudgesActivity.class));
        } else if (id == R.id.nav_contact) {
            startActivity(new Intent(this, Contact_Us_Activity.class));
        } else if (id == R.id.nav_about_us) {
            startActivity(new Intent(this, About_Us_Activity.class));
        } else if (id == R.id.nav_feedback) {
            startActivity(new Intent(this, Feedback_Activity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawers();
        return true;
    }

    private void logout() {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("HostelLite");
        ad.setMessage("Are you sure you want to logout?");
        ad.setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss());
        ad.setNegativeButton("Logout", (dialog, which) -> {
            editor.putBoolean("islogin", false).apply();
            startActivity(new Intent(Home_Page.this, LoginActivity.class));
            finish();
        });
        ad.show();
    }
}