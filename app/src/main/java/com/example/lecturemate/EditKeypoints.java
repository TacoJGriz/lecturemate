package com.example.lecturemate;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Activity meant to let the user edit keypoint name and location after filming a video
 */
public class EditKeypoints extends AppCompatActivity implements Runnable {
    private static final int PERIOD = 100;
    private boolean paused = false;
    private MaterialButton pauseButton;
    private int numKeyPoints;
    private SeekBar keyBar;
    private SeekBar seekBar;
    private ArrayList<Keypoint> keys;
    private int activeKeypoint;
    private View root = null;
    private MediaPlayer mp;

    private boolean tick = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_keypoints);
        root = findViewById(android.R.id.content);

        numKeyPoints = getIntent().getStringArrayListExtra("keypointNames").size();

        keyBar = findViewById(R.id.keyBar);
        keyBar.setMax(numKeyPoints - 1); //sets max number of key points on the key bar

        ArrayList<String> names = getIntent().getStringArrayListExtra("keypointNames");
        ArrayList<String> times = getIntent().getStringArrayListExtra("keypointTimes");

        keys = new ArrayList<>();

        for (int i = 0; i < names.size(); i++) {
            keys.add(new Keypoint(times.get(i), names.get(i), ""));
        }

        activeKeypoint = 0;

        setActiveKeypoint();

        //This method is called every time somebody moves the key bar
        keyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //All of this finds the time of the keypoint seeked to and seeks the video to that time
                activeKeypoint = progress;
                setActiveKeypoint();
                int s = keys.get(progress).getSeconds();
                int ms = (int) TimeUnit.MILLISECONDS.convert(s, TimeUnit.SECONDS);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mp.seekTo(ms, MediaPlayer.SEEK_CLOSEST);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        VideoView simpleVideoView = findViewById(R.id.videoView);

        simpleVideoView.setVideoURI(Uri.parse(getIntent().getStringExtra("URI")));
        simpleVideoView.start();

        seekBar = findViewById(R.id.seekBar3);

        //this is called when the video loads
        simpleVideoView.setOnPreparedListener(l -> {
            this.mp = l; //sets the media player variable
            seekBar.setMax(l.getDuration());
            l.setLooping(true);
            tick = true; //tick is what tells the seek bar to update its position
        });

        //This method is called every time somebody moves the seek bar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mp.seekTo(progress, MediaPlayer.SEEK_CLOSEST); //Change the video progress
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (!paused) {
                    mp.pause();
                }
                tick = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!paused) {
                    mp.start();
                }
                tick = true;
            }
        });

        pauseButton = findViewById(R.id.pauseButtonEditKeypoints);
        pauseButton.setOnClickListener(v -> { //when the pause button is pressed
            if (paused) {
                pauseButton.setIconResource(R.drawable.baseline_pause_24); //This changes the pause button icon
                mp.start();
            } else {
                pause();
            }
            paused = !paused;
        });

        MaterialButton addButton = findViewById(R.id.addButtonEditKeypoints);
        addButton.setOnClickListener(v -> {
            if (mp.getCurrentPosition() != 0) {
                save(); //save current keypoint
                long rawDur = mp.getCurrentPosition(); //get the video position
                Keypoint k = new Keypoint(rawDur, "Keypoint " + (numKeyPoints + 1), ""); //make a new keypoint
                if (keys.contains(k)) {
                    Toast.makeText(this, "2 keypoints cannot be at the same time", Toast.LENGTH_SHORT).show(); //make sure its not overlapping another keypoint
                } else {
                    keys.add(k);
                    pause();
                    paused = true;
                    numKeyPoints++;
                    keyBar.setMax(numKeyPoints - 1); //we need to update the key bar
                    Collections.sort(keys); //and sort the keypoints by time so they appear in the correct order
                    activeKeypoint = keys.indexOf(k);
                    setActiveKeypoint();
                }
            } else {
                Toast.makeText(this, "Please wait till the video is playing", Toast.LENGTH_SHORT).show();
            }
        });

        Button saveButton = findViewById(R.id.saveButtonEdit);
        saveButton.setOnClickListener(v -> {
            if (keys.size() > 0) {
                save();
            }

            ArrayList<String> names2 = new ArrayList<>();
            ArrayList<String> times2 = new ArrayList<>();
            ArrayList<String> descs2 = new ArrayList<>();

            //fill the array lists
            keys.forEach(keypoint -> {
                names2.add(keypoint.getName());
                times2.add(keypoint.getTime());
                descs2.add(keypoint.getDescription());
            });

            int millis = mp.getDuration();
            long seconds = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);

            long HH = seconds / 3600;
            long MM = (seconds % 3600) / 60;
            long SS = seconds % 60;

            String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", HH, MM, SS);

            //fill an intent with data to send over to main
            Intent intent = new Intent(EditKeypoints.this, MainActivity.class);
            intent.putExtra("load", true);
            intent.putExtra("uri", getIntent().getStringExtra("URI"));
            intent.putExtra("vidName", getIntent().getStringExtra("name"));
            intent.putStringArrayListExtra("names", names2);
            intent.putStringArrayListExtra("times", times2);
            intent.putStringArrayListExtra("descs", descs2);
            intent.putExtra("lectureName", getIntent().getStringExtra("lectureName"));
            intent.putExtra("dur", time);
            intent.putExtra("date", getIntent().getStringExtra("date"));
            startActivity(intent);
        });

        Button saveKeyButton = findViewById(R.id.SaveButton);
        saveKeyButton.setOnClickListener(v -> {
            if (keys.size() > 0) {
                save();
            }
            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        });

        Button deleteKeyButton = findViewById(R.id.deleteKeypoint);
        deleteKeyButton.setOnClickListener(v -> {
            if (numKeyPoints > 1) {
                numKeyPoints--;
                keys.remove(activeKeypoint);
                setActiveKeypoint();
                keyBar.setMax(numKeyPoints - 1);
            }
        });

        Button deleteButton = findViewById(R.id.DeleteButtonEdit);
        deleteButton.setOnClickListener(v -> {
            //this creates a popup that promps you if you mean to leave
            AlertDialog.Builder builder = new AlertDialog.Builder(EditKeypoints.this);

            builder.setMessage("Are you sure you want to delete the lecture?");
            builder.setTitle("Delete");
            builder.setCancelable(false);
            builder.setPositiveButton("Delete", (dialog, which) -> {
                Intent intent = new Intent(EditKeypoints.this, MainActivity.class);
                startActivity(intent);
            });
            builder.setNegativeButton("Go Back", (dialog, which) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });


    }

    /**
     * Sets the information in the layout to match the active keypoint
     */
    private void setActiveKeypoint() {
        if (keys.size() > 0) {
            Keypoint k = keys.get(activeKeypoint);
            keyBar.setProgress(activeKeypoint);

            EditText editName = findViewById(R.id.editKeypointName);
            EditText editTime = findViewById(R.id.timeEdit);
            EditText editDesc = findViewById(R.id.editKeypointDesc);

            editName.setText(k.getName());
            editTime.setText(k.getTime());
            if (k.getDescription().equals("")) {
                editDesc.setText(R.string.EnterDescText);
            } else {
                editDesc.setText(k.getDescription());
            }
        }
    }

    /**
     * Saves the active keypoint
     */
    private void save() {
        Keypoint k = keys.get(activeKeypoint);

        EditText editName = findViewById(R.id.editKeypointName);
        EditText editTime = findViewById(R.id.timeEdit);
        EditText editDesc = findViewById(R.id.editKeypointDesc);

        k.setName(editName.getText().toString());
        k.setTime(editTime.getText().toString());
        k.setDescription(editDesc.getText().toString());

        keys.set(activeKeypoint, k);
    }

    /**
     * Pauses the media player and flips the button icon
     */
    private void pause() {
        pauseButton.setIconResource(R.drawable.baseline_play_arrow_24);
        mp.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        run();
    }

    @Override
    public void onPause() {
        root.removeCallbacks(this);
        super.onPause();
    }

    @Override
    public void run() {
        if (tick) {
            seekBar.setProgress(mp.getCurrentPosition(), true); //update the seek bar
        }
        root.postDelayed(this, PERIOD); //rest 100ms
    }
}