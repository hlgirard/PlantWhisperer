package data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class MoistureHistoryRepository {

    private MoistureHistoryDao mHistoryDao;
    private LiveData<List<MoistureHistory>> mAllMoistureHistory;

    public MoistureHistoryRepository(Application application) {
        MoistureHistoryRoomDatabase mDatabase = MoistureHistoryRoomDatabase.getDatabase(application);
        mHistoryDao = mDatabase.historyDao();
        mAllMoistureHistory = mHistoryDao.getAllHistory();
    }

    public LiveData<List<MoistureHistory>> getAllMoistureHistory() { return mAllMoistureHistory; }

    public List<MoistureHistory> getAllHistoryList() { return mHistoryDao.getAllHistoryList(); }

    public List<MoistureHistory> getHistoryByPlantId(int plantId) { return mHistoryDao.getHistoryByPlantId(plantId); }

    public List<MoistureHistory> getHistoryByIdLaterThan(int plantId, long time) { return mHistoryDao.getHistoryByIdLaterThan(plantId, time); }

    public void deleteAllOlderThan(long time) { mHistoryDao.deleteAllOlderThan(time); }

    public void insert (MoistureHistory data) {
        new insertAsyncTask(mHistoryDao).execute(data);
    }

    private static class insertAsyncTask extends AsyncTask<MoistureHistory, Void, Void> {

        private MoistureHistoryDao mAsyncTaskDao;

        insertAsyncTask(MoistureHistoryDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final MoistureHistory... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public void update (MoistureHistory plant) { new updateAsyncTask(mHistoryDao).execute(plant); }

    private static class updateAsyncTask extends AsyncTask<MoistureHistory, Void, Void> {

        private MoistureHistoryDao mAsyncTaskDao;

        updateAsyncTask(MoistureHistoryDao dao) { mAsyncTaskDao = dao; }

        @Override
        protected Void doInBackground(final MoistureHistory... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    public void delete (MoistureHistory plant) { new deleteAsyncTask(mHistoryDao).execute(plant); }

    private static class deleteAsyncTask extends AsyncTask<MoistureHistory, Void, Void> {

        private MoistureHistoryDao mAsyncTaskDao;

        deleteAsyncTask(MoistureHistoryDao dao) { mAsyncTaskDao = dao; }

        @Override
        protected Void doInBackground(final MoistureHistory... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }
}
