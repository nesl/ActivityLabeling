package ucla.nesl.ActivityLabeling.service.sensordataprocessing;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;

/**
 * Created by timestring on 2/15/18.
 *
 * To get motion activity via Google API.
 */

public class MotionActivityDataCollector {

    private static final long MOTION_ACTIVITY_FETCH_FREQUENCY_SEC = 10 * 1000L;  // 10 seconds

    private Context mContext;
    private MotionActivityCallback mActivityCallback;

    private GoogleApiClient googleApiClient;


    public MotionActivityDataCollector(Context context, MotionActivityCallback activityCallback) {
        mContext = context;
        mActivityCallback = activityCallback;

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .build();

        googleApiClient.connect();
    }

    public void start() {
        Intent intent = new Intent(mContext, ActivityRecognizationIntentService.class);
        //TODO: what's the constant?
        PendingIntent pendingIntent = PendingIntent.getService(mContext,1, intent,PendingIntent.FLAG_UPDATE_CURRENT);

        //TODO why it's obselete?
        //TODO what's the constant?
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleApiClient,1, pendingIntent);
    }

    public void stop() {
        //TODO
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


    private class ActivityRecognizationIntentService extends IntentService {

        public ActivityRecognizationIntentService() {
            super("ActivityRecognizationIntentService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                mActivityCallback.onMotionActivityResult(
                        ActivityRecognitionResult.extractResult(intent));
            }
        }
    }
}
