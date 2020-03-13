package com.orangemuffin.impulse.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import com.orangemuffin.impulse.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/* Created by OrangeMuffin on 2018-03-17 */
public class LocalDataUtil {
    protected static final String ACCESS_TOKEN_LABEL = "ACCESS_TOKEN";

    protected static final String THEME_NAME_LABEL = "THEME_NAME";
    protected static final String ACTIVITY_RECREATE_LABEL = "THEME_STATUS";

    protected static final String EMOTE_STATUS_LABEL = "EMOTE_STATUS";
    protected static final String BADGE_STATUS_LABEL = "BADGE_STATUS";
    protected static final String BTTV_STATUS_LABEL = "BTTV_STATUS";
    protected static final String FFZ_STATUS_LABEL = "FFZ_STATUS";

    protected static final String EKEYBOARD_STATUS_LABEL = "EKEYBOARD_STATUS";
    protected static final String EKEYBOARD_RECENT_LABEL = "EKEYBOARD_RECENT";

    protected static final String DISPLAY_NAME_LABEL = "DISPLAY_NAME";
    protected static final String USER_NAME_LABEL = "USER_NAME";
    protected static final String USER_ID_LABEL = "USER_ID";

    protected static final String QUALITY_WIFI_LIVE_LABEL = "QUALITY_WIFI_LIVE";
    protected static final String QUALITY_WIFI_VOD_LABEL = "QUALITY_WIFI_VOD";
    protected static final String QUALITY_WIFI_CLIPS_LABEL = "QUALITY_WIFI_CLIPS";

    protected static final String QUALITY_MOBILE_LIVE_LABEL = "QUALITY_MOBILE_LIVE";
    protected static final String QUALITY_MOBILE_VOD_LABEL = "QUALITY_MOBILE_VOD";
    protected static final String QUALITY_MOBILE_CLIPS_LABEL = "QUALITY_MOBILE_CLIPS";

    protected static final String QUICK_SEEK_LABEL = "QUICK_SEEK";
    protected static final String CHAT_WIDTH_LABEL = "CHAT_WIDTH";

    protected static final String EXOPLAYER_STATUS_LABEL = "EXOPLAYER_STATUS";

    protected static final String SPINNER_STATUS_LABEL = "SPINNER_STATUS";
    protected static final String CHAT_LOADING_STATUS_LABEL = "CHAT_LOADING_STATUS";

    protected static final String SETTINGS_RECREATE_STATUS_LABEL = "SETTINGS_RECREATE_STATUS";
    protected static final String SETTINGS_LOGOUT_STATUS_LABEL = "SETTINGS_LOGOUT_STATUS";
    protected static final String SETTINGS_LOGIN_STATUS_LABEL = "SETTINGS_LOGIN_STATUS";

    protected static final String STREAM_IMAGE_SIZE_LABEL = "STREAM_IMAGE_SIZE";
    protected static final String CHANNEL_IMAGE_SIZE_LABEL = "CHANNEL_IMAGE_SIZE";
    protected static final String GAME_IMAGE_SIZE_LABEL = "GAME_IMAGE_SIZE";

    protected static final String CHAT_SWIPE_STATUS_LABEL = "CHAT_SWIPE_STATUS";

    protected static final String CROP_TO_FIT_LABEL = "CROP_TO_FIT";

    protected static final String NOTICE_TWITCH_ADS_STATUS = "NOTICE_TWITCH_ADS";

    protected static final String PROMPT_FIRST_DATE_STRING = "PROMPT_FIRST_DATE_STRING";

    protected static final String RECENT_EMOTES_JSON_STRING = "RECENT_EMOTES_JSON_STRING";

    protected static final String CLUNKY_PIP_STATUS = "CLUNKY_PIP";

    protected static final String OPENING_PAGE_STRING = "OPENING_PAGE_STRING";

    protected static final String SLEEP_TIMER_SET_STRING = "SLEEP_TIMER_SET_STRING";
    protected static final String SLEEP_TIMER_HOME_STRING = "SLEEP_TIMER_HOME_STRING";
    protected static final String SLEEP_TIMER_SCREEN_STRING = "SLEEP_TIMER_SCREEN_STRING";

