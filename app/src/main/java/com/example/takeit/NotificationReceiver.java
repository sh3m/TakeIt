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

        int    reminderId  = intent.getIntExtra(NotificationHelper.EXTRA_REMINDER_ID, -1);
        String title       = intent.getStringExtra(NotificationHelper.EXTRA_TITLE);
        String description = intent.getStringExtra(NotificationHelper.EXTRA_DESCRIPTION);
        int    timeMinutes = intent.getIntExtra(NotificationHelper.EXTRA_TIME_MINUTES, -1);

        if (reminderId == -1) return;

        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId);
        alarmIntent.putExtra(NotificationHelper.EXTRA_TITLE, title);
        alarmIntent.putExtra(NotificationHelper.EXTRA_DESCRIPTION, description);
        alarmIntent.putExtra(NotificationHelper.EXTRA_TIME_MINUTES, timeMinutes);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // 1. Always post a fullScreenIntent notification — guaranteed to work on all versions.
        //    Screen off/locked → system auto-launches AlarmActivity.
        //    Screen on → heads-up appears; AlarmActivity launches via step 2.
        PendingIntent pi = PendingIntent.getActivity(
                context, reminderId, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        postNotification(context, reminderId, title, description, pi);

        // 2. Start foreground service so it can launch AlarmActivity when screen is on.
        //    Wrapped in try/catch — if it fails, the notification above is the fallback.
        try {
            Intent svc = new Intent(context, AlarmService.class);
            svc.putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId);
            svc.putExtra(NotificationHelper.EXTRA_TITLE, title);
            svc.putExtra(NotificationHelper.EXTRA_DESCRIPTION, description);
            svc.putExtra(NotificationHelper.EXTRA_TIME_MINUTES, timeMinutes);
            if (Build.VERSION.SDK_INT >= 26) {
                Method m = Context.class.getMethod("startForegroundService", Intent.class);
                m.invoke(context, svc);
            } else {
                context.startService(svc);
            }
        } catch (Exception e) {
            // Service failed — notification above is still showing, alarm is not silent.
        }

        // 3. Reschedule for same time tomorrow
        if (timeMinutes >= 0) {
            Reminder reminder = new Reminder(reminderId, title, description, timeMinutes);
            NotificationHelper.scheduleReminder(context, reminder);
        }
    }

    private void postNotification(Context context, int id, String title,
                                  String description, PendingIntent pi) {
        String text = (description != null && !description.isEmpty())
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
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setFullScreenIntent(pi, true)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVibrate(new long[]{0, 600, 300, 600, 300});

        if (Build.VERSION.SDK_INT < 26) {
            try {
                Method m = Notification.Builder.class.getMethod("setPriority", int.class);
                m.invoke(builder, Notification.PRIORITY_MAX);
            } catch (Exception e) { /* ignored */ }
        }

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, builder.build());
    }

    private void rescheduleAllReminders(Context context) {
        ReminderDatabaseHelper db = new ReminderDatabaseHelper(context);
        for (Reminder reminder : db.getAllReminders()) {
            NotificationHelper.scheduleReminder(context, reminder);
        }
    }
}
