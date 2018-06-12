package data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PlantDao {
    @Query("SELECT * FROM plant_table ORDER BY id ASC")
    LiveData<List<Plant>> getAllPlants();

    @Query("SELECT * FROM plant_table WHERE id IN (:plantIds)")
    List<Plant> loadAllByIds(int[] plantIds);

    @Query("DELETE FROM plant_table")
    void deleteAll();

    @Insert
    void insert(Plant plant);

    @Update
    void update(Plant plant);

    @Delete
    void delete(Plant plant);
}

