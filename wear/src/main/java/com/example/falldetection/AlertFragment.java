package com.example.falldetection;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class AlertFragment extends Fragment {
    private static final String TAG = "AlertFragment";
    private static final long[] vibrationPattern = {0, 500, 500, 500, 500, 500, 500, 50, 250, 50, 250, 50, 500, 500, 500, 500, 500, 500, 500};
    private static final int vibrationRepeat = -1;

    private Button cancelButton;
    private Vibrator vibrator;
    private Timer timer;
    private SharedPreferences sharedPreferences;

    private boolean cancelAlert = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SensorData.get(getContext()).startHeartReader();
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        timer = new Timer();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alert_fragment, container, false);
//        View view = inflater.inflate(R.layout.alert_fragment, null);

        cancelButton = view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlert = false;
                vibrator.cancel();
                SensorData.get(getContext()).stopHeartReader();
                SensorData.get(getContext()).init();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        vibrator.vibrate(vibrationPattern, vibrationRepeat);
        SensorData.get(getContext()).startHeartReader();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!cancelAlert) {
                    alertContact();
                }
            }
        }, 5 * 1000);
    }

    @Override
    public void onStop() {
        super.onStop();
        vibrator.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SensorData.get(getContext()).stopHeartReader();
    }

    private void alertContact() {
        try {
            String tel = sharedPreferences.getString("pre_key_phone", null);
            Uri uri = Uri.parse("tel:" + tel);
            Intent intent = new Intent(Intent.ACTION_CALL, uri);
            getContext().startActivity(intent);

            SmsManager smsManager = SmsManager.getDefault();
            String msg = "摔倒了";
            Log.d(TAG, msg);
//            smsManager.sendTextMessage(tel, null, msg ,null, null);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}
