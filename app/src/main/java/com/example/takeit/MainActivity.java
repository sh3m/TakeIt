package com.example.takeit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity implements ReminderAdapter.OnReminderClickListener {

    private static final int REQUEST_ADD = 1001;
    private static final int REQUEST_EDIT = 1002;
    private static final int REQUEST_NOTIF_PERMISSION = 1003;

    private ReminderDatabaseHelper dbHelper;
    private ReminderAdapter adapter;
    private List<Reminder> reminders;
    private TextView tvEmpty;
    private View rootLayout;
    private Button btnAdd;
    private Button btnNightMode;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermissionIfNeeded();

        dbHelper = new ReminderDatabaseHelper(this);
        rootLayout = findViewById(R.id.rootLayout);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        listView = (ListView) findViewById(R.id.listView);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnNightMode = (Button) findViewById(R.id.btnNightMode);

        reminders = dbHelper.getAllReminders();
        adapter = new ReminderAdapter(this, reminders, this);
        listView.setAdapter(adapter);
        updateEmptyState();

        applyNightMode();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddReminderActivity.class);
                startActivityForResult(intent, REQUEST_ADD);
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

    private void applyNightMode() {
        boolean night = NightModeHelper.isNightMode(this);
        int bg     = NightModeHelper.bg(this);
        int hint   = NightModeHelper.hint(this);
        int accent = NightModeHelper.accent(this);

        rootLayout.setBackgroundColor(bg);
        listView.setBackgroundColor(bg);
        tvEmpty.setTextColor(hint);

        btnAdd.setBackgroundColor(accent);
        btnAdd.setTextColor(0xFFFFFFFF);

        if (night) {
            btnNightMode.setBackgroundColor(0xFF333333);
            btnNightMode.setTextColor(0xFFFFFFFF);
            btnNightMode.setText("Day Mode");
        } else {
            btnNightMode.setBackgroundColor(0xFFE0E0E0);
            btnNightMode.setTextColor(0xFF212121);
            btnNightMode.setText("Night Mode");
        }

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
