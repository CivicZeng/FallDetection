package com.example.falldetection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.LinkedList;

public class SensorData {
    private static final String TAG = "SensorData";

    private static SensorData mSensorData;

    private SensorManager sensorManager;
    private Sensor accelSensor, gyroSensor, magnSeneor, heartRate;
    private double[] accelValue = new double[3];
    private double[] gyroValue = new double[3];
    private double[] magnValue = new double[3];
    private double heartRateValue;
    public double[] std = {0, 0, 0};
    public boolean alert = false;
    public boolean running = true;

    public LinkedList<double[]> list = new LinkedList<>();

    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    accelValue[0] = event.values[0];
                    accelValue[1] = event.values[1];
                    accelValue[2] = event.values[2];
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    gyroValue[0] = event.values[0];
                    gyroValue[1] = event.values[1];
                    gyroValue[2] = event.values[2];
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magnValue[0] = event.values[0];
                    magnValue[1] = event.values[1];
                    magnValue[2] = event.values[2];
                    break;
            }
//            String msg = accelXValue + "," + accelYValue + "," + accelZValue + ","
//                    + gyroXValue + "," + gyroYValue + "," + gyroZValue + ","
//                    + magnXValue + "," + magnYValue + "," + magnZValue + ","
//                    + System.currentTimeMillis()
//                    + "\r\n";
            synchronized (this) {
                list.add(new double[]{accelValue[0], accelValue[1], accelValue[2], gyroValue[0], gyroValue[1], gyroValue[2], magnValue[0], magnValue[1], magnValue[2], System.currentTimeMillis()});
                //TODO 替换std提高精度
                if (list.size() > 60) {
                    for (int i = 0; i < std.length; ++i)
                        std[i] = Math.sqrt(Math.pow(std[i], 2) + (Math.pow(gyroValue[i], 2) - Math.pow(list.remove()[3], 2)) / 60);
                } else {
                    for (int i = 0; i < std.length; ++i)
                        std[i] = Math.sqrt((Math.pow(std[i], 2) * (list.size() - 1) + Math.pow(gyroValue[i], 2)) / list.size());
                }
            }
//            Log.d(TAG, "sensor std: " + String.valueOf(std[0]));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private SensorEventListener heartRateListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                heartRateValue = event.values[0];
                Log.d("Heart", "Heart rate: " + String.valueOf(heartRateValue));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public static SensorData get(Context context) {
        if (mSensorData == null) {
            mSensorData = new SensorData(context);
        }
        return mSensorData;
    }

    public SensorData(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnSeneor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        heartRate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(listener, gyroSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(listener, magnSeneor, SensorManager.SENSOR_DELAY_UI);
    }

    public void startHeartReader() {
        Log.d("Heart", "startHeartReader");
        sensorManager.registerListener(heartRateListener, heartRate, SensorManager.SENSOR_DELAY_UI);
        Log.d("Heart", String.valueOf(heartRate.isWakeUpSensor()));
    }

    public void stopHeartReader() {
        Log.d("Heart", "stopHeartReader");
        sensorManager.unregisterListener(heartRateListener);
        Log.d("Heart", String.valueOf(accelSensor.isWakeUpSensor()));
    }

    public double getHeartRateValue() {
        return heartRateValue;
    }

    public void init() {
        for (int i = 0; i < std.length; ++i)
            std[i] = 0;
        list.clear();
        alert = false;
    }

    public void destroy() {
        sensorManager.unregisterListener(listener);
        sensorManager.unregisterListener(heartRateListener);
    }
}
