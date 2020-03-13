package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.orangemuffin.impulse.models.ClipInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-04-21 */
public class FetchClipsTask extends AsyncTask<String, Void, List<ClipInfo>> {
    private Context context;
    private FetchClipsCallback callback;

    private String cursorId = null;

    public FetchClipsTask(Context context, FetchClipsCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected List<ClipInfo> doInBackground(String... strings) {
        String base_url = "https://api.twitch.tv/kraken/clips/top" + "?channel=" + strings[0]
                + "&limit=" + strings[1] + "&period=" + strings[2] + "&cursor=" + strings[3];

        List<ClipInfo> mResultList = new ArrayList<>();
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);

            cursorId = fullDataObject.getString("_cursor");
            if (cursorId.equals("")) {
                cursorId = "null";
            }

            JSONArray clipsArray = fullDataObject.getJSONArray("clips");

            for (int i = 0; i < clipsArray.length(); i++) {
                JSONObject clipObject = clipsArray.getJSONObject(i);

                mResultList.add(JSONParser.getClipInfo(clipObject));
            }
        } catch (Exception e) { }
        return mResultList;
    }

    @Override
    protected void onPostExecute(List<ClipInfo> clips) {
        super.onPostExecute(clips);
        callback.onClipsFetched(clips);
    }

    public String getCursorId() {
        return cursorId;
    }

    public interface FetchClipsCallback {
        void onClipsFetched(List<ClipInfo> clips);
    }
}
