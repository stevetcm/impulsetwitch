package com.orangemuffin.impulse.fragments.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.utils.LocalDataUtil;

/* Created by OrangeMuffin on 2019-07-04 */
public class SleepTimerFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sleep_timer_settings);

        final CheckBoxPreference home_screen_key = (CheckBoxPreference) findPreference("home_screen_key");
        if (!LocalDataUtil.getSleepTimerHome(getActivity())) {
            home_screen_key.setChecked(false);
        }

        home_screen_key.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    LocalDataUtil.setSleepTimerHome(getActivity(), (Boolean) newValue);
                }
                return true;
            }
        });

        final CheckBoxPreference lock_screen_key = (CheckBoxPreference) findPreference("lock_screen_key");
        if (!LocalDataUtil.getSleepTimerScreen(getActivity())) {
            lock_screen_key.setChecked(false);
        }

        lock_screen_key.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    LocalDataUtil.setSleepTimerScreen(getActivity(), (Boolean) newValue);
                }
                return true;
            }
        });
    }
}
