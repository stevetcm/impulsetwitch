package com.orangemuffin.impulse.tasks;

import android.os.AsyncTask;

import com.orangemuffin.impulse.models.VODInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.JSONParser;

import org.json.JSONObject;

/* Created by OrangeMuffin on 2018-04-28 */
public class CheckLiveChannelTask extends AsyncTask<String, Void, VODInfo> {
    private CheckLiveChannelCallback callback;

    public CheckLiveChannelTask(CheckLiveChannelCallback callback) {
        this.callback = callback;
    }

    @Override
    protected VODInfo doInBackground(String... strings) {
        String base_url = "https://api.twitch.tv/kraken/streams/" + strings[0];

        VODInfo mResult = null;
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);

            if (!fullDataObject.isNull("stream")) {
                mResult = JSONParser.parseLiveStreamObject(fullDataObject.getJSONObject("stream"));
            }
        } catch (Exception e) { }

        return mResult;
    }

    @Override
    protected void onPostExecute(VODInfo live) {
        super.onPostExecute(live);
        callback.onLiveChannelChecked(live);
    }

    public interface CheckLiveChannelCallback {
        void onLiveChannelChecked(VODInfo live);
    }
}