package com.example.lecturemate;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.transcribe.AmazonTranscribeClient;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobResult;
import com.amazonaws.services.transcribe.model.LanguageCode;
import com.amazonaws.services.transcribe.model.Media;
import com.amazonaws.services.transcribe.model.StartTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.TranscriptionJob;
import com.amazonaws.services.transcribe.model.TranscriptionJobStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class NetworkUtils {

    /**
     * Will take a video file and send it through AWS and save the transcript
     *
     * @param context
     * @param dir     use getExternalFileDir() in main
     * @param UUID    unique device id
     * @param vidUri  video location
     * @param vidName
     * @throws URISyntaxException
     * @throws IOException
     */
    public static void run(Context context, File dir, String UUID, String vidUri, String vidName, String key) throws URISyntaxException, IOException {

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider( //Create an identity to log onto my AWS services
                context,
                key,
                Regions.US_EAST_2 // Region
        );

        AmazonS3 s3client = new AmazonS3Client(credentialsProvider);

        Uri uri = Uri.parse(vidUri);

        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        InputStream stream = resolver.openInputStream(uri); //open the video

        Cursor cursor = resolver.query(uri,
                null, null, null, null);
        cursor.moveToFirst();
        long size = cursor.getLong(Math.abs(cursor.getColumnIndex(OpenableColumns.SIZE)));
        cursor.close(); //all of this is necessary for us to know what size the video is

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(size);

        String s3Location = "users/" + UUID + "/" + vidName.substring(0, vidName.indexOf('.')); //this will be the path in the s3 bucket
        String videoS3Location = s3Location + "/video.mp4";

        s3client.putObject(
                "lecture-mate",
                videoS3Location,
                stream, meta
        ); //uploaded B)

        stream.close();

        AmazonTranscribeClient transcribeClient = new AmazonTranscribeClient(credentialsProvider);
        transcribeClient.setRegion(Region.getRegion(Regions.US_EAST_2));

        String transcriptionJobName = vidName.substring(0, vidName.indexOf('.')) + "-transcript";
        String mediaType = "mp4";
        Media myMedia = new Media();
        myMedia.withMediaFileUri("s3://lecture-mate/" + videoS3Location); //

        String outputS3BucketName = "lecture-mate";

        StartTranscriptionJobRequest request = new StartTranscriptionJobRequest();
        request.withLanguageCode(LanguageCode.EnUS.toString());
        request.withMediaFormat(mediaType);
        request.withMedia(myMedia);
        request.withOutputBucketName(outputS3BucketName);
        request.withTranscriptionJobName(transcriptionJobName);

        transcribeClient.startTranscriptionJob(request);    //all of this is to make a new transcription job

        GetTranscriptionJobRequest getTranscriptionJobRequest = new GetTranscriptionJobRequest().withTranscriptionJobName(transcriptionJobName);

        boolean resultFound = false;
        TranscriptionJob transcriptionJob = new TranscriptionJob();
        GetTranscriptionJobResult getTranscriptionJobResult;
        while (!resultFound) { //this loop waits for Transcribe to do its thing
            getTranscriptionJobResult = transcribeClient.getTranscriptionJob(getTranscriptionJobRequest);
            transcriptionJob = getTranscriptionJobResult.getTranscriptionJob();
            if (transcriptionJob.getTranscriptionJobStatus()
                    .equalsIgnoreCase(TranscriptionJobStatus.COMPLETED.name())) {
                resultFound = true;
            } else if (transcriptionJob.getTranscriptionJobStatus()
                    .equalsIgnoreCase(TranscriptionJobStatus.FAILED.name())) {
                resultFound = true;
            } else if (transcriptionJob.getTranscriptionJobStatus()
                    .equalsIgnoreCase(TranscriptionJobStatus.IN_PROGRESS.name())) {
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    Log.d("Interrupted Exception {}", e.getMessage());
                }
            }
        }

        //gets the s3 URI of the transcript
        String URI = transcriptionJob.getTranscript().getTranscriptFileUri();
        AmazonS3URI s3ObjectURI = new AmazonS3URI(URI);
        S3Object transcript = s3client.getObject(s3ObjectURI.getBucket(), s3ObjectURI.getKey()); //and gets the object

        S3ObjectInputStream inputStream = transcript.getObjectContent();

        File folder = new File(dir, vidName.substring(0, vidName.lastIndexOf('.')));
        if (!folder.exists()) {
            folder.mkdir();
        }
        File tFile = new File(folder, "transcript.json");
        tFile.createNewFile(); //create the local transcript json file

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.copy(inputStream, tFile.toPath(), StandardCopyOption.REPLACE_EXISTING); //and write to it
        }

        s3client.copyObject(outputS3BucketName, transcript.getKey(), outputS3BucketName, s3Location + "/transcript.json"); //delete the file to save space in s3
        s3client.deleteObject(outputS3BucketName, transcript.getKey());
    }
}
