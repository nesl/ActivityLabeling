package ucla.nesl.ActivityLabeling;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.SparseArray;

/**
 * Created by timestring on 2/12/18.
 *
 * Notification helps populate the content of the notifications. Now we assume all the notifications
 * are static.
 */

public class NotificationHelper {

    public enum Type {
        FOREGROUND_SERVICE(12345),
        LOCATION_CHANGED(12346),
        ACTIVITY_CHANGED(12347);

        private final int notificationID;

        Type(int id) {
            notificationID = id;
        }

        public int getID() {
            return notificationID;
        }
    }


    private static final String CHANNEL_ID = "channel_0";

    private NotificationManager notificationManager;
    private Context mContext;

    private SparseArray<Notification> cache = new SparseArray<>();


    public NotificationHelper(Context context) {
        mContext = context;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // create a notification channel as Android O requires
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = mContext.getString(R.string.app_name);
            NotificationChannel mChannel = new NotificationChannel(
                    CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    public Notification getNotification(Type type) {
        // If the notification has created before, then return it
        Notification notification = cache.get(type.getID());
        if (notification != null) {
            return notification;
        }

        // If not, then create it based on the type
        PendingIntent activityPendingIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, MainActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(activityPendingIntent);

        switch (type) {
            case FOREGROUND_SERVICE:
                builder.setContentTitle("Location and Google Activity Service")
                        .setContentText("Monitoring location and user activity")
                        .setTicker("Monitoring location and user activity")
                        .setOngoing(true);

                break;
            case LOCATION_CHANGED:
                builder.setAutoCancel(true)
                        .setContentTitle("Location Changed")
                        .setContentText("Please update your activity information")
                        .setTicker("Please update your activity information")
                        .setPriority(Notification.PRIORITY_DEFAULT);
            case ACTIVITY_CHANGED:
                builder.setAutoCancel(true)
                        .setContentTitle("Motion status Changed")
                        .setContentText("Please update your activity information")
                        .setTicker("Please update your activity information")
                        .setPriority(Notification.PRIORITY_DEFAULT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        notification = builder.build();

        // Save a copy back to cache
        cache.put(type.getID(), notification);

        return notification;
    }

    public void sendNotification(Type type) {
        notificationManager.notify(type.getID(), getNotification(type));
    }

    public void cancelNotification(Type type) {
        notificationManager.cancel(type.getID());
    }

    public void serviceNotifyStartingForeground(Service service, Type type) {
        service.startForeground(type.getID(), getNotification(type));
    }


}
