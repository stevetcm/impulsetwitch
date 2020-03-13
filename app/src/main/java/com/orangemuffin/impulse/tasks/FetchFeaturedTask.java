package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.orangemuffin.impulse.models.StreamInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.JSONParser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/* Created by OrangeMuffin on 2018-03-17 */
public class FetchFeaturedTask extends AsyncTask<String, Void, List<StreamInfo>> {
    private FetchFeaturedCallback callback;
    private Context context;

    public FetchFeaturedTask(Context context, FetchFeaturedCallback callback) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected List<StreamInfo> doInBackground(String... strings) {
        String base_url = "https://api.twitch.tv/kraken/streams/featured?"
                + "limit=" + strings[0] + "&offset=" + strings[1];

        List<StreamInfo> mResultList = new ArrayList<>();
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);
            JSONArray topFeaturedArray = fullDataObject.getJSONArray("featured");

            for (int i = 0; i < topFeaturedArray.length(); i++) {
                // Get all the JSON objects we need to get all the required data.
                JSONObject topObject = topFeaturedArray.getJSONObject(i);
                JSONObject streamObject = topObject.getJSONObject("stream");

                int streamPriority = topObject.getInt("priority");
                mResultList.add(JSONParser.getStreamInfo(streamObject, streamPriority));
            }
        } catch (Exception e) { }

        Collections.sort(mResultList, new Comparator<StreamInfo>() {
            @Override
            public int compare(StreamInfo streamInfo, StreamInfo streamInfo2) {
                if (streamInfo.getPriority() == streamInfo2.getPriority()) {
                    return 0;
                }
                return streamInfo.getPriority() < streamInfo2.getPriority() ? -1 : 1;
            }
        });

        for (StreamInfo stream : mResultList) {
            Picasso.with(context).invalidate(Uri.parse(stream.getVideoPreviewUrl()));
        }

        return mResultList;
    }

    @Override
    protected void onPostExecute(List<StreamInfo> streams) {
        super.onPostExecute(streams);
        callback.onFeaturedFetched(streams);
    }

    public interface FetchFeaturedCallback {
        void onFeaturedFetched(List<StreamInfo> streams);
    }
}
