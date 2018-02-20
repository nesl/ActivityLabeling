package ucla.nesl.ActivityLabeling.service.sensordataprocessing;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;

/**
 * Created by timestring on 2/15/18.
 *
 * To get motion activity via Google API.
 */

public class MotionActivityDataCollector {

    private static final long MOTION_ACTIVITY_FETCH_FREQUENCY_MS = 10 * 1000L;  // 10 seconds

    private static final int PENDING_INTENT_REQUEST_CODE = 0;

    private MotionActivityCallback mActivityCallback;

    private ActivityRecognitionClient activityRecognitionClient;
    private PendingIntent activityRecognitionPendingIntent;
    private MotionActivityResultReceiver motionActivityReceiver;

    public MotionActivityDataCollector(Context context, MotionActivityCallback activityCallback) {
        mActivityCallback = activityCallback;

        activityRecognitionClient = ActivityRecognition.getClient(context);

        Intent intent = new Intent(context, DetectedActivitiesIntentService.class);
        activityRecognitionPendingIntent = PendingIntent.getService(
                context, PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        motionActivityReceiver = new MotionActivityResultReceiver();
        LocalBroadcastManager.getInstance(context).registerReceiver(motionActivityReceiver,
                new IntentFilter(DetectedActivitiesIntentService.MOTION_ACTIVITY_BROADCAST));
    }

    public void start() {
        activityRecognitionClient.requestActivityUpdates(
                MOTION_ACTIVITY_FETCH_FREQUENCY_MS, activityRecognitionPendingIntent);
    }

    public void stop() {
        activityRecognitionClient.removeActivityUpdates(activityRecognitionPendingIntent);
    }


    private GoogleApiClient.ConnectionCallbacks connectionCallbacks
            = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
        }

        @Override
        public void onConnectionSuspended(int i) {
        }
    };

    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener
            = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        }
    };


    private class MotionActivityResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ActivityRecognitionResult activityRecognitionResult = intent.getParcelableExtra(
                    DetectedActivitiesIntentService.EXTRA_MOTION_ACTIVITY_RESULT);
            mActivityCallback.onMotionActivityResult(activityRecognitionResult);
        }
    }
}
