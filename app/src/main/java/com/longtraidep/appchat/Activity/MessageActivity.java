package com.longtraidep.appchat.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.inputmethodservice.ExtractEditText;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.longtraidep.appchat.API.NotificationMessageAPI;
import com.longtraidep.appchat.Adapter.MessageAdapter;
import com.longtraidep.appchat.Object.Data;
import com.longtraidep.appchat.Object.Message;
import com.longtraidep.appchat.Object.Notification;
import com.longtraidep.appchat.R;
import com.longtraidep.appchat.Object.Users;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTvBack, mTvUsername;
    private CircleImageView mCImvAvatar;
    private ImageView mImvMenu, mImvSend;
    private Users mUser;
    private AppCompatEditText mEdtMessage;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private StorageReference mStoreRef;

    private RecyclerView mRcvMessages;
    private MessageAdapter mMessageAdapter;
    private List<Message> mMessageList;

    private ValueEventListener mMessageListener;

    private ProgressDialog mPrdDialog;

    //Path to sender and receiver
    private String mSenderId = "", mReceiverId = "";

    private ActivityResultLauncher<Intent> mImgLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        // Get image
                        Intent data = result.getData();
                        Uri imgPath = data.getData();

                        showImageLoadingDialog();

                        DatabaseReference db = mDatabaseRef.child("Chats").child(mSenderId).child(mReceiverId).push();
                        String senderRef = "Chats/" + mSenderId + "/" + mReceiverId;
                        String receiverRef = "Chats/" + mReceiverId + "/" + mSenderId;
                        String key = db.getKey();

                        // Upload image to Sender storage
                        StorageReference storeRef = mStoreRef.child(mSenderId).child(mReceiverId).child(imgPath.getLastPathSegment() + ".jpg");
                        storeRef.putFile(imgPath).addOnCompleteListener(MessageActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    // If task successful, get download URL and upload to firebase realtime Chats
                                    storeRef.getDownloadUrl().addOnSuccessListener(MessageActivity.this, new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            //Prepare data
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("sender", mSenderId);
                                            hashMap.put("message", uri.toString());
                                            hashMap.put("name", "");
                                            hashMap.put("type", "img");
                                            hashMap.put("seen", "false");

                                            //Push data to realtime database
                                            HashMap<String, Object> messagePath = new HashMap<>();
                                            messagePath.put(senderRef + "/" + key, hashMap);
                                            mDatabaseRef.updateChildren(messagePath).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    mPrdDialog.dismiss();
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        });
                        //Upload image to Receiver storage
                        StorageReference storageRef1 = mStoreRef.child(mReceiverId).child(mSenderId).child(imgPath.getLastPathSegment() + ".jpg");
                        storageRef1.putFile(imgPath).addOnCompleteListener(MessageActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    storageRef1.getDownloadUrl().addOnSuccessListener(MessageActivity.this, new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            //Prepare data
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("sender", mSenderId);
                                            hashMap.put("message", uri.toString());
                                            hashMap.put("name", "");
                                            hashMap.put("type", "img");
                                            hashMap.put("seen", "false");

                                            //Push data to realtime database
                                            HashMap<String, Object> messagePath = new HashMap<>();
                                            messagePath.put(receiverRef + "/" + key, hashMap);
                                            mDatabaseRef.updateChildren(messagePath).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {}
                                            });
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> mPdfLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK)
                    {
                        Intent data = result.getData();
                        Uri pdfPath = data.getData();

                        DatabaseReference db = mDatabaseRef.child("Chats").child(mSenderId).child(mReceiverId).push();
                        String senderRef = "Chats/" + mSenderId + "/" + mReceiverId;
                        String receiverRef = "Chats/" + mReceiverId + "/" + mSenderId;
                        String key = db.getKey();

                        // Upload pdf file to Sender storage
                        showPdfLoadingDialog();
                        StorageReference storeRef = mStoreRef.child(mSenderId).child(mReceiverId).child(pdfPath.getLastPathSegment() + ".pdf");
                        storeRef.putFile(pdfPath).addOnCompleteListener(MessageActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    storeRef.getDownloadUrl().addOnSuccessListener(MessageActivity.this, new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            //Prepare data
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("sender", mSenderId);
                                            hashMap.put("message", uri.toString());
                                            hashMap.put("name", getNamePdfFile(pdfPath));
                                            hashMap.put("type", "pdf");
                                            hashMap.put("seen", "false");

                                            HashMap<String, Object> messagePath = new HashMap<>();
                                            messagePath.put(senderRef + "/" + key, hashMap);
                                            mDatabaseRef.updateChildren(messagePath);
                                            mPrdDialog.dismiss();

                                            // Upload pdf file to Receiver storage
                                            StorageReference storeRef1 = mStoreRef.child(mReceiverId).child(mSenderId).child(pdfPath.getLastPathSegment() + ".pdf");
                                            storeRef1.putFile(pdfPath).addOnCompleteListener(MessageActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        HashMap<String, Object> messPath = new HashMap<>();
                                                        messPath.put(receiverRef + "/" + key, hashMap);
                                                        mDatabaseRef.updateChildren(messPath);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        });
                        Log.i("PDF path", pdfPath.getPath() + ", " + getNamePdfFile(pdfPath));
                    }
                }
            }
    );

    public String getNamePdfFile(Uri uri)
    {
        File myFile = new File(uri.toString());
        String displayName = "";

        if (uri.toString().startsWith("content://"))
        {
            Cursor cursor = null;
            try
            {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst())
                {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            finally
            {
                cursor.close();
            }
        }
        else if (uri.toString().startsWith("file://"))
        {
            displayName = myFile.getName();
        }
        return displayName;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //Get chosen user from message list
        Intent intent = getIntent();
        if (intent != null)
            mUser = (Users) intent.getSerializableExtra("user");

        //Get ID of sender and receiver
        mAuth = FirebaseAuth.getInstance();
        mSenderId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mReceiverId = mUser.getId();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mStoreRef = FirebaseStorage.getInstance().getReference();

        init();

        //Set avatar and name for receiver
        mTvUsername.setText(mUser.getUsername());
        if (!mUser.getImg().toLowerCase().equals("default"))
            Glide.with(this).load(mUser.getImg()).into(mCImvAvatar);
        else mCImvAvatar.setImageResource(R.mipmap.ic_launcher);

        seenMessage();
        displayMessage();
    }

    public void init() {
        mTvBack = (TextView) findViewById(R.id.tv_back);
        mTvUsername = (TextView) findViewById(R.id.tv_username);
        mCImvAvatar = (CircleImageView) findViewById(R.id.cimv_avatar);
        mImvMenu = (ImageView) findViewById(R.id.imv_menu_vertical);
        mImvSend = (ImageView) findViewById(R.id.imv_send_message);
        mEdtMessage = (AppCompatEditText) findViewById(R.id.edt_message);
        mRcvMessages = (RecyclerView) findViewById(R.id.rcv_message);

        mTvBack.setOnClickListener(this);
        mImvMenu.setOnClickListener(this);
        mImvSend.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                Intent i = new Intent(MessageActivity.this, MainActivity.class);
                startActivity(i);
                break;
            case R.id.imv_menu_vertical:
                sendFiles();
                break;
            case R.id.imv_send_message:
                sendMessage();
                break;
        }
    }

    public void sendFiles() {
        CharSequence options[] = new CharSequence[]{
                "Images",
                "PDF Files",
                "Doc Files"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this)
                .setTitle("Select files")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            if (intent.resolveActivity(getPackageManager()) != null)
                                mImgLauncher.launch(intent);
                        } else if (which == 1) {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            if (intent.resolveActivity(getPackageManager()) != null)
                                mPdfLauncher.launch(intent);
                        } else if (which == 2) {

                        }
                    }
                });
        builder.show();
    }

    public void sendMessage() {
        String message = Objects.requireNonNull(mEdtMessage.getText()).toString();

        if (!message.equals("")) {
            //Get ref to sender and receiver in Chats
            String senderRef = "Chats/" + mSenderId + "/" + mReceiverId;
            String receiverRef = "Chats/" + mReceiverId + "/" + mSenderId;

            DatabaseReference chatDbRef = mDatabaseRef.child("Chats").child(mSenderId).child(mReceiverId).push();

            String chatDbId = chatDbRef.getKey();

            HashMap<String, Object> messageInfo = new HashMap<>();
            messageInfo.put("sender", mSenderId);
            messageInfo.put("message", message);
            messageInfo.put("name", "");
            messageInfo.put("type", "text");
            messageInfo.put("seen", "false");

            HashMap<String, Object> messagePath = new HashMap<>();
            messagePath.put(senderRef + "/" + chatDbId, messageInfo);
            messagePath.put(receiverRef + "/" + chatDbId, messageInfo);

            mDatabaseRef.updateChildren(messagePath).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        // --- Push notification here --- //
                        Data data = new Data(mUser.getUsername(), message, mUser.getImg());
                        Notification notification = new Notification(data, mUser.getToken());
                        NotificationMessageAPI.mNotificationMessageAPI.postNotification(notification).enqueue(new Callback<Notification>() {
                            @Override
                            public void onResponse(Call<Notification> call, Response<Notification> response) {
                                Log.i("Success FCM", "Success push notification");
                            }

                            @Override
                            public void onFailure(Call<Notification> call, Throwable t) {
                                Log.i("Failed FCM", t.getMessage());
                            }
                        });
                    } else {
                        FirebaseAuthException e = (FirebaseAuthException) task.getException();
                        if (e != null)
                            Log.i("Firebase error", "Error code: " + e.getErrorCode() + ", " + e.getMessage());
                    }
                    mEdtMessage.setText("");
                }
            });
        }
    }

    public void displayMessage() {
        mMessageList = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(mUser);
        mRcvMessages.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference dbref = mDatabaseRef.child("Chats").child(mSenderId).child(mReceiverId);
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMessageList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    mMessageList.add(message);
                }
                mMessageAdapter.setData(mMessageList);
                mRcvMessages.setAdapter(mMessageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void seenMessage() {
        DatabaseReference ref = mDatabaseRef.child("Chats").child(mSenderId).child(mReceiverId);
        mMessageListener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 1;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (count < snapshot.getChildrenCount())
                        count++;
                    else {
                        Message message = snapshot1.getValue(Message.class);
                        if (!mSenderId.equals(message.getSender())) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("seen", "true");
                            snapshot1.getRef().updateChildren(hashMap);
                            mDatabaseRef.child("Chats").child(mReceiverId).child(mSenderId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshott) {
                                    for (DataSnapshot snapshot2 : snapshott.getChildren()) {
                                        if (snapshot1.getKey().equals(snapshot2.getKey())) {
                                            HashMap<String, Object> hashMap1 = new HashMap<>();
                                            hashMap1.put("seen", "true");
                                            snapshot2.getRef().updateChildren(hashMap1);
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }
                    }
                }
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
        mDatabaseRef.removeEventListener(mMessageListener);
        MainActivity.checkStatus("off");
    }

    public void showImageLoadingDialog() {
        mPrdDialog = new ProgressDialog(MessageActivity.this);
        mPrdDialog.show();
        mPrdDialog.setContentView(R.layout.progress_dialog_send_image);
        mPrdDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mPrdDialog.setCanceledOnTouchOutside(false);
    }

    public void showPdfLoadingDialog() {
        mPrdDialog = new ProgressDialog(MessageActivity.this);
        mPrdDialog.show();
        mPrdDialog.setContentView(R.layout.progress_dialog_send_pdf);
        mPrdDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mPrdDialog.setCanceledOnTouchOutside(false);
    }

}
