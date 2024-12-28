package com.mymiki.mimyki;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationScheduler {

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "default";
            CharSequence name = "Nhắc nhở";
            String description = "Kênh thông báo nhắc nhở công việc";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


    public static void scheduleNotification(Context context, String eventName, String eventTime, int offsetMinutes) {
        try {
            // Định dạng thời gian
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(eventTime));
            calendar.add(Calendar.MINUTE, -offsetMinutes); // Trừ đi offset

            // Intent thông báo
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("title", "Nhắc nhở công việc");
            intent.putExtra("message", "Sắp đến giờ cho sự kiện: " + eventName);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, eventName.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT
            );

            // Thiết lập AlarmManager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cancelNotification(Context context, String eventName) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, eventName.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent); // Hủy thông báo
        }
    }
}
