package com.hlgirard.android.plantwhisperer;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import data.MoistureHistory;
import data.MoistureHistoryRepository;
import data.Plant;
import data.PlantViewModel;

public class PlantListAdapter extends RecyclerView.Adapter<PlantListAdapter.PlantViewHolder> {

    class PlantViewHolder extends RecyclerView.ViewHolder {

        private final TextView name_tv;
        private final TextView soilMoist_tv;
        private final TextView last_update_tv;
        private final TextView location_tv;
        private final TextView mqtt_topic_tv;
        private final TextView error_tv;
        private final GraphView history_graphView;

        private final Button edit_button;

        private final LinearLayout expanded_ll;

        private PlantViewHolder(View itemView) {
            super(itemView);
            name_tv = itemView.findViewById(R.id.plant_name_tv);
            soilMoist_tv = itemView.findViewById(R.id.moisture_level);
            last_update_tv = itemView.findViewById(R.id.last_update);
            location_tv = itemView.findViewById(R.id.location_tv);
            mqtt_topic_tv = itemView.findViewById(R.id.mqtt_topic_tv);
            error_tv = itemView.findViewById(R.id.error);

            history_graphView = itemView.findViewById(R.id.history_graph);

            edit_button = itemView.findViewById(R.id.edit_button);

            expanded_ll = itemView.findViewById(R.id.extended_card_linlayout);
        }
    }

    private final LayoutInflater mInflater;
    private List<Plant> mPlants; // Cached copy of words
    private MoistureHistoryRepository mHistoryRepo;
    private Context mContext;

    PlantListAdapter(Context context, MoistureHistoryRepository HistoryRepo) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mHistoryRepo = HistoryRepo;
    }

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

            // Set last updated time
            DateFormat timeFormat = new SimpleDateFormat("MMM dd, YYYY HH:mm a");
            Date date_updated = new Date(currentPlant.getDateUpdated());
            String lastUpdate_text = "Last updated: " + timeFormat.format(date_updated);
            holder.last_update_tv.setText(lastUpdate_text);

            if (currentPlant.getMqttError() == 0) {

                // Set the moisture level
                String moistureText = Integer.toString(currentPlant.getHumidityLevel()) + "%";
                holder.soilMoist_tv.setText(moistureText);

                // Set the moisture circle color
                GradientDrawable moistureCircle = (GradientDrawable) holder.soilMoist_tv.getBackground();
                int moistureColor = getMoistureCircleColor(currentPlant.getHumidityLevel(), holder.soilMoist_tv.getContext());
                moistureCircle.setColor(moistureColor);

                // Set the error text visibility to GONE
                holder.error_tv.setVisibility(View.GONE);

                // Set the graphview visibility back to visible
                holder.history_graphView.setVisibility(View.VISIBLE);

            } else {

                // Set the moisture level
                String moistureText = "!";
                holder.soilMoist_tv.setText(moistureText);

                // Set the moisture circle color
                GradientDrawable moistureCircle = (GradientDrawable) holder.soilMoist_tv.getBackground();
                moistureCircle.setColor(ContextCompat.getColor(holder.soilMoist_tv.getContext(), R.color.errorRed));

                // Set the error text to visible
                holder.error_tv.setVisibility(View.VISIBLE);

                // If there has never been an update, say there is no data available
                if (currentPlant.getDateUpdated() == 0) {
                    holder.last_update_tv.setText("No data available");
                }

                // If there has not been an update for 7 days, hide graph view
                if (currentPlant.getDateUpdated() < System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) {
                    holder.history_graphView.setVisibility(View.GONE);
                }
            }

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

            // Attach onClickListener to the edit button
            holder.edit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), PlantEditorActivity.class);
                    intent.putExtra("id", currentPlant.getId());
                    v.getContext().startActivity(intent);
                }
            });

            // Populate the grapView
            holder.history_graphView.removeAllSeries();
            PointsGraphSeries<DataPoint> series = new PointsGraphSeries<DataPoint>(historyPoints(currentPlant.getId()));
            holder.history_graphView.addSeries(series);

            // Custom label formatter to show the hours
            holder.history_graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        // show hours before now for x values
                        int diffHours = (int) Math.floor((value));
                        return Integer.toString(diffHours) + "h";
                    } else {
                        // show normal y values
                        return super.formatLabel(value, isValueX);
                    }
                }
            });

            // set manual Y bounds
            holder.history_graphView.getViewport().setYAxisBoundsManual(true);
            holder.history_graphView.getViewport().setMinY(0);
            holder.history_graphView.getViewport().setMaxY(1000);
            holder.history_graphView.getViewport().setXAxisBoundsManual(true);
            holder.history_graphView.getViewport().setMaxX(0);
        }
    }

    DataPoint[] historyPoints(int plantId) {

        List<MoistureHistory> plantHistory = mHistoryRepo.getHistoryByPlantId(plantId);

        DataPoint[] series = new DataPoint[plantHistory.size()];

        for (int i = 0; i < plantHistory.size(); i++) {
            series[i] = new DataPoint((plantHistory.get(i).getDateTime() - System.currentTimeMillis())/(1000 * 60 * 60), plantHistory.get(i).getSoilMoisture());
        }


        return series;
    }

    void setPlants(List<Plant> plants){
        mPlants = plants;
        notifyDataSetChanged();
    }

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