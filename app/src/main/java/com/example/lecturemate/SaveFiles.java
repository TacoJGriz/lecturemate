package com.example.lecturemate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Contains some classes that work with files,
 * mainly JSON
 */
public class SaveFiles {

    /**
     * Create the keypoints.json file
     *
     * @param dir   call getExternalFileDir() in main
     * @param names list containing names of each keypoint
     * @param times list containing times of each keypoint
     * @param descs list containing descriptions of each keypoint
     */
    public static void keyPointsToJSON(File dir, String name, ArrayList<String> names, ArrayList<String> times, ArrayList<String> descs) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jObject;
        for (int i = 0; i < names.size(); i++) { //add keypoints to a JSONArray
            jObject = new JSONObject();
            jObject.put("name", names.get(i));
            jObject.put("time", times.get(i));
            jObject.put("description", descs.get(i));
            jsonArray.put(jObject);
        }

        JSONObject keypoints = new JSONObject();
        keypoints.put("numOfKeypoints", names.size());
        keypoints.put("keypoints", jsonArray);

        try { //save it all!
            File folder = new File(dir, name.substring(0, name.lastIndexOf('.')));
            if (!folder.exists()) {
                folder.mkdir();
            }
            File jsonFile = new File(folder, "keypoints.json");
            jsonFile.createNewFile();

            FileWriter file = new FileWriter(jsonFile);
            file.write(keypoints.toString());
            file.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create the lecture.json file
     *
     * @param dir         call getExternalFileDir() from main
     * @param name        name of video
     * @param lectureName name of lecture
     * @param dur         length of video
     * @param date        date of video
     * @param vidURI      video uri
     */
    public static void lectureToJSON(File dir, String name, String lectureName, String dur, String date, String vidURI) throws JSONException {
        JSONObject lecture = new JSONObject(); //make the json
        lecture.put("name", lectureName);
        lecture.put("date", date);
        lecture.put("duration", dur);
        lecture.put("videoURI", vidURI);


        try {
            File folder = new File(dir, name.substring(0, name.lastIndexOf('.')));//and write it
            if (!folder.exists()) {
                folder.mkdir();
            }
            File jsonFile = new File(folder, "lecture.json");
            jsonFile.createNewFile();

            FileWriter file = new FileWriter(jsonFile);
            file.write(lecture.toString());
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param dir call getExternalFileDir() from main
     * @return Returns an arraylist of each lectures JSON data to be displayed
     */
    public static ArrayList<JsonObject> loadLectures(File dir) {
        ArrayList<JsonObject> out = new ArrayList<>();
        File[] lectures = dir.listFiles();//get a list of files
        JsonParser parser;
        assert lectures != null;
        for (File lecture : lectures) {
            parser = new JsonParser();
            try {//get the json
                JsonObject json = parser.parse(new FileReader(new File(lecture, "lecture.json"))).getAsJsonObject();
                out.add(json);
            } catch (FileNotFoundException ignore) {
            }
        }

        return out;
    }

    /**
     * @param dir   call getExternalFileDir() from main
     * @param index index of the desired lecture (from the list of lectures)
     * @return Returns the lecture folder
     */
    public static File loadLecture(File dir, int index) {
        File[] lectures = dir.listFiles();
        assert lectures != null;

        return lectures[index];
    }
}
