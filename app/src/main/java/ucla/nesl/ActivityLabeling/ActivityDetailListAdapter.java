package ucla.nesl.ActivityLabeling;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

/**
 * Created by zxxia on 12/3/17.
 * Class to construct the activity list in main activity.
 */

public class ActivityDetailListAdapter extends BaseAdapter {
    private MainActivity mMainActivity;
    private List<ActivityDetail> mList;
    private LayoutInflater mInflater;
    private ActivityStorageManager mStore;

    private LocationService mService;

    ActivityDetailListAdapter(MainActivity activity, List<ActivityDetail> actsList, ActivityStorageManager actStoreMngr, LocationService service) {
        mMainActivity = activity;
        mList = actsList;

        mInflater = (LayoutInflater)mMainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mStore = actStoreMngr;
        mService = service;
    }

    public void updateService(LocationService service){
        mService = service;
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
        final View rowView = mInflater.inflate(R.layout.list_item_activitydetaillist, parent, false);



        final TextView startTV = rowView.findViewById(R.id.startTV);
        final TextView endTV = rowView.findViewById(R.id.endTV);
        final TextView startLocTV = rowView.findViewById(R.id.startLocTV);
        final TextView endLocTV = rowView.findViewById(R.id.endLocTV);
        final TextView ulocTV = rowView.findViewById(R.id.ulocTV);
        final TextView typeTV = rowView.findViewById(R.id.typeTV);
        final TextView dscrpTV = rowView.findViewById(R.id.dscrpTV);

        final ActivityDetail actInfo = (ActivityDetail) getItem(position);


        String content = Utils.timeToString(actInfo.getStartTime());
        startTV.setText(content);

        content = Utils.timeToString(actInfo.getEndTime());
        endTV.setText(content);


        content = Utils.locToString(actInfo.getStartLatitude(), actInfo.getStartLongitude());
        startLocTV.setText(content);

        content = Utils.locToString(actInfo.getEndLatitude(), actInfo.getEndLongitude());
        endLocTV.setText(content);

        content = actInfo.getMicrolocation();
        ulocTV.setText(content);

        content = actInfo.getActType();
        typeTV.setText(content);

        content = actInfo.getDescription();
        dscrpTV.setText(content);

        final Chronometer chronometer = rowView.findViewById(R.id.durationChrom);

        if (!actInfo.isStopped()) {
            // end time is not set so keep counting time
            long elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            chronometer.setBase(actInfo.getStartTime() - elapsedRealtimeOffset);
            chronometer.start();
        } else {
            // end time is set already, just calculate the duration
            long elapsedRealtimeOffset = actInfo.getEndTime() - SystemClock.elapsedRealtime();
            chronometer.setBase(actInfo.getStartTime() - elapsedRealtimeOffset);
        }


        final Button stopBtn = rowView.findViewById(R.id.stopBtn);
        if (actInfo.isStopped()) {
            stopBtn.setEnabled(false);
            stopBtn.setVisibility(View.GONE);
        }
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!actInfo.isStopped()) {
                    actInfo.setEndTime(Calendar.getInstance().getTime().getTime());

                    String content = Utils.timeToString(actInfo.getEndTime());
                    endTV.setText(content);

                    actInfo.setEndLocation(mService.getCurrentLocation());
                    content = Utils.locToString(actInfo.getEndLatitude(),actInfo.getEndLongitude());
                    endLocTV.setText(content);

                    chronometer.stop();
                    long elapsedRealtimeOffset = actInfo.getEndTime() - SystemClock.elapsedRealtime();
                    chronometer.setBase(actInfo.getStartTime() - elapsedRealtimeOffset);

                    mStore.saveOneActivity(actInfo);
                    stopBtn.setEnabled(false);
                    stopBtn.setVisibility(View.GONE);
                    mMainActivity.incrementNumOfStoredActivities();
                }
            }
        });

        return rowView;
    }
}


