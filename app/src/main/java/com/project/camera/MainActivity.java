package com.project.camera;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener, SearchView.OnQueryTextListener {

    DBHelper database;
    ArrayList<Person> listViewArray;
    ListView listView;
    ArrayAdapter<Person> arrayAdapter;
    SearchView searchView;
    InputMethodManager imm;
    private boolean searchState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addPerson = findViewById(R.id.imageButton);
        Button search = findViewById(R.id.imageButton3);
        Button nfc = findViewById(R.id.imageButton4);

        addPerson.setOnClickListener(this::onClick);
        search.setOnClickListener(this::searchButton);
        nfc.setOnClickListener(this::NFC);

        searchState = false;
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        searchView = findViewById(R.id.searchView);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setVisibility(View.GONE);

        TextView sort = findViewById(R.id.textView2);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(sort, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

        Spinner spinner = findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(adapter);

        database = new DBHelper(this);
        listViewArray = database.getAll();
        arrayAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, listViewArray);

        listView = findViewById(R.id.listView);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            Person person = arrayAdapter.getItem(arg2);
            int id_To_Search = person.getId();
            Intent intent = new Intent(getApplicationContext(),DisplayContact.class);

            intent.putExtra("id", id_To_Search);
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
        searchView.setQuery("",false);
        searchView.clearFocus();
        searchView.setVisibility(View.GONE);
    }

    public void onClick(View view){
        Intent intent = new Intent(getApplicationContext(), AddPerson.class);
        Bundle data = new Bundle();
        data.putString("Activity_Origin", "MainActivity");
        intent.putExtras(data);
        startActivity(intent);
    }

    public void searchButton(View view) {
        if (searchState) {
            searchView.setQuery("",false);
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
        if(manager.hasSystemFeature(PackageManager.FEATURE_NFC)) {
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

        if(selected.equals(recents)){
            Collections.sort(listViewArray, Person.IdSort);
            arrayAdapter.notifyDataSetChanged();
        } else if (selected.equals(name)){
            Collections.sort(listViewArray, Person.NameSort);
            arrayAdapter.notifyDataSetChanged();
        } else {
            Collections.sort(listViewArray, Person.PhoneSort);
            arrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //nothing
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        listViewArray.clear();
        listViewArray.addAll(database.search(query));
        arrayAdapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onQueryTextChange(@NonNull String newText) {
        listViewArray.clear();
        if(newText.length() >= 1) {
            listViewArray.addAll(database.search(newText));
        } else {
            listViewArray.addAll(database.getAll());
        }
        //listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        return false;
    }
}