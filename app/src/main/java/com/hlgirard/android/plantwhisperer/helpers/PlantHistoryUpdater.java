package com.hlgirard.android.plantwhisperer.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import data.MoistureHistory;
import data.MoistureHistoryRepository;
import data.Plant;
import data.PlantRepository;

public class PlantHistoryUpdater extends AsyncTask<Context, Void, Void> {

    PlantRepository mPlantRepo;
    MoistureHistoryRepository mHistoryRepo;
    String mBaseUrl = "https://api.thinger.io/v1/users/";
    String mHardcodedUrl = "hlgirard/buckets/plant_data"; // TODO: remove hardcoded url piece and use user data

    public PlantHistoryUpdater(PlantRepository plantRepo, MoistureHistoryRepository historyRepo) {
        mPlantRepo = plantRepo;
        mHistoryRepo = historyRepo;
    }

    @Override
    protected Void doInBackground(Context... contexts) {

        updateAllPlants(contexts[0]);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.v("PlantHistoryUpdater", "Async task terminated");
    }

    private void updateAllPlants(Context context) {

        // Initialize variables
        Plant currentPlant;
        ArrayList<MoistureHistory> moistureHistoryData;
        long latestUpdate;
        String requestParams;
        MoistureHistory latestData;

        // Obtain the plant list from the repository
        List<Plant> plantList = mPlantRepo.getPlantList();

        // Get latest update time

        latestUpdate = 0;

        // Iterate over all the plants and update their history and current data
        for (int i = 0; i < plantList.size(); i++) {

            currentPlant = plantList.get(i);
            latestData = mHistoryRepo.getLatestMoistureById(currentPlant.getId());
            if (latestData != null) {
                latestUpdate = latestData.getDateTime();
            } else {
                latestUpdate = 0;
            }

        }


        // Build the request, up to 200 items starting from the latest update  in the database
        requestParams = "/data?items=200&sort=desc";

        // Build the request URL and fetch the data from the server
        String requestUrl = mBaseUrl + mHardcodedUrl + requestParams;

        try {
            QueryUtils query = new QueryUtils();
            Log.v("PlantHistoryUpdater","Querying fetchHistoryData with url: " + requestUrl);

            moistureHistoryData = query.fetchHistoryData(mPlantRepo, requestUrl);

            Log.v("PlantHistoryUpdater", "Received " + String.valueOf(moistureHistoryData.size()) + " data points");

            if (moistureHistoryData.size() != 0) {

                // Go through the received data and insert into the database (we have only requested new items so no need to check here)
                for (int j = 0; j < moistureHistoryData.size(); j++) {
                    mHistoryRepo.insert(moistureHistoryData.get(j));
                }

                // Update the plants data
                for (int i = 0; i < plantList.size(); i++) {
                    currentPlant = plantList.get(i);

                    // Get the latest data now that the history database has updated
                    latestData = mHistoryRepo.getLatestMoistureById(currentPlant.getId());

                    // Set the current plant's properties
                    currentPlant.setHumidityLevel(latestData.getSoilMoisture());
                    currentPlant.setDateUpdated(latestData.getDateTime());
                    currentPlant.setMqttError(0);

                    // Update the plant
                    mPlantRepo.update(currentPlant);
                }

            }

        } catch (JSONException e) {
            Log.v("PlantUpdater", "Plant data update failed.");
        }


    }
}
