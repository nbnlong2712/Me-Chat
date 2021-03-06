package com.longtraidep.appchat.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.longtraidep.appchat.Adapter.ViewPagerAdapter;
import com.longtraidep.appchat.R;
import com.longtraidep.appchat.Object.Users;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseUser mFirebaseUser;
    DatabaseReference mDatabaseReference;
    private ViewPager2 mViewPager;
    private TabLayout mTabLayout;

    private ExtendedFloatingActionButton mFBtnUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null)
        {
            updateToken(mFirebaseUser);
        }
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        checkStatus("onl");

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        mViewPager.setAdapter(viewPagerAdapter);
        new TabLayoutMediator(mTabLayout, mViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText(R.string.chat);
                        break;
                    case 1:
                        tab.setText(R.string.user);
                        break;
                }
            }
        }).attach();
    }

    public void init() {
        mFBtnUser = (ExtendedFloatingActionButton) findViewById(R.id.fbtn_user);
        mViewPager = (ViewPager2) findViewById(R.id.vpg_main);
        mTabLayout = (TabLayout) findViewById(R.id.tablayout_main);

        mFBtnUser.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fbtn_user:
                startActivity(new Intent(MainActivity.this, UserActivity.class));
                break;
        }
    }

    public void updateToken(FirebaseUser firebaseUser) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(MainActivity.this, new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    mDatabaseReference.child("Users").child(firebaseUser.getUid()).child("Token").setValue(task.getResult())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task1) {
                                    if (task1.isSuccessful())
                                        Log.i("Token success", task.getResult());
                                    else Log.i("Token failed", Objects.requireNonNull(task.getException()).getMessage());
                                }
                            });
                }
            }
        });
    }

    public static void checkStatus(String state) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("State", state);

        FirebaseDatabase.getInstance().getReference().
                child("Users").
                child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkStatus("onl");
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkStatus("off");
    }
}
