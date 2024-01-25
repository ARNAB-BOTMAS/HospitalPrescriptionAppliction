package com.example.hospitalprescription;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private PrescriptionAdapter prescriptionAdapter;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private StorageTask downloadTask;
    public TextView textView;
    public DatabaseReference usersRef;

    public ImageView goBack, LogOut;


    public FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        floatingActionButton = findViewById(R.id.addDrImage);
        textView = findViewById(R.id.holderName);
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
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        sharedPreferences = getSharedPreferences("doctor_id", Context.MODE_PRIVATE);

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

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UploadPrescriptionActivity.class);
                startActivity(intent);
                finish();
            }
        });
        String userId = user.getUid();



        databaseReference = database.getReference("doctor").child(userId).child(doctorId);

        ListView listView = findViewById(R.id.listViewPrescriptions);
        List<Prescription> prescriptions = new ArrayList<>();
        prescriptionAdapter = new PrescriptionAdapter(this, prescriptions);
        listView.setAdapter(prescriptionAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Prescription selectedPrescription = prescriptionAdapter.getItem(position);
                if (selectedPrescription != null) {
                    downloadImage(selectedPrescription.getImageUrl());
                }
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Prescription> prescriptions = new ArrayList<>();
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    String imageUrl = dateSnapshot.getValue(String.class);
                    prescriptions.add(new Prescription(date, imageUrl));
                }

                prescriptionAdapter.clear();
                prescriptionAdapter.addAll(prescriptions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PrescriptionActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadImage(String imageUrl) {
        if (downloadTask != null && downloadTask.isInProgress()) {
            // A download is already in progress
            return;
        }

        StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);

        // Open the URL in the default web browser
        openImageInBrowser(imageUrl);

        Toast.makeText(PrescriptionActivity.this, "Opening image in browser...", Toast.LENGTH_SHORT).show();
    }

    private void openImageInBrowser(String imageUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Handle the case where no browser app is installed
            Toast.makeText(PrescriptionActivity.this, "No browser app found", Toast.LENGTH_SHORT).show();
        }
    }


}

