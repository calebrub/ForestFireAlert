package com.example.forestfirealert;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class Alerts extends Application {

    public static final String CHANNEL_1_ID = "channel1";
    public static final String  CHANNEL_2_ID = "channel2";

    @Override
    public void onCreate() {
        super.onCreate();
        createChannels();
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Wild Fire Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("You will be able to receive Wild fire Alerts");
            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Sensor Offline Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel2.setDescription("You will be able to receive sensor fault Alerts");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel1);
        manager.createNotificationChannel(channel2);
        }
    }
}
