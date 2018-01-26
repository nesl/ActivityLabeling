package ucla.nesl.ActivityLabeling;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zxxia on 12/3/17.
 * Data structure used to pass activity information between activities.
 */

public class ActivityDetail implements Parcelable {

    private long m_start = -1;
    private long m_end = -1;
    private double m_start_latitude = -1;
    private double m_start_longitude = -1;
    private double m_end_latitude = -1;
    private double m_end_longitude = -1;
    private String m_uloc = "";
    private String m_type = "";
    private String m_dscrp = "";

    ActivityDetail() {
    }
    ActivityDetail(long start, long end,
                   double start_latitude, double start_longitude,
                   double end_latitude, double end_longitude,
                   String uloc, String type, String dscrp) {
        m_start = start;
        m_end = end;
        m_start_latitude = start_latitude;
        m_start_longitude = start_longitude;
        m_end_latitude = end_latitude;
        m_end_longitude = end_longitude;
        m_uloc = uloc;
        m_type = type;
        m_dscrp = dscrp;

    }

    boolean isStopped() {
        return m_end != -1;
    }

    long getStartTime() {
        return m_start;
    }

    long getEndTime() {
        return m_end;
    }

    double getStartLatitude() {
        return m_start_latitude;
    }

    double getStartLongitude() {
        return m_start_longitude;
    }

    double getEndLatitude() {
        return m_end_latitude;
    }

    double getEndLongitude() {
        return m_end_longitude;
    }

    String getMicrolocation() {
        return m_uloc;
    }

    String getActType() {
        return m_type;
    }

    String getDescription() {
        return m_dscrp;
    }

    void setStartTime(long t) {
        m_start = t;
    }

    void setEndTime(long t) {
        m_end = t;
    }

    void setStartLocation(Location loc) {
        if (loc != null) {
            m_start_latitude = loc.getLatitude();
            m_start_longitude = loc.getLongitude();
        }
    }

    void setEndLocation(Location loc) {
        if (loc != null) {
            m_end_latitude = loc.getLatitude();
            m_end_longitude = loc.getLongitude();
        }
    }

    void setMicrolocation(String uloc) {
        if (uloc != null) {
            m_uloc= uloc;
        }
    }

    void setActType(String type) {
        if (type != null) {
            m_type = type;
        }
    }

    void setDescription(String description){
        if (description != null) {
            m_dscrp = description;
        }
    }

    String toCSVLine() {
        return  Long.toString(m_start) + ',' + Long.toString(m_end) + ',' +
                Utils.timeToString(m_start) + ','+ Utils.timeToString(m_end) + ',' +
                Double.toString(m_start_latitude) + ',' + Double.toString(m_start_longitude) + ',' +
                Double.toString(m_end_latitude) + ',' + Double.toString(m_end_longitude) + ',' +
                m_uloc + ',' + m_type + ',' +
                m_dscrp + ','+'\n';
    }

    public int describeContents() {
        return 0;
    }

    private ActivityDetail(Parcel in) {

        m_start = in.readLong();
        m_end = in.readLong();
        m_start_latitude = in.readDouble();
        m_start_longitude = in.readDouble();
        m_end_latitude = in.readDouble();
        m_end_longitude = in.readDouble();
        m_uloc = in.readString();
        m_type = in.readString();
        m_dscrp = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {

        out.writeLong(m_start);
        out.writeLong(m_end);
        out.writeDouble(m_start_latitude);
        out.writeDouble(m_start_longitude);
        out.writeDouble(m_end_latitude);
        out.writeDouble(m_end_longitude);
        out.writeString(m_uloc);
        out.writeString(m_type);
        out.writeString(m_dscrp);
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