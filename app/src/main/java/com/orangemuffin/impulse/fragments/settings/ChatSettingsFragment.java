package com.orangemuffin.impulse.fragments.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.rey.material.widget.Slider;

/* Created by OrangeMuffin on 2019-05-04 */
public class ChatSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.chat_settings);

        final Preference chatWidthKey = findPreference("chat_width_key");
        chatWidthKey.setSummary(LocalDataUtil.getChatWidth(getActivity()) + "%");
        chatWidthKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_chat_width, null);

                final Slider slider = (Slider) dialogView.findViewById(R.id.chat_width_slider);
                slider.setValueRange(25, 60, false);
                slider.setValue(LocalDataUtil.getChatWidth(getActivity()), false);

                builder.setCustomTitle(setupDialogTitle("Chat Width in Landscape"))
                        .setView(dialogView)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                LocalDataUtil.setChatWidth(getActivity(), slider.getValue());

                                chatWidthKey.setSummary(slider.getValue() + "%");
                            }
                        })
                        .setNegativeButton("Cancel", null);
                builder.create();
                setupDialogColorTheme(builder.show());

                return true;
            }
        });

        final CheckBoxPreference chatSwipeStatus = (CheckBoxPreference) findPreference("chat_swipe_key");
        if (!LocalDataUtil.getChatSwipeStatus(getActivity())) {
            chatSwipeStatus.setChecked(false);
            chatSwipeStatus.setSummary("Disabled");
        }

        chatSwipeStatus.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    LocalDataUtil.setChatSwipeStatus(getActivity(), (Boolean) newValue);
                    if ((Boolean) newValue) { chatSwipeStatus.setSummary("Enabled"); }
                    else { chatSwipeStatus.setSummary("Disabled"); }
                }
                return true;
            }
        });

        final CheckBoxPreference bttvEmotesStatus = (CheckBoxPreference) findPreference("bttv_emotes_key");
        if (!LocalDataUtil.getBttvStatus(getActivity())) {
            bttvEmotesStatus.setChecked(false);
            bttvEmotesStatus.setSummary("Disabled");
        }

        bttvEmotesStatus.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    LocalDataUtil.setBttvStatus(getActivity(), (Boolean) newValue);
                    if ((Boolean) newValue) { bttvEmotesStatus.setSummary("Enabled"); }
                    else { bttvEmotesStatus.setSummary("Disabled"); }
                }
                return true;
            }
        });

        final CheckBoxPreference ffzEmotesStatus = (CheckBoxPreference) findPreference("ffz_emotes_key");
        if (!LocalDataUtil.getFfzStatus(getActivity())) {
            ffzEmotesStatus.setChecked(false);
            ffzEmotesStatus.setSummary("Disabled");
        }

        ffzEmotesStatus.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    LocalDataUtil.setFfzStatus(getActivity(), (Boolean) newValue);
                    if ((Boolean) newValue) { ffzEmotesStatus.setSummary("Enabled"); }
                    else { ffzEmotesStatus.setSummary("Disabled"); }
                }
                return true;
            }
        });
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
