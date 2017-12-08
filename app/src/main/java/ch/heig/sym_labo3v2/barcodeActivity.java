package ch.heig.sym_labo3v2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;

public class barcodeActivity extends AppCompatActivity {

    private Button barocdeScannerButton;
    private TextView barcodeScannerResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        this.barocdeScannerButton = (Button) findViewById(R.id.barcodeScannerButton);
        this.barcodeScannerResult = (TextView) findViewById(R.id.barcodeResultTextView);

        barocdeScannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                barcodeScannerResult.setText(String.format("Format: %s, Content: %s", format, contents));
            } else if (resultCode == RESULT_CANCELED) {
                barcodeScannerResult.setText("Scanning canceled :(");
            }
        }
    }
}
