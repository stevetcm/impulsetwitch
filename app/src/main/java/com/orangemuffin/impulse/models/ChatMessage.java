package com.orangemuffin.impulse.models;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;

/* Created by OrangeMuffin on 2018-03-20 */
public class ChatMessage {
    private ArrayList<Bitmap> badges;
    private HashMap<String, ImgType> emotes;
    private String color;
    private String userName;
    private String message;
    private boolean isNotice;

    public ChatMessage(ArrayList<Bitmap> badges, HashMap<String, ImgType> emotes, String color, String userName, String message, boolean isNotice) {
        this.badges = badges;
        this.emotes = emotes;
        this.color = color;
        this.userName = userName;
        this.message = message;
        this.isNotice = isNotice;
    }

    public ArrayList<Bitmap> getBadges() {
        return badges;
    }

    public String getColor() {
        return color;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HashMap<String, ImgType> getEmotes() { return emotes; }

    public boolean isNotice() {
        return isNotice;
    }

    public void setNotice(boolean isNotice) {
        this.isNotice = isNotice;
    }
}
