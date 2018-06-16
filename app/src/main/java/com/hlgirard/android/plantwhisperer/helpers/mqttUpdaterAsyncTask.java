package com.hlgirard.android.plantwhisperer.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import data.Plant;
import data.PlantRepository;

public class mqttUpdaterAsyncTask extends AsyncTask<Context, Void, Void> {

    PlantRepository mPlantRepo;

    public mqttUpdaterAsyncTask(PlantRepository plantRepo) {
        mPlantRepo = plantRepo;
    }

    @Override
    protected Void doInBackground(Context... context) {

        Log.v("AsyncTaskdoInBackground","Initializing the AsyncTask doInBackground method");

        updateAllPlants(context[0]);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.v("mqttUpdateAsync", "Async task terminated");
    }

    private void updateAllPlants(Context context) {

        List<Plant> plantList = mPlantRepo.getPlantList();
        Hashtable<String, Integer> topicList = new Hashtable<String, Integer>();
        Plant currentPlant;

        if (plantList.size() != 0) {

            for (int i = 0; i < plantList.size(); i++) {
                currentPlant = plantList.get(i);
                topicList.put(currentPlant.getTopic(),currentPlant.getId());
            }

            startMqtt(topicList, context);

        } else {
            return;
        }
    }

    private void startMqtt(final Hashtable<String, Integer> topicList, Context context) {

        final int[] messagesReceived = new int[1];
        messagesReceived[0] = 0;
        long timeStarted = System.currentTimeMillis();

        final MqttHelper mqttHelper = new MqttHelper(context, topicList);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("startMqtt", "Received MQTT message: " + mqttMessage.toString() + " for plant #" + topicList.get(topic));
                updatePlantData(mqttMessage.toString(), topicList.get(topic));
                messagesReceived[0] += 1;
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        mqttHelper.connect();

        while (messagesReceived[0] < 3*topicList.size() & System.currentTimeMillis() - timeStarted < 5000) {}

        mqttHelper.close();

        Log.v("startMqtt", "Disconnected from the server, shutting down the startMqtt function");

    }

    private void updatePlantData(String mqttMessage, int plantIndex) {

        Log.v("updatePlantData", "Got MQTT data, starting database update for plantIndex " + plantIndex);

        Plant currentPlant = mPlantRepo.getPlantById(plantIndex);

        Log.v("updatePlantData", "Updating the database for plant " +
                currentPlant.getName() +
                " #" + currentPlant.getId());

        if (mqttMessage != null && !mqttMessage.isEmpty()) {

            // MQTT message is of form "58.00", must be converted to double and then integer before storing in database
            double mqttMessage_dbl = 0;
            {
                try {
                    mqttMessage_dbl = Double.parseDouble(mqttMessage);
                } catch (Exception e) {
                    Log.e("String2Int", "Bad conversion to integer", e);
                }
            }
            int newMoistureInt = (int) Math.floor(mqttMessage_dbl);

            currentPlant.setHumidityLevel(newMoistureInt);
            currentPlant.setDateUpdated(Calendar.getInstance().getTime().getTime());

            mPlantRepo.update(currentPlant);

            Log.v("updatePlantData", "Updated the data for plant " +
                    currentPlant.getName() +
                    " #" + currentPlant.getId());

        }
    }
}
