package com.example.takeit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ReminderDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "reminders.db";
    private static final int DATABASE_VERSION = 2;

    static final String TABLE_REMINDERS = "reminders";
    static final String COL_ID = "id";
    static final String COL_TITLE = "title";
    static final String COL_DESCRIPTION = "description";
    static final String COL_TIME_MINUTES = "time_minutes";

    public ReminderDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_REMINDERS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_TIME_MINUTES + " INTEGER NOT NULL"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(db);
    }

    public long addReminder(Reminder reminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, reminder.getTitle());
        values.put(COL_DESCRIPTION, reminder.getDescription());
        values.put(COL_TIME_MINUTES, reminder.getTimeMinutes());
        long id = db.insert(TABLE_REMINDERS, null, values);
        db.close();
        return id;
    }

    public List<Reminder> getAllReminders() {
        List<Reminder> reminders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REMINDERS, null, null, null, null, null,
                COL_TIME_MINUTES + " ASC");
        if (cursor.moveToFirst()) {
            do {
                reminders.add(fromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return reminders;
    }

    public Reminder getReminderById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REMINDERS, null,
                COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        Reminder reminder = null;
        if (cursor.moveToFirst()) reminder = fromCursor(cursor);
        cursor.close();
        db.close();
        return reminder;
    }

    public int updateReminder(Reminder reminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, reminder.getTitle());
        values.put(COL_DESCRIPTION, reminder.getDescription());
        values.put(COL_TIME_MINUTES, reminder.getTimeMinutes());
        int rows = db.update(TABLE_REMINDERS, values, COL_ID + "=?",
                new String[]{String.valueOf(reminder.getId())});
        db.close();
        return rows;
    }

    public void deleteReminder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REMINDERS, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    private Reminder fromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
        String desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
        int timeMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TIME_MINUTES));
        return new Reminder(id, title, desc, timeMinutes);
    }
}
