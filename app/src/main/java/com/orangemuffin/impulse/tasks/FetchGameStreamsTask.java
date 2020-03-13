package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.orangemuffin.impulse.models.StreamInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-03-18 */
public class FetchGameStreamsTask extends AsyncTask<String, Void, List<StreamInfo>> {
    private FetchGameStreamsCallback callback;
    private Context context;

    public FetchGameStreamsTask(Context context, FetchGameStreamsCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected List<StreamInfo> doInBackground(String... strings) {
        String gameName = strings[0];

        //convert symbols into ascii hex
        if (gameName.contains("&")) { gameName = gameName.replace("&", "%26"); }
        if (gameName.contains("+")) { gameName = gameName.replace("+", "%2B"); }

        String base_url = "https://api.twitch.tv/kraken/streams?game=" + gameName
                + "&limit=" + strings[1] + "&offset=" + strings[2];

        List<StreamInfo> mResultList = new ArrayList<>();
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);
            JSONArray gamesArray = fullDataObject.getJSONArray("streams");

            for (int i = 0; i < gamesArray.length(); i++) {
                JSONObject topObject = gamesArray.getJSONObject(i);

                mResultList.add(JSONParser.getStreamInfo(topObject));
            }
        } catch (Exception e) { }

        return mResultList;
    }

    @Override
    protected void onPostExecute(List<StreamInfo> streams) {
        super.onPostExecute(streams);
        callback.onGameStreamsFetched(streams);
    }

    public interface FetchGameStreamsCallback {
        void onGameStreamsFetched(List<StreamInfo> streams);
    }
}
