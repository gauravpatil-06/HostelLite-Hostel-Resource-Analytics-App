package com.example.trackingapp;

public class AboutItem {
    private int iconResId;
    private String title;
    private String content;
    private boolean isExpanded;

    public AboutItem(int iconResId, String title, String content) {
        this.iconResId = iconResId;
        this.title = title;
        this.content = content;
        this.isExpanded = false; // Initially, all items are collapsed
    }

    // Getters and Setters
    public int getIconResId() { return iconResId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}