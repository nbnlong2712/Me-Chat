package com.longtraidep.appchat.Adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.longtraidep.appchat.Dialog.ImageMessageDialog;
import com.longtraidep.appchat.Dialog.PdfMessageDialog;
import com.longtraidep.appchat.Object.Message;
import com.longtraidep.appchat.Object.Users;
import com.longtraidep.appchat.R;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {
    List<Message> mMessages;
    Users mUser;
    int senderTxt = 0, senderImg = 1, senderPdf = 4,
            receiverTxt = 2, receiverImg = 3, receiverPdf = 5;

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
        if (viewType == senderTxt) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sender, parent, false);
            return new MessageHolder(view);
        } else if (viewType == receiverTxt) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_receiver, parent, false);
            return new MessageHolder(view);
        } else if (viewType == senderImg) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_sender, parent, false);
            return new MessageHolder(view);
        } else if (viewType == receiverImg) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_receiver, parent, false);
            return new MessageHolder(view);
        } else if (viewType == senderPdf) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_sender, parent, false);
            return new MessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_receiver, parent, false);
            return new MessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        Message message = mMessages.get(position);
        //SENDER
        if (!message.getSender().equals(mUser.getId())) {
            //Type
            if (message.getType().equals("text")) {
                holder.mTvMessageSender.setText(message.getMessage());
            } else if (message.getType().equals("img")) {
                Glide.with(holder.itemView.getContext()).load(message.getMessage()).into(holder.mImageView);
            } else if (message.getType().equals("pdf")) {
                holder.mTvMessageSender.setText(message.getName());
                holder.mTvMessageSender.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("pdf test", message.getMessage());
                        FragmentManager fm = ((FragmentActivity)holder.itemView.getContext()).getSupportFragmentManager();
                        PdfMessageDialog pdfMessageDialog = PdfMessageDialog.newInstance(message.getMessage());
                        pdfMessageDialog.show(fm, "pdf");
                    }
                });
            }

            //Seen
            if (position == (mMessages.size() - 1)) {
                if (message.getSeen().equals("true")) {
                    holder.mTvSeenMessage.setVisibility(View.VISIBLE);
                    holder.mTvSeenMessage.setText(R.string.seen);
                } else {
                    holder.mTvSeenMessage.setVisibility(View.VISIBLE);
                    holder.mTvSeenMessage.setText(R.string.deliveried);
                }
            } else holder.mTvSeenMessage.setVisibility(View.GONE);
        }
        //RECEIVER
        else {
            //Type
            if (message.getType().equals("text")) {
                holder.mTvMessageReceiver.setText(message.getMessage());
            } else if (message.getType().equals("img")) {
                Glide.with(holder.itemView.getContext()).load(message.getMessage()).into(holder.mImageView);
            } else if (message.getType().equals("pdf")) {
                holder.mTvMessageReceiver.setText(message.getName());
                holder.mTvMessageReceiver.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentManager fm = ((FragmentActivity)holder.itemView.getContext()).getSupportFragmentManager();
                        PdfMessageDialog pdfMessageDialog = PdfMessageDialog.newInstance(message.getMessage());
                        pdfMessageDialog.show(fm, "pdf");
                    }
                });
            }

            //Seen
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
        if (mMessages.get(position).getSender().equals(firebaseUser.getUid())) {
            if (mMessages.get(position).getType().equals("text"))
                return senderTxt;
            else if (mMessages.get(position).getType().equals("img"))
                return senderImg;
            else if (mMessages.get(position).getType().equals("pdf"))
                return senderPdf;
        } else if (!mMessages.get(position).getSender().equals(firebaseUser.getUid())) {
            if (mMessages.get(position).getType().equals("text"))
                return receiverTxt;
            else if (mMessages.get(position).getType().equals("img"))
                return receiverImg;
            else if (mMessages.get(position).getType().equals("pdf"))
                return receiverPdf;
        }

        return 1000;
    }

    public class MessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView mCImvAvater;
        TextView mTvMessageReceiver, mTvMessageSender, mTvSeenMessage;
        ImageView mImageView;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            mTvSeenMessage = (TextView) itemView.findViewById(R.id.tv_seen_message);
            mCImvAvater = (CircleImageView) itemView.findViewById(R.id.cimv_avatar_receiver);
            mTvMessageReceiver = (TextView) itemView.findViewById(R.id.tv_message_receiver);
            mTvMessageSender = (TextView) itemView.findViewById(R.id.tv_message_sender);
            mImageView = (ImageView) itemView.findViewById(R.id.imv_message);

//            mImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Message message = mMessages.get(getAdapterPosition());
            if (message.getType().equals("img")) {
                FragmentManager fm = ((FragmentActivity) v.getContext()).getSupportFragmentManager();
                ImageMessageDialog dialog = ImageMessageDialog.newInstance(message.getMessage());
                dialog.show(fm, "message");
            }

        }

    }
}

