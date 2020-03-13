package com.orangemuffin.impulse.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.orangemuffin.impulse.twitchapi.TwitchAPIService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/* Created by OrangeMuffin on 2018-03-20 */
public class FetchStreamTask extends AsyncTask<String, Void, HashMap<String, String>> {
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

    private FetchStreamCallback callback;

    public FetchStreamTask(FetchStreamCallback callback) {
        this.callback = callback;
    }

    @Override
    protected HashMap<String, String> doInBackground(String... strings) {
        String streamerName = strings[0].toLowerCase();
        try {
            String urlToRead = "https://api.twitch.tv/api/channels/" + streamerName + "/access_token/";
            String jsonObject = TwitchAPIService.twitchV5Request(urlToRead);

            JSONObject resultJSON = new JSONObject(jsonObject);
            String tokenString = resultJSON.getString("token");

            //convert symbols into ascii hex
            if (tokenString.contains("+")) { tokenString = tokenString.replace("+", "%2B"); }

            String sig = resultJSON.getString("sig");

            String streamUrl = String.format("http://usher.ttvnw.net/api/channel/hls/%s.m3u8" +
                    "?player=twitchweb&" +
                    "&token=%s" +
                    "&sig=%s" +
                    "&allow_audio_only=true" +
                    "adblock=true" +
                    "&player_backend=html5" +
                    "&allow_source=true" +
                    "&baking_bread=false" +
                    "&fast_bread=true" +
                    "allow_spectre=false" +
                    "&type=any" +
                    "&p=%s", streamerName, tokenString, sig, "" + new Random().nextInt(6));

            return parseM3U8(streamUrl);
        } catch (Exception e) { }
        return null;
    }

    @Override
    protected void onPostExecute(HashMap<String, String> streamUrls) {
        super.onPostExecute(streamUrls);
        callback.onStreamFetched(streamUrls);
    }

    protected HashMap<String, String> parseM3U8(String urlToRead) {
        String result = "";

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(urlToRead).build();
            Response response = client.newCall(request).execute();

            result = response.body().string();
        } catch (Exception e) { }

        LinkedHashMap<String, String> resultList = new LinkedHashMap<>();
        resultList.put(QUALITY_AUTO, urlToRead);

        ArrayList<String> qualitiesAvailable = new ArrayList<>(Arrays.asList(QUALITIES));
        for(String quality : qualitiesAvailable) {
            Pattern p = Pattern.compile("GROUP-ID=\"" + quality +"\",NAME=\"" + "([\\s\\S]*)\",AUTOSELECT[\\s\\S]*" + "\"" + quality + "\"\\n(http:\\/\\/\\S+)");
            Matcher m = p.matcher(result);

            if(m.find()) {
                resultList.put(m.group(1), m.group(2));
            }
        }

        return resultList;
    }

    public interface FetchStreamCallback {
        void onStreamFetched(HashMap<String, String> streamUrls);
    }
}