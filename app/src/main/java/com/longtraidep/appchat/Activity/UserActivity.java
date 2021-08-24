package com.longtraidep.appchat.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.longtraidep.appchat.R;
import com.longtraidep.appchat.Object.Users;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTvUsername, mTvUserId, mTvEmail;
    private ExtendedFloatingActionButton mFBtnEdit, mFBtnLogout;

    private CircleImageView mCImvAvatar;

    private ImageButton mIBtnBack;
    FirebaseUser mFirebaseUser;
    DatabaseReference mDatabaseReference;

    private ProgressDialog mPrdLogin;

    private Users mUser = new Users();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        init();

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUser = snapshot.getValue(Users.class);
                if (mUser != null)
                    mTvUsername.setText(mUser.getUsername());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        setInfoUser();
    }

    public void init()
    {
        mIBtnBack = (ImageButton) findViewById(R.id.ibtn_back);
        mTvEmail = (TextView) findViewById(R.id.tv_email);
        mTvUserId = (TextView) findViewById(R.id.tv_id);
        mTvUsername = (TextView) findViewById(R.id.tv_username);
        mCImvAvatar = (CircleImageView) findViewById(R.id.cimv_avatar);
        mFBtnEdit = (ExtendedFloatingActionButton) findViewById(R.id.fbtn_edit);
        mFBtnLogout = (ExtendedFloatingActionButton) findViewById(R.id.fbtn_logout);

        mFBtnEdit.setOnClickListener(this);
        mFBtnLogout.setOnClickListener(this);
        mIBtnBack.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ibtn_back:
                startActivity(new Intent(UserActivity.this, MainActivity.class));
                break;
            case R.id.fbtn_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserActivity.this, LoginActivity.class));
                mPrdLogin = new ProgressDialog(UserActivity.this);
                mPrdLogin.show();
                mPrdLogin.setContentView(R.layout.progress_dialog_auth);
                mPrdLogin.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                break;
        }
    }

    public void setInfoUser()
    {
        mTvUserId.setText(mFirebaseUser.getUid());
        mTvEmail.setText(mFirebaseUser.getEmail());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPrdLogin.dismiss();
    }
}
