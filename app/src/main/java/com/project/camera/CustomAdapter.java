package com.project.camera;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<Item> list;
    private final Context context;
    private static DBHelper database;
    private final boolean locationMode;

    public CustomAdapter(ArrayList<Item> list, Context context, boolean locMode) {
        this.list = list;
        this.context = context;
        database = new DBHelper(context);
        locationMode = locMode;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return list.get(pos).getId();
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_listview, null);
        }

        //Handle TextView and display string from your list
        TextView tvContact= view.findViewById(R.id.tvContact);
        tvContact.setText(list.get(position).toString());

        //Handle buttons and add onClickListeners
        Button edit= view.findViewById(R.id.btn);
        Button delete= view.findViewById(R.id.btn2);

        edit.setOnClickListener(v -> {
            editListener(list.get(position).getId(), position);
        });

        delete.setOnClickListener(v -> {
            if (locationMode){
                if (!database.checkLocationInUse(list.get(position).getId())){
                    database.deleteLocation(list.get(position).getId());
                    list.clear();
                    list.addAll(database.getAllLocations());
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, R.string.location_error, Toast.LENGTH_LONG).show();
                }
            } else {
                if (!database.checkTypeInUse(list.get(position).getName())){
                    database.deleteType(list.get(position).getId());
                    list.clear();
                    list.addAll(database.getTypes());
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, R.string.type_error, Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    public void editListener (int id, int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getText(R.string.edit));

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(context.getText(R.string.confirm), (dialog, which) -> {
            if (locationMode){
                NotifyingThread update = new NetworkingThreads.UpdateLocations(input.getText().toString(), list.get(position).getName());
                update.start();
                database.updateLocation(id, input.getText().toString());
                input.clearFocus();
                dialog.cancel();
                list.clear();
                list.addAll(database.getAllLocations());
            } else {
                NotifyingThread update = new NetworkingThreads.UpdateTypes(input.getText().toString(), list.get(position).getName());
                update.start();
                database.updateType(id, input.getText().toString(), list.get(position).getName());
                input.clearFocus();
                dialog.cancel();
                list.clear();
                list.addAll(database.getTypes());
            }
            notifyDataSetChanged();
        });
        builder.setNegativeButton(context.getText(R.string.cancel), (dialog, which) -> {
            dialog.cancel();
            input.clearFocus();
        });

        input.setText(list.get(position).getName());

        builder.show();
    }
}
