package ucla.nesl.ActivityLabeling.storage;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

import ucla.nesl.ActivityLabeling.R;


/**
 * Created by zxxia on 12/18/17.
 *
 * Provide interface to
 *   - read/write user activities through database, and dump to a csv format
 *   - read/write user activity and micro location labels into files
 */

public class UserActivityStorageManager {
    private static final String TAG = UserActivityStorageManager.class.getSimpleName();

    private static final String usrActLog = "stoppedActivities.csv";
    private static final String usrOngoingActLog = "ongoingActivities.csv";
    private static final String FILE_NAME_USER_MICRO_LOCATION_LABELS = "uloc.txt";
    private static final String FILE_NAME_USER_ACTIVITY_LABELS = "type.txt";

    private UserActivityDatabase db;

    private File storageDir;

    private int numStoredOfActivities = 0;

    private ArrayList<String> defaultMicroLocationLabels;
    private ArrayList<String> defaultActivityLabels;

    public UserActivityStorageManager(Context context) {
        db = UserActivityDatabase.getAppDatabase(context);

        // file system check
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            throw new IllegalStateException("Cannot access external storage");
        }

        String appName = context.getString(R.string.app_name);
        storageDir = new File(Environment.getExternalStorageDirectory(), appName);
        if (!storageDir.exists()) {
            boolean success = storageDir.mkdirs();
            if (!success) {
                throw new IllegalStateException("Storage directory cannot be created");
            }
        }

        // default location and activity labels
        defaultMicroLocationLabels = new ArrayList<>(Arrays.asList(
                context.getResources().getStringArray(R.array.microlocations_array)));
        defaultActivityLabels = new ArrayList<>(Arrays.asList(
                context.getResources().getStringArray(R.array.activityTypes_array)));
    }

    /**
     * Returns ongoing user activities (recent one shows first), then finished user activities
     * within the past 24 hours (recent one shows first)
     */
    public ArrayList<UserActivity> getRecentActivities() {
        return new ArrayList(db.getOnGoingAndPast24HoursUserActivitiesLatestFirst());
    }

    public int getNumTotalUserActivities() {
        return db.getNumTotalUserActivities();
    }

    public void addNewUserActivity(UserActivity activity) {
        db.createUserActivity(activity);
    }

    public void updateUserActivity(UserActivity activity) {
        db.updateUserActivity(activity);
    }


    // ==== Activity and location labels ===========================================================
    public ArrayList<String> loadUsrUlocs() {
        return readLinesFromFileProvideDefault(
                FILE_NAME_USER_MICRO_LOCATION_LABELS, defaultMicroLocationLabels, false);
    }

    public ArrayList<String> loadUsrActTpyes() {
        return readLinesFromFileProvideDefault(
                FILE_NAME_USER_ACTIVITY_LABELS, defaultActivityLabels, false);
    }

    public void saveUsrUloc(ArrayList<String> items) {
        try {
            writeLinesToFile(FILE_NAME_USER_MICRO_LOCATION_LABELS, items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveUserActType(ArrayList<String> items) {
        try {
            writeLinesToFile(FILE_NAME_USER_ACTIVITY_LABELS, items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==== Helper methods ===========================================================
    private ArrayList<String> readLinesFromFile(String filename) throws IOException {
        ArrayList<String> resultList = new ArrayList<>();
        File file = new File(storageDir, filename);

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            Log.i(TAG, line);
            resultList.add(line.replace("\n", "").replace("\r", ""));
        }
        br.close();

        return resultList;
    }

    private ArrayList<String> readLinesFromFileProvideDefault(
            String filename, ArrayList<String> defaultVal, boolean allowEmpty) {
        try {
            ArrayList<String> locationLabels = readLinesFromFile(FILE_NAME_USER_MICRO_LOCATION_LABELS);
            if (locationLabels.size() > 0 || allowEmpty)
                return locationLabels;
        } catch (Exception e) {
            return defaultVal;  // make compiler happy
        }
        return defaultVal;
    }

    private void writeLinesToFile(String filename, ArrayList<String> lines) throws IOException {
        File file = new File(storageDir, filename);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for (String line : lines) {
            bw.write(line + '\n');
        }
        bw.close();
    }
}
