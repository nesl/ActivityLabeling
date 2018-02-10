package ucla.nesl.ActivityLabeling;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by zxxia on 12/3/17.
 * Data structure used to pass activity information between activities.
 */

public class ActivityDetail implements Parcelable {

    long startTimeMs = -1;
    long endTimeMs = -1;
    double startLatitude = -1;
    double startLongitude = -1;
    double endLatitude = -1;
    double endLongitude = -1;
    @NonNull String microLocationLabel = "";
    @NonNull String type = "";  //TODO: what type?
    @NonNull String description = "";


    ActivityDetail() {
    }

    boolean isStopped() {
        return endTimeMs != -1;
    }

    void setStartLocation(@NonNull Location loc) {
        startLatitude = loc.getLatitude();
        startLongitude = loc.getLongitude();
    }

    void setEndLocation(@NonNull Location loc) {
        endLatitude = loc.getLatitude();
        endLongitude = loc.getLongitude();
    }
    /*
    ActivityDetail(long _start, long end,
                   double start_latitude, double start_longitude,
                   double end_latitude, double end_longitude,
                   String uloc, String type, String dscrp) {
        startTimeMs = _start;
        endTimeMs = end;
        startLatitude = start_latitude;
        startLongitude = start_longitude;
        endLatitude = end_latitude;
        endLongitude = end_longitude;
        microLocationLabel = uloc;
        this.type = type;
        description = dscrp;
    }



    long getStartTime() {
        return startTimeMs;
    }

    long getEndTime() {
        return endTimeMs;
    }

    double getStartLatitude() {
        return startLatitude;
    }

    double getStartLongitude() {
        return startLongitude;
    }

    double getEndLatitude() {
        return endLatitude;
    }

    double getEndLongitude() {
        return endLongitude;
    }

    String getMicrolocation() {
        return microLocationLabel;
    }

    String getActType() {
        return type;
    }

    String getDescription() {
        return description;
    }

    void setStartTime(long t) {
        startTimeMs = t;
    }

    void setEndTime(long t) {
        endTimeMs = t;
    }

    void setStartLocation(@NonNull Location loc) {
        startLatitude = loc.getLatitude();
        startLongitude = loc.getLongitude();
    }

    void setEndLocation(@NonNull Location loc) {
        endLatitude = loc.getLatitude();
        endLongitude = loc.getLongitude();
    }

    void setMicrolocation(@NonNull String uloc) {
        if (uloc != null) {
            microLocationLabel = uloc;
        }
    }

    void setActType(String type) {
        if (type != null) {
            this.type = type;
        }
    }

    void setDescription(String description){
        if (description != null) {
            this.description = description;
        }
    }
*/
    String toCSVLine() {
        //TODO: The 3rd and 4th columns are redundant
        return  Long.toString(startTimeMs) + ',' + Long.toString(endTimeMs) + ',' +
                Utils.timeToString(startTimeMs) + ','+ Utils.timeToString(endTimeMs) + ',' +
                Double.toString(startLatitude) + ',' + Double.toString(startLongitude) + ',' +
                Double.toString(endLatitude) + ',' + Double.toString(endLongitude) + ',' +
                microLocationLabel + ',' + type + ',' +
                description + ','+'\n';
    }

    static ActivityDetail parseCSVLine(String csvLine) {
        csvLine = csvLine.replace("\n", "").replace("\r", "");
        String[] row = csvLine.split(",", -1);
        //Log.i(TAG, csvLine);

        ActivityDetail actInfo = new ActivityDetail();

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

    private ActivityDetail(Parcel in) {

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

    public static final Parcelable.Creator<ActivityDetail> CREATOR = new Parcelable.Creator<ActivityDetail>() {
        public ActivityDetail createFromParcel(Parcel in) {
            return new ActivityDetail(in);
        }

        public ActivityDetail[] newArray(int size) {
            return new ActivityDetail[size];
        }
    };
}