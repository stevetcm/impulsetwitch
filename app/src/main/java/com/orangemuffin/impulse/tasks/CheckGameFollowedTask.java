package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.orangemuffin.impulse.utils.LocalDataUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/* Created by OrangeMuffin on 2018-12-16 */
public class CheckGameFollowedTask extends AsyncTask<String, Void, Boolean> {
    private final int USER_NOT_FOLLOWING_CODE = 404;

    private Context context;
    private CheckGameFollowedCallBack callBack;

    public CheckGameFollowedTask(Context context, CheckGameFollowedCallBack callBack) {
        this.context = context;
        this.callBack = callBack;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String base_url = "https://api.twitch.tv/api/users/" + LocalDataUtil.getUserName(context) + "/follows/games/" + strings[0];

        try {
            URL url = new URL(base_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Client-ID", "iq8r1k7dkxrs5vzi4j5iaoptdgeip6");
            conn.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
            conn.setRequestMethod("GET");
            conn.connect();

            int response = conn.getResponseCode();

            return response != USER_NOT_FOLLOWING_CODE;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean isFollowing) {
        super.onPostExecute(isFollowing);
        callBack.onGameFollowedChecked(isFollowing);
    }

    public interface CheckGameFollowedCallBack {
        void onGameFollowedChecked(Boolean isFollowing);
    }
}
