package ucla.nesl.ActivityLabeling.storage;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ucla.nesl.ActivityLabeling.R;

/**
 * Created by zxxia on 12/18/17.
 * Manage log storage
 */

public class UserActivityStorageManager {
    private static final String TAG = UserActivityStorageManager.class.getSimpleName();

    private static final String usrActLog = "stoppedActivities.csv";
    private static final String usrOngoingActLog = "ongoingActivities.csv";
    private static final String usrUlocLog = "uloc.txt";
    private static final String usrActTypeLog = "type.txt";

    private Context mContext;

    private int numStoredOfActivities = 0;


    public UserActivityStorageManager(Context context) {
        mContext = context;
    }

    public int getNumberOfStoredActivities() {
        return numStoredOfActivities;
    }

    public void saveOneActivity(UserActivity actInfo) {
        if (!actInfo.isStopped()) {
            // Activity is not stopped yet.
            return;
        }

        if (isExternalStorageWritable()){
            String string = actInfo.toCSVLine();

            Log.i(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());

            File file = new File(getStorageDir(mContext.getString(R.string.app_name)), usrActLog);

            FileOutputStream stream;
            try{
                stream = new FileOutputStream(file, true);
                stream.write(string.getBytes());
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveOngoingActivities(List<UserActivity> actsList) {
        if (isExternalStorageWritable()){

            Log.i(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
            File file = new File(getStorageDir(mContext.getString(R.string.app_name)), usrOngoingActLog);
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
    }

    public ArrayList<UserActivity> getActivityLogs() {
        ArrayList<UserActivity> resultList  = new ArrayList<>();
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
        return resultList;
    }

    private ArrayList<String> load(String filename) {
        ArrayList<String> resultList  = new ArrayList<>();
        if (isExternalStorageWritable()) {
            File file = new File(getStorageDir(mContext.getString(R.string.app_name)), filename);
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    Log.i(TAG, line);
                    resultList.add(line);
                }
                br.close();
            }
            catch (IOException e) {
                //You'll need to add proper error handling here
            }
        }
        return resultList;
    }

    ArrayList<String> loadUsrUlocs() {
        return load(usrUlocLog);
    }

    ArrayList<String> loadUsrActTpyes() {
        return load(usrActTypeLog);
    }

    void saveUsrUloc(ArrayList<String> items) {
        writeToFile(usrUlocLog, items, false);
    }



    void saveUserActType( ArrayList<String> items) {
        writeToFile(usrActTypeLog, items, false);
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private File getStorageDir(String appname) {
        // Get the directory for the user's public pictures directory.
        File dir = new File(Environment.getExternalStorageDirectory(), appname);
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                Log.e("Save file", "mkdirs failed");
            }
        }
        return dir;
    }

    private void writeToFile(String filename, ArrayList<String> items, boolean append) {
        if (isExternalStorageWritable()){
            Log.i(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
            File file = new File(getStorageDir(mContext.getString(R.string.app_name)), filename);
            FileOutputStream stream;
            try{
                stream = new FileOutputStream(file, append);
                for (int i = 0; i < items.size(); i++) {
                    stream.write((items.get(i)+'\n').getBytes());
                }
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
