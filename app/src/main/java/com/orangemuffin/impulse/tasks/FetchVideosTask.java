package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.orangemuffin.impulse.models.VODInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.JSONParser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-03-22 */
public class FetchVideosTask extends AsyncTask<String, Void, List<VODInfo>> {
    private FetchVideosCallback callback;
    private String base_url;
    private Context context;
    private String channelId;

    public FetchVideosTask(Context context, String channelId, FetchVideosCallback callback) {
        this.callback = callback;
        this.context = context;
        this.channelId = channelId;
    }

    @Override
    protected List<VODInfo> doInBackground(String... strings) {
        base_url = "https://api.twitch.tv/kraken/channels/" + channelId + "/videos"
                + "?limit=" + strings[0] + "&offset=" + strings[1];

        List<VODInfo> mResultList = new ArrayList<>();
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);

            JSONObject fullDataObject = new JSONObject(jsonString);
            JSONArray videosArray = fullDataObject.getJSONArray("videos");

            for (int i = 0; i < videosArray.length(); i++) {
                JSONObject vodObject = videosArray.getJSONObject(i);
                VODInfo current = JSONParser.getVodInfo(vodObject);
                mResultList.add(current);
            }
        } catch (Exception e) { }

        for (VODInfo vod : mResultList) {
            Picasso.with(context).invalidate(Uri.parse(vod.getVodPreviewUrl()));
        }

        return mResultList;
    }

    @Override
    protected void onPostExecute(List<VODInfo> vods) {
        super.onPostExecute(vods);
        callback.onVideosFetched(vods);
    }

    public interface FetchVideosCallback {
        void onVideosFetched(List<VODInfo> vods);
    }


}
