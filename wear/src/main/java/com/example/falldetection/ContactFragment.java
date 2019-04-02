package com.example.falldetection;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class ContactFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "ContactFragment";
    public static final String KEY_NAME = "pre_key_name";
    public static final String KEY_PHONE = "pre_key_phone";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.contact_fragment);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //TODO 从平台数据库获取默认联系人信息
        Log.d("Home", "ContactFragment.onSharedPreferenceChanged");
        Log.d("Home", key);
        switch (key) {
            case KEY_NAME:
                Preference namePre = findPreference(key);
                namePre.setSummary(sharedPreferences.getString(key, "曾思钰"));
                break;
            case KEY_PHONE:
                Preference phonePre = findPreference(key);
                phonePre.setSummary(sharedPreferences.getString(key, "13586563351"));
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
        Log.d(TAG, "onPause");
    }
}
