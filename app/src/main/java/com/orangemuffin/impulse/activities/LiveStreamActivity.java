package com.orangemuffin.impulse.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Rational;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.fragments.ChatFragment;
import com.orangemuffin.impulse.fragments.StreamFragment;
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.orangemuffin.impulse.utils.MeasurementUtil;

/* Created by OrangeMuffin on 2018-03-18 */
public class LiveStreamActivity extends AppCompatActivity {
    private StreamFragment mStreamFragment;
    private ChatFragment mChatFragment;

    private FrameLayout chatFragmentLayout;

    private String lowestUrl;

    private boolean mBackstackLost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(LocalDataUtil.setupThemeLayout(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livestream);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            if (mStreamFragment == null) {
                mStreamFragment = new StreamFragment();

                Bundle bundle = new Bundle();
                bundle.putString("streamerName", getIntent().getStringExtra("streamerName"));
                bundle.putString("display_name", getIntent().getStringExtra("display_name"));
                bundle.putString("streamStatus", getIntent().getStringExtra("streamStatus"));
                bundle.putString("channelId", getIntent().getStringExtra("channelId"));
                bundle.putString("logoUrl", getIntent().getStringExtra("logoUrl"));
                bundle.putString("gameName", getIntent().getStringExtra("gameName"));
                bundle.putInt("viewCount", getIntent().getIntExtra("viewCount", 0));
                mStreamFragment.setArguments(bundle);
                fm.beginTransaction().replace(R.id.stream_fragment_container, mStreamFragment).commit();
            }

            if (mChatFragment == null) {
                mChatFragment = new ChatFragment();

                Bundle bundle = new Bundle();
                bundle.putString("streamerName", getIntent().getStringExtra("streamerName"));
                bundle.putString("channelId", getIntent().getStringExtra("channelId"));
                mChatFragment.setArguments(bundle);
                fm.beginTransaction().replace(R.id.chat_fragment_container, mChatFragment).commit();
            }

            chatFragmentLayout = (FrameLayout) findViewById(R.id.chat_fragment_container);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setupChatLandscape(chatFragmentLayout);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setupChatLandscape(chatFragmentLayout);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            resetChatLayout(chatFragmentLayout);

        }
        chatFragmentLayout.requestLayout();
    }

    private void setupChatLandscape(FrameLayout chatFragmentLayout) {
        RelativeLayout.LayoutParams chatFragmentParams = (RelativeLayout.LayoutParams) chatFragmentLayout.getLayoutParams();
        double chatWidth = LocalDataUtil.getChatWidth(this)/100.0;
        chatFragmentParams.width = (int) ((MeasurementUtil.getRealWidth(LiveStreamActivity.this))*(chatWidth));
        chatFragmentParams.addRule(RelativeLayout.BELOW, 0);
    }

    private void resetChatLayout(FrameLayout chatFragmentLayout) {
        RelativeLayout.LayoutParams chatFragmentParams = (RelativeLayout.LayoutParams) chatFragmentLayout.getLayoutParams();
        chatFragmentParams.addRule(RelativeLayout.BELOW, R.id.stream_fragment_container);
        chatFragmentParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
    }

    public void scrollRecyclerViewToBottom() {
        if (mChatFragment == null) {
            mChatFragment.scrollRecyclerViewToBottom();
        }
    }

    @Override
    public void onBackPressed() {
        if (mStreamFragment != null) {
            if (!mChatFragment.isEmoteKeyboardShown()) {
                if (mStreamFragment.backPressed()) {
                    mChatFragment.backPressed();
                    super.onBackPressed();
                }
            } else {
                mChatFragment.hideEmoteKeyboard();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mStreamFragment != null) {
                    super.onBackPressed();
                    mStreamFragment.backPressed();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setLowestUrl(String url) {
        lowestUrl = url;
    }

    @Override @TargetApi(26)
    public void finish() {
        if (mBackstackLost) {
            finishAndRemoveTask();
            startActivity(Intent.makeRestartActivityTask(new ComponentName(this, MainActivity.class)));
        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("streamUrl", lowestUrl);
            returnIntent.putExtra("channelId", getIntent().getStringExtra("channelId"));
            setResult(Activity.RESULT_OK, returnIntent);

            super.finish();
            overridePendingTransition(0, R.anim.slide_down);
        }
    }

    @Override @TargetApi(26)
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();

        if (canGoToPiPMode()) {
            Rational aspectRatio = new Rational(16,9);
            PictureInPictureParams params = new PictureInPictureParams.Builder().setAspectRatio(aspectRatio).build();
            enterPictureInPictureMode(params);
        }
    }

    @Override @TargetApi(26)
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (!isInPictureInPictureMode) {
            mBackstackLost = true;
        }
    }

    private boolean canGoToPiPMode() {
        return LocalDataUtil.getClunkyPiPStatus(getApplicationContext())
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && !(mStreamFragment.isAudioOnly() || mStreamFragment.isChatOnly());
    }
}
