package com.longtraidep.appchat.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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
import com.longtraidep.appchat.Object.Users;
import com.longtraidep.appchat.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton mIBtnBack;
    private CircleImageView mCImvAvatar;
    private TextView mTvEmail, mTvUserId;
    private EditText mEdtUsername;

    private ProgressDialog mPrdDialog;
    private ProgressBar mProgressBar;

    private ExtendedFloatingActionButton mFBtnSave;

    private StorageReference mAvtRef;
    private DatabaseReference mDbRef;
    private FirebaseAuth mAuth;

    private ActivityResultLauncher<Intent> mImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Uri imgPath = data.getData();
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(EditInfoActivity.this);
                        CropImage.ActivityResult result1 = CropImage.getActivityResult(data);
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        mAvtRef = FirebaseStorage.getInstance().getReference();
        mDbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        init();

        mDbRef.child("Users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                FirebaseUser firebaseUserMain = mAuth.getCurrentUser();
                if (firebaseUserMain != null)
                {
                    mTvEmail.setText(firebaseUserMain.getEmail());
                    mTvUserId.setText(firebaseUserMain.getUid());
                }
                mEdtUsername.setText("");

                mEdtUsername.setText(users.getUsername());

                if (users.getImg().toLowerCase().equals("default"))
                    mCImvAvatar.setImageResource(R.mipmap.ic_launcher);
                else Glide.with(getApplicationContext()).load(users.getImg()).into(mCImvAvatar);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.checkStatus("onl");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.checkStatus("off");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressBar.setVisibility(View.GONE);
        mFBtnSave.setVisibility(View.VISIBLE);
    }

    public void init() {
        mIBtnBack = (ImageButton) findViewById(R.id.ibtn_back);
        mCImvAvatar = (CircleImageView) findViewById(R.id.cimv_avatar);
        mTvEmail = (TextView) findViewById(R.id.tv_email);
        mTvUserId = (TextView) findViewById(R.id.tv_id);
        mEdtUsername = (EditText) findViewById(R.id.edt_username);
        mFBtnSave = (ExtendedFloatingActionButton) findViewById(R.id.fbtn_save);
        mProgressBar = findViewById(R.id.prb_loading);

        mFBtnSave.setOnClickListener(this);
        mIBtnBack.setOnClickListener(this);
        mCImvAvatar.setOnClickListener(this);

    }

    @SuppressLint({"NonConstantResourceId", "QueryPermissionsNeeded"})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_back:
                Intent i1 = new Intent(EditInfoActivity.this, UserActivity.class);
                startActivity(i1);
                break;
            case R.id.cimv_avatar:
                Intent i2 = new Intent(Intent.ACTION_GET_CONTENT);
                i2.setType("image/*");
                if (i2.resolveActivity(getPackageManager()) != null)
                    mImageLauncher.launch(i2);
                break;
            case R.id.fbtn_save:
                updateUserName();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                // Upload new avatar to Firebase storage
                StorageReference avtRef = mAvtRef.child(user.getUid()).child("Avatar").child(user.getUid() + ".jpg");
                showLoadingDialog();
                //Upload image to storage
                avtRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            //Get download URL of image just uploaded to pass into Img field to set avatar
                            avtRef.getDownloadUrl().addOnSuccessListener(EditInfoActivity.this, new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Put download URL just got to Img field and set avatar
                                    mDbRef.child("Users").child(user.getUid()).child("Img").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //Set avatar
                                                mDbRef.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        Users users = snapshot.getValue(Users.class);
                                                        if (users.getImg().toLowerCase().equals("default"))
                                                            mCImvAvatar.setImageResource(R.mipmap.ic_launcher);
                                                        else
                                                            Glide.with(EditInfoActivity.this).load(users.getImg()).into(mCImvAvatar);
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                    }
                                                });
                                                Toast.makeText(getApplicationContext(), "Change avatar successful!", Toast.LENGTH_SHORT).show();
                                                mPrdDialog.dismiss();
                                            } else {
                                                Log.i("Upload image error", task.getException().toString());
                                                Toast.makeText(getApplicationContext(), "Change avatar failed!", Toast.LENGTH_SHORT).show();
                                                mPrdDialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(EditInfoActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("Upload image error", task.getException().toString());
                                    Toast.makeText(getApplicationContext(), "Change avatar failed!", Toast.LENGTH_SHORT).show();
                                    mPrdDialog.dismiss();
                                }
                            });
                        } else {
                            Log.i("Upload image error", task.getException().toString());
                            Toast.makeText(getApplicationContext(), "Change avatar failed!", Toast.LENGTH_SHORT).show();
                            mPrdDialog.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void updateUserName() {
        String username = mEdtUsername.getText().toString().trim();

        if (!username.isEmpty() || username != null) {

            DatabaseReference dbref = mDbRef.child("Users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
            dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("LongLogTag")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Users users = snapshot.getValue(Users.class);

                    if (!username.equals(users.getUsername())) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mFBtnSave.setVisibility(View.GONE);

                        dbref.child("Username").setValue(username).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dbref.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Users users1 = snapshot.getValue(Users.class);
                                        mEdtUsername.setText(users1.getUsername());
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                                Toast.makeText(getApplicationContext(), "Changed user name sucessfully!", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(EditInfoActivity.this, UserActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                finish();
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    public void showLoadingDialog() {
        mPrdDialog = new ProgressDialog(EditInfoActivity.this);
        mPrdDialog.show();
        mPrdDialog.setContentView(R.layout.progress_dialog_avatar);
        mPrdDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mPrdDialog.setCanceledOnTouchOutside(false);
    }
}

