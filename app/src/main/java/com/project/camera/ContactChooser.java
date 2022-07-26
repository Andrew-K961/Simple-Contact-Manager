package com.project.camera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Objects;

public class ContactChooser extends AppCompatActivity {

    private ArrayAdapter<Person> arrayAdapter;
    private ArrayAdapter<Item> itemArrayAdapter;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_chooser);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        String mode = settings.getString("app_mode", "mode1");

        DBHelper db = new DBHelper(this);
        ListView list = findViewById(R.id.contactList);

        if (mode.equals("mode1")){
            ArrayList<Person> listViewArray = db.getAllPeople();
            arrayAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, listViewArray);

            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.contact_chooser);

            list.setAdapter(arrayAdapter);
            list.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
                Person person = arrayAdapter.getItem(arg2);
                int id = db.getCryptoId(person.getId(), false);
                Intent intent = new Intent(getApplicationContext(), NFCActivity.class);

                intent.putExtra("Id", id);
                intent.putExtra("Mode", 1);

                startActivity(intent);
            });
        } else {
            TextView text = findViewById(R.id.textView3);
            text.setText(R.string.select_item);

            ArrayList<Item> listViewArray = db.getAllItems();
            itemArrayAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, listViewArray);

            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.item_chooser);

            list.setAdapter(itemArrayAdapter);
            list.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
                Item item = itemArrayAdapter.getItem(arg2);
                int id = db.getCryptoId(item.getId(), true);
                Intent intent = new Intent(getApplicationContext(), NFCActivity.class);

                intent.putExtra("Id", id);
                intent.putExtra("Mode", 1);

                startActivity(intent);
            });
        }
    }
}