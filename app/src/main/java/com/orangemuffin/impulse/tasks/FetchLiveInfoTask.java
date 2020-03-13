package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.orangemuffin.impulse.models.StreamInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

public class FetchLiveInfoTask extends AsyncTask<String, Void, StreamInfo> {
    private FetchLiveInfoCallback callback;
    private Context context;

    public FetchLiveInfoTask(Context context, FetchLiveInfoCallback callback) {
        this.callback = callback;
        this.context = context;
    }


    @Override
    protected StreamInfo doInBackground(String... strings) {
        String base_url = "https://api.twitch.tv/kraken/streams/" + strings[0];

        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);

            JSONObject streamObject = fullDataObject.getJSONObject("stream");

            return JSONParser.getStreamInfo(streamObject);
        } catch (Exception e) { }

        return null;
    }

    @Override
    protected void onPostExecute(StreamInfo stream) {
        super.onPostExecute(stream);
        callback.onLiveInfoFetched(stream);
    }

    public interface FetchLiveInfoCallback {
        void onLiveInfoFetched(StreamInfo stream);
    }
}
