package com.example.swiftserve_admin;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    public static final String CHANNEL_ID = "swiftserve_channel";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SwiftServe Notifications",
                    NotificationManager.IMPORTANCE_HIGH // Change DEFAULT to HIGH for sound and pop-up
            );

            channel.setDescription("Reservation alerts for Admins");
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static void show(Context context, String title, String message) {
        // 1. Create a regular Intent to open the Dashboard
        android.content.Intent intent = new android.content.Intent(context, Dashboard.class);

        // Clear the stack so pressing 'back' doesn't get messy
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 2. Wrap it in a PendingIntent
        // FLAG_IMMUTABLE is required for newer Android versions (like in your Week 10 notes)
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE | android.app.PendingIntent.FLAG_UPDATE_CURRENT
        );

        // 3. Build the notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // Use HIGH for the 'Ding' and Pop-up
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true); // Removes the notification after the admin clicks it

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 4. Send it to the system tray
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
