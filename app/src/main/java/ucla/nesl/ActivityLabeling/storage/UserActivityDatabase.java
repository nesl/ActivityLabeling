package ucla.nesl.ActivityLabeling.storage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by timestring on 2/11/18.
 */

@Database(entities = {UserActivity.class}, version = 1)
public abstract class UserActivityDatabase extends RoomDatabase {

    private static UserActivityDatabase INSTANCE;

    public abstract UserActivityDao getDao();


    public static UserActivityDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), UserActivityDatabase.class, "user-activity-database")
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
