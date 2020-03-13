package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.orangemuffin.impulse.models.GameInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-08-15 */
public class FetchSearchGamesTask extends AsyncTask<String, Void, List<GameInfo>> {
    private Context context;
    private FetchSearchGamesCallback callback;

    private int totalSize = -1;

    public FetchSearchGamesTask(Context context, FetchSearchGamesCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected List<GameInfo> doInBackground(String... strings) {
        String base_url = "https://api.twitch.tv/kraken/search/games??type=suggest&query=" + strings[0];

        List<GameInfo> mResultList = new ArrayList<>();
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);

            //totalSize = fullDataObject.getInt("_total");

            JSONArray gamesArray = fullDataObject.getJSONArray("games");

            for (int i = 0; i < gamesArray.length(); i++) {
                JSONObject gameObject = gamesArray.getJSONObject(i);

                mResultList.add(JSONParser.getGameSearched(gameObject));
            }
        } catch (Exception e) { }
        return mResultList;
    }

    @Override
    protected void onPostExecute(List<GameInfo> games) {
        super.onPostExecute(games);
        callback.onSearchGamesFetched(games);
    }

    public int getTotalSize() {
        return totalSize;
    }

    public interface FetchSearchGamesCallback {
        void onSearchGamesFetched(List<GameInfo> games);
    }
}
