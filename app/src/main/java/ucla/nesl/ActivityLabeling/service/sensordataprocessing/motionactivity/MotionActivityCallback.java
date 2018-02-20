package ucla.nesl.ActivityLabeling.service.sensordataprocessing.motionactivity;

import com.google.android.gms.location.ActivityRecognitionResult;

/**
 * Created by timestring on 2/15/18.
 *
 * A callback class to return motion activity.
 */

public abstract class MotionActivityCallback {
    public abstract void onMotionActivityResult(ActivityRecognitionResult result);
}
