package com.project.camera;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
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

public class AddItem extends AppCompatActivity {

    private DBHelper database;
    private File photoFile = null;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentPhotoPath;
    private TextView imagePath;
    private TextInputEditText nameEditText;
    private TextInputLayout nameLayout;
    private TextInputEditText descEditText;
    private TextInputLayout descLayout;
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
                String nameWarning = getString(R.string.name_warning);
                nameLayout.setError(nameWarning);
            } else if(validate(s.toString(), true)) {
                String nameWarning = getString(R.string.name_warning2);
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

    TextWatcher descWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //nothing
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.length() == 0 || s.length() == 1) {
                String descWarning = getString(R.string.desc_warning);
                descLayout.setError(descWarning);
            } else if(validate(s.toString(), false)) {
                String descWarning = getString(R.string.desc_warning2);
                descLayout.setError(descWarning);
            } else {
                descLayout.setError(null);
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
        setContentView(R.layout.activity_add_item);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.takePicture).setOnClickListener(this::dispatchTakePictureIntent);

        database = new DBHelper(this);
        nameEditText = findViewById(R.id.nameEditText2);
        nameLayout = findViewById(R.id.nameLayout);
        descEditText = findViewById(R.id.descEditText);
        descLayout = findViewById(R.id.descLayout);
        imagePath = findViewById(R.id.imagePath);
        addButton = findViewById(R.id.addItem);
        extras = getIntent().getExtras();
        String originActivity = extras.getString("Activity_Origin");

        nameEditText.addTextChangedListener(nameWatcher);
        descEditText.addTextChangedListener(descWatcher);

        if (originActivity.equals("MainActivity")) {
            actionBar.setTitle(R.string.add_item);
            addSetup();
        } else if(originActivity.equals("Edit")) {
            actionBar.setTitle(R.string.edit);
            editSetup();
        }
    }

    private void addSetup() {
        addButton.setOnClickListener(v -> {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;
            String name = Objects.requireNonNull(nameEditText.getText()).toString();
            String desc = Objects.requireNonNull(descEditText.getText()).toString();

            if (validate(name, true) || validate(desc, false) || desc.length() < 2) {
                Toast toast = Toast.makeText(context, R.string.input_warning2, duration);
                toast.show();
            } else if (currentPhotoPath == null) {
                Toast toast = Toast.makeText(context, R.string.image_warning, duration);
                toast.show();
            } else {
                SecureRandom random = new SecureRandom();
                int cryptoId = random.nextInt();
                while (database.checkForCollision(cryptoId, true) || cryptoId == -1) {
                    cryptoId = random.nextInt();
                }
                if (database.insertItem(cryptoId, name, desc, currentPhotoPath)) {
                    Toast toast = Toast.makeText(context, R.string.add_success2, duration);
                    toast.show();
                }
                finish();
            }
        });
    }

    private void editSetup() {
        addButton.setText(R.string.update);
        String currentName = extras.getString("Name");
        String currentDesc = extras.getString("Desc");
        String currentImage = extras.getString("Image");
        currentPhotoPath = currentImage;
        nameEditText.setText(currentName);
        descEditText.setText(currentDesc);
        String formatted = getString(R.string.image_path1, currentImage);
        imagePath.setText(formatted);

        addButton.setOnClickListener(v -> {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;
            String name = Objects.requireNonNull(nameEditText.getText()).toString();
            String desc = Objects.requireNonNull(descEditText.getText()).toString();


            if (validate(name, true) || validate(desc, false)) {
                Toast toast = Toast.makeText(context, R.string.input_warning2, duration);
                toast.show();
            } else if (currentPhotoPath == null) {
                Toast toast = Toast.makeText(context, R.string.image_warning, duration);
                toast.show();
            } else {
                if(!(currentPhotoPath.equals(currentImage))){
                    File oldImage = new File(currentImage);
                    oldImage.delete();
                }
                if (database.updateItem(extras.getInt("id"), name, desc, currentPhotoPath)) {
                    Toast toast = Toast.makeText(context, R.string.edit_success2, duration);
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
                Toast.makeText(getApplicationContext(), R.string.error0, Toast.LENGTH_LONG).show();
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
        return image;
    }

    private boolean validate (String text, boolean name) {
        if (name){
            String regex = "[\\n]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            return matcher.find() || text.length() <= 1 || text.length() > 31;
        } else {
            return text.length() > 400;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}