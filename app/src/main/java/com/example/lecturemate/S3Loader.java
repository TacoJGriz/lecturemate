package com.example.lecturemate;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * to do all the async stuff with AWS
 */
public class S3Loader extends AsyncTaskLoader<String> {
    private final String mUid;
    private final Context mContext;
    private final String mUri;
    private final String mName;

    private final String mDir;

    private final String mKey;

    public S3Loader(@NonNull Context context, String dir, String uid, String uri, String name, String key) {
        super(context);

        mDir = dir;
        mUri = uri;
        mName = name;
        mUid = uid;
        mContext = context;
        mKey = key;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        try {
            NetworkUtils.run(mContext, new File(mDir), mUid, mUri, mName, mKey);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
