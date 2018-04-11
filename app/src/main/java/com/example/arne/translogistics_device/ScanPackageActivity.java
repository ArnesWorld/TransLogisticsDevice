package com.example.arne.translogistics_device;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import com.example.arne.translogistics_device.DAL.AppDataBase;
import com.example.arne.translogistics_device.Model.Package;
import com.google.gson.Gson;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import static android.Manifest.permission.CAMERA;
import com.example.arne.translogistics_device.DAL.AppDataBase;

public class ScanPackageActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {


    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView mScannerView;

    private Button btnEnterPackageData;
    private AppDataBase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_package);

                //Initialize database
        database = AppDataBase.getInstance(getApplicationContext());

        btnEnterPackageData = findViewById(R.id.btnEnterPackageData);

        btnEnterPackageData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Layout dialog = (Layout) getResources().getLayout(R.layout.dialog_package_input);
                buildDialog(v);
            }
        });

        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Toast.makeText(getApplicationContext(), "Permission already granted", Toast.LENGTH_LONG).show();

            } else {
                requestPermission();
            }
        }
    }

    private boolean checkPermission() {
        return ( ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA ) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if(mScannerView == null) {
                    mScannerView = new ZXingScannerView(this);
                    setContentView(mScannerView);
                }
                mScannerView.setResultHandler((ZXingScannerView.ResultHandler) this);
                mScannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {

        Gson gson = new Gson();
        Package p1 = convertToJson(rawResult.toString());

        mScannerView.stopCamera();

        AppDataBase dataBase = AppDataBase.getInstance(getApplicationContext());
        int id = (int)dataBase.packageModel().insertPackage(p1);

        Intent intent = new Intent(this, RecordDataActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);

    }

    public Package convertToJson(String convertThis){

        Gson gson = new Gson();
        String json = convertThis;
        Package p1 = gson.fromJson(json, Package.class);

        return p1;
    }



    private void buildDialog(View v) {
        PackageDialogFragment packDialog = new PackageDialogFragment();
        packDialog.show(getSupportFragmentManager(), "package");

    }
}
