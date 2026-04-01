package com.example.takeit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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

        // Launch AlarmActivity directly — AlarmManager grants a background activity exemption
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId);
        alarmIntent.putExtra(NotificationHelper.EXTRA_TITLE, title);
        alarmIntent.putExtra(NotificationHelper.EXTRA_DESCRIPTION, description);
        alarmIntent.putExtra(NotificationHelper.EXTRA_TIME_MINUTES, timeMinutes);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(alarmIntent);

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
