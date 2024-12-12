package com.example.lecturemate;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TranscriptItem implements Comparable {
    private String time;
    private String name;

    public TranscriptItem(String time, String name) {
        this.time = time;
        this.name = name;
    }

    public TranscriptItem(String time, String name, int index) {
        this.time = time;
        this.name = name;
    }

    public TranscriptItem(long time, String name) {
        long seconds = TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS);

        long HH = seconds / 3600;
        long MM = (seconds % 3600) / 60;
        long SS = seconds % 60;

        this.time = String.format(Locale.getDefault(), "%02d:%02d:%02d", HH, MM, SS);
        this.name = name;
    }

    public void changeTimeFormatToKeypoint() {
        long seconds = Integer.parseInt(time.substring(0, time.indexOf('.')));

        long HH = seconds / 3600;
        long MM = (seconds % 3600) / 60;
        long SS = seconds % 60;

        this.time = String.format(Locale.getDefault(), "%02d:%02d:%02d", HH, MM, SS);
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void appendName(String name) {
        if (name.equals(".") || name.equals(",") || name.equals(":") || name.equals(";")) {
            this.name = this.name + name;
        } else {
            this.name = this.name + " " + name;
        }
    }

    public int getSeconds() {
        return (Integer.parseInt(time.substring(0, 2)) * 3600) + (Integer.parseInt(time.substring(3, 5)) * 60) + Integer.parseInt(time.substring(6, 8));
    }

    @Override
    public int compareTo(Object o) {
        TranscriptItem k = (TranscriptItem) o;

        return this.getTime().compareTo(k.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranscriptItem keypoint = (TranscriptItem) o;
        return Objects.equals(time, keypoint.time);
    }

}
