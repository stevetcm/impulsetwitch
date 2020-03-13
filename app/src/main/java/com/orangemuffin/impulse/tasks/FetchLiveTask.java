package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.orangemuffin.impulse.models.StreamInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.orangemuffin.impulse.utils.JSONParser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-03-17 */
public class FetchLiveTask extends AsyncTask<String, Void, List<StreamInfo>> {
    private FetchLiveCallback callback;
    private Context context;

    public FetchLiveTask(Context context, FetchLiveCallback callback) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected List<StreamInfo> doInBackground(String... strings) {
        String base_url = "https://api.twitch.tv/kraken/streams/followed?"
                + "oauth_token=" + LocalDataUtil.getAccessToken(context)
                + "&limit=" + strings[0] + "&offset=" + strings[1] + "&stream_type=live";

        List<StreamInfo> mResultList = new ArrayList<>();
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);
            JSONArray streamsArray = fullDataObject.getJSONArray("streams");

            for (int i = 0; i < streamsArray.length(); i++) {
                JSONObject streamObject = streamsArray.getJSONObject(i);
                StreamInfo current = JSONParser.getStreamInfo(streamObject);
                mResultList.add(current);
            }
        } catch (Exception e) {
            return null; //null represents oauth error
        }

        for (StreamInfo stream : mResultList) {
            Picasso.with(context).invalidate(Uri.parse(stream.getVideoPreviewUrl()));
        }

        return mResultList;
    }

    @Override
    protected void onPostExecute(List<StreamInfo> streams) {
        super.onPostExecute(streams);
        callback.onLiveFetched(streams);
    }

    public interface FetchLiveCallback {
        void onLiveFetched(List<StreamInfo> streams);
    }
}

