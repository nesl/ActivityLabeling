package ucla.nesl.ActivityLabeling.storage;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ucla.nesl.ActivityLabeling.utils.Utils;

/**
 * Created by timestring on 2/11/18.
 */

@Dao
public interface UserActivityDao {
    @Insert
    void insert(UserActivity activity);

    @Query("SELECT * FROM user_activity WHERE end_time_ms = " + Utils.INVALID_TIME + "ORDER BY start_time_ms DESC")
    List<UserActivity> getAllWithInvalidEndTimeStartTimeDesc();

    @Query("SELECT * FROM user_activity WHERE end_time_ms >= :threshold ORDER BY start_time_ms DESC")
    List<UserActivity> getAllWithEndTimeGreaterThanStartTimeDesc(long threshold);

    @Update
    void update(UserActivity activity);
}
