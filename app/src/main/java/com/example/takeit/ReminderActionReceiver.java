package com.example.takeit;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderActionReceiver extends BroadcastReceiver {

    static final String ACTION_DISMISS = "com.example.takeit.DISMISS";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_DISMISS.equals(intent.getAction())) return;

        int reminderId = intent.getIntExtra(NotificationHelper.EXTRA_REMINDER_ID, -1);
        if (reminderId == -1) return;

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(reminderId);
    }
}
