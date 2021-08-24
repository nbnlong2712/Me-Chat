package com.longtraidep.appchat.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.longtraidep.appchat.Adapter.UserAdapter;
import com.longtraidep.appchat.R;
import com.longtraidep.appchat.User.Users;

import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment {

    private RecyclerView mRcvUsers;
    private DatabaseReference mDatabaseReference;
    private UserAdapter mUserAdapter;
    private List<Users> mUsers;

    public UserFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        mUsers = new ArrayList<>();
        init(view);

        mUserAdapter = new UserAdapter();

        readDataFromFirebase();

        return view;
    }

    public void init(View view) {
        mRcvUsers = (RecyclerView) view.findViewById(R.id.rcv_user);
    }

    public void readDataFromFirebase() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren())
                {
                    Users users = snapshot1.getValue(Users.class);
                    if (!users.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                    {
                        mUsers.add(users);
                    }
                }
                mUserAdapter.setData(mUsers);
                mRcvUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
                mRcvUsers.setAdapter(mUserAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
