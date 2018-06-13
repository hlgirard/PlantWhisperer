package data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class PlantViewModel extends AndroidViewModel {

    private PlantRepository mRepository;
    private LiveData<List<Plant>> mAllPlants;
    private List<Plant> mPlantList;

    public PlantViewModel(@NonNull Application application) {
        super(application);
        mRepository = new PlantRepository(application);
        mAllPlants = mRepository.getAllPlants();
        mPlantList = mRepository.getPlantList();
    }

    // Wrapper for the get methods
    public LiveData<List<Plant>> getAllPlants() { return mAllPlants; }

    public List<Plant> getPlantList() { return mPlantList; }

    public Plant getPlantById(int id) { return mRepository.getPlantById(id); }

    // Wrapper for the repository methods method
    public void insert(Plant plant) { mRepository.insert(plant); }

    public void update(Plant plant) { mRepository.update(plant); }

    public void delete(Plant plant) { mRepository.delete(plant); }
}
