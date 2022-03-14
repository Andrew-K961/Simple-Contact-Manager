package com.project.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ContactChooser extends AppCompatActivity {

    private ArrayAdapter<Person> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_chooser);

        DBHelper db = new DBHelper(this);
        ListView list = findViewById(R.id.contactList);
        ArrayList<Person> listViewArray = db.getAll();
        arrayAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, listViewArray);

        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            Person person = arrayAdapter.getItem(arg2);
            int id = db.getCryptoId(person.getId());
            Intent intent = new Intent(getApplicationContext(),NFC.class);

            intent.putExtra("Id", id);
            intent.putExtra("Mode", 1);

            startActivity(intent);
        });
    }
}