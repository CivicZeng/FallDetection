package com.example.falldetection;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.drawer.WearableNavigationDrawer;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends WearableActivity {
    private static final String TAG = "MainActivity";
    private static final String HANDLE_THREAD_NAME = "FallOver";
    private static final long[] vibrationPattern = {0, 500, 500, 500, 500, 500, 500, 50, 250, 50, 250, 50, 500, 500, 500, 500, 500, 500, 500};
    private static final int vibrationRepeat = -1;

    private static int REQUEST_CODE = 1;
    private static String[] permissions = new String[]{
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private Vibrator vibrator;
    private SensorData mSensorData;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private Button cancelAlert;
    private WearableNavigationDrawer mWearableNavigationDrawer;

    private void showAlertButton() {
        final Activity activity = this;
        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        cancelAlert.setBackgroundResource(R.drawable.cancel);
//                        cancelAlert.setText(String.valueOf(mSensorData.getHeartRateValue()));
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enables Always-on
        setAmbientEnabled();

        for (String permission : permissions)
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(permissions, REQUEST_CODE);

        mSensorData = new SensorData(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mWearableNavigationDrawer = (WearableNavigationDrawer) findViewById(R.id.top_navigation_drawer);
        mWearableNavigationDrawer.setAdapter(new NavigationAdapter(this));

        HomeFragment homeFragment = new HomeFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();

//        cancelAlert = (Button) findViewById(R.id.cancel_alert);
//        cancelAlert.setBackgroundColor(Color.BLACK);
//        cancelAlert.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                vibrator.cancel();
//                mSensorData.stopHeartReader();
//                mSensorData.init();
//                cancelAlert.setBackgroundColor(Color.BLACK);
//                cancelAlert.setText("");
//                cancelAlert.setClickable(false);
//            }
//        });
//        cancelAlert.setClickable(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String s : permissions) {
                if (ContextCompat.checkSelfPermission(this, s) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, REQUEST_CODE);
                }
            }
        }
        startBackgroundThread();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPause() {
        stopBackgroundThread();
        vibrator.cancel();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorData.destroy();
    }

    private final class NavigationAdapter extends WearableNavigationDrawer.WearableNavigationDrawerAdapter {
        private final Context mContext;

        NavigationAdapter(Context context) {
            mContext = context;
        }

        @Override
        public String getItemText(int i) {
            Log.d(TAG, "getItemTextId: " + String.valueOf(i));
            switch (i) {
                case 0:
                    return "SETTING";
                case 1:
                    return "CONTACT";
                case 2:
                    return "MAP";
                default:
                    return null;
            }
        }

        @Override
        public Drawable getItemDrawable(int i) {
            Log.d(TAG, "getItemDrawable: " + String.valueOf(i));
            switch (i) {
                case 0:
                    return getDrawable(R.drawable.switch_icon);
                case 1:
                    return getDrawable(R.drawable.contact_icon);
                case 2:
                    return getDrawable(R.drawable.map_icon);
                default:
                    return null;
            }
        }

        @Override
        public void onItemSelected(int i) {
            Log.d(TAG, "onItemSelected: " + String.valueOf(i));
            switch (i) {
                case 0:
                    HomeFragment homeFragment = new HomeFragment();
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
                    break;
                case 1:
                    ContactFragment settingFragment = new ContactFragment();
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, settingFragment).commit();
                    break;
                case 2:
                    MapFragment mapFragment = new MapFragment();
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, mapFragment).commit();
                    break;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        backgroundHandler.post(periodicCheck);
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundHandler = null;
            backgroundThread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Runnable periodicCheck =
            new Runnable() {
                @Override
                public void run() {
                    if (!mSensorData.alert) {
                        synchronized (this) {
                            for (double std : mSensorData.std) {
                                if (std > 3) {
                                    mSensorData.alert = true;
                                    alert();
                                }
                            }
                        }
                    }
                    backgroundHandler.post(periodicCheck);
                }
            };

    private void alert() {
        //TODO 通过平台推送消息
        Log.d(TAG, "alert");
        cancelAlert.setClickable(true);
        mSensorData.startHeartReader();
        showAlertButton();
        vibrator.vibrate(vibrationPattern, vibrationRepeat);
        if (mSensorData.getHeartRateValue() < 40 && checkMobileConnection()) {
            String tel = "";
            Uri uri = Uri.parse("tel:" + tel);
            Intent intent = new Intent(Intent.ACTION_CALL, uri);
            startActivity(intent);
        }
    }

    private boolean checkMobileConnection() {
        //TODO 检测手机是否连接
        Log.d(TAG, "checkMobileConnection");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bluetoothDevices) {
            Log.d(TAG, device.getName());
            Log.d(TAG, device.getAddress());
        }
        return true;
    }
}
