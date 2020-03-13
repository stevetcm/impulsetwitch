package com.orangemuffin.impulse.models;

/* Created by OrangeMuffin on 2018-03-22 */
public class VODInfo {
    private String vodId;
    private String vodTitle;
    private String viewCount;
    private String vodPreviewUrl;
    private String recorded_at;
    private String vodLength;
    private String vodGameName;
    private boolean liveStream;
    private String display_name;
    private String channel_id;
    private String logoUrl;
    private String channelName;

    public String getVodId() {
        return vodId;
    }

    public void setVodId(String vodId) {
        this.vodId = vodId;
    }

    public String getVodTitle() {
        return vodTitle;
    }

    public void setVodTitle(String vodTitle) {
        this.vodTitle = vodTitle;
    }

    public String getViewCount() {
        return viewCount;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public String getVodPreviewUrl() {
        return vodPreviewUrl;
    }

    public void setVodPreviewUrl(String vodPreviewUrl) {
        this.vodPreviewUrl = vodPreviewUrl;
    }

    public String getRecorded_at() {
        return recorded_at;
    }

    public void setRecorded_at(String recorded_at) {
        this.recorded_at = recorded_at;
    }

    public String getVodLength() {
        return vodLength;
    }

    public void setVodLength(String vodLength) {
        this.vodLength = vodLength;
    }

    public String getVodGameName() {
        return vodGameName;
    }

    public void setVodGameName(String vodGameName) {
        this.vodGameName = vodGameName;
    }

    public boolean isLiveStream() {
        return liveStream;
    }

    public void setLiveStream(boolean liveStream) {
        this.liveStream = liveStream;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}
