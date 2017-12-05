package com.ucla.zxxia.activitytracker;

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
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by zxxia on 12/3/17.
 */

public class ActivityDetailListAdapter extends BaseAdapter {
    private Context mContext;
    private List<ActivityDetail> mList;
    private LayoutInflater mInflater;

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

        String content = Constants.START_TIME + ": " + timeString;
        startTV.setText(content);

        content = Constants.LOCATION + ": " + actInfo.m_loc;
        locTV.setText(content);

        content = Constants.MICROLOCATION + ": " + actInfo.m_uloc;
        ulocTV.setText(content);

        content = Constants.TYPE + ": " + actInfo.m_type;
        typeTV.setText(content);

        content = Constants.DESCRIPTION + ": " + actInfo.m_dscrp;
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
                } else {
                    Toast.makeText(v.getContext(), "Activity has already been stopped!", Toast.LENGTH_LONG).show();
                }
            }
        });

        return rowView;
    }

}
