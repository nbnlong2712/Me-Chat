package com.longtraidep.appchat.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.longtraidep.appchat.Activity.MessageActivity;
import com.longtraidep.appchat.R;
import com.longtraidep.appchat.Object.Users;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {
    List<Users> mUsers;

    public UserAdapter() {}

    public void setData(List<Users> users)
    {
        mUsers = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        Users users = mUsers.get(position);
        holder.mTextView.setText(users.getUsername());
        if (users.getImg().toLowerCase().equals("default"))
            holder.mCircleImageView.setImageResource(R.mipmap.ic_launcher);
        else Glide.with(holder.itemView).load(users.getImg()).into(holder.mCircleImageView);
    }

    @Override
    public int getItemCount() {
        if (mUsers != null)
            return mUsers.size();
        return 0;
    }

    public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView mCircleImageView;
        TextView mTextView;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mCircleImageView = itemView.findViewById(R.id.cimv_avatar);
            mTextView = itemView.findViewById(R.id.tv_username);
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(v.getContext(), MessageActivity.class);
            i.putExtra("user", mUsers.get(getAdapterPosition()));
            v.getContext().startActivity(i);
        }
    }
}
