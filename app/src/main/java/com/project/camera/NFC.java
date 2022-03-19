package com.project.camera;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

public class NFC extends AppCompatActivity {

    private DBHelper db;
    private TextView mode;
    private TextView status;
    private TextView crypto_id;
    private Button button;
    private String writeId;
    private int contactId;
    private Tag nfcTag;
    private PendingIntent pendingIntent;
    private IntentFilter[] writeTagFilters;
    private NfcAdapter nfcAdapter;
    SharedPreferences settings;
    private boolean itemMode;

    TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //nothing
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //nothing
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                status.setTextSize(30);
            } else {
                status.setTextAppearance(R.style.Base_TextAppearance_AppCompat_Display1);
            }
        }
    };

    @SuppressLint("UnspecifiedImmutableFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        mode = findViewById(R.id.mode);
        status = findViewById(R.id.status);
        crypto_id = findViewById(R.id.cryptoId);
        button = findViewById(R.id.button2);
        db = new DBHelper(this);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        itemMode = !settings.getString("app_mode", "mode1").equals("mode1");

        if(getIntent().getIntExtra("Mode", 0) == 1){
            writeId = String.valueOf(getIntent().getIntExtra("Id", 0));
            mode.setText(R.string.mode1);
            button.setText(R.string.write);
            crypto_id.setText(getString(R.string.id3, writeId));
        } else {
            crypto_id.setText(getString(R.string.id3, " "));
            if (itemMode){
                button.setText(R.string.goTo_item);
            }
        }
        button.setOnClickListener(this::goTo_or_write);
        button.setVisibility(View.INVISIBLE);

        status.addTextChangedListener(watcher);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.nfc);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                    getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                    getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }
/*
   ******************** Read Mode
 */
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }

    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0){
            String empty = getString(R.string.empty);
            crypto_id.setText(getString(R.string.id3, empty));
            status.setText(R.string.error1);
            return;
        }

        String text;
        //String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
            return;
        }
        try {
            int readId = Integer.parseInt(text);
            contactId = db.nfcSearch(readId, itemMode);
            if (contactId == -1){
                crypto_id.setText(getString(R.string.id3, getString(R.string.empty)));
                status.setText(R.string.error3);
            } else {
                crypto_id.setText(getString(R.string.id3, text));
                status.setText(R.string.success);
                button.setVisibility(View.VISIBLE);
            }
        } catch (NumberFormatException e){
            Log.e("NumberFormatException", e.toString());
            crypto_id.setText(getString(R.string.id3, getString(R.string.empty)));
            status.setText(R.string.error2);
        }
    }
/*
    ********************** Write mode
*/
    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();

        status.setText(R.string.success);
        Toast.makeText(getApplicationContext(), R.string.write_success, Toast.LENGTH_SHORT).show();
        button.setVisibility(View.INVISIBLE);
    }

    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);
    }

// ************************************************************************

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
        if (mode.getText()==getText(R.string.mode0)){
            readFromIntent(intent);
        } else if (mode.getText()==getText(R.string.mode1)){
            button.setVisibility(View.VISIBLE);
            status.setText(R.string.tagFound);
        }
    }

    private void goTo_or_write(View view) {
        if (mode.getText()==getText(R.string.mode0)){
            Intent intent = new Intent(getApplicationContext(), Display.class);
            intent.putExtra("id", contactId);
            startActivity(intent);
        } else if (mode.getText()==getText(R.string.mode1)){
            try {
                write(writeId, nfcTag);
            } catch (IOException e){
                status.setText(R.string.error0);
                Toast.makeText(getApplicationContext(), R.string.error0, Toast.LENGTH_LONG ).show();
                e.printStackTrace();
            } catch (FormatException e){
                status.setText(R.string.error0);
                Toast.makeText(getApplicationContext(), R.string.error0, Toast.LENGTH_LONG ).show();
                e.printStackTrace();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.error0, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    @Override
    public void onPause(){
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }
}