package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.orangemuffin.impulse.models.VODInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;

import org.json.JSONObject;

/* Created by OrangeMuffin on 2018-05-01 */
public class FetchVideoTask extends AsyncTask<String, Void, String> {
    private FetchVideoCallback callback;
    private String base_url;

    public FetchVideoTask(FetchVideoCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        base_url = "https://api.twitch.tv/kraken/videos/" + strings[0];
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);

            return fullDataObject.getString("length");
        } catch (Exception e) { }

        return null;
    }

    @Override
    protected void onPostExecute(String vodLength) {
        super.onPostExecute(vodLength);
        callback.onVideoFetched(vodLength);
    }

    public interface FetchVideoCallback {
        void onVideoFetched(String vodLength);
    }
}
