package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.ConnectionUtil;
import com.orangemuffin.impulse.utils.LocalDataUtil;

import org.json.JSONObject;

/* Created by OrangeMuffin on 2018-03-23 */
public class HandleUserTask extends AsyncTask<Void, Void, Boolean> {
    private HandleUserCallback callback;
    private Context context;
    private String base_url;

    public HandleUserTask(Context context, HandleUserCallback callback) {
        this.callback = callback;
        this.context = context;
        base_url = "https://api.twitch.tv/kraken/user?oauth_token=" + LocalDataUtil.getAccessToken(context);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullJSONObject = new JSONObject(jsonString);

            LocalDataUtil.setUserDisplayName(context, fullJSONObject.getString("display_name"));

            String user_id = fullJSONObject.getString("_id");
            LocalDataUtil.setUserId(context,user_id);

            LocalDataUtil.setUserName(context, fullJSONObject.getString("name"));

            String logoUrl = fullJSONObject.getString("logo");
            LocalDataUtil.saveImageToStorage(ConnectionUtil.getBitmapFromUrl(logoUrl), "U" + fullJSONObject.getString("_id"), context);

            return true;
        } catch (Exception e) { }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean status) {
        super.onPostExecute(status);
        callback.onUserHandled(status);
    }

    public interface HandleUserCallback {
        void onUserHandled(boolean status);
    }
}
