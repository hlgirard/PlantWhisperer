package data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "moisture_history")
public class MoistureHistory {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    private int mPlantId;

    private int mSoilMoisture;

    private long mDateTime;

    public MoistureHistory(int plantId, int soilMoisture, long dateTime) {
        mPlantId = plantId;
        mSoilMoisture = soilMoisture;
        mDateTime = dateTime;
    }

    // Getters
    public int getId() { return id; }
    public int getPlantId() { return mPlantId;}
    public int getSoilMoisture() { return mSoilMoisture; }
    public long getDateTime() { return mDateTime; }

    // Setters
    public void setId(int newId) { id = newId; }
    public void setPlantId(int plant_id) { mPlantId = plant_id; }
    public void setSoilMoisture(int soil_moisture) { mSoilMoisture = soil_moisture; }
    public void setDateTime(long date) { mDateTime = date; }

}
