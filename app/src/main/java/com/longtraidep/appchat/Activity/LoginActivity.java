package com.longtraidep.appchat.Activity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.longtraidep.appchat.Object.Users;
import com.longtraidep.appchat.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtnLogin;
    private TextInputEditText mEdtEmail, mEdtPassword;
    private TextView mTvRegister;
    private ProgressDialog mPrdLogin;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String fullname = "";
    private String idFb = "";

    private LoginButton mBtnLoginFacebook;
    CallbackManager mCallbackManager;
    private ProgressBar mPrgFacebookLogin;

    private String mEmail = "", mPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //setup for Facebook login
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        mCallbackManager = CallbackManager.Factory.create();

        //Firebase auth (remember to import library Firebase auth in build.gradle)
        mAuth = FirebaseAuth.getInstance();
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUser = mAuth.getCurrentUser();
        if (mUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    // Initialize and mapping views
    public void init() {
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mEdtEmail = (TextInputEditText) findViewById(R.id.edt_email);
        mEdtPassword = (TextInputEditText) findViewById(R.id.edt_password);
        mTvRegister = (TextView) findViewById(R.id.tv_register);
        mBtnLoginFacebook = (LoginButton) findViewById(R.id.btn_login_facebook);
        mPrgFacebookLogin = (ProgressBar) findViewById(R.id.prb_loading);

        mBtnLogin.setOnClickListener(this);
        mTvRegister.setOnClickListener(this);

        mBtnLoginFacebook.setOnClickListener(this);
        mBtnLoginFacebook.setReadPermissions("email", "public_profile");
    }

    // Login with email
    public void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // User login sucessful
                        if (task.isSuccessful()) {
                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            //clear task và new task để khi user login thành công, nếu nhấn back sẽ ko trở lại đc màn hình login vì task đó đã đc clear và tạo mới
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            finish();
                        }
                        //User login fail
                        else {
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

    // ON CLICKED
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_register:
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
                break;
            case R.id.btn_login:
                mEmail = mEdtEmail.getText().toString().trim();
                mPassword = mEdtPassword.getText().toString().trim();
                if (mEmail.isEmpty() || mPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill enough!", Toast.LENGTH_SHORT).show();
                } else {
                    mPrdLogin = new ProgressDialog(LoginActivity.this);
                    mPrdLogin.show();
                    mPrdLogin.setContentView(R.layout.progress_dialog_auth);
                    mPrdLogin.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    mPrdLogin.setCanceledOnTouchOutside(false);
                    loginUser(mEmail, mPassword);
                }
                break;
            case R.id.btn_login_facebook:
                loginFacebook();
                break;
        }
    }

    // FACEBOOK
    public void loginFacebook() {
        // Callback registration
        mBtnLoginFacebook.setVisibility(View.GONE);
        mPrgFacebookLogin.setVisibility(View.VISIBLE);
        mBtnLoginFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("Success facebook", loginResult.toString());
                saveFacebookCredentialToFirebase(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.i("Cancel facebook", "Cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.i("Error facebook", exception.getMessage());
            }
        });
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
    }

    private void saveFacebookCredentialToFirebase(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user1 = mAuth.getCurrentUser();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user1.getUid());
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Users users = snapshot.getValue(Users.class);
                            if (users.getImg() == null) {
                                //String url = "https://graph.facebook.com/" + idFb + "/picture?type=large";
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("Id", user1.getUid());
                                hashMap.put("Username", fullname);
                                hashMap.put("Img", "Default");
                                databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.i("Error facebook database", error.getMessage());
                        }
                    });

                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                } else {
                    mBtnLoginFacebook.setVisibility(View.VISIBLE);
                    mPrgFacebookLogin.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Login with Facebook failed!", Toast.LENGTH_SHORT).show();
                    Log.i("Login Facebook", task.getException().getMessage());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Get Facebook info (name, picture,...)
        GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(@Nullable JSONObject jsonObject, @Nullable GraphResponse graphResponse) {
                Log.i("Result json", jsonObject.toString());
                Log.i("Result graph", graphResponse.toString());

                try {
                    fullname = jsonObject.getString("name");
                    idFb = jsonObject.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString("facebook_info", "name, id");
        graphRequest.setParameters(bundle);
        graphRequest.executeAsync();
    }
}