package com.example.takeit;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmActivity extends Activity {

    private Vibrator vibrator;
    private Ringtone ringtone;
    private int reminderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show over lock screen and wake screen (reflection for API 27+)
        if (Build.VERSION.SDK_INT >= 27) {
            try {
                Method showWhenLocked = Activity.class.getMethod("setShowWhenLocked", boolean.class);
                showWhenLocked.invoke(this, true);
                Method turnScreenOn = Activity.class.getMethod("setTurnScreenOn", boolean.class);
                turnScreenOn.invoke(this, true);
            } catch (Exception e) { /* ignored */ }
        }
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        setContentView(R.layout.activity_alarm);

        reminderId    = getIntent().getIntExtra(NotificationHelper.EXTRA_REMINDER_ID, -1);
        String title  = getIntent().getStringExtra(NotificationHelper.EXTRA_TITLE);
        String desc   = getIntent().getStringExtra(NotificationHelper.EXTRA_DESCRIPTION);
        int timeMin   = getIntent().getIntExtra(NotificationHelper.EXTRA_TIME_MINUTES, 0);

        TextView tvTime   = (TextView) findViewById(R.id.tvAlarmTime);
        TextView tvTitle  = (TextView) findViewById(R.id.tvAlarmTitle);
        TextView tvDesc   = (TextView) findViewById(R.id.tvAlarmDesc);
        Button   btnTaken = (Button)   findViewById(R.id.btnTaken);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, timeMin / 60);
        cal.set(Calendar.MINUTE,      timeMin % 60);
        tvTime.setText(new SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.getTime()));

        tvTitle.setText(title != null ? title : "Reminder");

        if (desc != null && !desc.isEmpty()) {
            tvDesc.setText(desc);
        } else {
            tvDesc.setVisibility(View.GONE);
        }

        startVibration();
        startSound();

        btnTaken.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dismiss(); }
        });
    }

    @SuppressWarnings("deprecation")
    private void startVibration() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator == null) return;
        long[] pattern = {0, 600, 300, 600, 300};
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                Class<?> veClass = Class.forName("android.os.VibrationEffect");
                Method createWaveform = veClass.getMethod("createWaveform", long[].class, int.class);
                Object effect = createWaveform.invoke(null, pattern, 0);
                Method vibrateNew = Vibrator.class.getMethod("vibrate", veClass);
                vibrateNew.invoke(vibrator, effect);
                return;
            } catch (Exception e) { /* fall through */ }
        }
        vibrator.vibrate(pattern, 0);
    }

    private void startSound() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        if (uri == null) return;
        ringtone = RingtoneManager.getRingtone(this, uri);
        if (ringtone == null) return;
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                Method setLooping = ringtone.getClass().getMethod("setLooping", boolean.class);
                setLooping.invoke(ringtone, true);
            } catch (Exception e) { /* ignored */ }
        }
        ringtone.play();
    }

    private void dismiss() {
        if (vibrator != null) vibrator.cancel();
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
        // Stop the foreground service (removes its notification)
        stopService(new Intent(this, AlarmService.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vibrator != null) vibrator.cancel();
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
    }
}
