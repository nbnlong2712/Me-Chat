package com.longtraidep.appchat.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.inputmethodservice.ExtractEditText;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.longtraidep.appchat.Adapter.MessageAdapter;
import com.longtraidep.appchat.Object.Message;
import com.longtraidep.appchat.R;
import com.longtraidep.appchat.Object.Users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTvBack, mTvUsername;
    private CircleImageView mCImvAvatar;
    private ImageView mImvMenu, mImvSend;
    private Users mUser;
    private AppCompatEditText mEdtMessage;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    private RecyclerView mRcvMessages;
    private MessageAdapter mMessageAdapter;
    private List<Message> mMessageList;

    private ValueEventListener mMessageListener;

    //Path to sender and receiver
    private String mSenderId = "", mReceiverId = "";

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

        init();

        //Set avatar and name for receiver
        mTvUsername.setText(mUser.getUsername());
        if (!mUser.getImg().toLowerCase().equals("default"))
            Glide.with(this).load(mUser.getImg()).into(mCImvAvatar);
        else mCImvAvatar.setImageResource(R.mipmap.ic_launcher);

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
                Toast.makeText(getApplicationContext(), "Menu", Toast.LENGTH_SHORT).show();
                break;
            case R.id.imv_send_message:
                sendMessage();
                break;
        }
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
            messageInfo.put("type", "text");
            messageInfo.put("isSeen", false);

            HashMap<String, Object> messagePath = new HashMap<>();
            messagePath.put(senderRef + "/" + chatDbId, messageInfo);
            messagePath.put(receiverRef + "/" + chatDbId, messageInfo);

            mDatabaseRef.updateChildren(messagePath).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){}
                    else
                    {
                        FirebaseAuthException e = (FirebaseAuthException) task.getException();
                        if (e != null)
                            Log.i("Firebase error","Error code: " + e.getErrorCode() + ", " + e.getMessage());
                    }
                    mEdtMessage.setText("");
                }
            });
        }
    }

    public void displayMessage()
    {
        mMessageList = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(mUser);
        mRcvMessages.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference dbref = mDatabaseRef.child("Chats").child(mSenderId).child(mReceiverId);
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMessageList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Message message = dataSnapshot.getValue(Message.class);
                    mMessageList.add(message);
                }
                mMessageAdapter.setData(mMessageList);
                mRcvMessages.setAdapter(mMessageAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void SeenMessage(String userId)
    {
        DatabaseReference ref = mDatabaseRef.child("Chats").child(mSenderId);
        mMessageListener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 1;
                for (DataSnapshot snapshot1 : snapshot.getChildren())
                {
                    if (count < snapshot.getChildrenCount())
                        count++;
                    else{
                        Message message = snapshot1.getValue(Message.class);
                        if (!mSenderId.equals(message.getSender()))
                        {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isSeen", true);
                            snapshot1.getRef().updateChildren(hashMap);
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
        MainActivity.checkStatus("off");
    }
}
