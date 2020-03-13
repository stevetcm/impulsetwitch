package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orangemuffin.impulse.fragments.ChatFragment;
import com.orangemuffin.impulse.models.EmoteInfo;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.ConnectionUtil;
import com.orangemuffin.impulse.utils.ConvertStringUtil;
import com.orangemuffin.impulse.utils.LocalDataUtil;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* Created by OrangeMuffin on 2018-05-25 */
public class FetchUserEmotesTask extends AsyncTask<String, Void, List<List<EmoteInfo>>> {
    private FetchUserEmotesCallback callback;
    private Context context;
    private ChatFragment chatFragment;

    public FetchUserEmotesTask(Context context, ChatFragment chatFragment, FetchUserEmotesCallback callback) {
        this.callback = callback;
        this.context = context;
        this.chatFragment = chatFragment;
    }

    @Override
    protected List<List<EmoteInfo>> doInBackground(String... strings) {
        List<List<EmoteInfo>> mResult = new ArrayList<>();

        if (LocalDataUtil.getFfzStatus(context)) {
            String ffz_url = "https://api.frankerfacez.com/v1/room/" + strings[0];

            try {
                String jsonString = TwitchAPIService.twitchV5Request(ffz_url);
                List<EmoteInfo> emotes = new ArrayList<>();

                JSONObject fullDataObject = new JSONObject(jsonString);
                JSONObject setsObjects = fullDataObject.getJSONObject("sets");

                Iterator<String> iter = setsObjects.keys();
                while (iter.hasNext()) {
                    String key = iter.next();

                    JSONObject set = setsObjects.getJSONObject(key);
                    JSONArray emoticonsFFZ = set.getJSONArray("emoticons");

                    for (int j = 0; j < emoticonsFFZ.length(); j++) {
                        JSONObject emoteObject = emoticonsFFZ.getJSONObject(j);

                        EmoteInfo emote = new EmoteInfo();

                        String id = "ffz" + emoteObject.getInt("id");
                        emote.setId(id);

                        String code = emoteObject.getString("name");
                        emote.setCode(code);

                        emote.setOwner_id("-3");

                        if (!LocalDataUtil.doesStorageFileExist("mEmote-" + id, context)) {
                            JSONObject urlsObject = emoteObject.getJSONObject("urls");

                            String imageUrl;
                            if (urlsObject.has("4")) {
                                imageUrl = "https:" + emoteObject.getJSONObject("urls").getString("4");
                            } else if (urlsObject.has("2")) {
                                imageUrl = "https:" + emoteObject.getJSONObject("urls").getString("2");
                            } else {
                                imageUrl = "https:" + emoteObject.getJSONObject("urls").getString("1");
                            }

                            Bitmap newEmoteFound = ConnectionUtil.getBitmapFromUrl(imageUrl);
                            LocalDataUtil.saveImageToStorage(newEmoteFound, "mEmote-" + id, context);
                        }

                        emote.setImage(LocalDataUtil.getImageFromStorage("mEmote-" + id, context));

                        chatFragment.addEmoteToFinder(code, id);
                        emotes.add(emote);
                    }
                }

                mResult.add(emotes);
            } catch (Exception e) { }
        }

        if (LocalDataUtil.getBttvStatus(context)) {
            String bttv_url_global = "https://api.betterttv.net/2/emotes";
            try {
                String jsonString = TwitchAPIService.twitchV5Request(bttv_url_global);
                List<EmoteInfo> emotes = new ArrayList<>();

                JSONObject topChannelEmotes = new JSONObject(jsonString);
                JSONArray channelEmotes = topChannelEmotes.getJSONArray("emotes");

                for (int i = 0; i < channelEmotes.length(); i++) {
                    JSONObject emoteObject = channelEmotes.getJSONObject(i);

                    EmoteInfo emote = new EmoteInfo();

                    String id = emoteObject.getString("id");
                    emote.setId(id);

                    String code = emoteObject.getString("code");
                    emote.setCode(code);

                    emote.setOwner_id("-4");

                    String type = emoteObject.getString("imageType");
                    emote.setType(type);

                    if (!LocalDataUtil.doesStorageFileExist("mEmote-" + id, context)) {
                        String imageUrl = "https://cdn.betterttv.net/emote/" + id + "/2x";
                        Bitmap newEmoteFound = ConnectionUtil.getBitmapFromUrl(imageUrl);
                        LocalDataUtil.saveImageToStorage(newEmoteFound, "mEmote-" + id, context);
                    }

                    emote.setImage(LocalDataUtil.getImageFromStorage("mEmote-" + id, context));

                    if (type.equals("gif")) {
                        String imageUrl = "https://cdn.betterttv.net/emote/" + id + "/2x";
                        InputStream inputStream = new URL(imageUrl).openStream();
                        emote.setGifByte(IOUtils.toByteArray(inputStream));
                        chatFragment.addGifToList(emote);
                    }

                    chatFragment.addEmoteToFinder(code, id);
                    emotes.add(emote);
                }

                mResult.add(emotes);
            } catch (Exception e) { }

            String bttv_url_channel = "https://api.betterttv.net/2/channels/" + strings[0];
            try {
                String jsonString = TwitchAPIService.twitchV5Request(bttv_url_channel);
                List<EmoteInfo> emotes = new ArrayList<>();

                JSONObject topChannelEmotes = new JSONObject(jsonString);
                JSONArray channelEmotes = topChannelEmotes.getJSONArray("emotes");

                for (int i = 0; i < channelEmotes.length(); i++) {
                    JSONObject emoteObject = channelEmotes.getJSONObject(i);

                    EmoteInfo emote = new EmoteInfo();

                    String id = emoteObject.getString("id");
                    emote.setId(id);

                    String code = emoteObject.getString("code");
                    emote.setCode(code);

                    emote.setOwner_id("-2");

                    String type = emoteObject.getString("imageType");
                    emote.setType(type);

                    if (!LocalDataUtil.doesStorageFileExist("mEmote-" + id, context)) {
                        String imageUrl = "https://cdn.betterttv.net/emote/" + id + "/2x";
                        Bitmap newEmoteFound = ConnectionUtil.getBitmapFromUrl(imageUrl);
                        LocalDataUtil.saveImageToStorage(newEmoteFound, "mEmote-" + id, context);
                    }

                    emote.setImage(LocalDataUtil.getImageFromStorage("mEmote-" + id, context));

                    if (type.equals("gif")) {
                        String imageUrl = "https://cdn.betterttv.net/emote/" + id + "/2x";
                        InputStream inputStream = new URL(imageUrl).openStream();
                        emote.setGifByte(IOUtils.toByteArray(inputStream));
                        chatFragment.addGifToList(emote);
                    }

                    chatFragment.addEmoteToFinder(code, id);
                    emotes.add(emote);
                }

                mResult.add(emotes);
            } catch (Exception e) { }
        }

        String base_url = "https://api.twitch.tv/kraken/users/" + LocalDataUtil.getUserId(context) + "/emotes?oauth_token="
                + LocalDataUtil.getAccessToken(context);

        try {
            String jsonString = TwitchAPIService.twitchV5Request(base_url);
            JSONObject fullDataObject = new JSONObject(jsonString);

            JSONObject setsObject = fullDataObject.getJSONObject("emoticon_sets");

            Iterator setsKeys = setsObject.keys();

            while (setsKeys.hasNext()) {
                String key = (String) setsKeys.next();

                if (setsObject.get(key) instanceof JSONArray) {
                    List<EmoteInfo> emotes = new ArrayList<>();

                    JSONArray set = setsObject.getJSONArray(key);

                    for (int i = 0; i < set.length(); i++) {
                        JSONObject emoteObject = set.getJSONObject(i);

                        EmoteInfo emote = new EmoteInfo();

                        String id = emoteObject.getString("id");
                        emote.setId(id);

                        String code = emoteObject.getString("code");

                        if (Integer.parseInt(id) <= 14) {
                            code = ConvertStringUtil.convertHtmlEntities(id);
                        }

                        emote.setCode(code);

                        emote.setOwner_id(key);

                        if (!LocalDataUtil.doesStorageFileExist("mEmote-" + id, context)) {
                            String imageUrl = "https://static-cdn.jtvnw.net/emoticons/v1/" + id + "/4.0";
                            Bitmap newEmoteFound = ConnectionUtil.getBitmapFromUrl(imageUrl);
                            LocalDataUtil.saveImageToStorage(newEmoteFound, "mEmote-" + id, context);
                        }

                        emote.setImage(LocalDataUtil.getImageFromStorage("mEmote-" + id, context));

                        chatFragment.addEmoteToFinder(code, id);
                        emotes.add(emote);
                    }

                    if (emotes.get(0).getOwner_id().equals("0")
                            && !emotes.get(0).getId().equals("1")) {
                        Collections.reverse(emotes);
                    }

                    if (emotes.get(0).getOwner_id().equals("19194")
                            && !emotes.get(0).getCode().equals("FlipThis")) {
                        Collections.reverse(emotes);
                    }

                    mResult.add(emotes);
                }
            }


            String json = LocalDataUtil.getRecentEmotes(context);
            if (!json.equals("NULL")) {
                Gson gson = new Gson();
                List<EmoteInfo> recentEmotes = gson.fromJson(json, new TypeToken<List<EmoteInfo>>(){}.getType());

                for (EmoteInfo recentEmote : recentEmotes) {
                    recentEmote.setImage(LocalDataUtil.getImageFromStorage("mEmote-" + recentEmote.getId(), context));

                    if (recentEmote.getType().equals("gif")) {
                        String imageUrl = "https://cdn.betterttv.net/emote/" + recentEmote.getId() + "/2x";
                        InputStream inputStream = new URL(imageUrl).openStream();
                        recentEmote.setGifByte(IOUtils.toByteArray(inputStream));
                    }

                    if (!chatFragment.userMapContainsKey(recentEmote.getCode())) {
                        recentEmote.setAllowed(false);
                    } else {
                        recentEmote.setAllowed(true);
                    }
                }

                if (!recentEmotes.isEmpty()) {
                    mResult.add(recentEmotes);
                }
            }

            LocalDataUtil.setEKeyboardStatus(chatFragment.getContext(), true);
        } catch (Exception e) { }

        return mResult;
    }

    @Override
    protected void onPostExecute(List<List<EmoteInfo>> emotes) {
        super.onPostExecute(emotes);
        callback.onUserEmotesFetched(emotes);
    }

    public interface FetchUserEmotesCallback {
        void onUserEmotesFetched(List<List<EmoteInfo>> emotes);
    }

}
