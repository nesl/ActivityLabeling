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
import java.util.List;

import ucla.nesl.ActivityLabeling.R;


/**
 * Created by zxxia on 12/18/17.
 * Manage log storage
 */

public class UserActivityStorageManager {
    private static final String TAG = UserActivityStorageManager.class.getSimpleName();

    private static final String usrActLog = "stoppedActivities.csv";
    private static final String usrOngoingActLog = "ongoingActivities.csv";
    private static final String FILE_NAME_USER_MICRO_LOCATION_LABELS = "uloc.txt";
    private static final String FILE_NAME_USER_ACTIVITY_LABELS = "type.txt";

    private UserActivityDatabase db;

    private Context mContext;
    private File storageDir;

    private int numStoredOfActivities = 0;

    private ArrayList<String> defaultMicroLocationLabels;
    private ArrayList<String> defaultActivityLabels;

    public UserActivityStorageManager(Context context) {
        mContext = context;
        db = UserActivityDatabase.getAppDatabase(context);

        // file system check
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            throw new IllegalStateException("Cannot access external storage");
        }

        String appName = mContext.getString(R.string.app_name);
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

    public int getNumberOfStoredActivities() {
        return numStoredOfActivities;
    }

    public void saveOneActivity(UserActivity actInfo) {
        if (!actInfo.isStopped()) {
            // Activity is not stopped yet.
            return;
        }

        String string = actInfo.toCSVLine();

        Log.i(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());

        File file = new File(storageDir, usrActLog);

        FileOutputStream stream;
        try{
            stream = new FileOutputStream(file, true);
            stream.write(string.getBytes());
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveOngoingActivities(List<UserActivity> actsList) {

        Log.i(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
        File file = new File(storageDir, usrOngoingActLog);
        FileOutputStream outputStream;
        try{
            outputStream = new FileOutputStream(file, false);

            for (int i = 0; i < actsList.size(); i++) {
                UserActivity actInfo = actsList.get(i);
                if (!actInfo.isStopped()) {

                    String string = actInfo.toCSVLine();
                    outputStream.write(string.getBytes());
                }
            }

            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<UserActivity> getActivityLogs() {
        /*ArrayList<UserActivity> resultList  = new ArrayList<>();
        if (isExternalStorageWritable()) {

            File file = new File(getStorageDir(mContext.getString(R.string.app_name)), usrActLog);
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String csvLine;
                long oneDayAgo = Calendar.getInstance().getTime().getTime() - TimeUnit.DAYS.toMillis(1);
                while ((csvLine = br.readLine()) != null) {
                    numStoredOfActivities++;
                    UserActivity actInfo = UserActivity.parseCSVLine(csvLine);
                    if (actInfo.endTimeMs > oneDayAgo) {
                        resultList.add(actInfo);
                    }
                }
                br.close();
            }
            catch (IOException e) {
                //TODO You'll need to add proper error handling here
            }

            file = new File(getStorageDir(mContext.getString(R.string.app_name)), usrOngoingActLog);
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String csvLine;
                while ((csvLine = br.readLine()) != null) {
                    resultList.add(UserActivity.parseCSVLine(csvLine));
                }
                br.close();
            }
            catch (IOException e) {
                //You'll need to add proper error handling here
            }
        }
        return resultList;*/
        return new ArrayList(db.getOnGoingAndPast24HoursUserActivitiesLatestFirst());
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
