package id.ac.stiki.doleno.digipub.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import id.ac.stiki.doleno.digipub.Constants;
import id.ac.stiki.doleno.digipub.R;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static id.ac.stiki.doleno.digipub.activities.SettingsActivity.bindPreferenceSummaryToValue;

public class MySettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_main, rootKey);
    }

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
        //Should stay before registering the OnSharedPreferenceChangeListener
        setDefaultValues();

        PreferenceManager.getDefaultSharedPreferences(requireActivity())
                .registerOnSharedPreferenceChangeListener(listener);

        // Measuring unit change listener
        bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference(getString(R.string.key_measuring_unit))));

        // Warning temperature change listener
        bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference(getString(R.string.key_warning_temperature))));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getDefaultSharedPreferences(requireActivity())
                .unregisterOnSharedPreferenceChangeListener(listener);
    }

    private void setDefaultValues(){
        EditTextPreference warningTemperaturePref = findPreference(getString(R.string.key_warning_temperature));
        assert warningTemperaturePref != null;
        warningTemperaturePref.setOnBindEditTextListener(new androidx.preference.EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            }
        });

        ListPreference measuringUnitPref = findPreference(getString(R.string.key_measuring_unit));
        if (warningTemperaturePref.getText() == null){
            warningTemperaturePref.setText(Constants.DEFAULT_VALUES.WARNING_TEMPERATURE);
        }
        if (measuringUnitPref.getValue() == null){
            measuringUnitPref.setValue(Constants.DEFAULT_VALUES.MEASURING_UNIT);
        }
    }
}
