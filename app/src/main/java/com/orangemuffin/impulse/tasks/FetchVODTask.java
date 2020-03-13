package com.orangemuffin.impulse.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.orangemuffin.impulse.twitchapi.TwitchAPIService;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* Created by OrangeMuffin on 2018-03-23 */
public class FetchVODTask extends AsyncTask<String, Void, HashMap<String, String>> {
    public static final String QUALITY_AUTO = "auto";
    public static final String QUALITY_SOURCE = "chunked";
    public static final String QUALITY_1080p60 = "1080p60";
    public static final String QUALITY_720p60 = "720p60";
    public static final String QUALITY_720p30 = "720p30";
    public static final String QUALITY_480p30 = "480p30";
    public static final String QUALITY_360p30 = "360p30";
    public static final String QUALITY_160p30 = "160p30";
    public static final String QUALITY_AUDIO_ONLY = "audio_only";

    public static final String[] QUALITIES = { QUALITY_SOURCE,
            QUALITY_1080p60, QUALITY_720p60, QUALITY_720p30, QUALITY_480p30,
            QUALITY_360p30, QUALITY_160p30, QUALITY_AUDIO_ONLY };

    private FetchVODCallback callback;

    public FetchVODTask(FetchVODCallback callback) {
        this.callback = callback;
    }

    @Override
    protected HashMap<String, String> doInBackground(String... strings) {
        try {
            String base_url = "https://api.twitch.tv/api/vods/" + strings[0] + "/access_token";
            String jsonObject = TwitchAPIService.twitchV5Request(base_url);

            JSONObject resultJSON = new JSONObject(jsonObject);
            // Remove all backslashes from the returned string. We need the string to make a JSONObject
            String tokenString = resultJSON.getString("token").replaceAll("\\\\", "");
            String sig = resultJSON.getString("sig");

            String vodURL = String.format("http://usher.twitch.tv/vod/%s?nauthsig=%s&nauth=%s&allow_source=true", strings[0], sig, tokenString);

            return parseM3U8(vodURL);
        } catch (Exception e) { }
        return null;
    }

    @Override
    protected void onPostExecute(HashMap<String, String> vodUrls) {
        super.onPostExecute(vodUrls);
        callback.onVODFetched(vodUrls);
    }

    protected HashMap<String, String> parseM3U8(String urlToRead) {
        HttpURLConnection conn = null;
        Scanner in = null;
        String line;
        String result = "";

        try {
            URL url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(3000);
            conn.setRequestMethod("GET");
            in = new Scanner(new InputStreamReader(conn.getInputStream()));

            while(in.hasNextLine()) {
                line = in.nextLine();
                result += line + "\n";
            }

            in.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(in != null)
                in.close();
            if(conn != null)
                conn.disconnect();
        }

        LinkedHashMap<String, String> resultList = new LinkedHashMap<>();
        resultList.put(QUALITY_AUTO, urlToRead);

        ArrayList<String> qualitiesAvailable = new ArrayList<>(Arrays.asList(QUALITIES));
        for(String quality : qualitiesAvailable) {
            Pattern p = Pattern.compile("GROUP-ID=\"" + quality +"\",NAME=\"" + "([\\s\\S]*)\",AUTOSELECT[\\s\\S]*" + "\"" + quality + "\"\\n(http:\\/\\/\\S+)");
            Matcher m = p.matcher(result);

            if(m.find()) {
                if (quality.equals("chunked")) {
                    resultList.put(m.group(1) + " (Source)", m.group(2));
                } else {
                    resultList.put(m.group(1), m.group(2));
                }
            }
        }

        return resultList;
    }

    public interface FetchVODCallback {
        void onVODFetched(HashMap<String, String> vodUrls);
    }
}
