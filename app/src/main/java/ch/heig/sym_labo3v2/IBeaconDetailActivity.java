package ch.heig.sym_labo3v2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

public class IBeaconDetailActivity extends AppCompatActivity {

    private TextView uuid = null;
    private TextView rssi = null;
    private TextView major = null;
    private TextView minor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ibeacon_detail);

        uuid = (TextView) findViewById(R.id.uuid);
        rssi = (TextView) findViewById(R.id.rssi);
        major = (TextView) findViewById(R.id.major);
        minor = (TextView) findViewById(R.id.minor);

        Beacon beacon = getIntent().getExtras().getParcelable(getString(R.string.extra_beacon));

        uuid.setText("UUID: " + beacon.getId1().toString());
        rssi.setText("RSSI:" + beacon.getRssi());
        major.setText("Major: " + beacon.getId2().toString());
        minor.setText("Minor: " + beacon.getId3().toString());
    }
}
