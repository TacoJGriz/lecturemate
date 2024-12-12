package com.example.lecturemate;

/**
 * Class that holds all the information for a keypoint
 */
public class Keypoint extends TranscriptItem {
    private String description;

    /**
     * @param time        Timestamp of keypoint, preferably in "HH:MM:SS"
     * @param name        Name of keypoint
     * @param description Description of keypoint
     */
    public Keypoint(String time, String name, String description) {
        super(time, name);
        this.description = description;
    }

    /**
     * @param time        Timestamp of keypoint in millis
     * @param name        Name of keypoint
     * @param description Description of keypoint
     */
    public Keypoint(long time, String name, String description) {
        super(time, name);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
