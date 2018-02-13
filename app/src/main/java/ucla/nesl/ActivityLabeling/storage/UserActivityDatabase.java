package ucla.nesl.ActivityLabeling.storage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by timestring on 2/11/18.
 */

@Database(entities = {UserActivity.class}, version = 1)
public abstract class UserActivityDatabase extends RoomDatabase {

    private static UserActivityDatabase INSTANCE;

    public abstract UserActivityDao getDao();


    public static UserActivityDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            // Since the database scale of this app will be small, we do the computation on the
            // main thread to simplify the development process. The impact of the app fluency should
            // be negligible.
            INSTANCE = Room.databaseBuilder(context, UserActivityDatabase.class, "user-activity-database")
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }


    public List<UserActivity> getOnGoingUserActivitiesLatestFirst() {
        return getDao().getAllWithInvalidEndTimeStartTimeDesc();
    }

    public List<UserActivity> getOnGoingAndPast24HoursUserActivitiesLatestFirst() {
        List<UserActivity> activities = getDao().getAllWithInvalidEndTimeStartTimeDesc();
        long oneDayAgo = Calendar.getInstance().getTime().getTime() - TimeUnit.DAYS.toMillis(1);
        activities.addAll(getDao().getAllWithEndTimeGreaterThanStartTimeDesc(oneDayAgo));
        return activities;
    }

    public void createUserActivity(UserActivity activity) {
        getDao().insert(activity);
    }

    public void updateUserActivity(UserActivity activity) {
        getDao().update(activity);
    }

    public int getNumTotalUserActivities() {
        return getDao().countAll();
    }
}
