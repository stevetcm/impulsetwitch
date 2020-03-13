package com.orangemuffin.impulse.models;

/* Created by OrangeMuffin on 2018-03-17 */
public class GameInfo {
    private String posterUrl;
    private String gameName;
    private int viewCount;

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}
