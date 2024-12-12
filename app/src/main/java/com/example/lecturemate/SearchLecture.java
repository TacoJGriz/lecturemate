package com.example.lecturemate;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SearchLecture extends AppCompatActivity implements Runnable {

    private static final int PERIOD = 100;
    private ArrayList<TranscriptItem> wordClumps;
    private MediaPlayer mp;
    private boolean paused = false;
    private View root = null;
    private SeekBar seekBar;
    private ArrayList<Keypoint> keys;
    private String transcript;
    private boolean tick = false;

    private VideoView simpleVideoView;

    private MaterialButton pauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_lecture);
        root = findViewById(android.R.id.content);

        wordClumps = new ArrayList<>();

        Intent intent = getIntent();
        int index = intent.getIntExtra("position", 0);
        File folder = SaveFiles.loadLecture(getExternalFilesDir(null), index);
        File keypointFile = new File(folder, "keypoints.json");
        File transcriptFile = new File(folder, "transcript.json");
        JsonObject jsonT;
        JsonObject jsonK;

        JsonParser parser = new JsonParser();
        try {
            jsonT = parser.parse(new FileReader(transcriptFile)).getAsJsonObject();
            jsonK = parser.parse(new FileReader(keypointFile)).getAsJsonObject();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        transcript = jsonT.getAsJsonObject("results").getAsJsonArray("transcripts").get(0).getAsJsonObject().get("transcript").getAsString();
        final int[] previousIndex = {-1};

        jsonT.getAsJsonObject("results").getAsJsonArray("items").forEach(j -> {
            JsonObject jo = j.getAsJsonObject();
            String word = jo.getAsJsonArray("alternatives").get(0).getAsJsonObject().get("content").getAsString();
            String timeRaw;
            try {
                timeRaw = jo.getAsJsonPrimitive("start_time").getAsString();
            } catch (NullPointerException e) {
                timeRaw = wordClumps.get(wordClumps.size() - 1).getSeconds() + ".00";
            }
            int i = transcript.indexOf(word, previousIndex[0]);
            previousIndex[0] = i + word.length();
            wordClumps.add(new TranscriptItem(timeRaw, word, i));
            wordClumps.get(wordClumps.size() - 1).changeTimeFormatToKeypoint();
        });

        Collections.sort(wordClumps);

        for (int i = 0; i < wordClumps.size() - 1; i++) {
            int sec = wordClumps.get(i).getSeconds();
            int sec2 = wordClumps.get(i + 1).getSeconds() - 6;
            if (sec > sec2) {
                wordClumps.get(i).appendName(wordClumps.get(i + 1).getName());
                wordClumps.remove(i + 1);
                i--;
            }
        }

        keys = new ArrayList<>();
        jsonK.getAsJsonArray("keypoints").forEach(j -> {
            JsonObject jo = j.getAsJsonObject();
            keys.add(new Keypoint(jo.get("time").getAsString(), jo.get("name").getAsString(), jo.get("description").getAsString()));
        });

        wordClumps.addAll(keys);
        Collections.sort(wordClumps);

        RecyclerView recyclerView = findViewById(R.id.transcriptRecycle);

        WordAdapter adapter = new WordAdapter(this, wordClumps);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchLecture.this));

        simpleVideoView = findViewById(R.id.videoView2);

        simpleVideoView.setVideoURI(Uri.parse(getIntent().getStringExtra("uri")));
        simpleVideoView.start();

        seekBar = findViewById(R.id.seekBar);
        simpleVideoView.setOnPreparedListener(l -> {
            this.mp = l;
            l.setLooping(true);
            seekBar.setMax(l.getDuration());
            tick = true;
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //mp.seekTo(progress, MediaPlayer.SEEK_CLOSEST);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (!paused) {
                    mp.pause();
                }
                tick = false;
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!paused) {
                    mp.start();
                }
                mp.seekTo(seekBar.getProgress(), MediaPlayer.SEEK_CLOSEST);
                tick = true;
            }
        });

        pauseButton = findViewById(R.id.pauseButtonEditKeypoints2);
        pauseButton.setOnClickListener(v -> {
            if (paused) {
                pauseButton.setIconResource(R.drawable.baseline_pause_24);
                mp.start();
            } else {
                pause();
            }
            paused = !paused;
        });


    }

    private void pause() {
        pauseButton.setIconResource(R.drawable.baseline_play_arrow_24);
        mp.pause();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setTime(int time) {
        long milis = TimeUnit.MILLISECONDS.convert(time, TimeUnit.SECONDS);

        mp.seekTo((int) milis, MediaPlayer.SEEK_CLOSEST);
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
            seekBar.setProgress(simpleVideoView.getCurrentPosition(), true);
        }
        root.postDelayed(this, PERIOD);
    }
}