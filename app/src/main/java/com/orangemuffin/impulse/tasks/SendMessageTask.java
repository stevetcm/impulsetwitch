package com.orangemuffin.impulse.tasks;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* Created by OrangeMuffin on 2018-04-01 */
public class SendMessageTask extends AsyncTask<Void, Void, Void> {
    private FetchChatTask chatTask;
    private String message;
    private HashMap<String, String> userEmoteFinder;

    public SendMessageTask(FetchChatTask chatTask, HashMap<String, String> userEmoteFinder, String message) {
        this.chatTask = chatTask;
        this.userEmoteFinder = userEmoteFinder;
        this.message = message;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (chatTask != null && message != null) {
            String[] words = message.split(" ");
            int count = 0;

            String[] arr = message.split(" ", 2);
            if (arr[0].equals("/me") && arr.length > 1) {
                count -= (arr[0].length() + 1);
            }

            HashMap<String, String> parseMessageEmotes = new HashMap<>();
            for (String word : words) {
                if (userEmoteFinder.containsKey(word)) {
                    String keyId = userEmoteFinder.get(word);
                    if (!parseMessageEmotes.containsKey(keyId)) {
                        parseMessageEmotes.put(keyId, String.valueOf(count) + "-" + String.valueOf(count + word.length() - 1));
                    } else {
                        String existingPosition = parseMessageEmotes.get(keyId);
                        parseMessageEmotes.put(keyId, existingPosition + "," + String.valueOf(count) + "-" + String.valueOf(count + word.length() - 1));
                    }

                }
                count += word.length()+1;
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

            chatTask.sendMessage(message, messageEmotes);
        }
        return null;
    }
}
