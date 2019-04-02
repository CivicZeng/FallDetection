package com.example.falldetection;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private Switch mSwitch;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.home_fragment, container, false);
        View view = inflater.inflate(R.layout.home_fragment, null);

        mSwitch = (Switch) view.findViewById(R.id.detection_switch);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
//                    Intent startIntent = new Intent(getContext(), SensorService.class);
//                    getContext().startService(startIntent);
                    SensorData.get(getContext()).running = true;
                } else {
//                    Intent stopIntent = new Intent(getContext(), SensorService.class);
//                    getContext().stopService(stopIntent);
                    SensorData.get(getContext()).running = false;
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }
}
