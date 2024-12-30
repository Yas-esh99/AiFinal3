package com.example.aifinal3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    
    private final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "bluetooth_service_channel";

    // Create notification channel for Android 8.0 and above
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bluetooth Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Create the actual notification
    public static Notification createNotification(Context context) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Bluetooth Service")
                .setContentText("Running Bluetooth operations in the background")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}