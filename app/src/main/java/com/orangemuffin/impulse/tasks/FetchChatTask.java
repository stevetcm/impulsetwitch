package com.orangemuffin.impulse.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.orangemuffin.impulse.models.ChatMessage;
import com.orangemuffin.impulse.models.EmoteInfo;
import com.orangemuffin.impulse.models.ImgType;
import com.orangemuffin.impulse.twitchapi.TwitchAPIService;
import com.orangemuffin.impulse.utils.ConnectionUtil;
import com.orangemuffin.impulse.utils.LocalDataUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* Created by OrangeMuffin on 2018-03-20 */
public class FetchChatTask extends AsyncTask<Void, Object, Void> {
    private String twitchChatServer = "irc.twitch.tv";
    private int twitchChatPort = 6667;

    private LinkedHashMap<String, String> userColors = LocalDataUtil.createLRUMap(150);

    private HashMap<String, Bitmap> subscriberBadges = new HashMap<>();

    private String user, oauth_key;
    private String channelName, hashChannel, channelId;

    private BufferedWriter writer;
    private BufferedReader reader;

    private Handler callbackHandler;

    private FetchChatCallback callback;

    boolean subscriberBadge = false, isStopping = false;

    private Context context;

    Pattern stdVarPattern = Pattern.compile("badges=([\\s\\S]*);color=(#?\\w*);display-name=(\\w+).*;emotes=([\\s\\S]*);flags=.*;id.*mod=(0|1);room-id=\\d+;.*subscriber=(0|1);.*turbo=(0|1);.* PRIVMSG #\\S* :(.*)");

    private ArrayList<Bitmap> userBadges;
    private String userDisplayName;
    private String userColor;
    private boolean userIsMod;
    private boolean userIsSubscriber;
    private boolean userIsTurbo;

    private String chatBroadcastLanguage;
    private boolean chatEmoteOnlyMode;
    private boolean chatR9kMode;
    private boolean chatSlowMode;
    private boolean chatSubsOnlyMode;

    private HashMap<String, String> userEmoteFinder = new HashMap<>();
    private boolean bttvEmotesStatus;
    private List<EmoteInfo> gifEmoteList = new ArrayList<>();

    Pattern roomStatePattern = Pattern.compile("@emote-only=(0|1).*;r9k=(0|1).*;slow=(0|\\d+);subs-only=(0|1)");

    Pattern userStatePattern = Pattern.compile("@.*badges=(.*);color=(#?\\w*);display-name=(.+);emote-sets=(.+);mod=(0|1);subscriber=(0|1);(turbo=(0|1)|user)");

    Pattern noticeLinePattern = Pattern.compile("system-msg=(.*);tmi-sent-ts.*:(.*)");

