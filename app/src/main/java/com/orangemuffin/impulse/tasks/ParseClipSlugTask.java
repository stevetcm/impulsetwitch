package com.orangemuffin.impulse.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.orangemuffin.impulse.twitchapi.TwitchAPIService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/* Created by OrangeMuffin on 2018-04-22 */
public class ParseClipSlugTask extends AsyncTask<String, Void, HashMap<String, String>> {

    private ParseClipSlugCallback callback;

    public ParseClipSlugTask(ParseClipSlugCallback callback) {
        this.callback = callback;
    }

    @Override
    protected HashMap<String, String> doInBackground(String... strings) {
        HashMap<String, String> resultMap = new LinkedHashMap<>();
        try {
            String base_url = "https://clips.twitch.tv/api/v2/clips/" + strings[0] + "/status";
            String jsonObject = TwitchAPIService.twitchV5Request(base_url);

            JSONObject fullJSONObject = new JSONObject(jsonObject);

            JSONArray clipArray = fullJSONObject.getJSONArray("quality_options");
            for (int i = 0; i < clipArray.length(); i++) {
                JSONObject currentObject = clipArray.getJSONObject(i);
                String key = currentObject.getString("quality") + "p";
                String value = currentObject.getString("source");

                resultMap.put(key, value);
            }

            return resultMap;
        } catch (Exception e) { }

        return null;
    }

    @Override
    protected void onPostExecute(HashMap<String, String> clipUrls) {
        super.onPostExecute(clipUrls);
        callback.onClipSlugParsed(clipUrls);
    }

    public interface ParseClipSlugCallback {
        void onClipSlugParsed(HashMap<String, String> clipUrls);
    }
}
