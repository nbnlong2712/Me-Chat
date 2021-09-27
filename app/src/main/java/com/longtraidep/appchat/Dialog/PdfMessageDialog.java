package com.longtraidep.appchat.Dialog;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.github.barteksc.pdfviewer.PDFView;
import com.longtraidep.appchat.R;

import java.util.Objects;

public class PdfMessageDialog extends DialogFragment {
    private ImageView mImvClose;
    private WebView mWebView;

    public static PdfMessageDialog newInstance(String data) {
        Bundle args = new Bundle();
        PdfMessageDialog fragment = new PdfMessageDialog();
        args.putString("data", data);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_view_pdf_message, container, false);
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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Get data
        String pdfPath = getArguments().getString("data", "");

        mImvClose = (ImageView) view.findViewById(R.id.imv_close);
        mWebView = (WebView) view.findViewById(R.id.wv_pdfview);

        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(pdfPath);

        mImvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }
}
