package com.project.camera;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddPerson extends AppCompatActivity {

    private DBHelper database;
    private File photoFile = null;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentPhotoPath;
    private TextView imagePath;
    private TextInputEditText nameEditText;
    private TextInputLayout nameLayout;
    private TextInputEditText phoneEditText;
    private TextInputLayout phoneLayout;
    private Button addButton;
    private Bundle extras;

    TextWatcher nameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //nothing
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.length() == 0 || s.length() == 1) {
                String nameWarning = getResources().getString(R.string.name_warning);
                nameLayout.setError(nameWarning);
            } else if(validate(s.toString(), true)) {
                String nameWarning = getResources().getString(R.string.name_warning2);
                nameLayout.setError(nameWarning);
            } else {
                nameLayout.setError(null);
            }
        }
        @Override
        public void afterTextChanged(Editable s) {
            //nothing
        }
    };
    TextWatcher phoneWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //nothing
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.length() != 10) {
                String phoneWarning = getResources().getString(R.string.phone_warning);
                phoneLayout.setError(phoneWarning);
            } else if(!TextUtils.isDigitsOnly(s)) {
                String phoneWarning = getResources().getString(R.string.phone_warning2);
                phoneLayout.setError(phoneWarning);
            } else {
                phoneLayout.setError(null);
            }
        }
        @Override
        public void afterTextChanged(Editable s) {
            //nothing
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        database = new DBHelper(this);
        nameEditText = findViewById(R.id.editTextPersonName);
        nameLayout = findViewById(R.id.editTextNameLayout);
        phoneEditText = findViewById(R.id.editTextPhone);
        phoneLayout = findViewById(R.id.editTextPhoneLayout);
        imagePath = findViewById(R.id.textView);
        addButton = findViewById(R.id.button);
        extras = getIntent().getExtras();
        String originActivity = extras.getString("Activity_Origin");

        phoneEditText.addTextChangedListener(phoneWatcher);
        nameEditText.addTextChangedListener(nameWatcher);

        assert originActivity != null;
        if (originActivity.equals("MainActivity")) {
            addSetup();
        } else if(originActivity.equals("Edit")) {
            editSetup();
        }
    }

    private void addSetup() {
        addButton.setOnClickListener(v -> {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            String name = Objects.requireNonNull(nameEditText.getText()).toString();
            String phone = Objects.requireNonNull(phoneEditText.getText()).toString();

            if (validate(name, true) || validate(phone, false)) {
                Toast toast = Toast.makeText(context, R.string.input_warning, duration);
                toast.show();
            } else if (currentPhotoPath == null) {
                Toast toast = Toast.makeText(context, R.string.image_warning, duration);
                toast.show();
            } else {
                SecureRandom random = new SecureRandom();
                int cryptoId = random.nextInt();
                while (database.checkForCollision(cryptoId) || cryptoId == -1) {
                    cryptoId = random.nextInt();
                }
                if (database.insert(cryptoId, name, phone, currentPhotoPath)) {
                    CharSequence text = getResources().getString(R.string.add_success);
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                finish();
            }
        });
    }

    private void editSetup() {
        addButton.setText(R.string.update);
        String currentName = extras.getString("Name");
        String currentPhone = extras.getString("Phone");
        String currentImage = extras.getString("Image");
        currentPhotoPath = currentImage;
        nameEditText.setText(currentName);
        phoneEditText.setText(currentPhone);
        String formatted = getString(R.string.image_path1, currentImage);
        imagePath.setText(formatted);

        addButton.setOnClickListener(v -> {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            String name = Objects.requireNonNull(nameEditText.getText()).toString();
            String phone = Objects.requireNonNull(phoneEditText.getText()).toString();


            if (validate(name, true) || validate(phone, false)) {
                Toast toast = Toast.makeText(context, R.string.input_warning, duration);
                toast.show();
            } else if (currentPhotoPath.length() < 20) {
                Toast toast = Toast.makeText(context, R.string.image_warning, duration);
                toast.show();
            } else {
                if(!(currentPhotoPath.equals(currentImage))){
                    File oldImage = new File(currentImage);
                    oldImage.delete();
                }
                if (database.update(extras.getInt("id"), name, phone, currentPhotoPath)) {
                    Toast toast = Toast.makeText(context, R.string.edit_success, duration);
                    toast.show();
                }
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            String text = getString(R.string.image_path1, currentPhotoPath);
            imagePath.setText(text);
        } else {
            File picture = new File(currentPhotoPath);
            if(picture.exists()){
                picture.delete();
            }
            currentPhotoPath = null;
        }
    }

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoURI;
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
                    photoURI = Uri.fromFile(photoFile);
                } else {
                    photoURI = FileProvider.getUriForFile(this,
                            "com.example.android.fileprovider",
                            photoFile);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(getApplicationContext(), R.string.error0, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir;
        if (Build.VERSION.SDK_INT<=Build.VERSION_CODES.KITKAT){
            storageDir = new File(Environment.getExternalStorageDirectory() + "/ContactManager/");
            if (!storageDir.exists()) {
                storageDir.mkdir();
            }
        } else {
            storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        /*File image = new File(getFilesDir(), imageFileName);
        if(image.createNewFile()){
            currentPhotoPath = image.getAbsolutePath();
        } else {
            image = null;
        }*/
        return image;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*public final boolean containsDigit(String s) {
        boolean containsDigit = false;

        if (s != null && !s.isEmpty()) {
            for (char c : s.toCharArray()) {
                if (containsDigit = Character.isDigit(c)) {
                    break;
                }
            }
        }
        return containsDigit;
    }*/

    private boolean validate (String text, boolean isName) {
        if(isName) {
            //String regex = "[&^*/$!@#<>;()\\\\|\\[\\]{}:?_~%µ¶×»÷¿°·€▲Δ¡△π™✓£¥✔®¬\u00AD§©¨¤¢+=\\n]";
            String regex = "[\\n]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            return matcher.find() || text.length() <= 1 || text.length() > 31;
        } else {
            return !TextUtils.isDigitsOnly(text) || text.length() != 10;
        }
    }
}