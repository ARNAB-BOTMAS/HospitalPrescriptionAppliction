package com.example.hospitalprescription;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class LoginActivity extends AppCompatActivity {

    public TextInputEditText emailInputText, passwordInputText;
    public TextView textViewGoto;
    public Button logBtn;
    public FirebaseAuth mAuth;
    public FirebaseUser User;
    public ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        emailInputText = findViewById(R.id.email);
        passwordInputText = findViewById(R.id.password);
        logBtn = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.progressBar);
        textViewGoto = findViewById(R.id.gotoReg);
        User = mAuth.getCurrentUser();


        if (User != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            textViewGoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            logBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    String email, password;

                    email = String.valueOf(emailInputText.getText());
                    password = String.valueOf(passwordInputText.getText());

                    if (TextUtils.isEmpty(email)){
                        Toast.makeText(LoginActivity.this, "Email is empty", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (TextUtils.isEmpty(password)) {
                        Toast.makeText(LoginActivity.this, "Password is empty", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        signInUser(email, password);
                    }
                }

                private void signInUser(String email, String password) {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
//                                    FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(LoginActivity.this, "Log in Successful",
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Log in failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
        }
    }
}