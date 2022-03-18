package com.project.camera;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.util.Objects;

public class Display extends AppCompatActivity {

    private DBHelper mydb;
    private int id;
    private String imagePath;
    private String rawName;
    private String rawPhone;
    private String rawDesc;
    private String mode;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        mode = settings.getString("app_mode", "mode1");

        Button editButton;
        Button deleteButton;

        Bundle extras = getIntent().getExtras();
        id = extras.getInt("id");
        mydb = new DBHelper(this);

        if (mode.equals("mode2")){
            setContentView(R.layout.activity_display_item);
            setItemInfo();
            editButton = findViewById(R.id.editButton);
            deleteButton = findViewById(R.id.deleteButton);
        } else {
            setContentView(R.layout.activity_display_contact);
            setContactInfo();
            editButton = findViewById(R.id.edit);
            deleteButton = findViewById(R.id.delete);
        }

        deleteButton.setOnClickListener(this::delete);
        editButton.setOnClickListener(this::edit);
    }

    private void setContactInfo() {
        TextView textViewName = findViewById(R.id.name);
        TextView textViewPhone = findViewById(R.id.phone);
        TextView textViewId = findViewById(R.id.id);
        ImageView picture = findViewById(R.id.imageView);

        Cursor person = mydb.getPersonRow(id);
        person.moveToFirst();

        imagePath = person.getString(4);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        String name = getString(R.string.name, person.getString(2));
        String formattedPhone = person.getString(3).replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1) $2-$3");
        String phone = getString(R.string.phone, formattedPhone);
        String crypto_id = getString(R.string.id2, person.getString(1));

        rawName = person.getString(2);
        rawPhone = person.getString(3);

        person.close();

        Objects.requireNonNull(getSupportActionBar()).setTitle(rawName);

        textViewName.setText(name);
        textViewPhone.setText(phone);
        textViewId.setText(crypto_id);

        try {
            ExifInterface ei = new ExifInterface(imagePath);
            if (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    == ExifInterface.ORIENTATION_ROTATE_90) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), R.string.error0, Toast.LENGTH_SHORT);
            toast.show();
        }
        picture.setImageBitmap(bitmap);
    }

    private void setItemInfo() {
        TextView textViewName = findViewById(R.id.itemName);
        TextView textViewDesc = findViewById(R.id.description);
        TextView textViewId = findViewById(R.id.cg_id);
        ImageView picture = findViewById(R.id.itemPicture);

        Cursor item = mydb.getItemRow(id);
        item.moveToFirst();

        imagePath = item.getString(4);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        String name = getString(R.string.name, item.getString(2));
        String desc = getString(R.string.desc2, item.getString(3));
        String crypto_id = getString(R.string.id2, item.getString(1));

        rawName = item.getString(2);
        rawDesc = item.getString(3);

        item.close();

        Objects.requireNonNull(getSupportActionBar()).setTitle(rawName);

        textViewName.setText(name);
        textViewDesc.setText(desc);
        textViewId.setText(crypto_id);

        textViewDesc.setMovementMethod(new ScrollingMovementMethod());

        try {
            ExifInterface ei = new ExifInterface(imagePath);
            if (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    == ExifInterface.ORIENTATION_ROTATE_90) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), R.string.error0, Toast.LENGTH_LONG);
            toast.show();
        }
        picture.setImageBitmap(bitmap);
    }

    public void delete(View view) {
        if (mode.equals("mode1")){
            mydb.deletePerson(id);
        } else {
            mydb.deleteItem(id);
        }
        File picture = new File(imagePath);
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        if(picture.delete()){
            CharSequence text = getResources().getString(R.string.delete_success);
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            CharSequence text = getResources().getString(R.string.delete_fail);
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        finish();
    }

    public void edit(View view) {
        Intent intent;
        Bundle data = new Bundle();
        data.putInt("id", id);
        data.putString("Activity_Origin", "Edit");
        data.putString("Name", rawName);
        data.putString("Image", imagePath);

        if (mode.equals("mode1")){
            intent = new Intent(this, AddPerson.class);
            data.putString("Phone", rawPhone);
        } else {
            intent = new Intent(this, AddItem.class);
            data.putString("Desc", rawDesc);
        }
        intent.putExtras(data);
        startActivity(intent);
    }
}