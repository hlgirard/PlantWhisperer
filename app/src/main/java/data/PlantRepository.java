package data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;

public class PlantRepository {

    private PlantDao mPlantDao;
    private List<Plant> mAllPlants;

    // Constructor for the repository
    // Gets a handle to the database and initializes the membre variables
    PlantRepository(Application application) {
        PlantRoomDatabase db = PlantRoomDatabase.getDatabase(application);
        mPlantDao = db.plantDao();
        mAllPlants = mPlantDao.getAllPlants();
    }

    List<Plant> getAllPlants() {
        return mAllPlants;
    }

    public void insert (Plant plant) {
        new insertAsyncTask(mPlantDao).execute(plant);
    }

    private static class insertAsyncTask extends AsyncTask<Plant, Void, Void> {

        private PlantDao mAsyncTaskDao;

        insertAsyncTask(PlantDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Plant... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public void update (Plant plant) { new updateAsyncTask(mPlantDao).execute(plant); }

    private static class updateAsyncTask extends AsyncTask<Plant, Void, Void> {

        private PlantDao mAsyncTaskDao;

        updateAsyncTask(PlantDao dao) { mAsyncTaskDao = dao; }

        @Override
        protected Void doInBackground(final Plant... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }
}