package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.orangemuffin.impulse.models.StreamInfo;
import com.orangemuffin.impulse.models.VODInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.JSONParser;
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2019-06-28 */
public class FetchRecentsTask extends AsyncTask<String, Void, List<VODInfo>> {
    private FetchRecentsCallback callback;
    private Context context;

    public FetchRecentsTask(Context context, FetchRecentsCallback callback) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected List<VODInfo> doInBackground(String... strings) {
        String base_url = "https://api.twitch.tv/kraken/videos/followed?"
                + "oauth_token=" + LocalDataUtil.getAccessToken(context)
                + "&broadcast_type=" + "archive" + "&limit=" + strings[0] + "&offset=" + strings[1];

        List<VODInfo> mResultList = new ArrayList<>();
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);

            JSONArray streamsArray = fullDataObject.getJSONArray("videos");

            for (int i = 0; i < streamsArray.length(); i++) {
                JSONObject streamObject = streamsArray.getJSONObject(i);
                if (!streamObject.getString("status").equals("recording")) {
                    VODInfo current = JSONParser.getVodInfo(streamObject);
                    mResultList.add(current);

                    Log.d("VOD LISTS", streamObject.getString("status") + " " + streamObject.getString("broadcast_type"));
                }
            }
        } catch (Exception e) {
            return null; //null represents oauth error
        }

        for (VODInfo stream : mResultList) {
            Picasso.with(context).invalidate(Uri.parse(stream.getVodPreviewUrl()));
        }

        return mResultList;
    }

    @Override
    protected void onPostExecute(List<VODInfo> streams) {
        super.onPostExecute(streams);
        callback.onRecentsFetched(streams);
    }

    public interface FetchRecentsCallback {
        void onRecentsFetched(List<VODInfo> streams);
    }
}
