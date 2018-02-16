package ucla.nesl.ActivityLabeling.service.sensordataprocessing;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by timestring on 2/15/18.
 *
 * The AggressiveLocationDataCollector sets the priority to be the highest possible.
 */

public class AggressiveLocationDataCollector {
    private static final String TAG = SensorDataProcessingService.class.getSimpleName();

    private static final long DEFAULT_INTERVAL_MS = 60 * 1000L;
    private static final float DEFAULT_MIN_DISPLACEMENT_METER = 50f;

    private LocationCallback mLocationCallback;

    private FusedLocationProviderClient locationClient;
    private Looper looper;

    private LocationRequest locationRequest;

    private boolean isCollectingData = false;

    public AggressiveLocationDataCollector(Context context, LocationCallback locationCallback) {
        mLocationCallback = locationCallback;

        locationClient = LocationServices.getFusedLocationProviderClient(context);
        looper = Looper.myLooper();
    }

    public void updateParameters(long intervalMs, float minDisplacementMeter) {
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(intervalMs)
                .setSmallestDisplacement(minDisplacementMeter);

        if (isCollectingData) {
            isCollectingData = false;
            start();
        }
    }

    public void start() {
        if (isCollectingData) {
            return;
        }

        try {
            locationClient.requestLocationUpdates(locationRequest, mLocationCallback, looper);
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Cannot request locations: " + unlikely);
        }
    }

    public void stop() {
        if (!isCollectingData) {
            return;
        }

        try {
            locationClient.removeLocationUpdates(mLocationCallback);
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Cannot remove location updates: " + unlikely);
        }
    }
}
