package com.project.camera;

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
    private TextView textViewName;
    private TextView textViewPhone;
    private TextView textViewDesc;
    private TextView textViewId;
    private ImageView picture;
    private TextView TV_quantity;
    private TextView textViewLocation;
    private String rawLocation;
    private int quantityInt;
    private int imageQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        mode = settings.getString("app_mode", "mode1");
        imageQuality = settings.getInt("image quality", 6) *100;

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
        picture = findViewById(R.id.imageView);
        textViewId = findViewById(R.id.id);
        textViewPhone = findViewById(R.id.phone);
        textViewName = findViewById(R.id.name);

        Cursor person = mydb.getPersonRow(id);
        person.moveToFirst();

        String name = getString(R.string.name, person.getString(2));
        String formattedPhone = person.getString(3).replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1) $2-$3");
        String phone = getString(R.string.phone, formattedPhone);
        String crypto_id = getString(R.string.id2, person.getString(1));

        rawName = person.getString(2);
        rawPhone = person.getString(3);

        Objects.requireNonNull(getSupportActionBar()).setTitle(rawName);

        textViewName.setText(name);
        textViewPhone.setText(phone);
        textViewId.setText(crypto_id);

        imagePath = person.getString(4);
        person.close();
        if (Objects.equals(imagePath, "")){
            picture.setVisibility(View.INVISIBLE);
            return;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, imageQuality);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

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
        textViewName = findViewById(R.id.itemName);
        textViewDesc = findViewById(R.id.description);
        textViewId = findViewById(R.id.cg_id);
        picture = findViewById(R.id.itemPicture);
        TV_quantity = findViewById(R.id.quantityDisplay);
        textViewLocation = findViewById(R.id.locationText);

        Cursor item = mydb.getItemRow(id);
        item.moveToFirst();

        quantityInt = item.getInt(5);

        String name = getString(R.string.name, item.getString(2));
        String desc = getString(R.string.desc2, item.getString(3));
        String crypto_id = getString(R.string.id2, item.getString(1));
        String quantity = getString(R.string.quantity_display, String.valueOf(quantityInt));

        rawName = item.getString(2);
        rawDesc = item.getString(3);

        Objects.requireNonNull(getSupportActionBar()).setTitle(rawName);

        textViewName.setText(name);
        textViewDesc.setText(desc);
        textViewId.setText(crypto_id);

        textViewDesc.setMovementMethod(new ScrollingMovementMethod());

        if (quantity.equals(getString(R.string.quantity_display, "-1"))){
            TV_quantity.setVisibility(View.GONE);
        } else {
            TV_quantity.setText(quantity);
        }
        if (item.getInt(6) == -1){
            textViewLocation.setVisibility(View.GONE);
        } else {
            rawLocation = mydb.getLocation(item.getInt(6));
            String location = getString(R.string.location_display, mydb.getLocation(item.getInt(6)));
            textViewLocation.setText(location);
        }

        imagePath = item.getString(4);
        item.close();
        if (Objects.equals(imagePath, "") || imagePath == null){
            picture.setVisibility(View.GONE);
            textViewDesc.setMaxLines(15);
            return;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, imageQuality);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

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

        //Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, DpToPx(450), DpToPx(550), false);
        picture.setImageBitmap(bitmap);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int IMAGE_MAX_SIZE) {
        int inSampleSize = 1;

        if (options.outHeight > IMAGE_MAX_SIZE || options.outWidth > IMAGE_MAX_SIZE) {
            inSampleSize = (int)Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
        }

        return inSampleSize;
    }

    public void delete(View view) {
        if (settings.getBoolean("Enable Sheets", false)){
            NotifyingThread delete = new NetworkingThreads.DeleteRow(id);
            delete.start();
        }

        if (mode.equals("mode1")){
            mydb.deletePerson(id);
        } else {
            mydb.deleteItem(id);
        }
        File picture = new File(imagePath);
        int duration = Toast.LENGTH_SHORT;
        if(picture.delete() || imagePath.equals("")){
            CharSequence text = getResources().getString(R.string.delete_success);
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        } else if (picture.exists()){
            CharSequence text = getResources().getString(R.string.delete_fail);
            Toast toast = Toast.makeText(this, text, duration);
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
            data.putInt("Quantity", quantityInt);
            data.putString("Location", rawLocation);
        }
        intent.putExtras(data);
        startActivity(intent);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Cursor updated;
        if (mode.equals("mode1")){
            updated = mydb.getPersonRow(id);
        } else {
            updated = mydb.getItemRow(id);
        }
        updated.moveToFirst();

        if(!Objects.equals(updated.getString(2), rawName)){
            String name = getString(R.string.name, updated.getString(2));
            rawName = updated.getString(2);
            textViewName.setText(name);
            Objects.requireNonNull(getSupportActionBar()).setTitle(rawName);
        }
        if(!Objects.equals(updated.getString(3), rawPhone) && mode.equals("mode1")){
            String formattedPhone = updated.getString(3).replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1) $2-$3");
            String phone = getString(R.string.phone, formattedPhone);
            rawPhone = updated.getString(3);
            textViewPhone.setText(phone);
        }
        if(!Objects.equals(updated.getString(3), rawDesc) && mode.equals("mode2")){
            String desc = getString(R.string.desc2, updated.getString(3));
            rawDesc = updated.getString(3);
            textViewDesc.setText(desc);
        }
        if(!Objects.equals(updated.getString(4), imagePath)){
            imagePath = updated.getString(4);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

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
            picture.setVisibility(View.VISIBLE);
        }
        if (mode.equals("mode2") && updated.getInt(5) != quantityInt){
            quantityInt = updated.getInt(5);
            if (quantityInt != -1){
                String formatted = getString(R.string.quantity_display, String.valueOf(quantityInt));
                TV_quantity.setText(formatted);
                TV_quantity.setVisibility(View.VISIBLE);
            } else{
                TV_quantity.setVisibility(View.GONE);
            }
        }
        if (mode.equals("mode2") && !Objects.equals(mydb.getLocation(updated.getInt(6)), rawLocation)){
            rawLocation = mydb.getLocation(updated.getInt(6));
            if (rawLocation.equals("-1") || rawLocation.equals("")){
                textViewLocation.setVisibility(View.GONE);
            } else {
                textViewLocation.setText(getString(R.string.location_display, rawLocation));
                textViewLocation.setVisibility(View.VISIBLE);
            }
        }
        updated.close();
    }
}