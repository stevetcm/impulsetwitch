package com.orangemuffin.impulse.fragments.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.activities.SettingsActivity;
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.rey.material.widget.Slider;

/* Created by OrangeMuffin on 2018-04-01 */
public class PlayerSettingsFragment extends PreferenceFragment {
    String[] qualities = {"Source", "720p60", "720p", "480p", "360p", "160p"};

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.player_settings);

        String live_wifi = LocalDataUtil.getDefaultQualityWifi(getActivity(), "live");
        String vod_wifi = LocalDataUtil.getDefaultQualityWifi(getActivity(), "vod");
        String clips_wifi = LocalDataUtil.getDefaultQualityWifi(getActivity(), "clips");

        final Preference wifiQualityKey = findPreference("wifi_quality_key");
        wifiQualityKey.setSummary(getDefaultQualitiesSummary(live_wifi, vod_wifi, clips_wifi));
        wifiQualityKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_default_quality, null);

                String live_wifi = LocalDataUtil.getDefaultQualityWifi(getActivity(), "live");
                String vod_wifi = LocalDataUtil.getDefaultQualityWifi(getActivity(), "vod");
                String clips_wifi = LocalDataUtil.getDefaultQualityWifi(getActivity(), "clips");

                final AppCompatSpinner liveSpinner = (AppCompatSpinner) dialogView.findViewById(R.id.livestream_spinner);
                ArrayAdapter<String> liveAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_element, qualities);
                liveSpinner.setAdapter(liveAdapter);

                final AppCompatSpinner vodSpinner = (AppCompatSpinner) dialogView.findViewById(R.id.vod_spinner);
                ArrayAdapter<String> vodAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_element, qualities);
                vodSpinner.setAdapter(vodAdapter);

                final AppCompatSpinner clipsSpinner = (AppCompatSpinner) dialogView.findViewById(R.id.clips_spinner);
                ArrayAdapter<String> clipsAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_element, qualities);
                clipsSpinner.setAdapter(clipsAdapter);

                for (int i = 0; i < qualities.length; i++) {
                    if (live_wifi.equals(qualities[i])) { liveSpinner.setSelection(i); }
                    if (vod_wifi.equals(qualities[i])) { vodSpinner.setSelection(i); }
                    if (clips_wifi.equals(qualities[i])) { clipsSpinner.setSelection(i); }
                }

                builder.setCustomTitle(setupDialogTitle("Default Quality WiFi"))
                       .setView(dialogView)
                       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               String live = liveSpinner.getSelectedItem().toString();
                               String vod = vodSpinner.getSelectedItem().toString();
                               String clips = clipsSpinner.getSelectedItem().toString();

                               LocalDataUtil.setDefaultQualityWifi(getActivity(), "live", live);
                               LocalDataUtil.setDefaultQualityWifi(getActivity(), "vod", vod);
                               LocalDataUtil.setDefaultQualityWifi(getActivity(), "clips", clips);

                               wifiQualityKey.setSummary(getDefaultQualitiesSummary(live, vod, clips));
                           }
                       })
                       .setNegativeButton("Cancel", null);

                builder.create();
                setupDialogColorTheme(builder.show());

                return true;
            }
        });

        String live_mobile = LocalDataUtil.getDefaultQualityMobile(getActivity(), "live");
        String vod_mobile = LocalDataUtil.getDefaultQualityMobile(getActivity(), "vod");
        String clips_mobile= LocalDataUtil.getDefaultQualityMobile(getActivity(), "clips");

        final Preference mobileQualityKey = findPreference("mobilie_quality_key");
        mobileQualityKey.setSummary(getDefaultQualitiesSummary(live_mobile, vod_mobile, clips_mobile));
        mobileQualityKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogContentView = inflater.inflate(R.layout.dialog_default_quality, null);

                String live_mobile = LocalDataUtil.getDefaultQualityMobile(getActivity(), "live");
                String vod_mobile = LocalDataUtil.getDefaultQualityMobile(getActivity(), "vod");
                String clips_mobile= LocalDataUtil.getDefaultQualityMobile(getActivity(), "clips");

                final AppCompatSpinner liveSpinner = (AppCompatSpinner) dialogContentView.findViewById(R.id.livestream_spinner);
                ArrayAdapter<String> liveAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_element, qualities);
                liveSpinner.setAdapter(liveAdapter);

                final AppCompatSpinner vodSpinner = (AppCompatSpinner) dialogContentView.findViewById(R.id.vod_spinner);
                ArrayAdapter<String> vodAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_element, qualities);
                vodSpinner.setAdapter(vodAdapter);

                final AppCompatSpinner clipsSpinner = (AppCompatSpinner) dialogContentView.findViewById(R.id.clips_spinner);
                ArrayAdapter<String> clipsAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_element, qualities);
                clipsSpinner.setAdapter(clipsAdapter);

                for (int i = 0; i < qualities.length; i++) {
                    if (live_mobile.equals(qualities[i])) { liveSpinner.setSelection(i); }
                    if (vod_mobile.equals(qualities[i])) { vodSpinner.setSelection(i); }
                    if (clips_mobile.equals(qualities[i])) { clipsSpinner.setSelection(i); }
                }

                builder.setCustomTitle(setupDialogTitle("Default Quality Mobile"))
                       .setView(dialogContentView)
                       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               String live = liveSpinner.getSelectedItem().toString();
                               String vod = vodSpinner.getSelectedItem().toString();
                               String clips = clipsSpinner.getSelectedItem().toString();

                               LocalDataUtil.setDefaultQualityMobile(getActivity(), "live", live);
                               LocalDataUtil.setDefaultQualityMobile(getActivity(), "vod", vod);
                               LocalDataUtil.setDefaultQualityMobile(getActivity(), "clips", clips);

                               mobileQualityKey.setSummary(getDefaultQualitiesSummary(live, vod, clips));
                           }
                       })
                       .setNegativeButton("Cancel", null);

                builder.create();
                setupDialogColorTheme(builder.show());

                return true;
            }
        });

        final Preference quickSeekKey = findPreference("quick_seek_key");
        quickSeekKey.setSummary(LocalDataUtil.getQuickSeekTime(getActivity()) + " Seconds");
        quickSeekKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_seek_time, null);

                final Slider slider = (Slider) dialogView.findViewById(R.id.seek_time_slider);
                slider.setValueRange(3, 65, false);
                slider.setValue(LocalDataUtil.getQuickSeekTime(getActivity()), false);

                builder.setCustomTitle(setupDialogTitle("Quick Seek Button Time"))
                        .setView(dialogView)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                LocalDataUtil.setQuickSeekTime(getActivity(), slider.getValue());

                                quickSeekKey.setSummary(slider.getValue() + " Seconds");
                            }
                        })
                        .setNegativeButton("Cancel", null);
                builder.create();
                setupDialogColorTheme(builder.show());

                return true;
            }
        });

        final CheckBoxPreference exoplayerKey = (CheckBoxPreference) findPreference("exoplayer_key");
        if (LocalDataUtil.getExoplayerStatus(getActivity())) {
            exoplayerKey.setChecked(true);
            exoplayerKey.setSummary("Enabled");
        }

        exoplayerKey.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    LocalDataUtil.setExoplayerStatus(getActivity(), (Boolean) newValue);
                    if ((Boolean) newValue) { exoplayerKey.setSummary("Enabled"); }
                    else { exoplayerKey.setSummary("Disabled"); }
                }
                return true;
            }
        });

        final CheckBoxPreference clunkyPiPKey = (CheckBoxPreference) findPreference("clunky_pip_key");
        if (!LocalDataUtil.getClunkyPiPStatus(getActivity())) {
            clunkyPiPKey.setChecked(false);
            clunkyPiPKey.setSummary("Disabled");
        }

        clunkyPiPKey.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    LocalDataUtil.setClunkyPiPStatus(getActivity(), (Boolean) newValue);
                    if ((Boolean) newValue) { clunkyPiPKey.setSummary("Enabled"); }
                    else { clunkyPiPKey.setSummary("Disabled"); }
                }
                return true;
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("category_player");
            preferenceCategory.removePreference(clunkyPiPKey);
        }

        Preference appearanceScreen = findPreference("sleep_timer_screen");
        appearanceScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((SettingsActivity) getActivity()).switchFragment("sleep_timer_screen");
                return true;
            }
        });
    }

    private String getDefaultQualitiesSummary(String live, String vod, String clips) {
        if (live.equals(vod) && live.equals(clips)) {
            return live;
        } else {
            return "Custom";
        }
    }

    private View setupDialogTitle(String title) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogTitleView = inflater.inflate(R.layout.dialog_custom_title, null);

        TextView custom_title = (TextView) dialogTitleView.findViewById(R.id.custom_title);
        custom_title.setText(title);


        return dialogTitleView;
    }

    private void setupDialogColorTheme(AlertDialog alertDialog) {
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
    }
}
