package com.example.arne.translogistics_device;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arne.translogistics_device.DAL.AppDataBase;
import com.example.arne.translogistics_device.Model.DataRecording;
import com.example.arne.translogistics_device.Model.DataSegment;
import com.example.arne.translogistics_device.Model.Package;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RecordDataActivity extends AppCompatActivity {

    private final int LOCATION_REQUEST_CODE = 10;
    private static int recordingInterval;

    private Handler handler = new Handler();
    private ShockMonitor shockMonitor;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastKnownLocation;
    private Chronometer chronometer;
    private Button btnStartStop;
    private TextView txtSstartTime, txtPackId, txtShockLimit, txtRecInt;
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private boolean running = false;
    private FrameLayout workAnimation;

    private SharedPreferences sharedPreferences;
    private String maxShockKey;
    private String recIntKey;
    private AppDataBase database;
    //Model objects
    private DataRecording dataRecording;
    private Package aPackage;
    private int packageId;

    private LocationCallback locationCallback;
    private LocationRequest locationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_data);
        if(savedInstanceState != null){
            packageId = savedInstanceState.getInt("packageId");
        }
        else{
            //Get package from intent
            packageId = getIntent().getIntExtra("packageId", -1);
        }
        //Initialize database
        database = AppDataBase.getInstance(getApplicationContext());


        sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        maxShockKey = getResources().getString(R.string.pref_max_shock_key);
        recIntKey = getResources().getString(R.string.pref_record_interval_key);

        //Initialize monitor-objects
        shockMonitor = new ShockMonitor(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
         setUpLocationRequest();
         setLastKnownLocation();
        //Initialize layout-objects
        txtPackId = findViewById(R.id.txtPackIdRecData);
        txtPackId.setText(String.valueOf(packageId));
        txtShockLimit = findViewById(R.id.txtShockLimit);
        txtShockLimit.setText(sharedPreferences.getString(maxShockKey, "1000"));
        txtRecInt = findViewById(R.id.txtRecInt);
        txtRecInt.setText(sharedPreferences.getString(recIntKey, "10"));
        btnStartStop = findViewById(R.id.btnStartRecording);
        txtSstartTime = findViewById(R.id.txtStartTime);
        chronometer = findViewById(R.id.timer);
        workAnimation = findViewById(R.id.workAnimation);
        workAnimation.setVisibility(View.INVISIBLE);
        //set event-handlers
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!running) {
                    startRecordingData();
                } else {
                    stopRecordingData();
                }
            }
        });
    }

    private void setUpLocationRequest() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                lastKnownLocation = locationResult.getLastLocation();

                Toast.makeText(getApplicationContext(), "New Location: " + lastKnownLocation.getLatitude() + " : " + lastKnownLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            }
        };
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(5000);
        //locationRequest.setSmallestDisplacement(20);

        startLocationUpdates();
    }
    //This is done via requestLocationUpdate

    private void startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, null);
        }else {
            String[] permissionRequest = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissionRequest, LOCATION_REQUEST_CODE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        txtShockLimit.setText(sharedPreferences.getString(maxShockKey, "1000"));
        txtRecInt.setText(sharedPreferences.getString(recIntKey, "10"));

    }

    private void setUpDummyPackage() {
        // clear database
      /*  database.dataSegmentModel().deleteAll();
        database.dataRecordingModel().deleteAll();
        database.packageModel().deleteAll();*/
        Package pack = new Package("Grapes", 500, "The Grape Company A/S", "London", "New York", "UPS");
        packageId = (int)database.packageModel().insertPackage(pack);
    }

    private void setLastKnownLocation() {
        //Here we ask if we'e granted permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        lastKnownLocation = location;
                        Toast.makeText(getApplicationContext(), "New Location: " + lastKnownLocation.getLatitude() + " : " + lastKnownLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else { // Else we ask for for it
            String[] permissionRequest = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissionRequest, LOCATION_REQUEST_CODE);
        }
    }

    private void startRecordingData() {
        //Get recording interval from sharedPreferences
        recordingInterval = Integer.parseInt(sharedPreferences.getString(getResources().getString(R.string.pref_record_interval_key), "5000"));
        //Initialize DataRecording object
        initDataRecording();
        //Start to chronometer
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        //Change button color and text
        btnStartStop.setBackgroundColor(Color.parseColor("#c62553"));
        btnStartStop.setText(R.string.btnStopRecording);
        //Get and Set start time and set start time text label
        String startTimeString = formatter.format(dataRecording.getStartTime());
        txtSstartTime.setText(startTimeString);
        //Start animation
        workAnimation.setVisibility(View.VISIBLE);
        //Start the handler
        handler.postDelayed(recordDataTask, 1000);
        //running is now true
        running = true;
    }

    private void initDataRecording() {
        dataRecording = new DataRecording();
        //Set start time
        Date startTime = Calendar.getInstance().getTime();
        dataRecording.setStartTime(startTime);
        //Set settings data from sharedpreferences
        dataRecording.setMaxShockLimit(Integer.parseInt(sharedPreferences.getString(maxShockKey, "1000")));
        dataRecording.setRecordingIntervals(recordingInterval);
        //Set reference to package
        dataRecording.setPackageId(packageId);
        //Insert DataRecording object to database og get id
        int id = (int)database.dataRecordingModel().insertDataRecording(dataRecording);
        dataRecording.setId(id);
        Log.i("DataRecordingActivity", "#########################DATARECORDING ID: " +dataRecording.getId());
    }

    private void stopRecordingData() {
        //Set button color and text
        btnStartStop.setBackgroundColor(Color.parseColor("#4adb48"));
        btnStartStop.setText(R.string.btnStartRecording);
        //Stop chronometer
        chronometer.stop();

        //Stop handler and remove pending calls
        handler.removeCallbacks(recordDataTask);
        handler = null;
        //Set stop time on DataRecording object
        dataRecording.setEndTime(Calendar.getInstance().getTime());
        //Update database with endTime
        database.dataRecordingModel().updateDataRecording(dataRecording);
        //Stop animation
        workAnimation.setVisibility(View.INVISIBLE);
        //it ain't running no more
        running = false;
        //stop location request
        mFusedLocationClient.removeLocationUpdates(locationCallback);
        startConfirmationDialog();
       // launchDisplayDataActivity();
    }

    private void startConfirmationDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sure You want to save data recording?")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                      launchDisplayRecordingsActivity();
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Delete current recording and Package
                        database.dataSegmentModel().deleteDataSegmentByRecordingId(dataRecording.getId());
                        database.dataRecordingModel().deleteDataRecordingById(dataRecording.getId());
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void launchDisplayRecordingsActivity() {
        Intent intent = new Intent(getApplicationContext(), DisplayRecordingsActivity.class);
        intent.putExtra("dataRecordingId", dataRecording.getId());
        startActivity(intent);
    }

    private Runnable recordDataTask = new Runnable() {
        @Override
        public void run() {
            //Add shock-data and location
            new InsertDataSegmentToDBTask().execute();
            if (handler != null) {
                handler.postDelayed(this, recordingInterval);
            }
        }
    };


    @Override
    protected void onDestroy() {
        AppDataBase.destroyInstance();
        super.onDestroy();
    }

    private class InsertDataSegmentToDBTask extends AsyncTask<Void,Void,DataSegment>{


        @Override
        protected DataSegment doInBackground(Void... voids) {
            Date timeOfRecording = Calendar.getInstance().getTime();
          //  setLastKnownLocation();
            DataSegment dataSegment = new DataSegment(timeOfRecording, lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
                    shockMonitor.getMaxShock(), shockMonitor.getShocksOverLimit(), dataRecording.getId());
            database.dataSegmentModel().insertDataSegment(dataSegment);
            shockMonitor.resetValues();
            return dataSegment;
        }

        @Override
        protected void onPostExecute(DataSegment dataSegment) {
            super.onPostExecute(dataSegment);
            Log.i("MyActivity", dataSegment.toString());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("packageId", packageId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recdata_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }





}
