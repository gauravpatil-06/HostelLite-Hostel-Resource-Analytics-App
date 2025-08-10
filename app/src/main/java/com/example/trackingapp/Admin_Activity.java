package com.example.trackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Admin_Activity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";
    private RecyclerView rvUsers;
    private FirebaseFirestore firestore;
    private UserAdapter userAdapter;
    private List<Map<String, Object>> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        firestore = FirebaseFirestore.getInstance();
        rvUsers = findViewById(R.id.rvUsers);

        setupUserRecyclerView();
        fetchUsers();
    }

    private void setupUserRecyclerView() {
        userAdapter = new UserAdapter(userList,
                // On Item Click
                user -> {
                    Intent intent = new Intent(Admin_Activity.this, UserDetailActivity.class);
                    intent.putExtra("USER_ID", (String) user.get("uid"));
                    intent.putExtra("USER_EMAIL", (String) user.get("email"));
                    startActivity(intent);
                },
                // On Delete Click
                user -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete User")
                            .setMessage("Are you sure you want to delete " + user.get("email") + "? This will delete all their data.")
                            .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                            .setNegativeButton("Cancel", null)
                            .show();
                }
        );
        rvUsers.setAdapter(userAdapter);
    }

    private void fetchUsers() {
        firestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> userData = document.getData();
                            if (userData.get("uid") == null) {
                                userData.put("uid", document.getId());
                            }
                            userList.add(userData);
                        }
                        userAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Error fetching users.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteUser(Map<String, Object> user) {
        String uid = (String) user.get("uid");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users").document(uid).delete()
                .addOnSuccessListener(aVoid -> {
                    firestore.collection("UsageData").document(uid).delete();
                    Toast.makeText(this, "User data for " + user.get("email") + " deleted.", Toast.LENGTH_SHORT).show();
                    fetchUsers();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete user data.", Toast.LENGTH_SHORT).show());
    }
}