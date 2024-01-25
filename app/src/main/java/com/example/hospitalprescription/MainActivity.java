package com.example.hospitalprescription;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public Button changeButton;
    public ImageView LogOut;
    private TextView textView;
    public FirebaseAuth mAuth;
    public FirebaseUser user;
    FloatingActionButton actionButton;
    public FirebaseDatabase database;
    public DatabaseReference usersRef;
    public SharedPreferences sharedPreferences;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("doctor_id", Context.MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        LogOut = findViewById(R.id.logOutBtn);
        actionButton = findViewById(R.id.addDr);
        textView = findViewById(R.id.holderName);
        getAllDoctorNames();

        ListView doctorListView = findViewById(R.id.doctorListView);

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
        doctorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Retrieve the clicked doctor's name
                String clickedDoctor = (String) parent.getItemAtPosition(position);
                getDoctorUniqueId(clickedDoctor);

                // Display the doctor's name (you can replace this with your desired action)
//                Toast.makeText(MainActivity.this, "Clicked on " + clickedDoctor, Toast.LENGTH_SHORT).show();
            }

            private void getDoctorUniqueId(String clickedDoctor) {
                usersRef.child(user.getUid()).child("doctors").orderByValue().equalTo(clickedDoctor)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    String uniqueId = dataSnapshot.getChildren().iterator().next().getKey();
                                    Toast.makeText(MainActivity.this, "Clicked on doctor with ID: " + uniqueId, Toast.LENGTH_SHORT).show();
                                    editor.putString("doctor_id", uniqueId);
                                    editor.apply();
                                    Intent intent = new Intent(getApplicationContext(), PrescriptionActivity.class);
                                    startActivity(intent);
                                    finish();

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle error
                            }
                        });
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UpdateActivity.class);
                startActivity(intent);
                finish();
            }
        });
        LogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void getAllDoctorNames() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");
        String uid = user.getUid();

        // Get a reference to the "doctors" node for the user
        DatabaseReference doctorsRef = usersRef.child(uid).child("doctors");

        doctorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> doctorNames = new ArrayList<>();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot doctorSnapshot : dataSnapshot.getChildren()) {
                        // Retrieve the doctor's name for each child node
                        String doctorName = doctorSnapshot.getValue(String.class);
                        doctorNames.add(doctorName);
                    }
                    // Display the doctor names in the UI
                    displayDoctorNames(doctorNames);
                } else {
                    // Handle the case where there are no doctors
                    Toast.makeText(MainActivity.this, "No doctors found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Toast.makeText(MainActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayDoctorNames(List<String> doctorNames) {
        ListView doctorListView = findViewById(R.id.doctorListView);

        // Create your custom adapter and set it to the ListView
        DoctorListAdapter adapter = new DoctorListAdapter(this, doctorNames);
        doctorListView.setAdapter(adapter);
    }



}