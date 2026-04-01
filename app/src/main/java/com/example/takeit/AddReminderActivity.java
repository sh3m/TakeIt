package com.example.takeit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
    private TextView tvSelectedDateTime;
    private Button btnPickDate, btnPickTime, btnSave;

    private final Calendar selectedCalendar = Calendar.getInstance();
    private boolean dateTimeSet = false;
    private int editingReminderId = -1;

    private static final SimpleDateFormat DISPLAY_FORMAT =
            new SimpleDateFormat("EEE, MMM d yyyy  h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        etTitle = (EditText) findViewById(R.id.etTitle);
        etDescription = (EditText) findViewById(R.id.etDescription);
        tvSelectedDateTime = (TextView) findViewById(R.id.tvSelectedDateTime);
        btnPickDate = (Button) findViewById(R.id.btnPickDate);
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

        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
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

        tvSelectedDateTime.setTextColor(accent);

        btnSave.setBackgroundColor(accent);
        btnSave.setTextColor(0xFFFFFFFF);
    }

    private void loadExistingReminder(int id) {
        ReminderDatabaseHelper db = new ReminderDatabaseHelper(this);
        Reminder reminder = db.getReminderById(id);
        if (reminder == null) return;

        etTitle.setText(reminder.getTitle());
        etDescription.setText(reminder.getDescription());
        selectedCalendar.setTimeInMillis(reminder.getDateTimeMillis());
        dateTimeSet = true;
        tvSelectedDateTime.setText(DISPLAY_FORMAT.format(selectedCalendar.getTime()));
    }

    private void showDatePicker() {
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedCalendar.set(Calendar.YEAR, year);
                selectedCalendar.set(Calendar.MONTH, month);
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateTimeDisplay();
            }
        }, selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedCalendar.set(Calendar.MINUTE, minute);
                selectedCalendar.set(Calendar.SECOND, 0);
                dateTimeSet = true;
                updateDateTimeDisplay();
            }
        }, selectedCalendar.get(Calendar.HOUR_OF_DAY),
                selectedCalendar.get(Calendar.MINUTE), false).show();
    }

    private void updateDateTimeDisplay() {
        tvSelectedDateTime.setText(DISPLAY_FORMAT.format(selectedCalendar.getTime()));
    }

    private void saveReminder() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }
        if (!dateTimeSet) {
            Toast.makeText(this, "Please set a date and time", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(this, "Please choose a future date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        ReminderDatabaseHelper db = new ReminderDatabaseHelper(this);

        if (editingReminderId != -1) {
            Reminder updated = new Reminder(editingReminderId, title, description,
                    selectedCalendar.getTimeInMillis(), false);
            db.updateReminder(updated);
            NotificationHelper.cancelReminder(this, editingReminderId);
            NotificationHelper.scheduleReminder(this, updated);
            Toast.makeText(this, "Reminder updated", Toast.LENGTH_SHORT).show();
        } else {
            Reminder reminder = new Reminder(0, title, description,
                    selectedCalendar.getTimeInMillis(), false);
            long newId = db.addReminder(reminder);
            reminder.setId((int) newId);
            NotificationHelper.scheduleReminder(this, reminder);
            Toast.makeText(this, "Reminder saved", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }
}
