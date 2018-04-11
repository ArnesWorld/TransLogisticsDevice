package com.example.arne.translogistics_device;


import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;

/**
 * Created by Arne on 22-02-2018.
 */

public class ShockMonitor implements SensorEventListener {

    public static int FORCE_THRESHOLD;
    private static final int TIME_THRESHOLD = 100;
    private static final int SHAKE_DURATION = 500;

    private float mLastX=-1.0f, mLastY=-1.0f, mLastZ=-1.0f;
    private long mLastTime;
    private long mLastShake;
    private float maxShock = 0;
    private SensorManager sensorManager;
    private Sensor sensor;

    private int shocksOverLimit;

    public ShockMonitor(Context context){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_GAME);

        String maxShockKey = context.getResources().getString(R.string.pref_max_shock_key);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        FORCE_THRESHOLD = Integer.parseInt(sharedPreferences.getString(maxShockKey, "1000"));
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)return;

        long now = System.currentTimeMillis();

        if((now-mLastTime)> TIME_THRESHOLD){

            long diff = now - mLastTime;
            float deltaAcceleration = Math.abs(event.values[SensorManager.DATA_X] + event.values[SensorManager.DATA_Y] + event.values[SensorManager.DATA_Z]
                    - mLastX - mLastY - mLastZ) / diff * 10000;
            if (deltaAcceleration > maxShock) maxShock = (int)deltaAcceleration;
            if (deltaAcceleration > FORCE_THRESHOLD && (now - mLastShake) > SHAKE_DURATION){

                shocksOverLimit++;
                mLastShake = now;
            }
            mLastTime = now;
            mLastX = event.values[SensorManager.DATA_X];
            mLastY = event.values[SensorManager.DATA_Y];
            mLastZ = event.values[SensorManager.DATA_Z];
        }
    }

    public float getMaxShock(){
        return maxShock;
    }

    public int getShocksOverLimit(){
        return shocksOverLimit;
    }

    public void resetValues(){
        maxShock = 0;
        shocksOverLimit = 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
