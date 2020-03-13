package com.orangemuffin.impulse.fragments.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.activities.LoginActivity;
import com.orangemuffin.impulse.activities.SettingsActivity;
import com.orangemuffin.impulse.receivers.MyAdminReceiver;
import com.orangemuffin.impulse.utils.LocalDataUtil;

/* Created by OrangeMuffin on 2018-03-29 */
public class SettingsFragment extends PreferenceFragment {
    private final int ACTIVITY_LOGIN_ID = 1006;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        Preference accountKey = findPreference("account_key");
        if (!LocalDataUtil.getAccessToken(getActivity()).equals("NULL")) {
            accountKey.setSummary("Logged in as " + LocalDataUtil.getUserDisplayName(getActivity()));
        }
        accountKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!LocalDataUtil.getAccessToken(getActivity()).equals("NULL")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_simple_text, null);

                    TextView login_text = (TextView) dialogView.findViewById(R.id.custom_text);
                    login_text.setText("Currently logged in as " + LocalDataUtil.getUserDisplayName(getActivity())
                            + ". Do you want to log out?");

                    View dialogTitleView = inflater.inflate(R.layout.dialog_custom_title, null);

                    TextView custom_title = (TextView) dialogTitleView.findViewById(R.id.custom_title);
                    custom_title.setText("Twitch Account");

                    builder.setCustomTitle(dialogTitleView)
                           .setView(dialogView)
                           .setPositiveButton("LOG OUT", new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface, int i) {
                                   LocalDataUtil.setAccessToken(getActivity(), "NULL");
                                   LocalDataUtil.setUserDisplayName(getActivity(), "NULL");
                                   LocalDataUtil.setUserName(getActivity(), "NULL");
                                   LocalDataUtil.setUserId(getActivity(), "NULL");

                                   LocalDataUtil.setActivityRecreate(getActivity(), true);
                                   LocalDataUtil.setSettingsActivityLogout(getActivity(), true);

                                   if (LocalDataUtil.getOpeningPage(getActivity()).equals("Live Followed Streams")
                                           || LocalDataUtil.getOpeningPage(getActivity()).equals("Followed Channels")) {
                                       LocalDataUtil.setOpeningPage(getActivity(), "Featured Streams");
                                   }

                                   getActivity().recreate();
                               }
                           })
                           .setNegativeButton("CANCEL", null);
                    builder.create();
                    setupDialogColorTheme(builder.show());
                } else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivityForResult(intent, ACTIVITY_LOGIN_ID);
                    getActivity().overridePendingTransition(R.anim.slide_up, R.anim.anim_stay);
                }
                return true;
            }
        });

        Preference appearanceScreen = findPreference("appearance_screen");
        appearanceScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((SettingsActivity) getActivity()).switchFragment("appearance_screen");
                return true;
            }
        });

        Preference chatScreen = findPreference("chat_screen");
        chatScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((SettingsActivity) getActivity()).switchFragment("chat_screen");
                return true;
            }
        });

        Preference playerScreen = findPreference("player_screen");
        playerScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((SettingsActivity) getActivity()).switchFragment("player_screen");
                return true;
            }
        });

        final CheckBoxPreference device_admin_key = (CheckBoxPreference) findPreference("device_admin_key");
        final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        final ComponentName compName = new ComponentName(getActivity(), MyAdminReceiver.class);

        boolean isDeviceAdmin = devicePolicyManager.isAdminActive(compName);
        if (isDeviceAdmin) {
            device_admin_key.setChecked(true);
            device_admin_key.setSummary("Enabled (Disable to Uninstall App)");
        }

        device_admin_key.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    if ((Boolean) newValue) {
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "If you would like to UNINSTALL this app, you will have to disable it from device administrators in:" +
                                "\nIn-app Settings > Device Administrator > Uncheck\nOR\nPhone's Settings > Security > Device Administrators > Uncheck the app");
                        startActivityForResult(intent, 11);
                    } else {
                        devicePolicyManager.removeActiveAdmin(compName);
                        device_admin_key.setSummary("Disabled");
                    }
                }
                return true;
            }
        });
    }

    private void setupDialogColorTheme(AlertDialog alertDialog) {
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_LOGIN_ID && data != null) {
            if (resultCode == Activity.RESULT_OK) {
                LocalDataUtil.setActivityRecreate(getActivity(), true);
                LocalDataUtil.setSettingsActivityLogin(getActivity(), true);

                getActivity().recreate();
            }
        } else if (requestCode == 11) {
            final CheckBoxPreference device_admin_key = (CheckBoxPreference) findPreference("device_admin_key");
            final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
            final ComponentName compName = new ComponentName(getActivity(), MyAdminReceiver.class);

            boolean isDeviceAdmin = devicePolicyManager.isAdminActive(compName);
            if (isDeviceAdmin) {
                device_admin_key.setChecked(true);
                device_admin_key.setSummary("Enabled (Disable to Uninstall App)");
            } else {
                device_admin_key.setChecked(false);
                device_admin_key.setSummary("Disabled");
            }
        }
    }
}
