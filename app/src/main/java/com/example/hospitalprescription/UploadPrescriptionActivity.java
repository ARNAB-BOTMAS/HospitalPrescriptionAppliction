package com.example.hospitalprescription;// UploadPrescriptionActivity.java
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class UploadPrescriptionActivity extends AppCompatActivity implements CalendarDialog.OnDateSelectedListener {

    private CalendarDialog calendarDialog;
    private TextView textView, datePre;
    public FirebaseAuth mAuth;
    public FirebaseUser user;
    public FirebaseDatabase database;
    public DatabaseReference usersRef;
    public SharedPreferences sharedPreferences;
    public Button yourButton, uploadPre, addBtn;
    public ImageView imageView, goBack, LogOut;
    public Uri selectedImageUri;
    public FirebaseStorage storage;
    public StorageReference storageReference;
    protected ProgressBar progressBar;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_prescription);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        storage =FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        yourButton = findViewById(R.id.btnSelect);
        uploadPre = findViewById(R.id.uploadPre);
        imageView = findViewById(R.id.imageBtn);
        datePre = findViewById(R.id.datePre);
        addBtn = findViewById(R.id.addPrescription);
        textView = findViewById(R.id.holderName);
        progressBar = findViewById(R.id.progressImageUp);
        goBack = findViewById(R.id.goBack);

        LogOut = findViewById(R.id.logOutBtn);

        LogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PrescriptionActivity.class);
                startActivity(intent);
                finish();
            }
        });

        calendarDialog = new CalendarDialog(this);
        calendarDialog.setOnDateSelectedListener(this);
        sharedPreferences = getSharedPreferences("doctor_id", Context.MODE_PRIVATE);

        yourButton.setOnClickListener(v -> showCalendarDialog());

        uploadPre.setOnClickListener(v -> checkAndRequestPermission());

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                uploadImage();
            }
        });

        String doctorId = sharedPreferences.getString("doctor_id", "");
        usersRef.child(user.getUid()).child("doctors").orderByKey().equalTo(doctorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Iterate through the snapshot (though there should be only one item)
                            for (DataSnapshot doctorSnapshot : snapshot.getChildren()) {
                                // Get the doctor name
                                String doctorName = doctorSnapshot.getValue(String.class);

                                // Now you have the doctorName, you can use it as needed
                                // For example, you can set it to a TextView
                                textView.setText(doctorName);
                            }
                        } else {
                            // Handle the case where no doctor with the specified ID is found
                            textView.setText("Doctor not found");
                        }
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        textView.setText("Doctor not found");
                    }
                });

        textView.setText(doctorId);
    }

    private void checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, open the gallery
            openGallery();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the gallery
                openGallery();
            }  // Permission denied, show a message or handle accordingly

        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Handle the selected image here
            selectedImageUri = data.getData();

            // Use Picasso to load the selected image into the ImageView
            Picasso.get().load(selectedImageUri).into(imageView);
            addBtn.setVisibility(View.VISIBLE);
        }
    }

    private void showCalendarDialog() {
        calendarDialog.show();
    }

    @Override
    public void onDateSelected(String selectedDate) {
        // Handle the selected date in your main activity
        datePre.setText(selectedDate);
        Toast.makeText(this, "Selected Date in Activity: " + selectedDate, Toast.LENGTH_SHORT).show();
    }

    private void uploadImage() {
        if (selectedImageUri != null) {
            String date = String.valueOf(datePre.getText());
            String doctorId = sharedPreferences.getString("doctor_id", "");
            String uid = user.getUid();
            StorageReference doctorsStorageRef = storageReference.child("Doctor").child(uid).child(doctorId);
            StorageReference imageRef = doctorsStorageRef.child(date + "/" + date + ".png" );

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // If image upload is successful, get the download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Save the image URL in the Realtime Database
                            saveImageUrlToDatabase(uid, doctorId, date, uri.toString());
                        });

                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Image Upload Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), PrescriptionActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveImageUrlToDatabase(String uid, String doctorId, String date, String imageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Set the image URL in the Realtime Database
        databaseReference.child("doctor").child(uid).child(doctorId).child(date).setValue(imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "database create successful", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                });
    }
}
