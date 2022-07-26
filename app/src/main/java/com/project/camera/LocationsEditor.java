package com.project.camera;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class LocationsEditor extends AppCompatActivity {

    private ArrayList<Item> locationList;
    private CustomAdapter arrayAdapter;
    private DBHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_editor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.edit_locations);
        }

        database = new DBHelper(this);
        FloatingActionButton addBtn = findViewById(R.id.floatingActionButton);
        ListView listView = findViewById(R.id.location_list);

        locationList = database.getAllLocations();
        arrayAdapter = new CustomAdapter(locationList, this, true);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        addBtn.setOnClickListener(this::addLocation);
    }

    public void addLocation (View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getText(R.string.add_location));

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton(getText(R.string.add), (dialog, which) -> {
            String location = input.getText().toString();
            location = location.trim();
            database.insertLocation(location);
            input.clearFocus();
            locationList.clear();
            locationList.addAll(database.getAllLocations());
            arrayAdapter.notifyDataSetChanged();
            dialog.cancel();
        });
        builder.setNegativeButton(getText(R.string.cancel), (dialog, which) -> {
            dialog.cancel();
            input.clearFocus();
        });

        AlertDialog alertToShow = builder.create();
        alertToShow.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertToShow.show();
    }
}