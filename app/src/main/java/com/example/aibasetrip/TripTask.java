package com.example.aibasetrip;

public class TripTask {
    private String title;
    private boolean isChecked;

    public TripTask(String title, boolean isChecked) {
        this.title = title;
        this.isChecked = isChecked;
    }

    public String getTitle() {
        return title;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
