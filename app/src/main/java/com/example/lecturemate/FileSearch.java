package com.example.lecturemate;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Activity that lets the user pick a lecture to open and view
 */
public class FileSearch extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_search);

        ArrayList<String> mNames = new ArrayList<>();
        ArrayList<String> mDates = new ArrayList<>();
        ArrayList<String> mDurs = new ArrayList<>();

        //get json for each lecture saved
        ArrayList<JsonObject> lecturesJson = SaveFiles.loadLectures(getExternalFilesDir(null));
        ArrayList<String> mURIs = new ArrayList<>();

        for (JsonObject jo : lecturesJson) {
            //populate arraylists with information from each lectures JSON
            Set<Map.Entry<String, JsonElement>> entries = jo.entrySet();
            String[] tmp = new String[4];
            int i = 0;
            for (Map.Entry<String, JsonElement> entry : entries) {
                tmp[i] = entry.getValue().getAsString();
                i++;
            }
            mNames.add(tmp[0]);
            mDates.add(tmp[1]);
            mDurs.add(tmp[2]);
            mURIs.add(tmp[3]);
        }

        RecyclerView recyclerView = findViewById(R.id.filesRecyc);

        FileAdapter adapter = new FileAdapter(this, mNames, mDates, mDurs, mURIs);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(FileSearch.this));
    }

}