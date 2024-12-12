package com.example.lecturemate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ImportVideo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_video);

        findViewById(R.id.AddKeypointsButton).setOnClickListener(v -> {
            EditText edit = v.getRootView().findViewById(R.id.editLectureName2);
            String text = edit.getText().toString();
            if (text.equals("")) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm", Locale.getDefault()); //if there is no name given, make it the date
                String currentDateandTime = sdf.format(new Date());
                text = "New Lecture " + currentDateandTime;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            String date = dateFormat.format(new Date()); //get the date
            EditText uriEdit = findViewById(R.id.editUri);
            //start the next activity
            Intent intent = new Intent(ImportVideo.this, EditKeypoints.class);
            intent.putExtra("name", text);
            intent.putExtra("lectureName", text);
            intent.putExtra("date", date);
            intent.putExtra("URI", uriEdit.getText().toString());
            intent.putStringArrayListExtra("keypointNames", new ArrayList<String>());
            intent.putStringArrayListExtra("keypointTimes", new ArrayList<String>());
            startActivity(intent);
        });
    }
}