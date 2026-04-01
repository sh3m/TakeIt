package com.example.takeit;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddReminderActivity extends Activity {

    static final String EXTRA_REMINDER_ID = "reminder_id";

    private EditText etTitle, etDescription;
    private TextView tvSelectedTime;
    private Button btnPickTime, btnSave;

    private int selectedHour = -1;
    private int selectedMinute = -1;
    private int editingReminderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        etTitle = (EditText) findViewById(R.id.etTitle);
        etDescription = (EditText) findViewById(R.id.etDescription);
        tvSelectedTime = (TextView) findViewById(R.id.tvSelectedDateTime);
        btnPickTime = (Button) findViewById(R.id.btnPickTime);
        btnSave = (Button) findViewById(R.id.btnSave);

        editingReminderId = getIntent().getIntExtra(EXTRA_REMINDER_ID, -1);
        if (editingReminderId != -1) {
            setTitle("Edit Reminder");
            loadExistingReminder(editingReminderId);
        } else {
            setTitle("Add Reminder");
        }

        applyNightMode();

        btnPickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveReminder();
            }
        });
    }

    private void applyNightMode() {
        int bg     = NightModeHelper.bg(this);
        int text   = NightModeHelper.text(this);
        int hint   = NightModeHelper.hint(this);
        int accent = NightModeHelper.accent(this);

        ScrollView scrollRoot = (ScrollView) findViewById(R.id.scrollRoot);
        scrollRoot.setBackgroundColor(bg);

        View layoutContent = findViewById(R.id.layoutContent);
        layoutContent.setBackgroundColor(bg);

        ((TextView) findViewById(R.id.tvLabelTitle)).setTextColor(hint);
        ((TextView) findViewById(R.id.tvLabelDescription)).setTextColor(hint);
        ((TextView) findViewById(R.id.tvLabelDateTime)).setTextColor(hint);

        int editBg = NightModeHelper.isNightMode(this) ? 0xFF2C2C2C : 0xFFFFFFFF;
        etTitle.setBackgroundColor(editBg);
        etTitle.setTextColor(text);
        etTitle.setHintTextColor(hint);
        etDescription.setBackgroundColor(editBg);
        etDescription.setTextColor(text);
        etDescription.setHintTextColor(hint);

        tvSelectedTime.setTextColor(accent);

        btnSave.setBackgroundColor(accent);
        btnSave.setTextColor(0xFFFFFFFF);
    }

    private void loadExistingReminder(int id) {
        ReminderDatabaseHelper db = new ReminderDatabaseHelper(this);
        Reminder reminder = db.getReminderById(id);
        if (reminder == null) return;

        etTitle.setText(reminder.getTitle());
        etDescription.setText(reminder.getDescription());
        selectedHour = reminder.getTimeMinutes() / 60;
        selectedMinute = reminder.getTimeMinutes() % 60;
        tvSelectedTime.setText(formatTime(selectedHour, selectedMinute));
    }

    private void showTimePicker() {
        int initHour = selectedHour >= 0 ? selectedHour : Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int initMinute = selectedMinute >= 0 ? selectedMinute : Calendar.getInstance().get(Calendar.MINUTE);
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                tvSelectedTime.setText(formatTime(selectedHour, selectedMinute));
            }
        }, initHour, initMinute, false).show();
    }

    private String formatTime(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.getTime());
    }

    private void saveReminder() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }
        if (selectedHour < 0) {
            Toast.makeText(this, "Please set a time", Toast.LENGTH_SHORT).show();
            return;
        }

        int timeMinutes = selectedHour * 60 + selectedMinute;
        ReminderDatabaseHelper db = new ReminderDatabaseHelper(this);

        if (editingReminderId != -1) {
            Reminder updated = new Reminder(editingReminderId, title, description, timeMinutes);
            db.updateReminder(updated);
            NotificationHelper.cancelReminder(this, editingReminderId);
            NotificationHelper.scheduleReminder(this, updated);
            Toast.makeText(this, "Reminder updated", Toast.LENGTH_SHORT).show();
        } else {
            Reminder reminder = new Reminder(0, title, description, timeMinutes);
            long newId = db.addReminder(reminder);
            reminder.setId((int) newId);
            NotificationHelper.scheduleReminder(this, reminder);
            Toast.makeText(this, "Reminder saved", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }
}
