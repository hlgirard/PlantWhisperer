package com.hlgirard.android.plantwhisperer;

import java.util.Date;

public class Plant {
    private String mName;
    private int mSoilMoisture;
    private Date mLastUpdate;

    public Plant(String name, int moisture, Date update) {
        mName = name;
        mSoilMoisture = moisture;
        mLastUpdate = update;
    }

    public String getName() {
        return mName;
    }

    public int getSoilMoisture() {
        return mSoilMoisture;
    }

    public Date getLastUpdate() {
        return mLastUpdate;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setSoilMoisture(int soilMoisture) {
        mSoilMoisture = soilMoisture;
    }

    public void setLastUpdate(Date lastUpdate) {
        mLastUpdate = lastUpdate;
    }
}
