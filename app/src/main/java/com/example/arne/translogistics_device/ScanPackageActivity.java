package com.example.arne.translogistics_device;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import com.example.arne.translogistics_device.DAL.AppDataBase;
import com.example.arne.translogistics_device.Model.Package;
import com.google.gson.Gson;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import static android.Manifest.permission.CAMERA;


public class ScanPackageActivity extends AppCompatActivity {


    private Button btnEnterPackageData;
    private QRScannerFragment qrFragmnet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_package);

        btnEnterPackageData = findViewById(R.id.btnEnterPackageData);

        btnEnterPackageData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDialog(v);
            }
        });
        qrFragmnet =(QRScannerFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentQRScanner);
    }

    private void buildDialog(View v) {
        qrFragmnet.stopCamera();
        PackageDialogFragment packDialog = new PackageDialogFragment(qrFragmnet);
        packDialog.show(getSupportFragmentManager(), "package");

    }
}
