package ucla.nesl.ActivityLabeling.activity.debug;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import ucla.nesl.ActivityLabeling.R;
import ucla.nesl.ActivityLabeling.service.sensordataprocessing.SensorDataProcessingService;
import ucla.nesl.ActivityLabeling.utils.Utils;

public class DebugActivity extends AppCompatActivity {

    private static final String TAG = DebugActivity.class.getSimpleName();

    private static final int UI_HANDLER_MSG_ID = 0;
    private static final long INFO_UPDATE_INTERVAL_MS = 10 * 1000L;

    private SensorDataProcessingService mService = null;

    private Handler uiHandler = null;

    private TextView infoText;

    //region Section: Activity life cycle
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = new Intent(this, SensorDataProcessingService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        infoText = findViewById(R.id.debugText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopInformationUpdatingLoop();
    }
    //endregion

    //region Section: Service connection
    // =============================================================================================
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.i(TAG, "onServiceConnected");
            SensorDataProcessingService.LocalBinder binder = (SensorDataProcessingService.LocalBinder) iBinder;
            mService = binder.getService();
            startInformationUpdatingLoop();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };
    //endregion

    //region Section: Debug information display
    // =============================================================================================
    private void startInformationUpdatingLoop() {
        if (uiHandler != null) {
            return;
        }

        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                showInformation();
                uiHandler.sendEmptyMessageDelayed(UI_HANDLER_MSG_ID, INFO_UPDATE_INTERVAL_MS);
            }
        };
        uiHandler.sendEmptyMessage(UI_HANDLER_MSG_ID);
    }

    private void stopInformationUpdatingLoop() {
        if (uiHandler == null) {
            return;
        }

        uiHandler.removeMessages(UI_HANDLER_MSG_ID);
        uiHandler = null;
    }

    private void showInformation() {
        infoText.setText(Utils.stringJoin("\n",
                "**** About service ****",
                "Created time: " + Utils.timeToString(mService.getCreatedTimestampMs()),
                "Location: " + Utils.locToString(mService.getCurrentLocation()),
                "Motion activity: " + mService.getCurrentMotionActivity()
        ));
    }
    //endregion

}
