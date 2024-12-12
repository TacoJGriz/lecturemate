package com.example.lecturemate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Necessary info before recording
 */
public class NewLecture extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_lecture);

        findViewById(R.id.AddKeypointsButton).setOnClickListener(v -> {
            EditText edit = v.getRootView().findViewById(R.id.editLectureName);
            String text = edit.getText().toString();
            if (text.equals("")) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm", Locale.getDefault()); //if there is no name given, make it the date
                String currentDateandTime = sdf.format(new Date());
                text = "New Lecture " + currentDateandTime;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            String date = dateFormat.format(new Date()); //get the date

            //start the next activity
            Intent intent = new Intent(NewLecture.this, TakeVideo.class);
            intent.putExtra("name", text);
            intent.putExtra("date", date);
            startActivity(intent);
        });
    }
}