package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.orangemuffin.impulse.models.ChannelInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-04-15 */
public class FetchSearchChannelsTask extends AsyncTask<String, Void, List<ChannelInfo>> {
    private Context context;
    private FetchSearchChannelsCallback callback;

    private int totalSize = -1;

    public FetchSearchChannelsTask(Context context, FetchSearchChannelsCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected List<ChannelInfo> doInBackground(String... strings) {
        String base_url = "https://api.twitch.tv/kraken/search/channels?query=" + strings[0]
                + "&limit=" + strings[1] + "&offset=" + strings[2];

        List<ChannelInfo> mResultList = new ArrayList<>();
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);

            totalSize = fullDataObject.getInt("_total");

            JSONArray channelsArray = fullDataObject.getJSONArray("channels");

            for (int i = 0; i < channelsArray.length(); i++) {
                JSONObject channelObject = channelsArray.getJSONObject(i);

                mResultList.add(JSONParser.getChannelInfo(channelObject));
            }
        } catch (Exception e) { }
        return mResultList;
    }

    @Override
    protected void onPostExecute(List<ChannelInfo> channels) {
        super.onPostExecute(channels);
        callback.onSearchChannelsFetched(channels);
    }

    public int getTotalSize() {
        return totalSize;
    }

    public interface FetchSearchChannelsCallback {
        void onSearchChannelsFetched(List<ChannelInfo> channels);
    }
}
