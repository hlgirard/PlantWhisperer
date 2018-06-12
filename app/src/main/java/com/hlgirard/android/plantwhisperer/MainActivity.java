package com.hlgirard.android.plantwhisperer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
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

import helpers.MqttHelper;

public class MainActivity extends AppCompatActivity {

    private DateFormat timeFormat = new SimpleDateFormat("MM:dd:YYYY HH:mm");
    private static int PLANT_LOADER_ID = 1;
    private ProgressBar loading_spinner;
    private TextView empty_tv;
    private PlantAdapter mAdapter;
    private MqttHelper mqttHelper;
    private List<Plant> fakePlantList = new ArrayList<Plant>();;

    Date fakeDate;

    {
        try {
            fakeDate = timeFormat.parse("5:18:2018 14:23");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the plant list view
        ListView plantListView = (ListView) findViewById(R.id.plant_list);


        // Set empty view to the list view
        empty_tv = (TextView) findViewById(R.id.empty_list_view);
        plantListView.setEmptyView(empty_tv);

        // Obtain the loading spinner object
        loading_spinner = (ProgressBar) findViewById(R.id.loading_spinner);

        // Create an instance of the list adapter PlantAdapter
        mAdapter = new PlantAdapter(MainActivity.this, fakePlantList);
        // Set it to the listView
        plantListView.setAdapter(mAdapter);

        fakePlantList.add(new Plant("Yucca", 25,fakeDate));
        fakePlantList.add(new Plant("Banana tree", 90,fakeDate));

        mAdapter.notifyDataSetChanged();



        //Check connectivity
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            // Start the Mqtt client
            startMqtt();
        } else {
            loading_spinner.setVisibility(View.GONE);
            empty_tv.setText("No internet connection");
        }


    }

    private void startMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug", mqttMessage.toString());
                updateUI(mqttMessage.toString(),0);
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

            // Get the appropriate plant from the list
            Plant currentPlant = fakePlantList.get(plantPosition);

            // Update the Soil Moisture level and the Last update value
            currentPlant.setSoilMoisture(mqttMessage_int);
            currentPlant.setLastUpdate(Calendar.getInstance().getTime());

            mAdapter.notifyDataSetChanged();
        }
    }

}
