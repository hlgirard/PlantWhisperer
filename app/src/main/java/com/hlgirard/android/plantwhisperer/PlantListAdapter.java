package com.hlgirard.android.plantwhisperer;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import data.Plant;
import data.PlantViewModel;

public class PlantListAdapter extends RecyclerView.Adapter<PlantListAdapter.PlantViewHolder> {

    class PlantViewHolder extends RecyclerView.ViewHolder {

        private final TextView name_tv;
        private final TextView soilMoist_tv;
        private final TextView last_update_tv;
        private final TextView location_tv;
        private final TextView mqtt_topic_tv;

        private final Button edit_button;

        private final LinearLayout expanded_ll;

        private PlantViewModel mPlantViewModel;

        private PlantViewHolder(View itemView) {
            super(itemView);
            name_tv = itemView.findViewById(R.id.plant_name_tv);
            soilMoist_tv = itemView.findViewById(R.id.moisture_level);
            last_update_tv = itemView.findViewById(R.id.last_update);
            location_tv = itemView.findViewById(R.id.location_tv);
            mqtt_topic_tv = itemView.findViewById(R.id.mqtt_topic_tv);

            edit_button = itemView.findViewById(R.id.edit_button);

            expanded_ll = itemView.findViewById(R.id.extended_card_linlayout);
        }
    }

    private final LayoutInflater mInflater;
    private List<Plant> mPlants; // Cached copy of words

    PlantListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    int mExpandedPosition = -1;
    int previousExpandedPosition = -1;

    @Override
    public PlantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.plant_list_item_view_card, parent, false);
        final PlantViewHolder holder = new PlantViewHolder(itemView);

        return holder;
    }

    @Override
    public void onBindViewHolder(PlantViewHolder holder, final int position) {
        if (mPlants != null) {
            final Plant currentPlant = mPlants.get(position);

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

            // Set the Location information
            holder.location_tv.setText(currentPlant.getLocation());

            // Set the MQTT Topic information
            holder.mqtt_topic_tv.setText(currentPlant.getTopic());

            // Handle the expansion / retraction of the cardview on tap
            final boolean isExpanded = position == mExpandedPosition;
            holder.expanded_ll.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            holder.itemView.setActivated(isExpanded);

            if (isExpanded)
                previousExpandedPosition = position;

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mExpandedPosition = isExpanded ? -1 : position;
                    notifyItemChanged(previousExpandedPosition);
                    notifyItemChanged(position);
                }
            });

            // Attach onClickListener to delete button
        /*holder.delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePlant(currentPlant);
            }
        });*/

            // Attach onClickListener to the edit button
            holder.edit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), PlantEditorActivity.class);
                    intent.putExtra("id", currentPlant.getId());
                    v.getContext().startActivity(intent);
                }
            });


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