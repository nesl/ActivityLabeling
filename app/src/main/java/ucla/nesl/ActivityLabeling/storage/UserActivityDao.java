package ucla.nesl.ActivityLabeling.storage;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

/**
 * Created by timestring on 2/11/18.
 */

@Dao
public interface UserActivityDao {
    @Insert
    void insert(UserActivity activity);

    //@Query("SELECT * FROM user")
    //List<User> getAll();

    //@Query("SELECT * FROM user where first_name LIKE  :firstName AND last_name LIKE :lastName")
    //User findByName(String firstName, String lastName);
}
