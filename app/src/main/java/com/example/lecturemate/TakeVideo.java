package com.example.lecturemate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Class to record a video and keypoints
 */
public class TakeVideo extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int AUDIO_PERMISSION_CODE = 101;
    private static final int WRITE_PERMISSION_CODE = 102;

    private VideoCapture videoCapture;

    private CameraSelector lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;

    private boolean rolling = false;
    private boolean paused = false;
    private Recording activeRecording;

    private Uri uri;

    private long dur;

    private KeypointAdapter adapter;

    private ArrayList<String> names;
    private ArrayList<String> times;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_video);

        //check for the proper permissions
        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
        checkPermission(Manifest.permission.RECORD_AUDIO, AUDIO_PERMISSION_CODE);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_PERMISSION_CODE);
        }

        names = new ArrayList<>();
        times = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recycle);

        adapter = new KeypointAdapter(this, names, times);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(TakeVideo.this));

        MaterialButton flipButton = findViewById(R.id.button15);
        flipButton.setOnClickListener(v -> {
            if (rolling) {
                if (paused) {
                    activeRecording.resume();
                    flipButton.setIconResource(R.drawable.baseline_pause_24);
                } else {
                    activeRecording.pause();
                    flipButton.setIconResource(R.drawable.baseline_play_arrow_24);
                }
                paused = !paused;
            } else {
                flipCamera();
            }
        });

        MaterialButton recordButton = findViewById(R.id.recordButtonAndAdd);
        recordButton.setOnClickListener(v -> {
            if (rolling) {
                addKeypoint();
            } else {
                startVideo();
                recordButton.setIconResource(R.drawable.baseline_add_24);
                flipButton.setIconResource(R.drawable.baseline_pause_24);
                rolling = !rolling;
            }
        });

        Button saveButton = findViewById(R.id.saveTakeVideo);
        saveButton.setOnClickListener(v -> {
            if (rolling) {
                activeRecording.stop();
                rolling = !rolling;
            } else {
                Toast toast = Toast.makeText(v.getContext(), "Cannot save until video is taken", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        Button deleteButton = findViewById(R.id.deleteTakeVideo);
        deleteButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(TakeVideo.this);

            builder.setMessage("Are you sure you want to delete the video?");
            builder.setTitle("Delete");
            builder.setCancelable(false);
            builder.setPositiveButton("Delete", (dialog, which) -> {
                Intent intent = new Intent(TakeVideo.this, MainActivity.class);
                startActivity(intent);
            });
            builder.setNegativeButton("Go Back", (dialog, which) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
                bindVideo(cameraProvider);
            } catch (ExecutionException | InterruptedException ignored) {
            }
        }, ContextCompat.getMainExecutor(this));

    }

    private void addKeypoint() {
        long seconds = TimeUnit.SECONDS.convert(dur, TimeUnit.MILLISECONDS);

        long HH = seconds / 3600;
        long MM = (seconds % 3600) / 60;
        long SS = seconds % 60;

        String stringdur = String.format(Locale.getDefault(), "%02d:%02d:%02d", HH, MM, SS);

        adapter.addKeyPoint(stringdur);
    }

    private void flipCamera() {
        if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA)
            lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
        else if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA)
            lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;
    }

    void startVideo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());

        String name = "lecturemate-" + currentDateandTime + ".mp4";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, name);

        MediaStoreOutputOptions mediaStoreOutput = new MediaStoreOutputOptions.Builder(this.getContentResolver(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build();

        Recorder recording = (Recorder) videoCapture.getOutput();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission(Manifest.permission.RECORD_AUDIO, AUDIO_PERMISSION_CODE);
        }
        activeRecording = recording.prepareRecording(this, mediaStoreOutput).withAudioEnabled().start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
            long nanos = videoRecordEvent.getRecordingStats().getRecordedDurationNanos();
            long seconds = TimeUnit.SECONDS.convert(nanos, TimeUnit.NANOSECONDS);

            long milis = TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS);

            long HH = seconds / 3600;
            long MM = (seconds % 3600) / 60;
            long SS = seconds % 60;

            String intentDur = String.format(Locale.getDefault(), "%02d:%02d:%02d", HH, MM, SS);
            dur = milis;

            if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) videoRecordEvent;
                uri = finalizeEvent.getOutputResults().getOutputUri();

                names = adapter.getNames();
                times = adapter.getTimes();

                Intent intent = new Intent(TakeVideo.this, EditKeypoints.class);
                intent.putExtra("URI", uri.toString());
                intent.putExtra("name", name);
                intent.putExtra("lectureName", getIntent().getStringExtra("name"));
                intent.putExtra("date", getIntent().getStringExtra("date"));
                intent.putExtra("dur", intentDur);
                intent.putStringArrayListExtra("keypointNames", names);
                intent.putStringArrayListExtra("keypointTimes", times);

                startActivity(intent);
            }
        });
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        @SuppressLint("RestrictedApi") CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing.getLensFacing() == null ? CameraSelector.LENS_FACING_FRONT : lensFacing.getLensFacing())
                .build();

        PreviewView previewView = findViewById(R.id.viewFinder);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        cameraProvider.bindToLifecycle(this, cameraSelector, preview);
    }

    void bindVideo(@NonNull ProcessCameraProvider cameraProvider) {
        QualitySelector qualitySelector = QualitySelector.fromOrderedList(
                Arrays.asList(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
                FallbackStrategy.lowerQualityOrHigherThan(Quality.SD));

        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .build();
        videoCapture = VideoCapture.withOutput(recorder);

        cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, videoCapture);
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}