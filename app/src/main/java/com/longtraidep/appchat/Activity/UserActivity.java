package com.longtraidep.appchat.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.longtraidep.appchat.Dialog.FullScreenImageDialog;
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
                if (mUser != null) {
                    mTvUsername.setText(mUser.getUsername());
                    if (mUser.getImg().toLowerCase().equals("default"))
                        mCImvAvatar.setImageResource(R.mipmap.ic_launcher);
                    else Glide.with(UserActivity.this).load(mUser.getImg()).into(mCImvAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        setInfoUser();
    }

    public void init() {
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
        mCImvAvatar.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_back:
                startActivity(new Intent(UserActivity.this, MainActivity.class));
                break;
            case R.id.fbtn_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserActivity.this, LoginActivity.class).
                        setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                showLoadingDialog();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                break;
            case R.id.cimv_avatar:
                FragmentManager fm = getSupportFragmentManager();
                FullScreenImageDialog imageDialog = FullScreenImageDialog.newInstance(mUser.getImg());
                imageDialog.show(fm, "avatar");
                break;
            case R.id.fbtn_edit:
                Intent i = new Intent(UserActivity.this, EditInfoActivity.class);
                startActivity(i);
                break;
        }
    }

    public void showLoadingDialog()
    {
        mPrdLogin = new ProgressDialog(UserActivity.this);
        mPrdLogin.show();
        mPrdLogin.setContentView(R.layout.progress_dialog_auth);
        mPrdLogin.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mPrdLogin.setCanceledOnTouchOutside(false);
    }

    public void setInfoUser() {
        mTvUserId.setText(mFirebaseUser.getUid());
        mTvEmail.setText(mFirebaseUser.getEmail());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPrdLogin.dismiss();
    }
}
