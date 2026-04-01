package com.example.takeit;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import java.lang.reflect.Constructor;

public class AlarmService extends Service {

    static final int FOREGROUND_NOTIF_ID = 9997;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Post the required foreground notification immediately
        try {
            startForeground(FOREGROUND_NOTIF_ID, buildForegroundNotification());
        } catch (Exception e) {
            // startForeground can throw on Android 14 without foregroundServiceType.
            // The notification posted by NotificationReceiver is still the fallback.
            stopSelf();
            return START_NOT_STICKY;
        }

        // Foreground services are always allowed to start activities
        if (intent != null) {
            Intent alarmIntent = new Intent(this, AlarmActivity.class);
            alarmIntent.putExtras(intent);
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(alarmIntent);
        }

        return START_NOT_STICKY;
    }

    private Notification buildForegroundNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                Constructor<Notification.Builder> ctor =
                        Notification.Builder.class.getConstructor(Context.class, String.class);
                Notification.Builder b = ctor.newInstance(this, NotificationHelper.CHANNEL_ID);
                b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                 .setContentTitle("Alarm firing…")
                 .setOngoing(true);
                return b.build();
            } catch (Exception e) { /* fall through */ }
        }
        return new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Alarm firing…")
                .setOngoing(true)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
