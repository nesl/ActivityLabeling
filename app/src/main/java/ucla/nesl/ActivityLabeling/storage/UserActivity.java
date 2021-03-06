package ucla.nesl.ActivityLabeling.storage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import ucla.nesl.ActivityLabeling.utils.Utils;

/**
 * Created by zxxia on 12/3/17.
 * Data structure used to pass activity information between activities.
 */

@Entity(tableName = "user_activity")
public class UserActivity implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int aID;

    @ColumnInfo(name = "start_time_ms")
    public long startTimeMs = Utils.INVALID_TIME;

    @ColumnInfo(name = "end_time_ms")
    public long endTimeMs = Utils.INVALID_TIME;

    @ColumnInfo(name = "start_lat")
    public double startLatitude = Utils.INVALID_LOCATION_VAL;

    @ColumnInfo(name = "start_lon")
    public double startLongitude = Utils.INVALID_LOCATION_VAL;

    @ColumnInfo(name = "end_lat")
    public double endLatitude = Utils.INVALID_LOCATION_VAL;

    @ColumnInfo(name = "end_lon")
    public double endLongitude = Utils.INVALID_LOCATION_VAL;

    @ColumnInfo(name = "uloc_label")
    public @NonNull String microLocationLabel = "";

    @ColumnInfo(name = "act_type")
    public @NonNull String type = "";  //TODO: what type?

    @ColumnInfo(name = "description")
    public @NonNull String description = "";


    public UserActivity() {
    }

    public boolean isStopped() {
        return endTimeMs != Utils.INVALID_TIME;
    }

    public void setStartLocation(Location loc) {
        if (loc == null) {
            startLatitude = Utils.INVALID_LOCATION_VAL;
            startLongitude = Utils.INVALID_LOCATION_VAL;
        } else {
            startLatitude = loc.getLatitude();
            startLongitude = loc.getLongitude();
        }
    }

    public void setEndLocation(Location loc) {
        if (loc == null) {
            endLatitude = Utils.INVALID_LOCATION_VAL;
            endLongitude = Utils.INVALID_LOCATION_VAL;
        } else {
            endLatitude = loc.getLatitude();
            endLongitude = loc.getLongitude();
        }
    }

    public String toCSVLine() {
        //TODO: The 3rd and 4th columns are redundant
        return  Long.toString(startTimeMs) + ',' + Long.toString(endTimeMs) + ',' +
                Utils.timeToString(startTimeMs) + ','+ Utils.timeToString(endTimeMs) + ',' +
                Double.toString(startLatitude) + ',' + Double.toString(startLongitude) + ',' +
                Double.toString(endLatitude) + ',' + Double.toString(endLongitude) + ',' +
                microLocationLabel + ',' + type + ',' +
                description + ','+'\n';
    }

    public static UserActivity parseCSVLine(String csvLine) {
        csvLine = csvLine.replace("\n", "").replace("\r", "");
        String[] row = csvLine.split(",", -1);
        //Log.i(TAG, csvLine);

        UserActivity actInfo = new UserActivity();

        actInfo.startTimeMs = Long.valueOf(row[0]);
        actInfo.endTimeMs = Long.valueOf(row[1]);
        actInfo.startLatitude = Double.valueOf(row[4]);
        actInfo.startLongitude = Double.valueOf(row[5]);
        actInfo.endLatitude = Double.valueOf(row[6]);
        actInfo.endLongitude = Double.valueOf(row[7]);
        actInfo.microLocationLabel = row[8];
        actInfo.type = row[9];
        actInfo.description = row[10];

        return actInfo;
    }

    public int describeContents() {
        return 0;
    }

    // ==== Parcel methods =========================================================================
    private UserActivity(Parcel in) {
        startTimeMs = in.readLong();
        endTimeMs = in.readLong();
        startLatitude = in.readDouble();
        startLongitude = in.readDouble();
        endLatitude = in.readDouble();
        endLongitude = in.readDouble();
        microLocationLabel = in.readString();
        type = in.readString();
        description = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(startTimeMs);
        out.writeLong(endTimeMs);
        out.writeDouble(startLatitude);
        out.writeDouble(startLongitude);
        out.writeDouble(endLatitude);
        out.writeDouble(endLongitude);
        out.writeString(microLocationLabel);
        out.writeString(type);
        out.writeString(description);
    }

    public static final Parcelable.Creator<UserActivity> CREATOR = new Parcelable.Creator<UserActivity>() {
        public UserActivity createFromParcel(Parcel in) {
            return new UserActivity(in);
        }

        public UserActivity[] newArray(int size) {
            return new UserActivity[size];
        }
    };

    // ==== Getters and setters for Room Database Library ==========================================

    /**
     * Please do not use getAID() directly in the code. It is declared for Room Database Library.
     */
    public int getAID() {
        return aID;
    }

    /**
     * Please do not use setAID() directly in the code. It is declared for Room Database Library.
     */
    public void setAID(int ID) {
        this.aID = ID;
    }
}