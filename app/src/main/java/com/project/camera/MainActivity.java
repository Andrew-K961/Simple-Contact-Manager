package com.project.camera;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener, SearchView.OnQueryTextListener {

    private DBHelper database;
    private ArrayList<Person> personArrayList;
    private ArrayAdapter<Person> personArrayAdapter;
    private ArrayList<Item> itemArrayList;
    private ArrayAdapter<Item> itemArrayAdapter;
    private SearchView searchView;
    private InputMethodManager imm;
    private boolean searchState;
    private String mode;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        mode = settings.getString("app_mode", "mode1");

//************** Initiate Buttons

        Button addPerson = findViewById(R.id.imageButton);
        Button search = findViewById(R.id.imageButton3);
        Button nfc = findViewById(R.id.imageButton4);

        addPerson.setOnClickListener(this::addButton);
        search.setOnClickListener(this::searchButton);
        nfc.setOnClickListener(this::NFC);

//************** Search

        searchState = false;
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        searchView = findViewById(R.id.searchView);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setVisibility(View.GONE);

//************** Sorter

        TextView sort = findViewById(R.id.sort_by);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(sort, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter;

//************* Mode

        ListView listView = findViewById(R.id.listView);
        database = new DBHelper(this);
        if (mode.equals("mode2")){
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.sort_array2, android.R.layout.simple_spinner_item);

            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.inventory);

            addPerson.setText(R.string.add_item);

            searchView.setQueryHint(getString(R.string.inventory_search));

            itemArrayList = database.getAllItems();
            itemArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemArrayList);
        } else {
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.sort_array1, android.R.layout.simple_spinner_item);

            personArrayList = database.getAllPeople();
            personArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, personArrayList);
            listView.setAdapter(personArrayAdapter);
        }

//*************

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(adapter);

//************** List

        listView.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            Intent intent;
            if (mode.equals("mode1")) {
                Person person = personArrayAdapter.getItem(arg2);
                intent = new Intent(getApplicationContext(), DisplayContact.class);
                intent.putExtra("id", person.getId());
            } else {
                Item item = itemArrayAdapter.getItem(arg2);
                intent = new Intent(getApplicationContext(), DisplayContact.class); //TODO change to display item
                intent.putExtra("id", item.getId());
            }
            startActivity(intent);
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        searchView.setQuery("", false);
        searchView.clearFocus();
        searchView.setVisibility(View.GONE);
    }

    public void addButton(View view) {
        Intent intent = new Intent(getApplicationContext(), AddPerson.class);
       // Bundle data = new Bundle();
        intent.putExtra("Activity_Origin", "MainActivity");
       // intent.putExtras(data);
        startActivity(intent);
    }

    public void searchButton(View view) {
        if (searchState) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.setVisibility(View.GONE);
            searchState = false;
        } else {
            searchView.setVisibility(View.VISIBLE);
            searchView.setFocusable(true);
            searchView.setOnQueryTextListener(this);
            searchView.requestFocus();
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            searchState = true;
        }
    }

    public void NFC(View view) {
        PackageManager manager = getApplicationContext().getPackageManager();
        if (manager.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            NFCFragment bottomSheet = new NFCFragment();
            bottomSheet.show(getSupportFragmentManager(),
                    "ModalBottomSheet");
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.no_nfc, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        String selected = parent.getItemAtPosition(position).toString();
        String recents = getResources().getString(R.string.recents);
        String name = getResources().getString(R.string.hint1);
        if (mode.equals("mode1")) {
            if (selected.equals(recents)) {
                Collections.sort(personArrayList, Person.IdSort);
            } else if (selected.equals(name)) {
                Collections.sort(personArrayList, Person.NameSort);
            } else {
                Collections.sort(personArrayList, Person.PhoneSort);
            }
            personArrayAdapter.notifyDataSetChanged();
        } else {
            if (selected.equals(recents)) {
                Collections.sort(itemArrayList, Item.IdSort);
            } else {
                Collections.sort(itemArrayList, Item.NameSort);
            }
            itemArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //nothing
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        personArrayList.clear();
        personArrayList.addAll(database.searchPeople(query));
        personArrayAdapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onQueryTextChange(@NonNull String newText) {
        if (mode.equals("mode1")) {
            personArrayList.clear();
            if (newText.length() >= 1) {
                personArrayList.addAll(database.searchPeople(newText));
            } else {
                personArrayList.addAll(database.getAllPeople());
            }
            personArrayAdapter.notifyDataSetChanged();
            return false;
        } else {
            itemArrayList.clear();
            if (newText.length() >= 1) {
                itemArrayList.addAll(database.searchItem(newText));
            } else {
                itemArrayList.addAll(database.getAllItems());
            }
            itemArrayAdapter.notifyDataSetChanged();
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_button, menu);
        // first parameter is the file for icon and second one is menu
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), Settings.class);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }
}