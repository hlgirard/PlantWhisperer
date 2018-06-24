package com.hlgirard.android.plantwhisperer.helpers;

import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.hlgirard.android.plantwhisperer.MainActivity;
import com.hlgirard.android.plantwhisperer.R;

import java.util.List;

import data.MoistureHistoryRepository;
import data.Plant;
import data.PlantRepository;

public class dataUpdateJobService extends JobService {

    PlantHistoryUpdater mUpdaterAsyncTask;
    PlantRepository mPlantRepo;
    MoistureHistoryRepository mHistoryRepo;

    private static String NOTIFICATION_CHANNEL_ID = "low_moisture_alert";

    @Override
    public boolean onStartJob(final JobParameters params) {

        mPlantRepo = new PlantRepository(getApplication());
        mHistoryRepo = new MoistureHistoryRepository(getApplication());

        mUpdaterAsyncTask = new PlantHistoryUpdater(mPlantRepo, mHistoryRepo) {

            // Make sure the onPostExecute calls jobFinished
            @Override
            protected void onPostExecute(Void aVoid) {
                checkNotification();
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

    private void checkNotification() {
        List<Plant> plantList = mPlantRepo.getPlantList();

        for (int i = 0; i < plantList.size(); i++) {
            if (plantList.get(i).getHumidityLevel() < 15) {
                createNotification(plantList.get(i).getId());
            }
        }
    }

    private void createNotification(int plantId) {

        Plant plant = mPlantRepo.getPlantById(plantId);

        String textTitle = plant.getName() + " is getting dry!";
        String textContent = "Soil moisture level is down to " +
                String.valueOf(plant.getHumidityLevel()) +
                " %";

        // Create an intent to go to the main activity of the app
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Create the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_small_icon)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, mBuilder.build());


    }

}
