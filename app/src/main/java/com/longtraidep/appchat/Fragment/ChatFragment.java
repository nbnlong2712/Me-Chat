package com.longtraidep.appchat.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.longtraidep.appchat.Adapter.UserAdapter;
import com.longtraidep.appchat.Object.Message;
import com.longtraidep.appchat.Object.Users;
import com.longtraidep.appchat.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatFragment extends Fragment {
    private RecyclerView mRcvChatUsers;
    private List<Users> mUsers;
    private UserAdapter mUserAdapter;
    private TextView mTvNotifyAmountUser;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRf;

    private ProgressBar mPrbLoading;

    public ChatFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        mUsers = new ArrayList<>();
        mUserAdapter = new UserAdapter();

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRf = FirebaseDatabase.getInstance().getReference();

        init(view);

        getChatUsers();

        return view;
    }

    public void init(View view){
        mRcvChatUsers = (RecyclerView) view.findViewById(R.id.rcv_chat_user);
        mPrbLoading = (ProgressBar) view.findViewById(R.id.prb_loading);
        mTvNotifyAmountUser = (TextView) view.findViewById(R.id.tv_amount_user);
    }

    public void getChatUsers()
    {
        mPrbLoading.setVisibility(View.VISIBLE);
        List<String> listUid = new ArrayList<>();
        DatabaseReference dbref = mDatabaseRf.child("Chats").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot: snapshot.getChildren())
                {
                    listUid.add(dataSnapshot.getKey());
                }

                DatabaseReference dbref1 = mDatabaseRf.child("Users");
                dbref1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mUsers.clear();

                        for (DataSnapshot dataSnapshot: snapshot.getChildren())
                        {
                            Users users = dataSnapshot.getValue(Users.class);
                            if (listUid.contains(users.getId()))
                                mUsers.add(users);
                        }
                        if (mUsers.isEmpty() || mUsers==null)
                        {
                            mTvNotifyAmountUser.setVisibility(View.VISIBLE);
                        }
                        else mTvNotifyAmountUser.setVisibility(View.GONE);

                        mPrbLoading.setVisibility(View.GONE);
                        mUserAdapter.setData(mUsers);
                        mRcvChatUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
                        mRcvChatUsers.setAdapter(mUserAdapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
