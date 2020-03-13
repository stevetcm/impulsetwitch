package com.orangemuffin.impulse.models;

import android.graphics.Bitmap;

/* Created by OrangeMuffin on 2019-04-08 */
public class ImgType {
    private String type = "png";
    private Bitmap bitmap;
    private byte[] gifByte;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public byte[] getGifByte() {
        return gifByte;
    }

    public void setGifByte(byte[] gifByte) {
        this.gifByte = gifByte;
    }
}
