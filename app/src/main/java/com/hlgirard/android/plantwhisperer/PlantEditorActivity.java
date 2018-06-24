package com.hlgirard.android.plantwhisperer;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

import data.MoistureHistoryRepository;
import data.Plant;
import data.PlantRepository;
import data.PlantViewModel;

public class PlantEditorActivity extends AppCompatActivity {

    Boolean mPlantHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPlantHasChanged = true;
            return false;
        }
    };

    private EditText mNameEditText;
    private EditText mMqttEditText;
    private EditText mLocationEditText;

    private Button mRemoveButton;
    private Button mResetButton;

    private PlantViewModel mPlantViewModel;
    private MoistureHistoryRepository mHistoryRepository;

    Bundle extras;
    private int plantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_plant_name);
        mMqttEditText = (EditText) findViewById(R.id.edit_mqtt_topic);
        mLocationEditText = (EditText) findViewById(R.id.edit_location);

        mRemoveButton = (Button) findViewById(R.id.remove_button);
        mResetButton = (Button) findViewById(R.id.reset_button);

        // Set onTouchListener to know whether the user has interacted with the views
        mNameEditText.setOnTouchListener(mTouchListener);
        mMqttEditText.setOnTouchListener(mTouchListener);

        // Get the view model
        mPlantViewModel = ViewModelProviders.of(this).get(PlantViewModel.class);

        // Get the history repo
        mHistoryRepository = new MoistureHistoryRepository(getApplication());

        //Get the intent
        Intent intent = getIntent();
        extras = intent.getExtras();

        if (extras == null) {
            setTitle("Add a Plant");
            mRemoveButton.setVisibility(View.GONE);
        } else {
            setTitle("Edit Plant");
            plantId = extras.getInt("id");
            displayInfo(plantId);

            // Set the Delete button OnClickListener
            mRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog(plantId);
                }
            });

            // Set the Reset button OnClickListener
            mResetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showResetConfirmationDialog(plantId);
                }
            });

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    private void savePlant() {
        String name = mNameEditText.getText().toString().trim();
        String mqttTopic = mMqttEditText.getText().toString().trim();
        String location = mLocationEditText.getText().toString().trim();

        if (extras == null) {
            Plant newPlant = new Plant(name, 00000000, 0, mqttTopic, location, 0);
            Log.v("PlantEditorActivity", "Inserting a new plant" + newPlant.toString());
            mPlantViewModel.insert(newPlant);
        } else {
            Log.v("savePlant","Attempting to update plant with id " + plantId);
            Plant currentPlant = mPlantViewModel.getPlantById(plantId);
            currentPlant.setName(name);
            currentPlant.setLocation(location);
            currentPlant.setTopic(mqttTopic);
            mPlantViewModel.update(currentPlant);
        }
    }

    private void deletePlant(int id) {
        Log.v("deletePlant", "Attempting to delete plant with id " + id);
        Plant toDelete = mPlantViewModel.getPlantById(id);
        Log.v("deletePlant", "Preparing to delete plant #" + toDelete.getId());
        mPlantViewModel.delete(toDelete);
        finish();
    }

    private void resetPlantHistory(int id) {
        Log.v("resetPlant", "Resetting history for plant with id #" + id);
        mHistoryRepository.deleteHistoryByPlantId(id);
        finish();
    }

    private void displayInfo(int id) {
        Log.v("displayInfo","Displaying info of plant with id " + id);
        Plant currentPlant = mPlantViewModel.getPlantById(id);

        mNameEditText.setText(currentPlant.getName());
        mLocationEditText.setText(currentPlant.getLocation());
        mMqttEditText.setText(currentPlant.getTopic());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Insert new pet into database
                savePlant();
                // Exit the editor activity and return to main screen
                finish();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPlantHasChanged) {
                    NavUtils.navigateUpFromSameTask(PlantEditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(PlantEditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog(final int plantId) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the plant.
                deletePlant(plantId);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showResetConfirmationDialog(final int plantId) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Reset all history for this plant?");
        builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Reset" button, so reset the plant.
                resetPlantHistory(plantId);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You have unsaved changes, do you want to discard them?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
