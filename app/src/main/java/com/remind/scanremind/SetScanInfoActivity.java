package com.remind.scanremind;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class SetScanInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_scan_info);


        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("SCAN CODE");
        integrator.initiateScan();



        Intent intent = new Intent(this.getIntent());
        String s = intent.getStringExtra("bbb");
        TextView textView = findViewById(R.id.textView);
        textView.setText(s);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                //Toast.makeText(getApplicationContext(), result.getContents(), Toast.LENGTH_SHORT).show();

                TextView textView2 = findViewById(R.id.textViewBarnum);
                textView2.setText(result.getContents());
            }
            else {
                Toast.makeText(getApplicationContext(), "No Result", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void setDateBtnOnClick() {
        Toast.makeText(getApplicationContext(), "date", Toast.LENGTH_SHORT).show();
    }
}
