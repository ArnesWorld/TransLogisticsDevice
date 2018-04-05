package com.example.arne.translogistics_device;

import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

   // SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_shockmoniter);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        EditTextPreference maxShock = (EditTextPreference) findPreference(getResources().getString(R.string.pref_max_shock_key));
        maxShock.setSummary(maxShock.getText());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (pref instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) pref;
            editTextPreference.setSummary(sharedPreferences.getString(getResources().getString(R.string.pref_max_shock_key),""));
        }
    }
}
