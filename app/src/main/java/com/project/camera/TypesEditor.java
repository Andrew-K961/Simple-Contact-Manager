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

public class TypesEditor extends AppCompatActivity {

    private ArrayList<Item> typeList;
    private CustomAdapter arrayAdapter;
    private DBHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_types_editor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.edit_types);
        }

        database = new DBHelper(this);
        FloatingActionButton addBtn = findViewById(R.id.add_type);
        ListView listView = findViewById(R.id.types_list);

        typeList = database.getTypes();
        arrayAdapter = new CustomAdapter(typeList, this, false);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        addBtn.setOnClickListener(this::addType);
    }

    public void addType (View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getText(R.string.add_type));

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton(getText(R.string.add), (dialog, which) -> {
            String type = input.getText().toString();
            type = type.trim();
            database.insertType(type);
            input.clearFocus();
            typeList.clear();
            typeList.addAll(database.getTypes());
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