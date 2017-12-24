package ucla.nesl.ActivityLabeling;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

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
    private ActivityStorageManager mStore;


    /**
     * TextView Lables
     */
    private static final String START_TIME = "Start Time";
    private static final String END_TIME = "End Time";
    private static final String START_LOCATION = "Start Location";
    private static final String END_LOCATION = "End Location";
    private static final String MICROLOCATION = "Microlocation";
    private static final String TYPE = "Activity Type";
    private static final String DESCRIPTION = "Description";




    ActivityDetailListAdapter(Context context, List<ActivityDetail> actsList, ActivityStorageManager actStoreMngr) {
        mContext = context;
        mList = actsList;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mStore = actStoreMngr;
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {

        // Display items in from  new(top) to old(bottom)
        return mList.get(getCount() - 1 - position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get view for row item
        @SuppressLint("ViewHolder") View rowView = mInflater.inflate(R.layout.list_item_activitydetaillist, parent, false);



        final TextView startTV = rowView.findViewById(R.id.startTV);
        final TextView endTV = rowView.findViewById(R.id.endTV);
        final TextView startLocTV = rowView.findViewById(R.id.startLocTV);
        final TextView endLocTV = rowView.findViewById(R.id.endLocTV);
        final TextView ulocTV = rowView.findViewById(R.id.ulocTV);
        final TextView typeTV = rowView.findViewById(R.id.typeTV);
        final TextView dscrpTV = rowView.findViewById(R.id.dscrpTV);

        final ActivityDetail actInfo = (ActivityDetail) getItem(position);

        String timeString = DateFormat.format("HH:mm:ss MM/dd/yyyy", new Date(actInfo.m_start)).toString();
        String content = START_TIME + ": " + timeString;
        startTV.setText(content);


        if (actInfo.m_end == -1) {
            timeString = "N/A";
        } else {
            timeString = DateFormat.format("HH:mm:ss MM/dd/yyyy", new Date(actInfo.m_end)).toString();
        }
        content = END_TIME + ": " + timeString;
        endTV.setText(content);


        content = START_LOCATION + ": ";
        if (actInfo.m_latitude == -1 || actInfo.m_longitude == -1) {
            content += "N/A";
        } else {
            content += actInfo.m_latitude + " ," +actInfo. m_longitude;
        }
        startLocTV.setText(content);

        content = END_LOCATION + ": ";

        if (actInfo.m_latitude == -1 || actInfo.m_longitude == -1) {
            content += "N/A";
        } else {
            content += actInfo.m_latitude + " ," +actInfo. m_longitude;
        }
        endLocTV.setText(content);



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


        final Button stopBtn = rowView.findViewById(R.id.stopBtn);
        if (actInfo.m_end != -1) {
            stopBtn.setEnabled(false);
            stopBtn.setVisibility(View.GONE);
        }
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actInfo.m_end == -1) {
                    actInfo.m_end = Calendar.getInstance().getTime().getTime();

                    String content = END_TIME + ": " + DateFormat.format("HH:mm:ss MM/dd/yyyy", new Date(actInfo.m_end)).toString();
                    endTV.setText(content);

                    chronometer.stop();
                    long elapsedRealtimeOffset = actInfo.m_end - SystemClock.elapsedRealtime();
                    chronometer.setBase(actInfo.m_start - elapsedRealtimeOffset);

                    mStore.saveOneActivity(actInfo);
                    stopBtn.setEnabled(false);
                    stopBtn.setVisibility(View.GONE);
                }
            }
        });

        return rowView;
    }



}


