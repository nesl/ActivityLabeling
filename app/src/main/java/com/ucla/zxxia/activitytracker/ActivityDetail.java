package com.ucla.zxxia.activitytracker;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zxxia on 12/3/17.
 */

public class ActivityDetail implements Parcelable {
    long m_start;
    long m_end;
    String m_loc;
    String m_uloc;
    String m_type;
    String m_dscrp;

    ActivityDetail(long start, long end, String loc, String uloc, String type, String dscrp) {
        this.m_start = start;
        this.m_end = end;
        this.m_loc = loc;
        this.m_uloc = uloc;
        this.m_type = type;
        this.m_dscrp = dscrp;
    }

    public int describeContents() {
        return 0;
    }

    private ActivityDetail(Parcel in) {
        m_start = in.readLong();
        m_end = in.readLong();
        m_loc = in.readString();
        m_uloc = in.readString();
        m_type = in.readString();
        m_dscrp = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(m_start);
        out.writeLong(m_end);
        out.writeString(m_loc);
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
};