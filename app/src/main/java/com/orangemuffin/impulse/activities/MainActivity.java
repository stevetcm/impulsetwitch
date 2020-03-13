package com.orangemuffin.impulse.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.fragments.SearchFragment;
import com.orangemuffin.impulse.tabmanagers.MainTabManager;
import com.orangemuffin.impulse.tasks.CheckChannelFollowedTask;
import com.orangemuffin.impulse.tasks.CheckGameFollowedTask;
import com.orangemuffin.impulse.tasks.FollowChannelTask;
import com.orangemuffin.impulse.tasks.FollowGameTask;
import com.orangemuffin.impulse.tasks.UnfollowChannelTask;
import com.orangemuffin.impulse.tasks.UnfollowGameTask;
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.orangemuffin.impulse.utils.MeasurementUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Stack;


public class MainActivity extends AppCompatActivity {
    private final int ACTIVITY_LOGIN_ID = 1006;
    private boolean loginReturned = false;
    private final int ACTIVITY_SETTINGS_ID = 1002;
    private boolean settingsReturned = false;
    private final int ACTIVITY_LIVESTREAM_ID = 4367;

    private Toolbar mToolbar;
    private TextView toolbar_title;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;
    private View navbar_header;

    private int fragmentTop;
    private Stack<Integer> orientationStack = new Stack<>();
    private Stack<Fragment> customBackStack = new Stack<>();

    private Menu mainMenu;

    private boolean doubleBackToExitPressedOnce;

    private ImageView action_follow_icon;
    private boolean followStatus = false;

