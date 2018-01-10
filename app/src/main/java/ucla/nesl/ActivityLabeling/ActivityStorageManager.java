package ucla.nesl.ActivityLabeling;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by zxxia on 12/18/17.
 * Manage log storage
 */

public class ActivityStorageManager {
    private static final String TAG = ActivityStorageManager.class.getSimpleName();
    private Context m_context;
    private static final String usrActLog = "stoppedActivities.csv";
    private static final String usrOngoingActLog = "ongoingActivities.csv";
    private static final String usrUlocLog = "uloc.txt";
    private static final String usrActTypeLog = "type.txt";


    ActivityStorageManager(Context context) {
        m_context = context;
    }

    void saveOneActivity(ActivityDetail actInfo) {
        if (!actInfo.isStopped()) {
            // Activity is not stopped yet.
            return;
        }

        if (isExternalStorageWritable()){
            String string = actInfo.toCSVLine();

            Log.i(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());

            File file = new File(getStorageDir(m_context.getString(R.string.app_name)), usrActLog);

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

    void saveOngoingActivities(List<ActivityDetail> actsList) {
        if (isExternalStorageWritable()){

            Log.i(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
            File file = new File(getStorageDir(m_context.getString(R.string.app_name)), usrOngoingActLog);
            FileOutputStream outputStream;
            try{
                outputStream = new FileOutputStream(file, false);

                for (int i = 0; i < actsList.size(); i++) {
                    ActivityDetail actInfo = actsList.get(i);
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

    ArrayList<ActivityDetail> getActivityLogs() {
        ArrayList<ActivityDetail> resultList  = new ArrayList<>();
        if (isExternalStorageWritable()) {


            File file = new File(getStorageDir(m_context.getString(R.string.app_name)), usrActLog);
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String csvLine;
                while ((csvLine = br.readLine()) != null) {
                    String[] row = csvLine.split(",");
                    Log.i(TAG, csvLine);
                    long startTime = Long.valueOf(row[0]);
                    long endTime = Long.valueOf(row[1]);
                    if (endTime >= Calendar.getInstance().getTime().getTime() - 24 * 3600 * 1000) {
                        ActivityDetail actInfo  = new ActivityDetail(startTime, endTime,
                                Double.valueOf(row[2]), Double.valueOf(row[3]),
                                Double.valueOf(row[4]), Double.valueOf(row[5]),
                                row[6], row[7], row[8]);
                        resultList.add(actInfo);
                    }
                }
                br.close();
            }
            catch (IOException e) {
                //You'll need to add proper error handling here
            }

            file = new File(getStorageDir(m_context.getString(R.string.app_name)), usrOngoingActLog);
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String csvLine;
                while ((csvLine = br.readLine()) != null) {
                    String[] row = csvLine.split(",");
                    Log.i(TAG, csvLine);
                    long startTime = Long.valueOf(row[0]);
                    long endTime = Long.valueOf(row[1]);

                    ActivityDetail actInfo  = new ActivityDetail(startTime, endTime,
                            Double.valueOf(row[2]), Double.valueOf(row[3]),
                            Double.valueOf(row[4]), Double.valueOf(row[5]),
                            row[6], row[7], row[8]);
                    resultList.add(actInfo);
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
            File file = new File(getStorageDir(m_context.getString(R.string.app_name)), filename);
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

    public ArrayList<String> loadUsrUlocs() {
        return load(usrUlocLog);
    }

    public ArrayList<String> loadUsrActTpyes() {
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

    private void writeToFile(String filename, ArrayList<String> items, Boolean append) {
        if (isExternalStorageWritable()){
            Log.i(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
            File file = new File(getStorageDir(m_context.getString(R.string.app_name)), filename);
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
