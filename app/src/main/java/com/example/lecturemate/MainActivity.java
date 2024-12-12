package com.example.lecturemate;

import static androidx.camera.core.impl.utils.ContextUtil.getApplicationContext;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import org.json.JSONException;

import java.io.File;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private String uid;
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ApplicationInfo ai = null;
        try {
            ai = getApplicationContext().getPackageManager()
                    .getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        Object keyObj = ai.metaData.get("keyValue");
        String keyStr = (String) keyObj;


        uid = UUID.randomUUID().toString(); //create an instance-unique id. This will be used later for s3

        File folder = getExternalFilesDir(null);//make sure the local directory is good to be searched
        if (!folder.exists()) {
            folder.mkdir();
        }

        if (LoaderManager.getInstance(this).getLoader(0) != null) {
            LoaderManager.getInstance(this).initLoader(0, null, this);
        }

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getBooleanExtra("load", false)) { //if we have a  video to process
                loading = true;
                findViewById(R.id.loadingAPIText).setVisibility(View.VISIBLE); //show the loading bar
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                try {
                    SaveFiles.keyPointsToJSON( //save the keypoints.json file
                            getExternalFilesDir(null),
                            intent.getStringExtra("vidName"),
                            intent.getStringArrayListExtra("names"),
                            intent.getStringArrayListExtra("times"),
                            intent.getStringArrayListExtra("descs"));
                    SaveFiles.lectureToJSON(getExternalFilesDir(null), //save the lecture.json file
                            intent.getStringExtra("vidName"),
                            intent.getStringExtra("lectureName"),
                            intent.getStringExtra("date"),
                            intent.getStringExtra("dur"),
                            intent.getStringExtra("uri"));

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                runNetwork(getExternalFilesDir(null), intent.getStringExtra("uri"), intent.getStringExtra("vidName"), keyStr); //aws go brrrr
            }

        }

        findViewById(R.id.recordButton).setOnClickListener(v -> {
            if (!loading) {
                Intent newIntent = new Intent(MainActivity.this, NewLecture.class);
                startActivity(newIntent);
            }
        });

        findViewById(R.id.openButton).setOnClickListener(v -> {
            if (!loading) {
                Intent newIntent = new Intent(MainActivity.this, FileSearch.class);
                startActivity(newIntent);
            }
        });

        findViewById(R.id.importButton).setOnClickListener(v -> {
            if (!loading) {
                Intent newIntent = new Intent(MainActivity.this, ImportVideo.class);
                startActivity(newIntent);
            }
        });
    }

    public void runNetwork(File dir, String uri, String name, String key) {
        Bundle keyBundle = new Bundle();
        keyBundle.putString("dir", dir.getPath());
        keyBundle.putString("uid", uid);
        keyBundle.putString("uri", uri);
        keyBundle.putString("name", name);
        keyBundle.putString("key", key);
        LoaderManager.getInstance(this).restartLoader(0, keyBundle, this);
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        assert args != null;
        return new S3Loader(this, args.getString("dir"), args.getString("uid"), args.getString("uri"), args.getString("name"), args.getString("key"));
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        findViewById(R.id.loadingAPIText).setVisibility(View.INVISIBLE); //hide progress bar
        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        loading = false;
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}