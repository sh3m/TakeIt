package com.example.takeit;

public class Reminder {
    private int id;
    private String title;
    private String description;
    private long dateTimeMillis;
    private boolean isDone;

    public Reminder(int id, String title, String description, long dateTimeMillis, boolean isDone) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dateTimeMillis = dateTimeMillis;
        this.isDone = isDone;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getDateTimeMillis() { return dateTimeMillis; }
    public void setDateTimeMillis(long dateTimeMillis) { this.dateTimeMillis = dateTimeMillis; }

    public boolean isDone() { return isDone; }
    public void setDone(boolean done) { isDone = done; }
}
