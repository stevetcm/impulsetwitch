package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.orangemuffin.impulse.models.ChannelInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.orangemuffin.impulse.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-03-19 */
public class FetchFollowedTask extends AsyncTask<String, Void, List<ChannelInfo>> {
    private Context context;
    private FetchFollowedCallback callback;

    private int totalSize = -1;

    public FetchFollowedTask(Context context, FetchFollowedCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected List<ChannelInfo> doInBackground(String... strings) {

        String base_url = "https://api.twitch.tv/kraken/users/" + LocalDataUtil.getUserId(context) + "/follows/channels"
                + "?limit=" + strings[0] + "&offset=" + strings[1] + "&sortby=created_at";

        List<ChannelInfo> mResultList = new ArrayList<>();
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);

            totalSize = fullDataObject.getInt("_total");

            JSONArray followsArray = fullDataObject.getJSONArray("follows");

            for (int i = 0; i < followsArray.length(); i++) {
                JSONObject channelObject = followsArray.getJSONObject(i).getJSONObject("channel");

                mResultList.add(JSONParser.getChannelInfo(channelObject));
            }
        } catch (Exception e) { }
        return mResultList;
    }

    @Override
    protected void onPostExecute(List<ChannelInfo> channels) {
        super.onPostExecute(channels);
        callback.onFollowedFetched(channels);
    }

    public int getTotalSize() {
        return totalSize;
    }

    public interface FetchFollowedCallback {
        void onFollowedFetched(List<ChannelInfo> channels);
    }
}
