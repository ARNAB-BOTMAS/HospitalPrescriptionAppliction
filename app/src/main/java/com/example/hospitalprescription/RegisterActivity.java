package com.example.hospitalprescription;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    public TextInputEditText userInputText, emailInputText, passwordInputText, confirmPasswordInputText;
    public TextView textViewGoto;
    public Button regBtn;
    public FirebaseAuth mAuth;
    public FirebaseUser User;
    public ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser();
        userInputText = findViewById(R.id.userName);
        emailInputText = findViewById(R.id.email);
        passwordInputText = findViewById(R.id.password);
        confirmPasswordInputText = findViewById(R.id.conPassword);
        regBtn = findViewById(R.id.regBtn);
        progressBar = findViewById(R.id.progressBar);
        textViewGoto = findViewById(R.id.gotoLogin);
        if (User != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            textViewGoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });


            regBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    String user, email, password, confirmPassword;

                    user = String.valueOf(userInputText.getText());
                    email = String.valueOf(emailInputText.getText());
                    password = String.valueOf(passwordInputText.getText());
                    confirmPassword = String.valueOf(confirmPasswordInputText.getText());

                    if (TextUtils.isEmpty(user)){
                        Toast.makeText(RegisterActivity.this, "Username is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else if (TextUtils.isEmpty(email)){
                        Toast.makeText(RegisterActivity.this, "Email is empty", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (TextUtils.isEmpty(password)) {
                        Toast.makeText(RegisterActivity.this, "Password is empty", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (TextUtils.isEmpty(confirmPassword)) {
                        Toast.makeText(RegisterActivity.this, "Confirm password is required", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (!TextUtils.equals(password, confirmPassword)) {
                        Toast.makeText(RegisterActivity.this, "Password not match", Toast.LENGTH_SHORT).show();
                        return;
                    } else{
                        createUser(email, password, user);
                    }
                }

                private void createUser(String email, String password, String name) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        User = mAuth.getCurrentUser();
                                        assert User != null;
                                        createUserProfile(User.getUid(), name, email, password);
                                        Toast.makeText(RegisterActivity.this, "Authentication successful", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                private void createUserProfile(String uid, String name, String email, String password) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference usersRef = database.getReference("users");

                    UserProfile userProfile = new UserProfile(name, email, password);
                    usersRef.child(uid).setValue(userProfile);
                }
            });
        }
    }
}