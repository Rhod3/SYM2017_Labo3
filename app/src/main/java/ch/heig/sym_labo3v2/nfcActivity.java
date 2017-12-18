package ch.heig.sym_labo3v2;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class nfcActivity extends AppCompatActivity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";

    private final int AUTHENTICATE_MAX = 10;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    //
    private TextView accessGrantedTextView;
    private TextView currentSecurityLevelTextView;
    private TextView lastTimeScannedTextView;
    private Button nfcOnlyButton;
    private Button nfcAndPasswordButton;
    private EditText passwordEditText;

    private NfcAdapter mNfcAdapter;
    private Timer timer;
    TimerTask task;

    private int time = 20;
    private boolean timerRunning = false;
    private byte securityAccessLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("ON CREATE IN");
        setContentView(R.layout.activity_nfc);

        accessGrantedTextView = (TextView) findViewById(R.id.nfcMainTextView);
        currentSecurityLevelTextView = (TextView) findViewById(R.id.nfcSecurityLeveTextView);
        lastTimeScannedTextView = (TextView) findViewById(R.id.lastScanTextView);

        passwordEditText = (EditText) findViewById(R.id.passwordInput);
        nfcAndPasswordButton = (Button) findViewById(R.id.nfcAndPasswordButton);
        nfcOnlyButton = (Button) findViewById(R.id.nfcOnlyButton);

        nfcOnlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAccessLevel();
            }
        });
        nfcAndPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!passwordEditText.getText().toString().isEmpty()) {
                    displayAccessLevel();
                } else {
                    accessGrantedTextView.setText("Please fill in a password");
                }
            }
        });

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!mNfcAdapter.isEnabled()) {
            accessGrantedTextView.setText("NFC is disabled.");
        } else {
            accessGrantedTextView.setText("NFC is enabled.");
        }
        System.out.println("ON CREATE OUT");
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch();
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch();

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.out.println("On New Intent");
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    // called in onResume()
    private void setupForegroundDispatch() {
        System.out.println("IN FORE");
        if (mNfcAdapter == null) return;

        final Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        try {
            ndef.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e(TAG, "MalformedMimeTypeException", e);
        }

        IntentFilter[] intentFiltersArray = new IntentFilter[]{ndef,};
        String[][] techListArray = new String[][]{new String[]{NfcV.class.getName()}};

        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListArray);
        System.out.println("FORE DONE");
    }

    // called in onPause()
    private void stopForegroundDispatch() {
        System.out.println("IN STOP");
        if (mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
        System.out.println("STOP DONE");
    }

    private void handleIntent(Intent intent) {
        System.out.println("In Handle Intent");
        securityAccessLevel = AUTHENTICATE_MAX;
        if (!timerRunning) {
            startTimer();
        }
        lastTimeScannedTextView.setText(sdf.format(new Date()));
        
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    public void startTimer() {
        timer = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayCurrentSecurityLevel();
                        securityAccessLevel--;
                        System.out.println("Security Level updated " + securityAccessLevel);
                        if (securityAccessLevel < 0) {
                            System.out.println("Timer cancelled");
                            timerRunning = false;
                            timer.cancel();
                        }
                    }
                });
            }
        };
        timerRunning = true;
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                accessGrantedTextView.setText("Read content: " + result);
            }
        }
    }

    private void displayAccessLevel() {
        String access = "You have no access right. Please scan an NFC badge.";
        if (securityAccessLevel > 7 && securityAccessLevel <= 10) {
            access = "You have a high level access";
        } else if (securityAccessLevel > 4) {
            access = "You have a medium level access";
        } else if (securityAccessLevel > 0) {
            access = "You have a low level access";
        }
        accessGrantedTextView.setText(access);
    }

    private void displayCurrentSecurityLevel() {
        currentSecurityLevelTextView.setText(String.format("Security Level %d", securityAccessLevel));
    }
}
