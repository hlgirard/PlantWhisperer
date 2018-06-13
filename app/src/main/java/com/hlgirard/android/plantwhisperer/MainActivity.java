package com.hlgirard.android.plantwhisperer;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import data.Plant;
import data.PlantViewModel;
import helpers.MqttHelper;

public class MainActivity extends AppCompatActivity {

    private static int PLANT_LOADER_ID = 1;
    private ProgressBar loading_spinner;
    private TextView empty_tv;
    private PlantListAdapter mAdapter;
    private MqttHelper mqttHelper;
    private List<Plant> plantList;
    private PlantViewModel mPlantViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the plant list view
        RecyclerView plantRecyclerView = findViewById(R.id.plant_list);

        // TODO: Set empty view to the list view
        //empty_tv = (TextView) findViewById(R.id.empty_list_view);



        // Obtain the loading spinner object
        loading_spinner = (ProgressBar) findViewById(R.id.loading_spinner);

        // Create an instance of the list adapter PlantAdapter
        mAdapter = new PlantListAdapter(MainActivity.this);
        // Set it to the listView
        plantRecyclerView.setAdapter(mAdapter);
        plantRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PlantEditorActivity.class);
                startActivity(intent);
            }
        });

        // Get a viewModel
        // TODO: Move database access to background tasks (cannot access db on main thread)
        mPlantViewModel = ViewModelProviders.of(this).get(PlantViewModel.class);
        List<Plant> plantList = mPlantViewModel.getAllPlants();
        mAdapter.setPlants(plantList);
        mAdapter.notifyDataSetChanged();


        //Check connectivity
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            // Update the plant data
            updateAllPlants();
        } else {
            loading_spinner.setVisibility(View.GONE);
            empty_tv.setText("No internet connection");
        }


    }

    private void updateAllPlants() {
        plantList = mPlantViewModel.getAllPlants();

        String mqttTopic;

        for (int i = 0; i < plantList.size(); i++) {
            Plant currentPlant = plantList.get(i);
            mqttTopic = currentPlant.getTopic();
            startMqtt(mqttTopic, i);
        }
    }

    private void updatePlantData(String mqttMessage, int plantIndex) {
        Plant currentPlant = plantList.get(plantIndex);

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

            mPlantViewModel.update(currentPlant);

            Log.v("updatePlantData", "Updated the data for " + currentPlant.getName());

            mAdapter.notifyDataSetChanged();
        }
    }

    private void startMqtt(String topic, final int plantIndex) {
        mqttHelper = new MqttHelper(getApplicationContext(), topic);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug", "Received MQTT message: " + mqttMessage.toString());
                loading_spinner.setVisibility(View.GONE);
                updatePlantData(mqttMessage.toString(), plantIndex);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    private void updateUI(String mqttMessage, int plantPosition) {

        loading_spinner.setVisibility(View.GONE);

        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (mqttMessage != null && !mqttMessage.isEmpty()) {

            // Convert the string mqtt_message (e.g. "58.00") to double
            double mqttMessage_dbl = 0;
            {
                try {
                    mqttMessage_dbl = Double.parseDouble(mqttMessage);
                }
                catch (Exception e) {
                    Log.e("String2Int","Bad conversion to integer",e);
                }
            }

            // Floor the double and cast to integer to be used as the moisture level value
            int mqttMessage_int = (int) Math.floor(mqttMessage_dbl);

            // TESTING: Add a new plant with the appropriate moisture level
            // Plant newPlant = new Plant("Test Yucca", Calendar.getInstance().getTime().getTime(), mqttMessage_int, "archblob/moisture", "Living Room", 0);

            mAdapter.notifyDataSetChanged();
        }
    }

}
