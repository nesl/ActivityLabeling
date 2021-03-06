package ucla.nesl.ActivityLabeling.service.sensordataprocessing.motionactivity;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionIntentService extends IntentService {

    private static final String TAG = ActivityRecognitionIntentService.class.getSimpleName();

    static final String MOTION_ACTIVITY_BROADCAST = ".broadcast";

    static final String EXTRA_MOTION_ACTIVITY_RESULT = ".motionActivityResult";

    public ActivityRecognitionIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        DetectedActivity detectedActivity = result.getMostProbableActivity();
        Log.i(TAG, "activities detected: " + detectedActivity.toString());

        Intent intentToSend = new Intent(MOTION_ACTIVITY_BROADCAST);
        intentToSend.putExtra(EXTRA_MOTION_ACTIVITY_RESULT, result);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentToSend);
    }
}
