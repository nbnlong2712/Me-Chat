package com.longtraidep.appchat.Dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.longtraidep.appchat.R;

import java.util.Objects;

public class ImageMessageDialog extends DialogFragment {
    private ImageView mImvClose;
    private PhotoView mPImvAvatar;

    public static ImageMessageDialog newInstance(String imgPath)
    {
        ImageMessageDialog dialog = new ImageMessageDialog();

        Bundle bundle = new Bundle();
        bundle.putString("data", imgPath);
        dialog.setArguments(bundle);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_full_screen_image_message, container, false);
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

        //Get data (image path)
        String data = getArguments().getString("data", "");

        mImvClose = (ImageView) view.findViewById(R.id.imv_close);
        mPImvAvatar = (PhotoView) view.findViewById(R.id.pimv_avatar);

        mImvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        Glide.with(getActivity()).load(data).into(mPImvAvatar);
    }
}
