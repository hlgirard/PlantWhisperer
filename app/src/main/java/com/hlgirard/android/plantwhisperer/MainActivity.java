package com.hlgirard.android.plantwhisperer;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import data.Plant;
import data.PlantRepository;
import data.PlantViewModel;

import com.hlgirard.android.plantwhisperer.helpers.dataUpdateJobService;
import com.hlgirard.android.plantwhisperer.helpers.mqttUpdaterAsyncTask;

public class MainActivity extends AppCompatActivity {

    private static int PLANT_LOADER_JOB_ID = 1;
    private ProgressBar loading_spinner;
    private TextView empty_tv;
    private PlantListAdapter mAdapter;
    private PlantRepository mPlantRepository;
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


        // Get a viewModel and repository
        mPlantViewModel = ViewModelProviders.of(this).get(PlantViewModel.class);
        mPlantRepository = new PlantRepository(getApplication());

        // Bind an observer to the viewModel and update adapter on change
        mPlantViewModel.getAllPlants().observe(this, new Observer<List<Plant>>() {
            @Override
            public void onChanged(@Nullable List<Plant> plants) {
                // Update the cached copy of the words in the adapter.
                mAdapter.setPlants(plants);
            }
        });

        // Setup the data update Jobscheduler
        JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        jobScheduler.schedule(new JobInfo.Builder(PLANT_LOADER_JOB_ID, new ComponentName(this, dataUpdateJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(60 * 60 * 1000)
                .setPersisted(true)
                .build());
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Check connectivity
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            // Update the plant data
            Log.v("Main Activity", "Triggering the data update");
            updateData(mPlantRepository);
            loading_spinner.setVisibility(View.GONE);
        } else {
            loading_spinner.setVisibility(View.GONE);
            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.parent_view), "No internet connection", Snackbar.LENGTH_LONG);
            mySnackbar.show();
        }
    }

    public void updateData(PlantRepository plantRepo) {
        mqttUpdaterAsyncTask updaterAsyncTask = new mqttUpdaterAsyncTask(plantRepo);
        updaterAsyncTask.execute(getApplicationContext());
    }

}
