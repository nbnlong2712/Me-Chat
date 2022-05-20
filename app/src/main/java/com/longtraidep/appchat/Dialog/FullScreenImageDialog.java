package com.longtraidep.appchat.Dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.longtraidep.appchat.Activity.EditInfoActivity;
import com.longtraidep.appchat.R;

import java.util.Objects;

public class FullScreenImageDialog extends DialogFragment {
    private ImageView mImvClose;
    private PhotoView mPImvAvatar;
    private ExtendedFloatingActionButton mFBtnUpdateAvatar;

    //Được dùng khi khởi tạo fragment mục đích nhận giá trị
    public static FullScreenImageDialog newInstance(String imgPath)
    {
        FullScreenImageDialog dialog = new FullScreenImageDialog();
        Bundle args = new Bundle();
        args.putString("data", imgPath);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_full_screen_image, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = Objects.requireNonNull(getDialog()).getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Get image path (data) from bundle (args)
        String data = getArguments().getString("data", "default");

        mImvClose = (ImageView) view.findViewById(R.id.imv_close);
        mPImvAvatar = (PhotoView) view.findViewById(R.id.pimv_avatar);
        mFBtnUpdateAvatar = (ExtendedFloatingActionButton) view.findViewById(R.id.fbtn_edit_avatar);

        if (data.toLowerCase().equals("default"))
            mPImvAvatar.setImageResource(R.mipmap.ic_launcher);
        else Glide.with(getActivity()).load(data).into(mPImvAvatar);

        mImvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getDialog()).dismiss();
            }
        });

        mFBtnUpdateAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), EditInfoActivity.class);
                startActivity(i);
            }
        });
    }
}
