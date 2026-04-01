package com.example.takeit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAllReminders(context);
            return;
        }

        int    reminderId = intent.getIntExtra(NotificationHelper.EXTRA_REMINDER_ID, -1);
        String title      = intent.getStringExtra(NotificationHelper.EXTRA_TITLE);
        String description = intent.getStringExtra(NotificationHelper.EXTRA_DESCRIPTION);
        int    timeMinutes = intent.getIntExtra(NotificationHelper.EXTRA_TIME_MINUTES, -1);

        if (reminderId == -1) return;

        // Build PendingIntent that opens the full-screen alarm activity
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId);
        alarmIntent.putExtra(NotificationHelper.EXTRA_TITLE, title);
        alarmIntent.putExtra(NotificationHelper.EXTRA_DESCRIPTION, description);
        alarmIntent.putExtra(NotificationHelper.EXTRA_TIME_MINUTES, timeMinutes);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent alarmPending = PendingIntent.getActivity(
                context, reminderId,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = buildNotification(context, title, description, alarmPending);
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(reminderId, notification);

        // Reschedule for same time tomorrow
        if (timeMinutes >= 0) {
            Reminder reminder = new Reminder(reminderId, title, description, timeMinutes);
            NotificationHelper.scheduleReminder(context, reminder);
        }
    }

    private Notification buildNotification(Context context, String title,
                                           String description, PendingIntent alarmPending) {
        String contentText = (description != null && !description.isEmpty())
                ? description : "Time for your reminder!";

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                Constructor<Notification.Builder> ctor =
                        Notification.Builder.class.getConstructor(Context.class, String.class);
                builder = ctor.newInstance(context, NotificationHelper.CHANNEL_ID);
            } catch (Exception e) {
                builder = new Notification.Builder(context);
            }
        } else {
            builder = new Notification.Builder(context);
        }

        builder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title != null ? title : "Reminder")
                .setContentText(contentText)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(alarmPending)
                .setFullScreenIntent(alarmPending, true)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVibrate(new long[]{0, 600, 300, 600, 300});

        // Pre-Oreo priority
        if (Build.VERSION.SDK_INT < 26) {
            try {
                Method setPriority = Notification.Builder.class.getMethod("setPriority", int.class);
                setPriority.invoke(builder, Notification.PRIORITY_MAX);
            } catch (Exception e) { /* ignored */ }
        }

        return builder.build();
    }

    private void rescheduleAllReminders(Context context) {
        ReminderDatabaseHelper db = new ReminderDatabaseHelper(context);
        for (Reminder reminder : db.getAllReminders()) {
            NotificationHelper.scheduleReminder(context, reminder);
        }
    }
}