    public static void setAccessToken(Context context, String accessToken) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(ACCESS_TOKEN_LABEL, accessToken);
        editor.apply();
    }

    public static String getAccessToken(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(ACCESS_TOKEN_LABEL, "NULL");
    }

    public static void setUserDisplayName(Context context, String displayName) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(DISPLAY_NAME_LABEL, displayName);
        editor.apply();
    }

    public static String getUserDisplayName(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(DISPLAY_NAME_LABEL, "NULL");
    }

    public static void setUserName(Context context, String name) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(USER_NAME_LABEL, name);
        editor.apply();
    }

    public static String getUserName(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(USER_NAME_LABEL, "NULL");
    }

    public static void setUserId(Context context, String id) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(USER_ID_LABEL, id);
        editor.apply();
    }

    public static String getUserId(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(USER_ID_LABEL, "NULL");
    }

    public static void setThemeName(Context context, String themeName) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(THEME_NAME_LABEL, themeName);
        editor.apply();
    }

    public static String getThemeName(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(THEME_NAME_LABEL, "Indigo Theme");
    }

    public static int setupThemeLayout(Context context) {
        String currentTheme = LocalDataUtil.getThemeName(context);
        if (currentTheme.equals("Indigo Theme")) {
            return R.style.IndigoTheme;
        } else if (currentTheme.equals("Dark Theme")) {
            return R.style.DarkTheme;
        } else if (currentTheme.equals("Black Theme")) {
            return R.style.BlackTheme;
        } else if (currentTheme.equals("White Theme")) {
            return R.style.WhiteTheme;
        } else if (currentTheme.equals("Twitch Theme")) {
            return R.style.TwitchTheme;
        }
        return -1;
    }

    public static void setEmotesStatus(Context context, boolean emoteStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(EMOTE_STATUS_LABEL, emoteStatus);
        editor.apply();
    }

    public static boolean getEmotesStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(EMOTE_STATUS_LABEL, false);
    }

    public static void setBadgeStatus(Context context, boolean badgeStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(BADGE_STATUS_LABEL, badgeStatus);
        editor.apply();
    }

    public static boolean getBadgeStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(BADGE_STATUS_LABEL, false);
    }

    public static void setBttvStatus(Context context, boolean bttvStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(BTTV_STATUS_LABEL, bttvStatus);
        editor.apply();
    }

    public static boolean getBttvStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(BTTV_STATUS_LABEL, true);
    }

    public static void setFfzStatus(Context context, boolean ffzStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(FFZ_STATUS_LABEL, ffzStatus);
        editor.apply();
    }

    public static boolean getFfzStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(FFZ_STATUS_LABEL, true);
    }

    public static void setEKeyboardStatus(Context context, boolean eKeyboardStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(EKEYBOARD_STATUS_LABEL, eKeyboardStatus);
        editor.apply();
    }

    public static boolean getEKeyboardStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(EKEYBOARD_STATUS_LABEL, false);
    }

    public static void setEkeyboardRecent(Context context, String eKeyboardRecent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(EKEYBOARD_RECENT_LABEL, eKeyboardRecent);
        editor.apply();
    }

    public static String getEkeyboardRecent(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(EKEYBOARD_RECENT_LABEL, "NULL");
    }

    public static void saveImageToStorage(Bitmap image, String fileName, Context context) {
        try {
            // Create an ByteArrayOutputStream and feed a compressed bitmap image in it
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, byteStream); // PNG as only format with transparency

            // Create a FileOutputStream with out key and set the mode to private to ensure
            // Only this app and read the file. Write out ByteArrayOutput to the file and close it
            FileOutputStream fileOut = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOut.write(byteStream.toByteArray());
            byteStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getImageFromStorage(String fileName, Context context) throws IOException {
        InputStream fileIn = context.openFileInput(fileName);
        return BitmapFactory.decodeStream(fileIn);
    }

    public static boolean doesStorageFileExist(String fileName, Context context){
        File file = context.getFileStreamPath(fileName);
        return file.exists();
    }

    public static void setVODProgress(Context context, String vodId, int progress) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("v" + vodId, progress);
        editor.apply();
    }

    public static int getVODProgress(Context context, String vodId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt("v" + vodId, 0);
    }

    public static void setActivityRecreate(Context context, boolean state) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(ACTIVITY_RECREATE_LABEL, state);
        editor.apply();
    }

    public static boolean getActivityRecreate(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(ACTIVITY_RECREATE_LABEL, false);
    }

    public static void setDefaultQualityWifi(Context context, String type, String quality) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        if (type.equals("live")) editor.putString(QUALITY_WIFI_LIVE_LABEL, quality);
        else if (type.equals("vod")) editor.putString(QUALITY_WIFI_VOD_LABEL, quality);
        else if (type.equals("clips")) editor.putString(QUALITY_WIFI_CLIPS_LABEL, quality);
        editor.apply();
    }

    public static String getDefaultQualityWifi(Context context, String type) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (type.equals("live")) return sp.getString(QUALITY_WIFI_LIVE_LABEL, "Source");
        else if (type.equals("vod")) return sp.getString(QUALITY_WIFI_VOD_LABEL, "Source");
        else if (type.equals("clips")) return sp.getString(QUALITY_WIFI_CLIPS_LABEL, "Source");

        return "Source";
    }

    public static void setDefaultQualityMobile(Context context, String type, String quality) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        if (type.equals("live")) editor.putString(QUALITY_MOBILE_LIVE_LABEL, quality);
        else if (type.equals("vod")) editor.putString(QUALITY_MOBILE_VOD_LABEL, quality);
        else if (type.equals("clips")) editor.putString(QUALITY_MOBILE_CLIPS_LABEL, quality);
        editor.apply();
    }

    public static String getDefaultQualityMobile(Context context, String type) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (type.equals("live")) return sp.getString(QUALITY_MOBILE_LIVE_LABEL, "Source");
        else if (type.equals("vod")) return sp.getString(QUALITY_MOBILE_VOD_LABEL, "Source");
        else if (type.equals("clips")) return sp.getString(QUALITY_MOBILE_CLIPS_LABEL, "Source");

        return "Source";
    }

    public static void setQuickSeekTime(Context context, int time) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(QUICK_SEEK_LABEL, time);
        editor.apply();
    }

    public static int getQuickSeekTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(QUICK_SEEK_LABEL, 10);
    }

    public static void setChatWidth(Context context, int width) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(CHAT_WIDTH_LABEL, width);
        editor.apply();
    }

    public static int getChatWidth(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(CHAT_WIDTH_LABEL, 35);
    }

    public static int getResIdFromAttribute(Activity activity, int attr) {
        if (attr == 0) {  return 0; }

        TypedValue typedvalueattr = new TypedValue();
        activity.getTheme().resolveAttribute(attr, typedvalueattr, true);
        return typedvalueattr.resourceId;
    }

    public static <K, V> LinkedHashMap<K, V> createLRUMap(final int maxEntries) {
        return new LinkedHashMap<K, V>(maxEntries*10/7, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public static void setChatLoadingStatus(Context context, boolean status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(CHAT_LOADING_STATUS_LABEL, status);
        editor.apply();
    }

    public static boolean getChatLoadingStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(CHAT_LOADING_STATUS_LABEL, false);
    }

    public static void setExoplayerStatus(Context context, boolean status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(EXOPLAYER_STATUS_LABEL, status);
        editor.apply();
    }

    public static boolean getExoplayerStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(EXOPLAYER_STATUS_LABEL, false);
    }

    public static void setSpinnerStatus(Context context, boolean status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SPINNER_STATUS_LABEL, status);
        editor.apply();
    }

    public static boolean getSpinnerStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SPINNER_STATUS_LABEL, false);
    }

    public static void setSettingsActivityRecreate(Context context, boolean status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SETTINGS_RECREATE_STATUS_LABEL, status);
        editor.apply();
    }

    public static boolean getSettingsActivityRecreate(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SETTINGS_RECREATE_STATUS_LABEL, false);
    }

    public static void setSettingsActivityLogout(Context context, boolean status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SETTINGS_LOGOUT_STATUS_LABEL, status);
        editor.apply();
    }

    public static boolean getSettingsActivityLogout(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SETTINGS_LOGOUT_STATUS_LABEL, false);
    }

    public static void setSettingsActivityLogin(Context context, boolean status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SETTINGS_LOGIN_STATUS_LABEL, status);
        editor.apply();
    }

    public static boolean getSettingsActivityLogin(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SETTINGS_LOGIN_STATUS_LABEL, false);
    }

    public static void setImageLayoutSize(Context context, String type, String size) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        if (type.equals("stream_image")) editor.putString(STREAM_IMAGE_SIZE_LABEL, size);
        else if (type.equals("channel_image")) editor.putString(CHANNEL_IMAGE_SIZE_LABEL, size);
        else if (type.equals("game_image")) editor.putString(GAME_IMAGE_SIZE_LABEL, size);
        editor.apply();
    }

    public static String getImageLayoutSize(Context context, String type) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (type.equals("stream_image")) return sp.getString(STREAM_IMAGE_SIZE_LABEL, "Large");
        else if (type.equals("channel_image")) return sp.getString(CHANNEL_IMAGE_SIZE_LABEL, "Large");
        else if (type.equals("game_image")) return sp.getString(GAME_IMAGE_SIZE_LABEL, "Large");

        return "Large";
    }

    public static void setChatSwipeStatus(Context context, boolean status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(CHAT_SWIPE_STATUS_LABEL, status);
        editor.apply();
    }

    public static boolean getChatSwipeStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(CHAT_SWIPE_STATUS_LABEL, true);
    }

    public static void setCropToFitStatus(Context context, boolean status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(CROP_TO_FIT_LABEL, status);
        editor.apply();
    }

    public static boolean getCropToFitStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(CROP_TO_FIT_LABEL, false);
    }

    public static void setNoticeTwitchAds(Context context, boolean status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(NOTICE_TWITCH_ADS_STATUS, status);
        editor.apply();
    }

    public static boolean getNoticeTwitchAds(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(NOTICE_TWITCH_ADS_STATUS, true);
    }

    public static void setPromptFirst(Context context, String strDate) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PROMPT_FIRST_DATE_STRING, strDate);
        editor.apply();
    }

    public static String getPromptFirst(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PROMPT_FIRST_DATE_STRING, "2019-03-29 11:52:35");
    }

    public static void setRecentEmotes(Context context, String json) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(RECENT_EMOTES_JSON_STRING, json);
        editor.apply();
    }

    public static String getRecentEmotes(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(RECENT_EMOTES_JSON_STRING, "NULL");
    }

    public static void setClunkyPiPStatus(Context context, boolean status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(CLUNKY_PIP_STATUS, status);
        editor.apply();
    }

    public static boolean getClunkyPiPStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(CLUNKY_PIP_STATUS, true);
    }

    public static void setOpeningPage(Context context, String page) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(OPENING_PAGE_STRING, page);
        editor.apply();
    }

    public static String getOpeningPage(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(OPENING_PAGE_STRING, "Featured Streams");
    }

    public static void setSleepTimerTime(Context context, int time) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SLEEP_TIMER_SET_STRING, time);
        editor.apply();
    }

    public static int getSleepTimerTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(SLEEP_TIMER_SET_STRING, 15);
    }

    public static void setSleepTimerHome(Context context, boolean homeStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SLEEP_TIMER_HOME_STRING, homeStatus);
        editor.apply();
    }

    public static boolean getSleepTimerHome(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SLEEP_TIMER_HOME_STRING, true);
    }

    public static void setSleepTimerScreen(Context context, boolean screenStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SLEEP_TIMER_SCREEN_STRING, screenStatus);
        editor.apply();
    }

    public static boolean getSleepTimerScreen(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SLEEP_TIMER_SCREEN_STRING, true);
    }
}
