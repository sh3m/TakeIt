package com.example.takeit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity implements ReminderAdapter.OnReminderClickListener {

    private static final int REQUEST_ADD  = 1001;
    private static final int REQUEST_EDIT = 1002;
    private static final int REQUEST_NOTIF_PERMISSION = 1003;

    private ReminderDatabaseHelper dbHelper;
    private ReminderAdapter adapter;
    private List<Reminder> reminders;

    private View rootLayout;
    private View header;
    private View headerDivider;
    private ListView listView;
    private TextView tvEmpty;
    private Button btnAdd;
    private ImageButton btnNightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("");

        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermissionIfNeeded();
        requestOverlayPermissionIfNeeded();

        dbHelper      = new ReminderDatabaseHelper(this);
        rootLayout    = findViewById(R.id.rootLayout);
        header        = findViewById(R.id.header);
        headerDivider = findViewById(R.id.headerDivider);
        listView      = (ListView)     findViewById(R.id.listView);
        tvEmpty       = (TextView)     findViewById(R.id.tvEmpty);
        btnAdd        = (Button)       findViewById(R.id.btnAdd);
        btnNightMode  = (ImageButton)  findViewById(R.id.btnNightMode);

        reminders = dbHelper.getAllReminders();
        adapter   = new ReminderAdapter(this, reminders, this);
        listView.setAdapter(adapter);
        updateEmptyState();
        applyTheme();

        // Reschedule all reminders every time the app opens — ensures alarms survive
        // APK reinstalls, reboots, or any AlarmManager state loss.
        for (Reminder r : reminders) {
            NotificationHelper.scheduleReminder(this, r);
        }

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, AddReminderActivity.class), REQUEST_ADD);
            }
        });

        btnNightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NightModeHelper.toggle(MainActivity.this);
                recreate();
            }
        });
    }

    private void applyTheme() {
        int bg         = NightModeHelper.bg(this);
        int hintColor  = NightModeHelper.hint(this);
        int accentColor = NightModeHelper.accent(this);
        int divColor   = NightModeHelper.divider(this);

        rootLayout.setBackgroundColor(bg);
        header.setBackgroundColor(bg);
        headerDivider.setBackgroundColor(divColor);
        listView.setBackgroundColor(bg);
        tvEmpty.setTextColor(hintColor);
        btnNightMode.setColorFilter(accentColor);

        adapter.notifyDataSetChanged();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            String perm = "android.permission.POST_NOTIFICATIONS";
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{perm}, REQUEST_NOTIF_PERMISSION);
            }
        }
    }

    private void requestOverlayPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                .setTitle("Allow alarm display")
                .setMessage("To show alarms instantly over other apps, enable \"Display over other apps\" for TakeIt.")
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        startActivity(new Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName())
                        ));
                    }
                })
                .setNegativeButton("Later", null)
                .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) refreshReminders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshReminders();
    }

    private void refreshReminders() {
        reminders.clear();
        reminders.addAll(dbHelper.getAllReminders());
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        tvEmpty.setVisibility(reminders.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEdit(Reminder reminder) {
        Intent intent = new Intent(this, AddReminderActivity.class);
        intent.putExtra(AddReminderActivity.EXTRA_REMINDER_ID, reminder.getId());
        startActivityForResult(intent, REQUEST_EDIT);
    }

    @Override
    public void onDelete(final Reminder reminder) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Reminder")
                .setMessage("Delete \"" + reminder.getTitle() + "\"?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NotificationHelper.cancelReminder(MainActivity.this, reminder.getId());
                        dbHelper.deleteReminder(reminder.getId());
                        refreshReminders();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
