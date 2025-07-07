package com.example.trackingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    // --- Constants ---
    private static final String TAG = "ProfileFragment";
    private static final String PREFS_FILE_NAME = "EncryptedUserProfile";
    private static final String KEY_NAME = "key_name";
    private static final String KEY_MOBILE = "key_mobile";
    private static final String KEY_EMAIL = "key_email";
    private static final String KEY_GENDER = "key_gender";
    private static final String KEY_AGE = "key_age";
    private static final String KEY_ADDRESS = "key_address";
    private static final String KEY_IMAGE_PATH = "key_image_path";
    private static final String PROFILE_IMAGE_NAME = "profile_image.png";

    // --- UI Views ---
    private CircleImageView civProfilePhoto;
    private FloatingActionButton fabChooseImage;
    private TextInputEditText etName, etMobileNo, etEmailId, etAddress, etAge;
    private AutoCompleteTextView actGender;
    private MaterialButton btnSaveChanges;
    private ProgressBar progressBar;

    // --- Dependencies & Handlers ---
    private SharedPreferences encryptedPrefs;
    private FirebaseFirestore db;
    private final String userId = "user123";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Bitmap newBitmapToSave = null;

    // --- Activity Result Launcher for simple image picking ---
    private final ActivityResultLauncher imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        handleSelectedImage(selectedImageUri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initializeViews(view);
        initializeDependencies();
        setupGenderDropdown();
        setupEventListeners();
        loadProfileData();
        return view;
    }

    private void initializeViews(@NonNull View view) {
        civProfilePhoto = view.findViewById(R.id.civProfilePhoto);
        fabChooseImage = view.findViewById(R.id.fabChooseImage);
        etName = view.findViewById(R.id.etProfileName);
        etMobileNo = view.findViewById(R.id.etProfileMobileNo);
        etEmailId = view.findViewById(R.id.etProfileEmailId);
        actGender = view.findViewById(R.id.actProfileGender);
        etAge = view.findViewById(R.id.etProfileAge);
        etAddress = view.findViewById(R.id.etProfileAddress);
        btnSaveChanges = view.findViewById(R.id.btnProfileSaveChanges);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void initializeDependencies() {
        db = FirebaseFirestore.getInstance();
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            encryptedPrefs = EncryptedSharedPreferences.create(
                    PREFS_FILE_NAME,
                    masterKeyAlias,
                    requireContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Could not create EncryptedSharedPreferences, falling back to standard.", e);
            encryptedPrefs = requireActivity().getSharedPreferences("UserProfile_unencrypted", Context.MODE_PRIVATE);
        }
    }

    private void setupGenderDropdown() {
        String[] genders = new String[]{"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, genders);
        actGender.setAdapter(adapter);
    }

    private void setupEventListeners() {
        // Click the camera icon to UPLOAD/CHANGE the image
        fabChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Click the main image to VIEW it in full screen
        civProfilePhoto.setOnClickListener(v -> showFullScreenImage());

        // Click the save button
        btnSaveChanges.setOnClickListener(v -> {
            if (validateInputs()) {
                saveProfileData();
            }
        });
    }

    private void handleSelectedImage(@NonNull Uri imageUri) {
        try {
            newBitmapToSave = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
            civProfilePhoto.setImageBitmap(newBitmapToSave);
            btnSaveChanges.setText("Save Profile & New Image");
        } catch (IOException e) {
            Log.e(TAG, "Failed to load selected image from URI", e);
            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    // ***************************************************************
    //  👇 UPDATED: This method now checks for a new unsaved image first
    // ***************************************************************
    private void showFullScreenImage() {
        // Check if there is a new, unsaved image preview
        if (newBitmapToSave != null) {
            // If yes, save it to a temporary file to show it
            String tempPath = saveImageToInternalStorage(newBitmapToSave, "temp_profile_image.png");
            if (tempPath != null) {
                FullScreenImageDialog.newInstance(tempPath).show(getParentFragmentManager(), "FullScreenImageDialog");
            }
        } else {
            // If no new image, show the permanently saved one
            String imagePath = encryptedPrefs.getString(KEY_IMAGE_PATH, null);
            if (imagePath != null && new File(imagePath).exists()) {
                FullScreenImageDialog.newInstance(imagePath).show(getParentFragmentManager(), "FullScreenImageDialog");
            } else {
                Toast.makeText(requireContext(), "No profile image to view.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInputs() {
        etName.setError(null);
        etMobileNo.setError(null);
        etEmailId.setError(null);

        if (TextUtils.isEmpty(etName.getText().toString().trim())) {
            etName.setError("Name is required");
            return false;
        }
        if (etMobileNo.getText().toString().trim().length() != 10) {
            etMobileNo.setError("Mobile number must be 10 digits");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(etEmailId.getText().toString().trim()).matches()) {
            etEmailId.setError("Enter a valid email address");
            return false;
        }
        return true;
    }

    private void saveProfileData() {
        setLoadingState(true);

        executor.execute(() -> {
            String imagePath = encryptedPrefs.getString(KEY_IMAGE_PATH, null);
            if (newBitmapToSave != null) {
                // Save the new bitmap to the permanent file location
                imagePath = saveImageToInternalStorage(newBitmapToSave, PROFILE_IMAGE_NAME);
                newBitmapToSave = null; // Clear after saving
            }

            Map<String, Object> profileData = new HashMap<>();
            profileData.put(KEY_NAME, etName.getText().toString().trim());
            profileData.put(KEY_MOBILE, etMobileNo.getText().toString().trim());
            profileData.put(KEY_EMAIL, etEmailId.getText().toString().trim());
            profileData.put(KEY_GENDER, actGender.getText().toString().trim());
            profileData.put(KEY_AGE, etAge.getText().toString().trim());
            profileData.put(KEY_ADDRESS, etAddress.getText().toString().trim());

            saveDataToLocal(profileData, imagePath);
            saveDataToFirebase(profileData);

            handler.post(() -> {
                setLoadingState(false);
                btnSaveChanges.setText("Save Changes");
                Toast.makeText(requireContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadProfileData() {
        setLoadingState(true);
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            populateUI(data);
                            saveDataToLocal(data, null);
                        }
                    } else {
                        loadProfileFromLocal();
                    }
                    setLoadingState(false);
                })
                .addOnFailureListener(e -> {
                    loadProfileFromLocal();
                    setLoadingState(false);
                });
    }

    private void loadProfileFromLocal() {
        etName.setText(encryptedPrefs.getString(KEY_NAME, ""));
        etMobileNo.setText(encryptedPrefs.getString(KEY_MOBILE, ""));
        etEmailId.setText(encryptedPrefs.getString(KEY_EMAIL, ""));
        actGender.setText(encryptedPrefs.getString(KEY_GENDER, ""), false);
        etAge.setText(encryptedPrefs.getString(KEY_AGE, ""));
        etAddress.setText(encryptedPrefs.getString(KEY_ADDRESS, ""));

        String imagePath = encryptedPrefs.getString(KEY_IMAGE_PATH, null);
        if (imagePath != null) {
            Bitmap bitmap = loadImageFromStorage(imagePath);
            if (bitmap != null) {
                civProfilePhoto.setImageBitmap(bitmap);
            }
        }
    }

    private void populateUI(@NonNull Map<String, Object> data) {
        etName.setText((String) data.get(KEY_NAME));
        etMobileNo.setText((String) data.get(KEY_MOBILE));
        etEmailId.setText((String) data.get(KEY_EMAIL));
        actGender.setText((String) data.get(KEY_GENDER), false);
        etAge.setText((String) data.get(KEY_AGE));
        etAddress.setText((String) data.get(KEY_ADDRESS));
    }

    private void saveDataToLocal(@NonNull Map<String, Object> data, @Nullable String imagePath) {
        SharedPreferences.Editor editor = encryptedPrefs.edit();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof String) {
                editor.putString(entry.getKey(), (String) entry.getValue());
            }
        }
        if (imagePath != null) {
            editor.putString(KEY_IMAGE_PATH, imagePath);
        }
        editor.apply();
    }

    private void saveDataToFirebase(@NonNull Map<String, Object> data) {
        db.collection("users").document(userId).set(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Profile data synced to Firestore."))
                .addOnFailureListener(e -> Log.e(TAG, "Error syncing profile to Firestore", e));
    }

    // --- UPDATED: This method now accepts a file name ---
    private String saveImageToInternalStorage(@NonNull Bitmap bitmap, @NonNull String fileName) {
        File directory = requireContext().getFilesDir();
        File file = new File(directory, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Failed to save image to internal storage", e);
            return null;
        }
    }

    @Nullable
    private Bitmap loadImageFromStorage(@NonNull String path) {
        try {
            File f = new File(path);
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (Exception e) {
            Log.e(TAG, "Failed to load image from internal storage", e);
            return null;
        }
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSaveChanges.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSaveChanges.setEnabled(true);
        }
    }
}