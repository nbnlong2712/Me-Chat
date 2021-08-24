package com.longtraidep.appchat.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.longtraidep.appchat.Object.Message;
import com.longtraidep.appchat.Object.Users;
import com.longtraidep.appchat.R;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {
    List<Message> mMessages;
    Users mUser;
    int sender = 0, receiver = 1;

    public MessageAdapter(Users users) {
        this.mUser = users;
    }

    public void setData(List<Message> messages) {
        this.mMessages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //tham số viewType chính là kết quả trả về của getItemViewType()
        if (viewType == sender) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sender, parent, false);
            return new MessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_receiver, parent, false);
            return new MessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        Message message = mMessages.get(position);
        if (!message.getSender().equals(mUser.getId()))
            holder.mTvMessageSender.setText(message.getMessage());
        else {
            holder.mTvMessageReceiver.setText(message.getMessage());
            if (mUser.getImg().toLowerCase().equals("default"))
                holder.mCImvAvater.setImageResource(R.mipmap.ic_launcher);
            else
                Glide.with(holder.itemView).load(mUser.getImg()).into(holder.mCImvAvater);
        }
    }

    @Override
    public int getItemCount() {
        if (mMessages != null)
            return mMessages.size();
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser firebaseUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser());
        if (mMessages.get(position).getSender().equals(firebaseUser.getUid()))
            return sender;
        else return receiver;
    }

    public class MessageHolder extends RecyclerView.ViewHolder {
        CircleImageView mCImvAvater;
        TextView mTvMessageReceiver, mTvMessageSender;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            mCImvAvater = itemView.findViewById(R.id.cimv_avatar_receiver);
            mTvMessageReceiver = itemView.findViewById(R.id.tv_message_receiver);
            mTvMessageSender = itemView.findViewById(R.id.tv_message_sender);
        }
    }
}
