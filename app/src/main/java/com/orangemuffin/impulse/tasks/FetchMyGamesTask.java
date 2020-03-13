package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.orangemuffin.impulse.models.GameInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.JSONParser;
import com.orangemuffin.impulse.utils.LocalDataUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-12-07 */
public class FetchMyGamesTask extends AsyncTask<String, Void, List<GameInfo>> {
    private FetchMyGamesCallback callback;
    private Context context;

    public FetchMyGamesTask(Context context, FetchMyGamesCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected List<GameInfo> doInBackground(String... strings) {
        String base_url = "https://api.twitch.tv/api/users/" + LocalDataUtil.getUserName(context)
                + "/follows/games/live?limit=" + strings[0] + "&offset=" + strings[1];

        List<GameInfo> mResultList = new ArrayList<>();
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);

            JSONArray gamesArray = fullDataObject.getJSONArray("follows");

            for (int i = 0; i < gamesArray.length(); i++) {
                JSONObject followsObject = gamesArray.getJSONObject(i);

                mResultList.add(JSONParser.getGameInfo(followsObject));
            }
        } catch (Exception e) { }

        return mResultList;
    }

    @Override
    protected void onPostExecute(List<GameInfo> games) {
        super.onPostExecute(games);
        callback.onMyGamesFetched(games);
    }

    public interface FetchMyGamesCallback {
        void onMyGamesFetched(List<GameInfo> games);
    }
}
