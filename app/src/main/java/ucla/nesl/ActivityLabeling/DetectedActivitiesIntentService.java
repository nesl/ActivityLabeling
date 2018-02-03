package ucla.nesl.ActivityLabeling;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class DetectedActivitiesIntentService extends IntentService {

    static final String ACTION_BROADCAST = ".broadcast";

    static final String EXTRA_DETECTED_ACTIVITY = ".detectedActivity";

    private static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();
    public DetectedActivitiesIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        DetectedActivity detectedActivity = result.getMostProbableActivity();


        // Log each activity.
        Log.i(TAG, "activities detected");
        Log.i(TAG, Utils.getActivityString(detectedActivity.getType())
                + " " + detectedActivity.getConfidence() + "%");

        // Notify anyone listening for broadcasts about the new location.
        Intent intentToSend = new Intent(ACTION_BROADCAST);
        intentToSend.putExtra(EXTRA_DETECTED_ACTIVITY, detectedActivity);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentToSend);
    }
}
