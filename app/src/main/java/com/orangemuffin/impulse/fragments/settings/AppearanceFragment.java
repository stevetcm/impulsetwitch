package com.orangemuffin.impulse.fragments.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
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
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.rey.material.widget.Slider;

/* Created by OrangeMuffin on 2018-08-13 */
public class AppearanceFragment extends PreferenceFragment {
    String[] pages_1 = {"Featured Streams", "Top Streams", "Browse Games"};
    String[] pages_2 = {"Featured Streams", "Top Streams", "Live Followed Streams", "Followed Channels", "Browse Games"};

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.appearance);

        Preference themeKey = findPreference("theme_key");
        themeKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String currentTheme = LocalDataUtil.getThemeName(getActivity());
                if (currentTheme.equals("Indigo Theme")) {
                    LocalDataUtil.setThemeName(getActivity(), "Twitch Theme");
                } else if (currentTheme.equals("Twitch Theme")) {
                    LocalDataUtil.setThemeName(getActivity(), "Dark Theme");
                } else if (currentTheme.equals("Dark Theme")) {
                    LocalDataUtil.setThemeName(getActivity(), "Black Theme");
                } else if (currentTheme.equals("Black Theme")) {
                    LocalDataUtil.setThemeName(getActivity(), "White Theme");
                } else if (currentTheme.equals("White Theme")) {
                    LocalDataUtil.setThemeName(getActivity(), "Indigo Theme");
                }
                LocalDataUtil.setActivityRecreate(getActivity(), true);
                LocalDataUtil.setSettingsActivityRecreate(getActivity(), true);
                getActivity().recreate();
                return true;
            }
        });

        final String opening_page = LocalDataUtil.getOpeningPage(getActivity());

        final Preference openingKey = findPreference("opening_key");
        openingKey.setSummary(opening_page);
        openingKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_opening_page, null);

                final AppCompatSpinner openingSpinner = (AppCompatSpinner) dialogView.findViewById(R.id.opening_page_spinner);

                ArrayAdapter<String> openingAdapter;
                if (LocalDataUtil.getAccessToken(getActivity()).equals("NULL")) {
                    openingAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_element, pages_1);
                } else {
                    openingAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_element, pages_2);
                }

                openingSpinner.setAdapter(openingAdapter);

                for (int i = 0; i < pages_2.length; i++) {
                    if (opening_page.equals(pages_2[i])) { openingSpinner.setSelection(i); }
                }

                builder.setCustomTitle(setupDialogTitle("Opening Page"))
                        .setView(dialogView)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String opening = openingSpinner.getSelectedItem().toString();

                                LocalDataUtil.setOpeningPage(getActivity(), opening);

                                openingKey.setSummary(opening);
                            }
                        })
                        .setNegativeButton("Cancel", null);

                builder.create();
                setupDialogColorTheme(builder.show());

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
