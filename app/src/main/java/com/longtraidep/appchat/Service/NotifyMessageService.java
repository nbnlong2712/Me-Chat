package com.longtraidep.appchat.Service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.longtraidep.appchat.Activity.LoginActivity;
import com.longtraidep.appchat.Activity.MainActivity;
import com.longtraidep.appchat.Application.MyApplication;
import com.longtraidep.appchat.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

public class NotifyMessageService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> map = remoteMessage.getData();

        String name = map.get("name");
        String message = map.get("message");
        String img = map.get("img");

        try {
            sendNotification(name, message, img);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("notify service error", e.getMessage() + ", " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void sendNotification(String name, String message, String img) throws IOException {
        /*//Convert string url to Uri
        Uri myUri = Uri.parse(img);
        //Convert uri to bitmap
        ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), myUri);
        Bitmap bitmap = ImageDecoder.decodeBitmap(source);*/

        Bitmap bitmap = getBitmapFromURL(img);

        Intent i;
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            i = new Intent(this, MainActivity.class);
        else
            i = new Intent(this, LoginActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 12, i, PendingIntent.FLAG_UPDATE_CURRENT);

        @SuppressLint("ResourceAsColor") NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.mechat_icon)
                .setContentTitle("New Message!")
                .setLargeIcon(bitmap)
                .setContentText(name + ": " + message)
                .setColor(Color.GREEN)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {
            notificationManager.notify(1, builder.build());
        }
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}
