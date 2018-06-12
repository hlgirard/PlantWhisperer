package com.hlgirard.android.plantwhisperer;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class PlantAdapter extends ArrayAdapter<Plant> {

    private DateFormat timeFormat = new SimpleDateFormat("MMM dd, YYYY HH:mm a");

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.plant_list_item_view, parent, false);
        }

        Plant currentPlant = getItem(position);

        // Set the plant name
        TextView name_tv = (TextView) listItemView.findViewById(R.id.plant_name_tv);
        name_tv.setText(currentPlant.getName());

        // Set the plant Soil moisture level
        TextView soilMoist_tv = (TextView) listItemView.findViewById(R.id.moisture_level);
        String moistureText = Integer.toString(currentPlant.getSoilMoisture()) + "%";
        soilMoist_tv.setText(moistureText);

        //Set the proper background color for the moisture circle
        GradientDrawable moistureCircle = (GradientDrawable) soilMoist_tv.getBackground();
        int moistureColor = getMoistureCircleColor(currentPlant.getSoilMoisture());
        moistureCircle.setColor(moistureColor);

        // Set the last update time
        TextView last_update = (TextView) listItemView.findViewById(R.id.last_update);
        String lastUpdate_text = "Last updated: " + timeFormat.format(currentPlant.getLastUpdate());
        last_update.setText(lastUpdate_text);

        return listItemView;
    }

    PlantAdapter(Activity context, List<Plant> plantList) {

        super(context, 0, plantList);

    }

    private int getMoistureCircleColor(int moistureLevel) {
        int moistureColorResourceID;

        if (moistureLevel > 90) {
            moistureColorResourceID = R.color.above90;
        } else if (moistureLevel > 70) {
            moistureColorResourceID = R.color.between70and90;
        } else if (moistureLevel > 50) {
            moistureColorResourceID = R.color.between50and70;
        } else if (moistureLevel > 30) {
            moistureColorResourceID = R.color.between30and50;
        } else {
            moistureColorResourceID = R.color.below30;
        }

        return ContextCompat.getColor(getContext(), moistureColorResourceID);
    }
}
