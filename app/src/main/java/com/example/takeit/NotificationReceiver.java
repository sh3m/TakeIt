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

        int reminderId = intent.getIntExtra(NotificationHelper.EXTRA_REMINDER_ID, -1);
        String title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE);
        String description = intent.getStringExtra(NotificationHelper.EXTRA_DESCRIPTION);

        if (reminderId == -1) return;

        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPending = PendingIntent.getActivity(
                context, reminderId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = buildNotification(context, title, description, openPending);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(reminderId, notification);

        ReminderDatabaseHelper db = new ReminderDatabaseHelper(context);
        db.markDone(reminderId, true);
    }

    /**
     * Builds a Notification compatible with all API levels.
     * On API 26+, uses the two-arg Builder(Context, channelId) via reflection.
     * On API < 26, uses the single-arg Builder(Context).
     */
    private Notification buildNotification(Context context, String title,
                                           String description, PendingIntent contentIntent) {
        String contentText = (description != null && !description.isEmpty())
                ? description : "Time for your reminder!";

        if (Build.VERSION.SDK_INT >= 26) {
            // Must use Notification.Builder(Context, String) on API 26+
            try {
                Constructor<Notification.Builder> ctor =
                        Notification.Builder.class.getConstructor(Context.class, String.class);
                Notification.Builder builder = ctor.newInstance(
                        context, NotificationHelper.CHANNEL_ID);

                // Use reflection for setSmallIcon, setContentTitle, etc.
                // (These methods exist on API 23 too, so direct calls are fine.)
                builder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle(title != null ? title : "Reminder")
                        .setContentText(contentText)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent);

                // setPriority moved to channel on API 26+, but calling it is harmless
                Method setPriority = Notification.Builder.class
                        .getMethod("setPriority", int.class);
                setPriority.invoke(builder, Notification.PRIORITY_HIGH);

                return builder.build();
            } catch (Exception e) {
                // Fall through to legacy path
            }
        }

        // API < 26 path
        return new Notification.Builder(context)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title != null ? title : "Reminder")
                .setContentText(contentText)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build();
    }

    private void rescheduleAllReminders(Context context) {
        ReminderDatabaseHelper db = new ReminderDatabaseHelper(context);
        long now = System.currentTimeMillis();
        for (Reminder reminder : db.getAllReminders()) {
            if (!reminder.isDone() && reminder.getDateTimeMillis() > now) {
                NotificationHelper.scheduleReminder(context, reminder);
            }
        }
    }
}
