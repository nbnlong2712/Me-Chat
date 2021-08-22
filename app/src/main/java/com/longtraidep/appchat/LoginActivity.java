package com.longtraidep.appchat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtnLogin;
    private TextInputEditText mEdtEmail, mEdtPassword;
    private TextView mTvRegister;
    private ProgressDialog mPrdLogin;

    private FirebaseAuth mAuth;

    private String mEmail = "", mPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Firebase auth (remember to import library Firebase auth in build.gradle)
        mAuth = FirebaseAuth.getInstance();

        init();

        mBtnLogin.setOnClickListener(this);
    }

    public void init() {
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mEdtEmail = (TextInputEditText) findViewById(R.id.edt_email);
        mEdtPassword = (TextInputEditText) findViewById(R.id.edt_password);
        mTvRegister = (TextView) findViewById(R.id.tv_register);

        mTvRegister.setOnClickListener(this);
    }

    public void loginUser(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // User login sucessful
                        if (task.isSuccessful())
                        {
                            FirebaseUser user = mAuth.getCurrentUser();

                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            //clear task và new task để khi user login thành công, nếu nhấn back sẽ ko trở lại đc màn hình login vì task đó đã đc clear và tạo mới
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            finish();
                        }
                        //User login fail
                        else
                        {
                            mPrdLogin.dismiss();
                            Toast.makeText(getApplicationContext(), "Invalid email or password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        mPrdLogin.dismiss();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.tv_register:
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
                break;
            case R.id.btn_login:
                mEmail = mEdtEmail.getText().toString().trim();
                mPassword = mEdtPassword.getText().toString().trim();
                if(mEmail.isEmpty() || mPassword.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Please fill enough!", Toast.LENGTH_SHORT).show();
                }
                else {
                    mPrdLogin = new ProgressDialog(LoginActivity.this);
                    mPrdLogin.show();
                    mPrdLogin.setContentView(R.layout.progress_dialog_auth);
                    mPrdLogin.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    loginUser(mEmail, mPassword);
                }
                break;
        }
    }
}