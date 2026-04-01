package com.example.takeit;

public class Reminder {
    private int id;
    private String title;
    private String description;
    private int timeMinutes; // minutes since midnight (0–1439)

    public Reminder(int id, String title, String description, int timeMinutes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timeMinutes = timeMinutes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTimeMinutes() { return timeMinutes; }
    public void setTimeMinutes(int timeMinutes) { this.timeMinutes = timeMinutes; }
}
