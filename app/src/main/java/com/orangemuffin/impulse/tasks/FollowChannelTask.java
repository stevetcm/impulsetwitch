package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.orangemuffin.impulse.utils.LocalDataUtil;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/* Created by OrangeMuffin on 2018-03-21 */
public class FollowChannelTask extends AsyncTask<String, Void, Boolean> {
    private final int FOLLOW_UNSUCCESFUL = 422;
    private Context context;
    private FollowChannelCallBack callback;

    public FollowChannelTask(Context context, FollowChannelCallBack callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(context, "Following...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String base_url = "https://api.twitch.tv/kraken/users/" + LocalDataUtil.getUserId(context) + "/follows/channels/" + strings[0]
                + "?oauth_token=" + LocalDataUtil.getAccessToken(context);

        try {
            URL url = new URL(base_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Client-ID", "iq8r1k7dkxrs5vzi4j5iaoptdgeip6");
            conn.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
            conn.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write("Resource content");
            out.close();
            int response = conn.getResponseCode();

            return response != FOLLOW_UNSUCCESFUL;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean onFollow) {
        super.onPostExecute(onFollow);
        callback.onFollowChannelSuccessful(onFollow);
    }

    public interface FollowChannelCallBack {
        void onFollowChannelSuccessful(Boolean onFollow);
    }
}
