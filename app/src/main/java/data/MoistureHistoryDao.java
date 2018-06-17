package data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MoistureHistoryDao {

    @Query("SELECT * FROM moisture_history ORDER BY id ASC")
    LiveData<List<MoistureHistory>> getAllHistory();

    @Query("SELECT * FROM moisture_history ORDER BY id ASC")
    List<MoistureHistory> getAllHistoryList();

    @Query("SELECT * FROM moisture_history WHERE mPlantId = (:plantID)")
    List<MoistureHistory> getHistoryByPlantId(int plantID);

    @Query("SELECT * FROM moisture_history WHERE mPlantId = (:plantID) AND mDateTime > (:time)")
    List<MoistureHistory> getHistoryByIdLaterThan(int plantID, long time);

    @Query("DELETE FROM moisture_history WHERE mDateTime < (:timeLimit)")
    void deleteAllOlderThan(long timeLimit);

    @Query("DELETE FROM moisture_history")
    void deleteAll();

    @Insert
    void insert(MoistureHistory data);

    @Update
    void update(MoistureHistory data);

    @Delete
    void delete(MoistureHistory data);
}
