package com.hlgirard.android.plantwhisperer.helpers;

import android.app.job.JobParameters;
import android.app.job.JobService;

import data.MoistureHistoryRepository;
import data.PlantRepository;

public class dataUpdateJobService extends JobService {

    PlantHistoryUpdater mUpdaterAsyncTask;

    @Override
    public boolean onStartJob(final JobParameters params) {

        PlantRepository mPlantRepo = new PlantRepository(getApplication());
        MoistureHistoryRepository mHistoryRepo = new MoistureHistoryRepository(getApplication());

        mUpdaterAsyncTask = new PlantHistoryUpdater(mPlantRepo, mHistoryRepo) {

            // Make sure the onPostExecute calls jobFinished
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                jobFinished(params, false);
            }
        };


        mUpdaterAsyncTask.execute(getApplicationContext());

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        // Cancel the AsyncTask if it is still running
        if (mUpdaterAsyncTask != null) {
            mUpdaterAsyncTask.cancel(true);
        }

        //Request a rescheduling of the job if it was cancelled prematurely
        return true;
    }

}
