package com.example.hospitalprescription;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Map;
import java.util.Objects;

public class UpdateActivity extends AppCompatActivity {
    public TextInputEditText doctorNames;
    public Button upload;
    public FirebaseUser user;
    public FirebaseAuth mAuth;
    public ProgressBar progressBar;
    public ImageView goBack, LogOut;
    public FirebaseDatabase database;
    public DatabaseReference usersRef;

    public TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        doctorNames = findViewById(R.id.doctorName);
        upload = findViewById(R.id.uploadBtn);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
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

        usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String displayName = snapshot.child("displayName").getValue(String.class);
                textView.setText(displayName);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                textView.setText("no user find!!!");
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dr;
                dr = String.valueOf(doctorNames.getText());
                if (TextUtils.isEmpty(dr)){
                    Toast.makeText(UpdateActivity.this, "Doctor Name required", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    addDoctorName(dr);
                }
            }

            private void addDoctorName(String dr) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference usersRef = database.getReference("users");
                FirebaseStorage storage =FirebaseStorage.getInstance();
                StorageReference storageReference = storage.getReference();
                String uid = user.getUid();

                // Get a reference to the "doctors" node for the user
                DatabaseReference doctorsRef = usersRef.child(uid).child("doctors");

                // Push the new doctor name to generate a unique key
                DatabaseReference newDoctorRef = doctorsRef.push();
                String doctorId = newDoctorRef.getKey();
                newDoctorRef.setValue(dr)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
//                                progressBar.setVisibility(View.GONE);
                                assert doctorId != null;
                                StorageReference doctorsStorageRef = storageReference.child("Doctor").child(uid).child(doctorId);
                                doctorsStorageRef.child(dr).putBytes(new byte[0]).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(UpdateActivity.this, "Doctor folder created", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(UpdateActivity.this, "Failed to create doctor folder", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Toast.makeText(UpdateActivity.this, "Doctor name added", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(UpdateActivity.this, "Failed to add doctor name", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        });
    }
}