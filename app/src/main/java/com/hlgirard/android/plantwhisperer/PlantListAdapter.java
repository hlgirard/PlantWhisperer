package com.hlgirard.android.plantwhisperer;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import data.Plant;

public class PlantListAdapter extends RecyclerView.Adapter<PlantListAdapter.PlantViewHolder> {

    class PlantViewHolder extends RecyclerView.ViewHolder {

        private final TextView name_tv;
        private final TextView soilMoist_tv;
        private final TextView last_update_tv;


        private PlantViewHolder(View itemView) {
            super(itemView);
            name_tv = itemView.findViewById(R.id.plant_name_tv);
            soilMoist_tv = itemView.findViewById(R.id.moisture_level);
            last_update_tv = itemView.findViewById(R.id.last_update);
        }
    }

    private final LayoutInflater mInflater;
    private List<Plant> mPlants; // Cached copy of words

    PlantListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public PlantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.plant_list_item_view, parent, false);
        return new PlantViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PlantViewHolder holder, int position) {
        if (mPlants != null) {
            Plant currentPlant = mPlants.get(position);

            // Set the name
            holder.name_tv.setText(currentPlant.getName());

            // Set the moisture level
            String moistureText = Integer.toString(currentPlant.getHumidityLevel()) + "%";
            holder.soilMoist_tv.setText(moistureText);

            // Set the moisture circle color
            GradientDrawable moistureCircle = (GradientDrawable) holder.soilMoist_tv.getBackground();
            int moistureColor = getMoistureCircleColor(currentPlant.getHumidityLevel(), holder.soilMoist_tv.getContext());
            moistureCircle.setColor(moistureColor);

            // Set last updated time
            DateFormat timeFormat = new SimpleDateFormat("MMM dd, YYYY HH:mm a");
            Date date_updated = new Date(currentPlant.getDateUpdated());
            String lastUpdate_text = "Last updated: " + timeFormat.format(date_updated);
            holder.last_update_tv.setText(lastUpdate_text);

        }
    }

    void setPlants(List<Plant> plants){
        mPlants = plants;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mPlants != null)
            return mPlants.size();
        else return 0;
    }

    private int getMoistureCircleColor(int moistureLevel, Context context) {
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

        return ContextCompat.getColor(context, moistureColorResourceID);
    }
}