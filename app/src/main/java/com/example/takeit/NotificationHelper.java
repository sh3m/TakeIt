package com.example.takeit;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Calendar;

public class NotificationHelper {

    static final String CHANNEL_ID = "reminder_channel";
    static final String EXTRA_REMINDER_ID = "reminder_id";
    static final String EXTRA_TITLE = "title";
    static final String EXTRA_DESCRIPTION = "description";
    static final String EXTRA_TIME_MINUTES = "time_minutes";

    private static final int IMPORTANCE_HIGH = 4;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        try {
            Class<?> channelClass = Class.forName("android.app.NotificationChannel");
            Constructor<?> ctor = channelClass.getConstructor(
                    String.class, CharSequence.class, int.class);
            Object channel = ctor.newInstance(CHANNEL_ID, "Reminders", IMPORTANCE_HIGH);
            Method setDesc = channelClass.getMethod("setDescription", String.class);
            setDesc.invoke(channel, "Daily reminder alarms");
            Method enableVibration = channelClass.getMethod("enableVibration", boolean.class);
            enableVibration.invoke(channel, true);
            Method setVibrationPattern = channelClass.getMethod("setVibrationPattern", long[].class);
            setVibrationPattern.invoke(channel, new long[]{0, 600, 300, 600, 300});
            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Method create = nm.getClass().getMethod("createNotificationChannel", channelClass);
            create.invoke(nm, channel);
        } catch (Exception e) {
            // ignored
        }
    }

    public static void scheduleReminder(Context context, Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(EXTRA_REMINDER_ID, reminder.getId());
        intent.putExtra(EXTRA_TITLE, reminder.getTitle());
        intent.putExtra(EXTRA_DESCRIPTION, reminder.getDescription());
        intent.putExtra(EXTRA_TIME_MINUTES, reminder.getTimeMinutes());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = nextTriggerTime(reminder.getTimeMinutes());

        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
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

    /** Returns the next wall-clock time (ms) for the given time-of-day. Today if not yet passed, tomorrow if it has. */
    static long nextTriggerTime(int timeMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, timeMinutes / 60);
        cal.set(Calendar.MINUTE, timeMinutes % 60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return cal.getTimeInMillis();
    }
}
