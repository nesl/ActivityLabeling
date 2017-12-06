package ucla.nesl.ActivityLabeling;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by zxxia on 12/3/17.
 * Class to construct the activity list in main activity.
 */

public class ActivityDetailListAdapter extends BaseAdapter {
    private Context mContext;
    private List<ActivityDetail> mList;
    private LayoutInflater mInflater;

    private static final String START_TIME = "Start Time";
    private static final String LOCATION = "Location";
    private static final String MICROLOCATION = "Microlocation";
    private static final String TYPE = "Activity Type";
    private static final String DESCRIPTION = "Description";

    ActivityDetailListAdapter(Context context, List<ActivityDetail> actsList) {
        mContext = context;
        mList = actsList;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get view for row item
        @SuppressLint("ViewHolder") View rowView = mInflater.inflate(R.layout.list_item_activitydetaillist, parent, false);

        TextView startTV = rowView.findViewById(R.id.startTV);
        TextView locTV = rowView.findViewById(R.id.locTV);
        TextView ulocTV = rowView.findViewById(R.id.ulocTV);
        TextView typeTV = rowView.findViewById(R.id.typeTV);
        TextView dscrpTV = rowView.findViewById(R.id.dscrpTV);

        final ActivityDetail actInfo = (ActivityDetail) getItem(position);

        String timeString = DateFormat.format("HH:mm:ss MM/dd/yyyy", new Date(actInfo.m_start)).toString();

        String content = START_TIME + ": " + timeString;
        startTV.setText(content);

        content = LOCATION + ": " + actInfo.m_loc;
        locTV.setText(content);

        content = MICROLOCATION + ": " + actInfo.m_uloc;
        ulocTV.setText(content);

        content = TYPE + ": " + actInfo.m_type;
        typeTV.setText(content);

        content = DESCRIPTION + ": " + actInfo.m_dscrp;
        dscrpTV.setText(content);

        final Chronometer chronometer = rowView.findViewById(R.id.durationChrom);

        if (actInfo.m_end == -1) {
            // end time is not set so keep counting time
            long elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            chronometer.setBase(actInfo.m_start - elapsedRealtimeOffset);
            chronometer.start();
        } else {
            // end time is set already, just calculate the duration
            long elapsedRealtimeOffset = actInfo.m_end - SystemClock.elapsedRealtime();
            chronometer.setBase(actInfo.m_start - elapsedRealtimeOffset);
        }


        Button stopBtn = rowView.findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actInfo.m_end == -1) {
                    actInfo.m_end = Calendar.getInstance().getTime().getTime();
                    chronometer.stop();

                    if (isExternalStorageWritable()){
                        String filename = "log.csv";
                        String string = Long.toString(actInfo.m_start) + ',' +
                                Long.toString(actInfo.m_end) + ',' + actInfo.m_loc + ',' +
                                actInfo.m_uloc + ',' + actInfo.m_type + ',' + actInfo.m_dscrp + '\n';
                        //Log.e("Save file", Environment.getExternalStorageDirectory().getAbsolutePath());
                        File file = new File(getStorageDir(v.getContext().getString(R.string.app_name)), filename);

                        FileOutputStream stream;
                        try{
                            stream = new FileOutputStream(file, true);
                            stream.write(string.getBytes());
                            stream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    Toast.makeText(v.getContext(), "Activity has already been stopped!", Toast.LENGTH_LONG).show();
                }
            }
        });

        return rowView;
    }
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public File getStorageDir(String appname) {
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


