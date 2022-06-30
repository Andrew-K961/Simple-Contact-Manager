package com.project.camera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
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

            assert locationButton != null;
            assert upload != null;
            assert enableSheets != null;
            assert setId != null;

            if (settings.getBoolean("Enable Sheets", false)){
                upload.setEnabled(true);
                setId.setEnabled(true);
            } else {
                upload.setEnabled(false);
                setId.setEnabled(false);
            }

            locationButton.setOnPreferenceClickListener(v -> {
                Intent intent = new Intent(getActivity(), LocationsEditor.class);
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
                    NetworkingThreads.setVariables(getActivity().getAssets(),
                            new DBHelper(getContext()), settings.getString("Sheet Id", ""));

                    NotifyingThread setup = new NetworkingThreads.Setup();
                    setup.setName("Setup");
                    setup.addListener(this);
                    setup.start();
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