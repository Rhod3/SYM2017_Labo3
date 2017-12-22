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

        //Setting the UI
        uuid = (TextView) findViewById(R.id.uuid);
        rssi = (TextView) findViewById(R.id.rssi);
        major = (TextView) findViewById(R.id.major);
        minor = (TextView) findViewById(R.id.minor);

        //Getting the element selected in the iBeaconActivity activity.
        Beacon beacon = getIntent().getExtras().getParcelable(getString(R.string.extra_beacon));

        uuid.setText(R.string.beacon_detail_uuid + beacon.getId1().toString());
        rssi.setText(R.string.beacon_detail_rssi + beacon.getRssi());
        major.setText(R.string.beacon_detail_major + beacon.getId2().toString());
        minor.setText(R.string.beacon_detail_minor + beacon.getId3().toString());
    }
}
