package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.orangemuffin.impulse.utils.LocalDataUtil;

import java.net.HttpURLConnection;
import java.net.URL;

/* Created by OrangeMuffin on 2018-03-21 */
public class UnfollowGameTask extends AsyncTask<String, Void, Boolean> {
    private final int UNFOLLOW_SUCCESS = 204;
    private Context context;
    private UnfollowGameCallBack callback;

    public UnfollowGameTask(Context context, UnfollowGameCallBack callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(context, "Unfollowing...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String base_url = "https://api.twitch.tv/api/users/" + LocalDataUtil.getUserName(context) + "/follows/games/" + strings[0]
                + "?oauth_token=" + LocalDataUtil.getAccessToken(context);

        try {
            URL url = new URL(base_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Client-ID", "iq8r1k7dkxrs5vzi4j5iaoptdgeip6");
            conn.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
            conn.setRequestMethod("DELETE");

            int response = conn.getResponseCode();

            return response == UNFOLLOW_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean onUnfollow) {
        super.onPostExecute(onUnfollow);
        callback.onUnfollowGameSuccessful(onUnfollow);
    }

    public interface UnfollowGameCallBack {
        void onUnfollowGameSuccessful(Boolean onUnfollow);
    }
}
