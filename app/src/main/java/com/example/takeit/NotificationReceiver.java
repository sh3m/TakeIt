package com.example.takeit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

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

        // Start AlarmService as a foreground service.
        // Foreground services are exempt from background activity launch restrictions —
        // the service then starts AlarmActivity directly.
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId);
        serviceIntent.putExtra(NotificationHelper.EXTRA_TITLE, title);
        serviceIntent.putExtra(NotificationHelper.EXTRA_DESCRIPTION, description);
        serviceIntent.putExtra(NotificationHelper.EXTRA_TIME_MINUTES, timeMinutes);

        if (Build.VERSION.SDK_INT >= 26) {
            try {
                java.lang.reflect.Method m = Context.class.getMethod("startForegroundService", Intent.class);
                m.invoke(context, serviceIntent);
            } catch (Exception e) {
                context.startService(serviceIntent);
            }
        } else {
            context.startService(serviceIntent);
        }

        // Reschedule for same time tomorrow
        if (timeMinutes >= 0) {
            Reminder reminder = new Reminder(reminderId, title, description, timeMinutes);
            NotificationHelper.scheduleReminder(context, reminder);
        }
    }

    private void rescheduleAllReminders(Context context) {
        ReminderDatabaseHelper db = new ReminderDatabaseHelper(context);
        for (Reminder reminder : db.getAllReminders()) {
            NotificationHelper.scheduleReminder(context, reminder);
        }
    }
}
