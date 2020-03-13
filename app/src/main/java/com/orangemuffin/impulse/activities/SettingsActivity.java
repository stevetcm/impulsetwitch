package com.orangemuffin.impulse.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.fragments.settings.AppearanceFragment;
import com.orangemuffin.impulse.fragments.settings.ChatSettingsFragment;
import com.orangemuffin.impulse.fragments.settings.PlayerSettingsFragment;
import com.orangemuffin.impulse.fragments.settings.SettingsFragment;
import com.orangemuffin.impulse.fragments.settings.SleepTimerFragment;
import com.orangemuffin.impulse.utils.LocalDataUtil;

/* Created by OrangeMuffin on 2018-03-22 */
public class SettingsActivity extends AppCompatActivity {

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private Toolbar mToolbar;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(LocalDataUtil.setupThemeLayout(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFragmentManager = getFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView, new SettingsFragment()).commit();

        getFragmentManager().executePendingTransactions();

        if (LocalDataUtil.getSettingsActivityRecreate(this)) { //need to redirect to appearance fragment
            LocalDataUtil.setSettingsActivityRecreate(this, false);

            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.hide(mFragmentManager.findFragmentById(R.id.containerView))
                    .add(R.id.containerView, new AppearanceFragment()).addToBackStack(null).commit();
        }
    }

    public void switchFragment(String key) {
        if (key.equals("player_screen")) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.hide(mFragmentManager.findFragmentById(R.id.containerView))
                    .add(R.id.containerView, new PlayerSettingsFragment()).addToBackStack(null).commit();
        } else if (key.equals("appearance_screen")) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.hide(mFragmentManager.findFragmentById(R.id.containerView))
                    .add(R.id.containerView, new AppearanceFragment()).addToBackStack(null).commit();
        } else if (key.equals("chat_screen")) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.hide(mFragmentManager.findFragmentById(R.id.containerView))
                    .add(R.id.containerView, new ChatSettingsFragment()).addToBackStack(null).commit();
        } else if (key.equals("sleep_timer_screen")) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.hide(mFragmentManager.findFragmentById(R.id.containerView))
                    .add(R.id.containerView, new SleepTimerFragment()).addToBackStack(null).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mFragmentManager = getFragmentManager();

        if (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.remove(mFragmentManager.findFragmentById(R.id.containerView)).commit();
            mFragmentManager.popBackStack();
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            mFragmentManager.popBackStack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        if (LocalDataUtil.getActivityRecreate(this)) {
            setResult(Activity.RESULT_OK, returnIntent);
            LocalDataUtil.setActivityRecreate(this, false);
        } else {
            setResult(Activity.RESULT_CANCELED, returnIntent);
        }

        super.finish();
        overridePendingTransition(0, R.anim.slide_right);
    }
}
