package data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class PlantViewModel extends AndroidViewModel {

    private PlantRepository mRepository;
    private List<Plant> mAllPlants;

    public PlantViewModel(@NonNull Application application) {
        super(application);
        mRepository = new PlantRepository(application);
        mAllPlants = mRepository.getAllPlants();
    }

    // Wrapper for the get method
    public List<Plant> getAllPlants() { return mAllPlants; }

    // Wrapper for the insert method
    public void insert(Plant plant) { mRepository.insert(plant); }

    public void update(Plant plant) { mRepository.update(plant); }
}
