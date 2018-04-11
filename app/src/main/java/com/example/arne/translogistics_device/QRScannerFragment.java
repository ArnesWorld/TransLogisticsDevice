package com.example.arne.translogistics_device;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.arne.translogistics_device.DAL.AppDataBase;
import com.example.arne.translogistics_device.Model.Package;
import com.google.gson.Gson;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;


/**
 * A simple {@link Fragment} subclass.
 */
public class QRScannerFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView mScannerView;

    public QRScannerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Toast.makeText(getContext(), "Permission already granted", Toast.LENGTH_LONG).show();

            } else {
                requestPermission();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mScannerView = new ZXingScannerView(getContext());


        return mScannerView;
    }

    private boolean checkPermission() {
        return ( ContextCompat.checkSelfPermission(getContext(), CAMERA ) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if(mScannerView == null) {
                    mScannerView = new ZXingScannerView(getActivity());
                   // setContentView(mScannerView);
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
        AppDataBase dataBase = AppDataBase.getInstance(getContext());
        int id = (int)dataBase.packageModel().insertPackage(p1);

        Intent intent = new Intent(getActivity(), RecordDataActivity.class);
        intent.putExtra("packageId", id);
        startActivity(intent);

    }

    public Package convertToJson(String convertThis){

        Gson gson = new Gson();
        String json = convertThis;
        Package p1 = gson.fromJson(json, Package.class);

        return p1;
    }

    public void stopCamera(){
        mScannerView.stopCamera();
    }


}
