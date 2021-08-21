package com.longtraidep.appchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

// token github: 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
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtnRegister;
    private TextInputEditText mEdtFullName, mEdtEmail, mEdtPassword;
    private TextView mTvLogin;

    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;
    String mEmail = "", mPassword = "", mFullname = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Set up Firebase auth
        mAuth = FirebaseAuth.getInstance();

        init();


        mBtnRegister.setOnClickListener(this);
    }

    public void init() {
        mBtnRegister = (Button) findViewById(R.id.btn_register);
        mEdtFullName = (TextInputEditText) findViewById(R.id.edt_fullname);
        mEdtEmail = (TextInputEditText) findViewById(R.id.edt_email);
        mEdtPassword = (TextInputEditText) findViewById(R.id.edt_password);
        mTvLogin = (TextView) findViewById(R.id.tv_login);

        mTvLogin.setOnClickListener(this);
    }

    public void registerNewUser(String fullname, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //If register sucessful
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userId = "";
                            if (firebaseUser != null)
                                userId = firebaseUser.getUid();

                            mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                            // HashMap
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("Id", userId);
                            hashMap.put("Username", fullname);
                            hashMap.put("Img", "Default");

                            //After register sucessful, redirect user to LoginActivity
                            mDatabaseRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        Toast.makeText(getApplicationContext(), "Register successful!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            });
                        }
                        //If register fail
                        else {
                            FirebaseAuthException e = (FirebaseAuthException) task.getException();
                            if (e != null)
                                Log.i("Firebase auth test", e.getMessage() + ", " + e.getErrorCode());
                            Toast.makeText(getApplicationContext(), "Invalid email or password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_login:
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
                break;
            case R.id.btn_register:
                mEmail = mEdtEmail.getText().toString().trim();
                mPassword = mEdtPassword.getText().toString().trim();
                mFullname = mEdtFullName.getText().toString().trim();

                if (mEmail.isEmpty() || mPassword.isEmpty() || mFullname.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill enough!", Toast.LENGTH_SHORT).show();
                } else {
                    registerNewUser(mFullname, mEmail, mPassword);
                }
                break;
        }
    }
}

