# Reminder App

An Android reminder app that lets you create, edit, and delete reminders with scheduled notifications.

## Features

- **Create reminders** with a title, optional description, and a specific date/time
- **Edit existing reminders** - update any field and the alarm reschedules automatically
- **Delete reminders** with a confirmation dialog
- **Mark as done** using a checkbox - strikes through the title and cancels the alarm
- **Push notifications** fire at the scheduled time with a "Mark Done" action button
- **Persistent storage** via SQLite - reminders survive app restarts
- **Boot receiver** reschedules pending reminders after device reboot

## Tech Stack

- Java (Android SDK 26+, targets SDK 34)
- SQLite via `SQLiteOpenHelper`
- `AlarmManager` with `setExactAndAllowWhileIdle` for reliable alarms
- `BroadcastReceiver` for notifications and boot handling
- `RecyclerView` with `MaterialCardView` items
- Material Components (MDC) UI

## Project Structure

```
app/src/main/
├── java/com/example/reminderapp/
│   ├── MainActivity.java          # List screen
│   ├── AddReminderActivity.java   # Add/edit screen
│   ├── Reminder.java              # Data model
│   ├── ReminderAdapter.java       # RecyclerView adapter
│   ├── ReminderDatabaseHelper.java # SQLite CRUD
│   ├── NotificationHelper.java    # Channel setup & alarm scheduling
│   ├── NotificationReceiver.java  # Fires notification at alarm time
│   └── ReminderActionReceiver.java # Handles "Mark Done" from notification
└── res/
    ├── layout/                    # activity_main, activity_add_reminder, item_reminder
    └── values/                    # strings, colors, themes
```

## Build

Open in Android Studio and run on a device or emulator running Android 8.0+.
