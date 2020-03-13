package com.orangemuffin.impulse.tasks;

import android.os.AsyncTask;

import com.orangemuffin.impulse.models.GameInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-03-18 */
public class FetchAllGamesTask extends AsyncTask<String, Void, List<GameInfo>> {
    private FetchAllGamesCallback callback;

    public FetchAllGamesTask(FetchAllGamesCallback callback) {
        this.callback = callback;
    }

    @Override
    protected List<GameInfo> doInBackground(String... strings) {
        String base_url = "https://api.twitch.tv/kraken/games/top?limit=" + strings[0] + "&offset=" + strings[1];

        List<GameInfo> mResultList = new ArrayList<>();
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);
            JSONArray gamesArray = fullDataObject.getJSONArray("top");

            for (int i = 0; i < gamesArray.length(); i++) {
                JSONObject topObject = gamesArray.getJSONObject(i);

                mResultList.add(JSONParser.getGameInfo(topObject));
            }
        } catch (Exception e) { }

        return mResultList;
    }

    @Override
    protected void onPostExecute(List<GameInfo> games) {
        super.onPostExecute(games);
        callback.onAllGamesFetched(games);
    }

    public interface FetchAllGamesCallback {
        void onAllGamesFetched(List<GameInfo> games);
    }
}