    public FetchChatTask(Context context, FetchChatCallback callback, String streamerName, String channelId) {
        this.context = context;

        if (LocalDataUtil.getUserName(context).equals("NULL")) {
            user = "justinfan" + String.valueOf(new Random().nextInt(9000)+1000);
        } else {
            user = LocalDataUtil.getUserName(context);
        }

        oauth_key = "oauth:" + LocalDataUtil.getAccessToken(context);
        this.channelName = streamerName;
        this.hashChannel = "#" + channelName;
        this.channelId = channelId;
        callbackHandler = new Handler();
        this.callback = callback;

        bttvEmotesStatus = LocalDataUtil.getBttvStatus(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        publishProgress("ON_MESSAGE", new ChatMessage(null, null, null, null, "Connecting...", false));
        subscriberBadges.putAll(getSubscriberBadges(channelId));

        if (!LocalDataUtil.getBadgeStatus(context)) {
            publishProgress("ON_MESSAGE", new ChatMessage(null, null, null, null, "Fetching Twitch Badges... (Once)", false));
        }

        fetchGlobalBadges();

        connect(twitchChatServer, twitchChatPort);

        return null;
    }

    @Override
    protected void onProgressUpdate(final Object... objects) {
        super.onProgressUpdate(objects);
        final String update = (String) objects[0];
        callbackHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (update) {
                    case "ON_MESSAGE":
                        callback.onChatFetched((ChatMessage) objects[1]);
                        break;
                    case "ON_BAN_MESSAGE":
                        callback.onBanChatFetched((String) objects[1]);
                }
            }
        });
    }

    private void connect(String address, int port) {
        try {
            @SuppressWarnings("resource")
            Socket socket = new Socket(address, port);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.write("PASS " + oauth_key + "\r\n");
            writer.write("NICK " + user + "\r\n");
            writer.write("USER " + user + " \r\n");
            writer.flush();

            String line = "";
            while ((line = reader.readLine()) != null) {
                if (isStopping) {
                    leaveChannel();
                    //Log.d("TWITCH CHAT CONNECTION", "Stopping chat for " + channelName);
                    break;
                }

                if (line.contains("004 " + user + " :")) {
                    //Log.d("TWITCH CHAT CONNECTION", "<" + line);
                    //Log.d("TWITCH CHAT CONNECTION", "Connected >> " + user + " ~ irc.twitch.tv");
                    sendRawMessage("CAP REQ :twitch.tv/tags twitch.tv/commands");
                    sendRawMessage("JOIN " + hashChannel + "\r\n");
                    publishProgress("ON_MESSAGE", new ChatMessage(null, null, null, null, "Welcome to the chat room!", false));
                } else if(userDisplayName == null && line.contains("USERSTATE " + hashChannel)) {
                    //Log.d("TWITCH CHAT USERSTATE LINE", line);
                    handleUserstate(line);
                } else if(line.contains("ROOMSTATE " + hashChannel)) {
                    //Log.d("TWITCH CHAT ROOMSTATE LINE", line);
                    handleRoomstate(line);
                } else if(line.contains("NOTICE " + hashChannel)) {
                    //Log.d("TWITCH CHAT NOTICE LINE", line);
                    handleNotice(line);
                } else if (line.startsWith("PING")) { // Twitch wants to know if we are still here. Send PONG and Server info back
                    writer.write("PONG " + line.substring(5) + "\r\n");
                    writer.flush();
                } else if (line.contains("PRIVMSG")) {
                    //Log.d("TWITCH CHAT LINE PRIVMSG", line);
                    handleMessage(line);
                } else if (line.contains("CLEARCHAT")) {
                    //Log.d("TWITCH CHAT BAN LINE", line);
                    handleBanMessage(line);
                } else if (line.toLowerCase().contains("disconnected"))	{
                    publishProgress("ON_RECONNECTING", "Reconnecting...");
                    connect(address, port);
                } else if(line.contains("NOTICE * :Error logging in")) {
                    publishProgress("ON_CONNECTION_FAILED", "Connection Failed >_<");
                } else {
                    //Log.d("TWITCH CHAT CONNECTION", "<" + line);
                }
            }
        } catch (IOException e) {
            connect(twitchChatServer, twitchChatPort);
        }
    }

    private void sendRawMessage(String message) {
        try {
            writer.write(message + " \r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message, String messageEmotes) {
        try {
            if (writer != null) {
                writer.write("PRIVMSG " + hashChannel + " :" + message + "\r\n");
                writer.flush();

                HashMap<String, ImgType> emotesMap = new HashMap<>();
                if (messageEmotes.contains("/")) {
                    String[] split = messageEmotes.split("/");
                    for (int i = 0; i < split.length; i++) {
                        String[] innerSplit = split[i].split(":");
                        emotesMap.put(innerSplit[1], matchTwitchEmotes(innerSplit[0]));
                    }
                } else if (!messageEmotes.equals("")) {
                    String[] split = messageEmotes.split(":");
                    emotesMap.put(split[1], matchTwitchEmotes(split[0]));
                }

                publishProgress("ON_MESSAGE", new ChatMessage(userBadges, emotesMap, userColor, userDisplayName, message, false));
                if (chatSubsOnlyMode && !userIsSubscriber) {
                    publishProgress("ON_MESSAGE", new ChatMessage(null, null, null, null, "This room is in subscribers only mode. To talk, purchase the channel's subscription.", false));
                }
            }
        } catch (Exception e) { }
    }

    private void handleUserstate(String line) {
        Matcher userstateMatcher = userStatePattern.matcher(line);
        if (userstateMatcher.find()) {
            userBadges = matchMessageBadges(userstateMatcher.group(1));

            userColor = userstateMatcher.group(2);
            if (userColor.equals("")) {
                Random rand = new Random();
                userColor = "#" + Integer.toHexString(Color.argb(255, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
            }

            userDisplayName = userstateMatcher.group(3);

            userIsSubscriber = userstateMatcher.group(6).equals("1");

            /*userIsMod = userstateMatcher.group(5).equals("1");
            if (userstateMatcher.groupCount() > 8) {
                userIsTurbo = userstateMatcher.group(8).equals("1");
            }*/
        } else {
            Toast.makeText(context, "Twitch changed something again...", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRoomstate(String line) {
        Matcher roomstateMatcher = roomStatePattern.matcher(line);
        if (roomstateMatcher.find()) {
            //chatBroadcastLanguage = roomstateMatcher.group(1);
            chatEmoteOnlyMode = roomstateMatcher.group(1).equals("1");
            chatSubsOnlyMode = roomstateMatcher.group(4).equals("1");
        } else {
            Toast.makeText(context, "Twitch changed something again...", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleNotice(String line) {
        Matcher noticeLineMatcher = noticeLinePattern.matcher(line);
        if (noticeLineMatcher.find()) {
            /* //disabling subscription alerts
            String noticeMessage = noticeLineMatcher.group(1);
            noticeMessage = noticeMessage.replace("\\s", " ");
            publishProgress("ON_MESSAGE", new ChatMessage(null, null, null, null, noticeMessage, true));*/
        } else {
            Matcher noticeMessageMatcher = Pattern.compile("@msg-id=(.*) :.*NOTICE.* :(.*)").matcher(line);
            if (noticeMessageMatcher.find()) {
                if (!noticeMessageMatcher.group(1).equals("msg_subsonly")) {
                    publishProgress("ON_MESSAGE", new ChatMessage(null, null, null, null, noticeMessageMatcher.group(2), true));
                }
            }
        }
    }

    private void handleBanMessage(String line) {
        Pattern pattern = Pattern.compile("CLEARCHAT #\\S* :(.*)");
        Matcher banMatcher = pattern.matcher(line);

        if(banMatcher.find()) {
            String displayName = banMatcher.group(1);
            publishProgress("ON_BAN_MESSAGE", displayName);
        }
    }

    private void handleMessage(String line) {
        Matcher stdVarMatcher = stdVarPattern.matcher(line);

        if(stdVarMatcher.find()) {
            ArrayList<Bitmap> messageBadges = matchMessageBadges(stdVarMatcher.group(1));

            String displayName = stdVarMatcher.group(3);
            String message = stdVarMatcher.group(8).replace("\u0001", "");

            HashMap<String, ImgType> emotesMap = new HashMap<>();

            String emote = stdVarMatcher.group(4);

            emote += bttvProcess(emote, message);

            //Log.d("TWITCH CHAT LINE PROCESS", displayName + ": " + message + " with " + "{" + emote + "}");

            if (emote.contains("/")) {
                String[] split = emote.split("/");
                for (int i = 0; i < split.length; i++) {
                    String[] innerSplit = split[i].split(":");
                    emotesMap.put(innerSplit[1], matchTwitchEmotes(innerSplit[0]));
                }
            } else if (!emote.equals("")) {
                String[] split = emote.split(":");
                emotesMap.put(split[1], matchTwitchEmotes(split[0]));
            }

            String color = stdVarMatcher.group(2);
            if (color.equals("") && !userColors.containsKey(displayName)) {
                Random rand = new Random();
                color = "#" + Integer.toHexString(Color.argb(255, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
                userColors.put(displayName, color);
            } else if (color.equals("") && userColors.containsKey(displayName)) {
                color = userColors.get(displayName);
            }

            publishProgress("ON_MESSAGE", new ChatMessage(messageBadges, emotesMap, color, displayName, message, false));
        } else {
           //Log.e("TWITCH CHAT MESSAGE", "Failed to find message pattern in: \n" + line);
        }
    }

    public void leaveChannel() {
        sendRawMessage("PART " + hashChannel);
    }

    private String bttvProcess(String emote, String message) {
        if (userEmoteFinder != null && !userEmoteFinder.isEmpty()) {
            String[] words = message.split(" ");
            int count = 0;

            String[] arr = message.split(" ", 2);
            if (arr[0].contains("ACTION") && arr[0].length() < 8 && arr.length > 1) {
                count -= (arr[0].length() + 1);
            }

            HashMap<String, String> parseMessageEmotes = new HashMap<>();
            for (String word : words) {
                if (userEmoteFinder.containsKey(word)) {
                    String keyId = userEmoteFinder.get(word);

                    if (!emote.contains(keyId)) {
                        if (!parseMessageEmotes.containsKey(keyId)) {
                            parseMessageEmotes.put(keyId, String.valueOf(count) + "-" + String.valueOf(count + word.length() - 1));
                        } else {
                            String existingPosition = parseMessageEmotes.get(keyId);
                            parseMessageEmotes.put(keyId, existingPosition + "," + String.valueOf(count) + "-" + String.valueOf(count + word.length() - 1));
                        }
                    }

                }
                count += word.length() + 1;
            }

            String messageEmotes = "";
            Iterator it = parseMessageEmotes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (!messageEmotes.equals("")) {
                    messageEmotes += "/";
                }
                messageEmotes += pair.getKey() + ":" + pair.getValue();
            }

            if (emote.equals("")) {
                return messageEmotes;
            } else {
                return "/" + messageEmotes;
            }
        }
        return "";
    }

    public void addBttvEmotes(HashMap<String, String> userEmoteFinder) {
        this.userEmoteFinder = userEmoteFinder;
    }

    public void addGifEmoteList(List<EmoteInfo> gifEmoteList) {
        this.gifEmoteList = gifEmoteList;
    }

    ImgType matchTwitchEmotes(String emoteId) {
        ImgType imgType = new ImgType();
        if (!LocalDataUtil.doesStorageFileExist("mEmote-" + emoteId, context)) {
            String imageUrl = "https://static-cdn.jtvnw.net/emoticons/v1/" + emoteId + "/4.0";
            Bitmap newEmoteFound = ConnectionUtil.getBitmapFromUrl(imageUrl);
            LocalDataUtil.saveImageToStorage(newEmoteFound, "mEmote-" + emoteId, context);

            imgType.setBitmap(newEmoteFound);
            return imgType;
        }


        try {
            for (EmoteInfo gifEmote : gifEmoteList) {
                if (gifEmote.getId().equals(emoteId)) {
                    if (gifEmote.getType().equals("gif")) {
                        imgType.setType("gif");
                        imgType.setGifByte(gifEmote.getGifByte());
                        return imgType;
                    } else {
                        imgType.setBitmap(LocalDataUtil.getImageFromStorage("mEmote-" + emoteId, context));
                        return imgType;
                    }
                }
            }
        } catch (Exception e) { }

        try {
            imgType.setBitmap(LocalDataUtil.getImageFromStorage("mEmote-" + emoteId, context));
        } catch (IOException e) { }
        return imgType;
    }

    private ArrayList<Bitmap> matchMessageBadges(String badgeMatcher) {
        ArrayList<Bitmap> messageBadges = new ArrayList<>();

        if (badgeMatcher.contains(",")) {
            String[] split = badgeMatcher.split(",");
            for (int i = 0; i < split.length; i++) {
                if (split[i].contains("subscriber") && subscriberBadges.size() > 0) {
                    messageBadges.add(subscriberBadges.get(split[i]));
                } else {
                    try {
                        messageBadges.add(LocalDataUtil.getImageFromStorage("badge-" + split[i].replace("/", "-"), context));
                    } catch (Exception e) { }
                }
            }
        } else if (!badgeMatcher.equals("")) {
            if (badgeMatcher.contains("subscriber") && subscriberBadges.size() > 0) {
                messageBadges.add(subscriberBadges.get(badgeMatcher));
            } else {
                try {
                    messageBadges.add(LocalDataUtil.getImageFromStorage("badge-" + badgeMatcher.replace("/", "-"), context));
                } catch (Exception e) { }
            }
        }

        return messageBadges;
    }

    HashMap<String, Bitmap> getSubscriberBadges(String channelId) {
        HashMap<String, Bitmap> subscriberBadges = new HashMap<>();
        final String URL = "https://badges.twitch.tv/v1/badges/channels/" + channelId + "/display?language=en";

        try {
            JSONObject dataObject = new JSONObject(TwitchAPIService.twitchV5Request(URL));
            JSONObject subscriberObject = dataObject.getJSONObject("badge_sets").getJSONObject("subscriber");
            JSONObject versionsObject = subscriberObject.getJSONObject("versions");

            Iterator keys = versionsObject.keys();
            while (keys.hasNext()) {
                Object key = keys.next();
                JSONObject value = versionsObject.getJSONObject((String) key);
                String imageUrl = value.getString("image_url_4x");
                subscriberBadges.put("subscriber/" + key.toString(), ConnectionUtil.getBitmapFromUrl(imageUrl));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (subscriberBadges.size() > 0) {
            subscriberBadge = true;
        } else {
            try {
                LocalDataUtil.getImageFromStorage("badge-subscriber-0", context);
            } catch (Exception e) {
                String imageUrl = "https://static-cdn.jtvnw.net/badges/v1/5d9f2208-5dd8-11e7-8513-2ff4adfae661/3";
                LocalDataUtil.saveImageToStorage(ConnectionUtil.getBitmapFromUrl(imageUrl), "badge-subscriber-0", context);
                LocalDataUtil.saveImageToStorage(ConnectionUtil.getBitmapFromUrl(imageUrl), "badge-subscriber-1", context);
            }
        }

        return subscriberBadges;
    }

    void fetchGlobalBadges() {
        final String URL = "https://badges.twitch.tv/v1/badges/global/display?language=en";
        try {
            JSONObject dataObject = new JSONObject(TwitchAPIService.twitchV5Request(URL));
            JSONObject globalNameObject = dataObject.getJSONObject("badge_sets");

            Iterator keys = globalNameObject.keys();
            while (keys.hasNext()) {
                Object key = keys.next();

                if (!key.equals("subscriber") || !subscriberBadge) {
                    JSONObject value = globalNameObject.getJSONObject((String) key);
                    JSONObject versionsObject = value.getJSONObject("versions");

                    Iterator innerKeys = versionsObject.keys();
                    while (innerKeys.hasNext()) {
                        Object innerKey = innerKeys.next();

                        JSONObject innerValue = versionsObject.getJSONObject((String) innerKey);
                        String imageUrl = innerValue.getString("image_url_4x");

                        if (!LocalDataUtil.doesStorageFileExist("badge-" + key + "-" + innerKey, context)) {
                            LocalDataUtil.saveImageToStorage(ConnectionUtil.getBitmapFromUrl(imageUrl), "badge-" + key + "-" + innerKey, context);
                            //Log.d("FETCHING GLOBAL BADGES", key + "/" + innerKey);
                        }
                    }
                }
            }

            LocalDataUtil.setBadgeStatus(context, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        isStopping = true;
    }

    public interface FetchChatCallback {
        void onChatFetched(ChatMessage chatMessage);
        void onBanChatFetched(String displayName);
    }
}
