package com.example.takeit;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class NotificationHelper {

    static final String CHANNEL_ID = "reminder_channel";
    static final String EXTRA_REMINDER_ID = "reminder_id";
    static final String EXTRA_TITLE = "title";
    static final String EXTRA_DESCRIPTION = "description";

    // NotificationManager.IMPORTANCE_HIGH = 4 (API 26+)
    private static final int IMPORTANCE_HIGH = 4;

    /**
     * Creates a notification channel on API 26+. Safe to call on all API levels;
     * uses reflection so the code compiles against API 23.
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        try {
            Class<?> channelClass = Class.forName("android.app.NotificationChannel");
            Constructor<?> ctor = channelClass.getConstructor(
                    String.class, CharSequence.class, int.class);
            Object channel = ctor.newInstance(CHANNEL_ID, "Reminders", IMPORTANCE_HIGH);

            // channel.setDescription("Reminder notifications")
            Method setDesc = channelClass.getMethod("setDescription", String.class);
            setDesc.invoke(channel, "Reminder notifications");

            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Method create = nm.getClass().getMethod("createNotificationChannel", channelClass);
            create.invoke(nm, channel);
        } catch (Exception e) {
            // Silently ignored — notifications will still appear but may be uncategorised
        }
    }

    public static void scheduleReminder(Context context, Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(EXTRA_REMINDER_ID, reminder.getId());
        intent.putExtra(EXTRA_TITLE, reminder.getTitle());
        intent.putExtra(EXTRA_DESCRIPTION, reminder.getDescription());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= 23) {
            // setExactAndAllowWhileIdle available from API 23
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.getDateTimeMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminder.getDateTimeMillis(),
                    pendingIntent
            );
        }
    }

    public static void cancelReminder(Context context, int reminderId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }
}
