package com.orangemuffin.impulse.twitchapi;

import android.util.Log;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/* Created by OrangeMuffin on 2018-03-17 */
public class TwitchAPIService {
    private String LOG_TAG = getClass().getName();

    protected static final String CLIENT_ID = "CLIENT_ID";
    protected static final String ACCEPT_HEADER = "application/vnd.twitchtv.v5+json";
    protected static final String BASE_USER_INFO_URL = "https://api.twitch.tv/kraken/user?oauth_token=";

    protected static final String OAUTH_URL = "https://api.twitch.tv/kraken/oauth2/authorize?response_type=token&client_id="
            + CLIENT_ID + "&redirect_uri=http://localhost&scope=user_read+chat_login+user_follows_edit+user_subscriptions";

    public static String getOAuthUrl() {
        return OAUTH_URL;
    }

    public static String twitchV5Request(String urlToRead) {
        HttpURLConnection conn = null;
        Scanner in = null;
        String result = "";

        try {
            URL url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Client-ID", CLIENT_ID);
            conn.setRequestProperty("Accept", ACCEPT_HEADER);
            conn.setRequestMethod("GET");
            in = new Scanner(new InputStreamReader(conn.getInputStream()));

            while(in.hasNextLine()) {
                String line = in.nextLine();
                result += line;
            }

            in.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(in != null)
                in.close();
            if(conn != null)
                conn.disconnect();
        }

        if(result.length() == 0 || (result.length() >= 1 && result.charAt(0) != '{')) {
            //Log.v("LOAD URL TO STRING", "Error reading: " + "\"" + urlToRead + "\"");
            //Log.v("LOAD URL TO STRING", "Result of reading: " + "\"" + result + "\"");
        }

        return result;
    }
}
