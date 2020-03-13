package com.orangemuffin.impulse.models;

import android.graphics.Bitmap;

/* Created by OrangeMuffin on 2018-05-25 */
public class EmoteInfo {
    private String id;
    private String code;
    private String owner_id;
    private Bitmap image;
    private String type = "png";
    private byte[] gifByte;
    private boolean allowed = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getGifByte() {
        return gifByte;
    }

    public void setGifByte(byte[] gifByte) {
        this.gifByte = gifByte;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }
}
