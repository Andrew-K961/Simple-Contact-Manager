package com.project.camera;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements ThreadCompleteListener{

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Preference locationButton = findPreference("edit locations");
            Preference upload = findPreference("test");
            Preference enableSheets = findPreference("Enable Sheets");
            Preference setId = findPreference("Sheet Id");
            Preference typeButton = findPreference("edit types");

            assert locationButton != null;
            assert upload != null;
            assert enableSheets != null;
            assert setId != null;
            assert typeButton != null;

            if (settings.getString("app_mode", "mode2").equals("mode1")){
                enableSheets.setEnabled(false);
                upload.setEnabled(false);
                setId.setEnabled(false);
            } else {
                if (settings.getBoolean("Enable Sheets", false)){
                    upload.setEnabled(true);
                    setId.setEnabled(true);
                } else {
                    upload.setEnabled(false);
                    setId.setEnabled(false);
                }
            }

            locationButton.setOnPreferenceClickListener(v -> {
                Intent intent = new Intent(getActivity(), LocationsEditor.class);
                startActivity(intent);
                return false;
            });

            typeButton.setOnPreferenceClickListener(v -> {
                Intent intent = new Intent(getActivity(), TypesEditor.class);
                startActivity(intent);
                return false;
            });

            enableSheets.setOnPreferenceClickListener(v -> {
                if (settings.getBoolean("Enable Sheets", false)){
                    upload.setEnabled(true);
                    setId.setEnabled(true);
                } else {
                    upload.setEnabled(false);
                    setId.setEnabled(false);
                }
                return false;
            });

            upload.setOnPreferenceClickListener(v -> {
                if (settings.getString("Sheet Id", "").equals("")){
                    Toast.makeText(getContext(), R.string.sheets_error1, Toast.LENGTH_LONG).show();
                } else {
                    Context context = getActivity();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(context.getText(R.string.upload_confirm));

                    final EditText input = new EditText(context);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    builder.setPositiveButton(context.getText(R.string.yes), (dialog, which) -> {
                        if (input.getText().toString().contentEquals(context.getText(R.string.confirm))){
                            NetworkingThreads.setVariables(getActivity().getAssets(),
                                    new DBHelper(getContext()), settings.getString("Sheet Id", ""));

                            NotifyingThread setup = new NetworkingThreads.Setup();
                            setup.setName("Setup");
                            setup.addListener(this);
                            setup.start();
                        } else {
                            dialog.cancel();
                            Toast.makeText(context, R.string.no, Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.setNegativeButton(context.getText(R.string.no), (dialog, which) -> {
                        dialog.cancel();
                        input.clearFocus();
                    });

                    builder.show();
                }
                return false;
            });
        }

        @Override
        public void notifyOfThreadComplete(Thread thread) {
            if (thread.getName().equals("Setup")){
                Thread uploadThread = new Thread(new NetworkingThreads.UploadAll());
                uploadThread.start();
            }
        }
    }
}