    private String currentGameString = null;
    private Bundle currentProfileBundle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(LocalDataUtil.setupThemeLayout(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar_title = (TextView) findViewById(R.id.toolbar_title);

        mFragmentManager = getSupportFragmentManager();
        String opening_page = LocalDataUtil.getOpeningPage(this);
        if (LocalDataUtil.getAccessToken(this).equals("NULL")) {

        }

        if (opening_page.equals("Featured Streams")) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.replace(R.id.content_frame, prepareFragmentType("Featured", new Bundle())).commit();
            fragmentTop = R.id.nav_featured;
        } else if (opening_page.equals("Top Streams")) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.replace(R.id.content_frame, prepareFragmentType("Top", new Bundle())).commit();
            fragmentTop = R.id.nav_top;

        } else if (opening_page.equals("Live Followed Streams")) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.replace(R.id.content_frame, prepareFragmentType("Live", new Bundle())).commit();
            fragmentTop = R.id.nav_live;
        } else if (opening_page.equals("Followed Channels")) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.replace(R.id.content_frame, prepareFragmentType("Followed", new Bundle())).commit();
            fragmentTop = R.id.nav_followed;
        } else if (opening_page.equals("Browse Games")) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.replace(R.id.content_frame, prepareFragmentType("Browse Games", new Bundle())).commit();
            fragmentTop = R.id.nav_browsegames;
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mNavigationView = (NavigationView) findViewById(R.id.navigationView);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                if (menuItem.getItemId() == R.id.nav_settings) {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivityForResult(intent, ACTIVITY_SETTINGS_ID);
                    overridePendingTransition(R.anim.slide_left, R.anim.anim_stay);
                } else if (menuItem.getItemId() != fragmentTop) {
                    customBackStack.clear(); //reset stack
                    orientationStack.clear(); //reset stack
                    clearFragmentBackStack(); //reset stack

                    currentGameString = null;
                    currentProfileBundle = null;

                    switchTabManager(menuItem.getItemId());
                }
                return true;
            }
        });
        mNavigationView.setCheckedItem(fragmentTop);
        navbar_header = mNavigationView.getHeaderView(0);
        navbar_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LocalDataUtil.getAccessToken(getApplicationContext()).equals("NULL")) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(intent, ACTIVITY_LOGIN_ID);
                    overridePendingTransition(R.anim.slide_up, R.anim.anim_stay);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater = getLayoutInflater();

                    View dialogView = inflater.inflate(R.layout.dialog_simple_text, null);
                    TextView login_text = (TextView) dialogView.findViewById(R.id.custom_text);
                    login_text.setText("Currently logged in as " + LocalDataUtil.getUserDisplayName(MainActivity.this)
                            + ". Do you want to log out?");

                    View dialogTitleView = inflater.inflate(R.layout.dialog_custom_title, null);
                    TextView custom_title = (TextView) dialogTitleView.findViewById(R.id.custom_title);
                    custom_title.setText("Twitch Account");

                    builder.setCustomTitle(dialogTitleView)
                            .setView(dialogView)
                            .setPositiveButton("LOG OUT", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    LocalDataUtil.setAccessToken(MainActivity.this, "NULL");
                                    LocalDataUtil.setUserDisplayName(MainActivity.this, "NULL");
                                    LocalDataUtil.setUserName(MainActivity.this, "NULL");
                                    LocalDataUtil.setUserId(MainActivity.this, "NULL");

                                    fragmentTop = R.id.nav_featured;
                                    currentGameString = null;
                                    currentProfileBundle = null;

                                    if (LocalDataUtil.getOpeningPage(MainActivity.this).equals("Live Followed Streams")
                                            || LocalDataUtil.getOpeningPage(MainActivity.this).equals("Followed Channels")) {
                                        LocalDataUtil.setOpeningPage(MainActivity.this, "Featured Streams");
                                    }

                                    recreate();
                                }
                            })
                            .setNegativeButton("CANCEL", null);
                    builder.create();

                    AlertDialog alertDialog = builder.show();
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                }
            }
        });

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            refreshNavBarUI();
        }

        checkNavBarItems(); //check navigation bar for logged in items

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        action_follow_icon = (ImageView) findViewById(R.id.action_follow_icon);

        if (LocalDataUtil.getNoticeTwitchAds(MainActivity.this)) {
            LocalDataUtil.setPromptFirst(MainActivity.this, MeasurementUtil.convertDateToString(Calendar.getInstance().getTime()));
            LocalDataUtil.setNoticeTwitchAds(MainActivity.this, false);
            displayTwitchAdsNotice();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    private MainTabManager prepareFragmentType(String type, Bundle bundle) {
        MainTabManager mainTabManager = new MainTabManager();

        bundle.putString("fragment_type", type);
        mainTabManager.setArguments(bundle);

        customBackStack.push(mainTabManager);
        return mainTabManager;
    }

    public void switchTabManager(int menuItemId) {
        switch (menuItemId) {
            case R.id.nav_featured:
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                mFragmentTransaction.replace(R.id.content_frame, prepareFragmentType("Featured", new Bundle())).commit();
                fragmentTop = R.id.nav_featured;
                break;
            case R.id.nav_top:
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                mFragmentTransaction.replace(R.id.content_frame, prepareFragmentType("Top", new Bundle())).commit();
                fragmentTop = R.id.nav_top;
                break;
            case R.id.nav_live:
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                mFragmentTransaction.replace(R.id.content_frame, prepareFragmentType("Live", new Bundle())).commit();
                fragmentTop =  R.id.nav_live;
                break;
            case R.id.nav_followed:
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                mFragmentTransaction.replace(R.id.content_frame, prepareFragmentType("Followed", new Bundle())).commit();
                fragmentTop = R.id.nav_followed;
                break;
            case R.id.nav_browsegames:
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                mFragmentTransaction.replace(R.id.content_frame, prepareFragmentType("Browse Games", new Bundle())).commit();
                fragmentTop = R.id.nav_browsegames;
                break;
            case R.id.nav_search:
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);

                Fragment tempFragment = new SearchFragment();

                mFragmentTransaction.replace(R.id.content_frame, tempFragment).commit();
                customBackStack.push(tempFragment);
                fragmentTop = R.id.nav_search;
                break;
        }
        toolbar_title.setText("Impulse");
        setFollowIconFunctionality(false, null, null, null);
        mNavigationView.setCheckedItem(fragmentTop);
    }

    public void switchToGameStreamsList(String gameName) {
        currentGameString = gameName;
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        mFragmentTransaction.hide(mFragmentManager.findFragmentById(R.id.content_frame))
                            .add(R.id.content_frame, prepareFragmentType(gameName, new Bundle())).addToBackStack(null).commit();

        orientationStack.push(getResources().getConfiguration().orientation);

        toolbar_title.setText("Game");
        setFollowIconFunctionality(true, null, null, gameName);
    }

    public void switchToChannelProfile(Bundle bundle) {
        currentProfileBundle = bundle;
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        mFragmentTransaction.hide(mFragmentManager.findFragmentById(R.id.content_frame))
                .add(R.id.content_frame, prepareFragmentType("Channel Profile", bundle)).addToBackStack(null).commit();

        orientationStack.push(getResources().getConfiguration().orientation);

        toolbar_title.setText(bundle.getString("display_name"));
        setFollowIconFunctionality(true, bundle.getString("channelId"), bundle.getString("display_name"), null);
    }

    public void setFollowIconFunctionality(boolean isVisible, String channelId, String display_name, String gameName) {
        if (isVisible) {
            if (channelId != null) {
                if (!LocalDataUtil.getAccessToken(this).equals("NULL")) {
                    action_follow_icon.setVisibility(View.VISIBLE);
                    new CheckChannelFollowedTask(this, new CheckChannelFollowedTask.CheckChannelFollowedCallBack() {
                        @Override
                        public void onChannelFollowedChecked(Boolean isFollowing) {
                            if (isFollowing) {
                                updateFollowIcon(followStatus = true);
                            }
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, channelId);
                    setFollowChannelIconClickListener(channelId, display_name);
                }
            } else {
                if (!LocalDataUtil.getAccessToken(this).equals("NULL")) {
                    action_follow_icon.setVisibility(View.VISIBLE);
                    new CheckGameFollowedTask(this, new CheckGameFollowedTask.CheckGameFollowedCallBack() {
                        @Override
                        public void onGameFollowedChecked(Boolean isFollowing) {
                            if (isFollowing) {
                                updateFollowIcon(followStatus = true);
                            }
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, gameName);
                    setFollowGameIconClickListener(gameName);
                }
            }
        } else {
            action_follow_icon.setVisibility(View.GONE);
            updateFollowIcon(followStatus = false);
        }
    }

    private void updateFollowIcon(boolean isFollowing) {
        if (isFollowing) {
            action_follow_icon.setImageResource(R.drawable.ic_favorite_white_24dp);
        } else {
            action_follow_icon.setImageResource(R.drawable.ic_favorite_border_white_24dp);
        }

        if (LocalDataUtil.getThemeName(this).equals("White Theme")) {
            action_follow_icon.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
    }

    public void setFollowChannelIconClickListener(final String channelId, final String display_name) {
        action_follow_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!followStatus) {
                    new FollowChannelTask(getApplicationContext(), new FollowChannelTask.FollowChannelCallBack() {
                        @Override
                        public void onFollowChannelSuccessful(Boolean onFollow) {
                            updateFollowIcon(followStatus = onFollow);
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, channelId);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater = getLayoutInflater();

                    View dialogView = inflater.inflate(R.layout.dialog_simple_text, null);
                    TextView unfollow_text = (TextView) dialogView.findViewById(R.id.custom_text);
                    unfollow_text.setText("Unfollow " + display_name + "?");

                    builder.setView(dialogView)
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new UnfollowChannelTask(getApplicationContext(), new UnfollowChannelTask.UnFollowChannelCallBack() {
                                        @Override
                                        public void onUnfollowChannelSuccessful(Boolean onUnfollow) {
                                            updateFollowIcon(followStatus = !onUnfollow);
                                        }
                                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, channelId);
                                }
                            })
                            .setNegativeButton("NO", null);
                    builder.create();

                    AlertDialog alertDialog = builder.show();
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                }
            }
        });
    }

    public void setFollowGameIconClickListener(final String gameName) {
        action_follow_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!followStatus) {
                    new FollowGameTask(getApplicationContext(), new FollowGameTask.FollowGameCallBack() {
                        @Override
                        public void onFollowGameSuccessful(Boolean onFollow) {
                            updateFollowIcon(followStatus = onFollow);
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, gameName);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater = getLayoutInflater();

                    View dialogView = inflater.inflate(R.layout.dialog_simple_text, null);
                    TextView unfollow_text = (TextView) dialogView.findViewById(R.id.custom_text);
                    unfollow_text.setText("Unfollow " + gameName + "?");

                    builder.setView(dialogView)
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new UnfollowGameTask(getApplicationContext(), new UnfollowGameTask.UnfollowGameCallBack() {
                                        @Override
                                        public void onUnfollowGameSuccessful(Boolean onUnfollow) {
                                            updateFollowIcon(followStatus = !onUnfollow);
                                        }
                                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, gameName);
                                }
                            })
                            .setNegativeButton("NO", null);
                    builder.create();

                    AlertDialog alertDialog = builder.show();
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                }
            }
        });
    }

    private void clearFragmentBackStack() {
        for(int i = 0; i < mFragmentManager.getBackStackEntryCount(); i++) {
            mFragmentManager.popBackStack();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_LOGIN_ID && data != null) {
            if (resultCode == Activity.RESULT_OK) {
                loginReturned = true; //hmm activity is not being created in time?
            }
        } else if (requestCode == ACTIVITY_SETTINGS_ID && data != null) {
            mNavigationView.setCheckedItem(fragmentTop);
            if (resultCode == Activity.RESULT_OK) {
                settingsReturned = true; //hmm activity is not being created in time?
            }
        } else if (requestCode == ACTIVITY_LIVESTREAM_ID && data != null) {
            if (resultCode == Activity.RESULT_OK) {
                //livestreamReturned = true; //hmm activity is not being created in time?
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("fragmentTop", fragmentTop);
        outState.putString("currentGameString", currentGameString);
        outState.putBundle("currentProfileBundle", currentProfileBundle);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int currentTop = savedInstanceState.getInt("fragmentTop");
        String currentGame = savedInstanceState.getString("currentGameString");
        Bundle currentProfile = savedInstanceState.getBundle("currentProfileBundle");

        switchTabManager(currentTop);
        getSupportFragmentManager().executePendingTransactions();

        if (currentGame != null && currentProfile != null) {
            switchToGameStreamsList(currentGame);
            getSupportFragmentManager().executePendingTransactions();
            switchToChannelProfile(currentProfile);
        } else if (currentGame == null && currentProfile != null) {
            switchToChannelProfile(currentProfile);
        } else if (currentGame != null && currentProfile == null) {
            switchToGameStreamsList(currentGame);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (loginReturned) {
            loginReturned = false;
            switchTabManager(R.id.nav_live);
            checkMenuIcon(mainMenu);
            checkNavBarItems();
        }
        if (settingsReturned) {
            settingsReturned = false;

            if (LocalDataUtil.getSettingsActivityLogout(this)) {
                LocalDataUtil.setSettingsActivityLogout(this, false);

                fragmentTop = R.id.nav_featured;
                currentGameString = null;
                currentProfileBundle = null;
            } else if (LocalDataUtil.getSettingsActivityLogin(this)) {
                LocalDataUtil.setSettingsActivityLogin(this, false);

                fragmentTop = R.id.nav_live;
                currentGameString = null;
                currentProfileBundle = null;
            }

            clearFragmentBackStack(); //reset stack to prepare for recreation
            recreate();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed() seems to be working fine on its own?
        if(mFragmentManager.getBackStackEntryCount() == 0) {
            if(mDrawerLayout.isDrawerOpen(GravityCompat.START) && !doubleBackToExitPressedOnce) {
                mDrawerLayout.closeDrawers();
            } else if (!doubleBackToExitPressedOnce) {
                mDrawerLayout.openDrawer(GravityCompat.START);
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

                //timer to reset double back pressed
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            } else {
                super.onBackPressed();
            }
        } else {
            if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawers();
            } else {
                mFragmentManager.popBackStack();

                if (customBackStack.pop().getArguments().getString("fragment_type").equals("Channel Profile")) {
                    currentProfileBundle = null; //leaving profile menu
                } else {
                    currentGameString = null; //else leaving game menu (can't be leaving main menu)
                }

                if (!orientationStack.isEmpty()) {
                    if (orientationStack.pop() != getResources().getConfiguration().orientation) {
                        refreshFragment();
                    }
                }

                //backed to game menu
                if (currentGameString != null) {
                    toolbar_title.setText("Game");
                } else { //else backed to main menu (can't be profile)
                    toolbar_title.setText("Impulse");
                }

                setFollowIconFunctionality(false, null, null, null);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (LocalDataUtil.getThemeName(this).equals("White Theme")) {
            menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_refresh_black_24dp));
        }
        mainMenu = menu;
        checkMenuIcon(mainMenu);
        return true;
    }

    public void checkMenuIcon(Menu menu) {
        MenuItem loginItem = menu.getItem(0); //get login text icon
        if (!LocalDataUtil.getAccessToken(this).equals("NULL")) {
            loginItem.setVisible(false);
        } else {
            loginItem.setVisible(true);
        }
    }

    public void checkNavBarItems() {
        if (!LocalDataUtil.getAccessToken(this).equals("NULL")) {
            ((TextView) navbar_header.findViewById(R.id.navbar_status)).setText("Logged in as " + LocalDataUtil.getUserDisplayName(this));

            try {
                ((ImageView) navbar_header.findViewById(R.id.navbar_icon)).setImageBitmap(LocalDataUtil.getImageFromStorage("U" + LocalDataUtil.getUserId(this), this));
            } catch (Exception e) { }

            mNavigationView.getMenu().findItem(R.id.nav_live).setVisible(true);
            mNavigationView.getMenu().findItem(R.id.nav_followed).setVisible(true);
        } else {
            if (LocalDataUtil.getThemeName(this).equals("White Theme")) {
                ((ImageView) navbar_header.findViewById(R.id.navbar_icon)).setImageResource(R.drawable.ic_logo_normal_black);
            }
            mNavigationView.getMenu().findItem(R.id.nav_live).setVisible(false);
            mNavigationView.getMenu().findItem(R.id.nav_followed).setVisible(false);
        }
    }

    private void refreshNavBarUI() {
        //S6 Display Specs: 2560 x 1440
        if (MeasurementUtil.dpToPixel(575) > MeasurementUtil.getRealHeight(MainActivity.this)) {
            navbar_header.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            ((RelativeLayout.LayoutParams) navbar_header.findViewById(R.id.navbar_info_container).getLayoutParams())
                    .addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        } else {
            navbar_header.getLayoutParams().height = MeasurementUtil.dpToPixel(171);
            ((RelativeLayout.LayoutParams) navbar_header.findViewById(R.id.navbar_info_container).getLayoutParams())
                    .addRule(RelativeLayout.CENTER_VERTICAL, 0);
        }
    }

    private void refreshFragment() {
        Fragment currentFragment = customBackStack.peek();
        if (currentFragment instanceof MainTabManager) {
            ((MainTabManager) currentFragment).recreateFragment();
        } else if (currentFragment instanceof SearchFragment) {
            ((SearchFragment) currentFragment).recreateFragment();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_refresh:
                Fragment currentFragment = mFragmentManager.findFragmentById(R.id.content_frame);
                if (currentFragment instanceof MainTabManager) {
                    ((MainTabManager) currentFragment).refreshRecyclerView();
                } else if (currentFragment instanceof SearchFragment) {
                    ((SearchFragment) currentFragment).refreshRecyclerView();
                }
                return true;
            case R.id.action_login:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, ACTIVITY_LOGIN_ID);
                overridePendingTransition(R.anim.slide_up, R.anim.anim_stay);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);

        refreshFragment();
        refreshNavBarUI();
    }

    private void displayTwitchAdsNotice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_simple_text, null);
        TextView notice_text = (TextView) dialogView.findViewById(R.id.custom_text);
        notice_text.setText("Twitch is now adding ads directly into the stream, so ads will most probably appear!");

        View dialogTitleView = inflater.inflate(R.layout.dialog_custom_title, null);
        TextView custom_title = (TextView) dialogTitleView.findViewById(R.id.custom_title);
        custom_title.setText("Notice");

        builder.setCustomTitle(dialogTitleView)
                .setView(dialogView)
                .setPositiveButton("OK", null);
        builder.create();

        AlertDialog alertDialog = builder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
    }
}
