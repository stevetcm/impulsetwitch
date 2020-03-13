package com.orangemuffin.impulse.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.tasks.HandleUserTask;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.ConnectionUtil;
import com.orangemuffin.impulse.utils.LocalDataUtil;

/* Created by OrangeMuffin on 2018-03-17 */
public class LoginActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private WebView loginView;

    private LinearLayout linlaHeaderProgress;
    private ProgressBar pbHeaderProgress;

    private boolean status = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(LocalDataUtil.setupThemeLayout(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Twitch Login");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        pbHeaderProgress = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        if (LocalDataUtil.getThemeName(this).equals("Black Theme")) {
            pbHeaderProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black), PorterDuff.Mode.SRC_IN);
        }

        loginView = (WebView) findViewById(R.id.loginView);
        loginView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        loginView.getSettings().setSaveFormData(false);
        loginView.getSettings().setJavaScriptEnabled(true);
        loginView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        if (ConnectionUtil.isNetworkAvailable(this)) {
            loginView.loadUrl(TwitchAPIService.getOAuthUrl());
        } else {
            loginView.loadUrl("about:blank");
        }

        loginView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                linlaHeaderProgress.setVisibility(View.GONE);
                loginView.setVisibility(View.VISIBLE);

                if (url.contains("access_token")) {
                    loginView.setVisibility(View.GONE);
                    String startIdentifier = "access_token";
                    String endIdentifier = "&scope";

                    int startIndex = url.indexOf(startIdentifier) + startIdentifier.length() + 1;
                    int lastIndex = url.indexOf(endIdentifier);

                    String token = url.substring(startIndex, lastIndex);

                    LocalDataUtil.setAccessToken(getApplicationContext(), token);

                    status = true;

                    new HandleUserTask(getApplicationContext(), new HandleUserTask.HandleUserCallback() {
                        @Override
                        public void onUserHandled(boolean status) {
                            if (LocalDataUtil.getOpeningPage(LoginActivity.this).equals("Featured Streams")) {
                                LocalDataUtil.setOpeningPage(LoginActivity.this, "Live Followed Streams");
                            }
                            if (status) finish();
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
    }

    private void clearCookiesAndCache() {
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
        }
        else {
            cookieManager.removeAllCookie();
        }
        loginView.clearCache(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((ViewGroup) loginView.getParent()).removeView(loginView);
        loginView.removeAllViews();
        loginView.destroy();
    }

    @Override
    public void finish() {
        clearCookiesAndCache();

        if (status) {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
        }

        super.finish();
        overridePendingTransition(0, R.anim.slide_down);
    }
}
