package com.polover.digipub.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.polover.digipub.Constants;
import com.polover.digipub.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment {

        private SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        if (key.equals(getString(R.string.key_measuring_unit))){
                            String preferenceValue = prefs.getString(getString(R.string.key_measuring_unit), Constants.DEFAULT_VALUES.MEASURING_UNIT);
                            int previousWarningTemperature =
                                    Integer.parseInt(prefs.getString(getString(R.string.key_warning_temperature), Constants.DEFAULT_VALUES.WARNING_TEMPERATURE));
                            if(Integer.parseInt(preferenceValue) == Constants.MEASURING_UNIT.CELSIUS){
                                int newWarningTemperature = (int)Math.round((previousWarningTemperature-32) / 1.8);
                                prefs.edit().putString("key_warning_temperature",Integer.toString(newWarningTemperature)).apply();
                            } else {
                                int newWarningTemperature = (int)(previousWarningTemperature*1.8 +32);
                                prefs.edit().putString("key_warning_temperature",Integer.toString(newWarningTemperature)).apply();
                            }
                        }
                    }
                };

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            //Should stay before registering the OnSharedPreferenceChangeListener
            setDefaultValues();

            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .registerOnSharedPreferenceChangeListener(listener);

            // Measuring unit change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_measuring_unit)));

            // Warning temperature change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_warning_temperature)));
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .unregisterOnSharedPreferenceChangeListener(listener);
        }

        private void setDefaultValues(){
            EditTextPreference warningTemperaturePref = (EditTextPreference) findPreference(getString(R.string.key_warning_temperature));
            ListPreference measuringUnitPref = (ListPreference) findPreference(getString(R.string.key_measuring_unit));
            if (warningTemperaturePref.getText() == null){
                warningTemperaturePref.setText(Constants.DEFAULT_VALUES.WARNING_TEMPERATURE);
            }
            if (measuringUnitPref.getValue() == null){
                measuringUnitPref.setValue(Constants.DEFAULT_VALUES.MEASURING_UNIT);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else if (preference instanceof EditTextPreference) {
                // Update the changed warning temperature summary to its new value
                preference.setSummary(stringValue + "°");
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };
}
