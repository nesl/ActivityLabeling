package ucla.nesl.ActivityLabeling;

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

/**
 * Created by zxxia on 12/18/17.
 * Manage log storage
 */

public class ActivityStorageManager {
    private static final String TAG = ActivityStorageManager.class.getSimpleName();
    private Context m_context;
    private String filename = "log.csv";

    ActivityStorageManager(Context context) {
        m_context = context;
    }

    void saveOneActivity(ActivityDetail actInfo) {
        if (actInfo.m_end == -1) {
            // Activity is not stopped yet.
            return;
        }

        if (isExternalStorageWritable()){
            String string = Long.toString(actInfo.m_start) + ',' + Long.toString(actInfo.m_end) + ',' +
                            Double.toString(actInfo.m_latitude) + ',' + Double.toString(actInfo.m_longitude) + ',' +
                            actInfo.m_uloc + ',' + actInfo.m_type + ',' +
                            actInfo.m_dscrp + '\n';

            Log.i(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());

            File file = new File(getStorageDir(m_context.getString(R.string.app_name)), filename);

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

    void saveActivities(List<ActivityDetail> actsList) {
        if (isExternalStorageWritable()){

            Log.i(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
            File file = new File(getStorageDir(m_context.getString(R.string.app_name)), filename);
            FileOutputStream outputStream;
            try{
                outputStream = new FileOutputStream(file, true);

                for (int i = 0; i < actsList.size(); i++) {
                    ActivityDetail actInfo = actsList.get(i);
                    if (actInfo.m_end != -1) {
                        continue;
                    }
                    String string = Long.toString(actInfo.m_start) + ',' + Long.toString(Calendar.getInstance().getTime().getTime()) + ',' +
                            Double.toString(actInfo.m_latitude) + ',' + Double.toString(actInfo.m_longitude) + ',' +
                            actInfo.m_uloc + ',' + actInfo.m_type + ',' +
                            actInfo.m_dscrp + '\n';

                    outputStream.write(string.getBytes());
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
            File file = new File(getStorageDir(m_context.getString(R.string.app_name)), filename);
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String csvLine;
                while ((csvLine = br.readLine()) != null) {
                    String[] row = csvLine.split(",");
                    Log.i(TAG, csvLine);
                    long startTime = Long.valueOf(row[0]);
                    if (startTime >= Calendar.getInstance().getTime().getTime() - 24 * 3600 * 1000) {
                        ActivityDetail actInfo  = new ActivityDetail(startTime,
                                Long.valueOf(row[1]), Double.valueOf(row[2]),
                                Double.valueOf(row[3]), row[4], row[5], row[6]);
                        resultList.add(actInfo);
                    }
                }
                br.close();
            }
            catch (IOException e) {
                //You'll need to add proper error handling here
            }
        }

        return resultList;
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
}
