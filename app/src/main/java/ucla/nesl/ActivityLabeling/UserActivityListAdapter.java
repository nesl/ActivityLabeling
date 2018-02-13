package ucla.nesl.ActivityLabeling;

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

import ucla.nesl.ActivityLabeling.storage.UserActivity;
import ucla.nesl.ActivityLabeling.storage.UserActivityStorageManager;
import ucla.nesl.ActivityLabeling.utils.Utils;

/**
 * Created by zxxia on 12/3/17.
 * Class to construct the activity list in main activity.
 */

public class UserActivityListAdapter extends BaseAdapter {
    private List<UserActivity> mList;
    private LayoutInflater mInflater;
    private UserActivityStorageManager mStore;

    private LocationService mService;

    UserActivityListAdapter(MainActivity activity, List<UserActivity> actsList,
                            UserActivityStorageManager actStoreMngr, LocationService service) {
        mList = actsList;

        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public UserActivity getItem(int position) {
        //TODO: it shouldn't need to reverse the order from inside
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

        final UserActivity actInfo = getItem(position);


        startTV.setText(Utils.timeToString(actInfo.startTimeMs));
        endTV.setText(Utils.timeToString(actInfo.endTimeMs));
        startLocTV.setText(Utils.locToString(actInfo.startLatitude, actInfo.startLongitude));
        endLocTV.setText(Utils.locToString(actInfo.endLatitude, actInfo.endLongitude));
        ulocTV.setText(actInfo.microLocationLabel);
        typeTV.setText(actInfo.type);
        dscrpTV.setText(actInfo.description);

        final Chronometer chronometer = rowView.findViewById(R.id.durationChrom);

        if (!actInfo.isStopped()) {
            // end time is not set so keep counting time
            long elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            chronometer.setBase(actInfo.startTimeMs - elapsedRealtimeOffset);
            chronometer.start();
        } else {
            // end time is set already, just calculate the duration
            long elapsedRealtimeOffset = actInfo.endTimeMs - SystemClock.elapsedRealtime();
            chronometer.setBase(actInfo.startTimeMs - elapsedRealtimeOffset);
        }


        final Button stopBtn = rowView.findViewById(R.id.stopBtn);

        if (actInfo.isStopped()) {
            // Hide the stop button if the user activity is done
            stopBtn.setEnabled(false);
            stopBtn.setVisibility(View.GONE);
        }
        else {
            // provide the action when the stop button is clicked
            stopBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // update ambient attributes of the user activity
                    actInfo.endTimeMs = Calendar.getInstance().getTime().getTime();
                    actInfo.setEndLocation(mService.getCurrentLocation());

                    chronometer.stop();
                    long elapsedRealtimeOffset = actInfo.endTimeMs - SystemClock.elapsedRealtime();
                    chronometer.setBase(actInfo.startTimeMs - elapsedRealtimeOffset);

                    // update UI
                    endTV.setText(Utils.timeToString(actInfo.endTimeMs));
                    endLocTV.setText(Utils.locToString(actInfo.endLatitude, actInfo.endLongitude));
                    stopBtn.setEnabled(false);
                    stopBtn.setVisibility(View.GONE);

                    // synchronize with the persistent storage
                    mStore.updateUserActivity(actInfo);
                }
            });
        }

        return rowView;
    }
}


