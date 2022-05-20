package com.longtraidep.appchat.API;

import com.longtraidep.appchat.Object.Notification;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationMessageAPI {
    //URL: https://fcm.googleapis.com/

    NotificationMessageAPI mNotificationMessageAPI = new Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificationMessageAPI.class);

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAvCMQpt8:APA91bELi7kOeq_IKZYmY6A63qL5_ZBLuNVMVmOaA9cpFO5Rg9KoqMdUDHZyL4prLCVEpief8t7ioeyNeQjqVCDBuq1rLGu0kxxy3DFgJE9u9hgwoZG1E2WUEWj6CThsrgdvGNSWRlW5"
    })
    @POST("fcm/send")
    Call<Notification> postNotification(@Body Notification notification);
}
