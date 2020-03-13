package com.orangemuffin.impulse.utils;

import com.orangemuffin.impulse.models.ChannelInfo;
import com.orangemuffin.impulse.models.ClipInfo;
import com.orangemuffin.impulse.models.GameInfo;
import com.orangemuffin.impulse.models.StreamInfo;
import com.orangemuffin.impulse.models.VODInfo;

import org.json.JSONObject;

/* Created by OrangeMuffin on 2018-03-17 */
public class JSONParser {
    public static StreamInfo getStreamInfo(JSONObject streamObject) throws Exception {
        StreamInfo stream = new StreamInfo();

        stream.setStreamType(streamObject.getString("stream_type"));

        stream.setViewCount(streamObject.getInt("viewers"));

        String previewTemplate = streamObject.getJSONObject("preview").getString("template");
        String videoPreview = previewTemplate.replaceAll("[{]width[}]", "1280").replaceAll("[{]height[}]", "720");
        stream.setVideoPreviewUrl(videoPreview);

        JSONObject channelObject = streamObject.getJSONObject("channel");

        stream.setLogoUrl(channelObject.getString("logo"));

        stream.setStreamerName(channelObject.getString("name"));
        stream.setDisplayName(channelObject.getString("display_name"));
        stream.setStreamStatus(channelObject.getString("status"));
        stream.setGameName(channelObject.getString("game"));
        stream.setFollowersCount(Integer.parseInt(channelObject.getString("followers")));
        stream.setUserId(channelObject.getString("_id"));

        return stream;
    }

    public static StreamInfo getStreamInfo(JSONObject streamObject, int priority) throws Exception {
        StreamInfo stream = getStreamInfo(streamObject);
        stream.setPriority(priority);

        return stream;
    }

    public static GameInfo getGameInfo(JSONObject topObject) throws Exception {
        GameInfo game = new GameInfo();
        JSONObject gameObject = topObject.getJSONObject("game");
        game.setGameName(gameObject.getString("name"));

        JSONObject previewsObject = gameObject.getJSONObject("box");
        String boxTemplate = previewsObject.getString("template");
        String boxUrl = boxTemplate.replaceAll("[{]width[}]", "272").replaceAll("[{]height[}]", "380");
        game.setPosterUrl(boxUrl);

        game.setViewCount(topObject.getInt("viewers"));

        return game;
    }

    public static GameInfo getGameSearched(JSONObject gameObject) throws Exception {
        GameInfo game = new GameInfo();
        game.setGameName(gameObject.getString("name"));
        JSONObject previewsObject = gameObject.getJSONObject("box");
        String boxTemplate = previewsObject.getString("template");
        String boxUrl = boxTemplate.replaceAll("[{]width[}]", "272").replaceAll("[{]height[}]", "380");
        game.setPosterUrl(boxUrl);

        game.setViewCount(gameObject.getInt("popularity"));
        return game;
    }

    public static ChannelInfo getChannelInfo(JSONObject channelObject) throws Exception {
        ChannelInfo channel = new ChannelInfo();

        channel.setLogoUrl(channelObject.getString("logo"));
        channel.setDisplayName(channelObject.getString("display_name"));
        channel.setChannelName(channelObject.getString("name"));
        channel.setChannelId(channelObject.getString("_id"));

        return channel;
    }

    public static VODInfo getVodInfo(JSONObject vodObject) throws Exception {
        VODInfo vod = new VODInfo();

        vod.setVodId(vodObject.getString("_id").replace("v", ""));
        vod.setVodTitle(vodObject.getString("title"));
        vod.setViewCount(vodObject.getString("views"));
        vod.setVodPreviewUrl(vodObject.getJSONObject("preview").getString("large"));
        vod.setVodLength(vodObject.getString("length"));
        vod.setRecorded_at(vodObject.getString("recorded_at"));
        vod.setVodGameName(vodObject.getString("game"));

        JSONObject channelObject = vodObject.getJSONObject("channel");
        vod.setDisplay_name(channelObject.getString("display_name"));
        vod.setChannelName(channelObject.getString("name"));
        vod.setChannel_id(channelObject.getString("_id"));
        vod.setLogoUrl(channelObject.getString("logo"));

        return vod;
    }

    public static VODInfo parseLiveStreamObject(JSONObject streamObject) throws Exception {
        VODInfo vod = new VODInfo();

        JSONObject channelObject = streamObject.getJSONObject("channel");
        vod.setVodTitle(channelObject.getString("status"));

        vod.setViewCount(streamObject.getString("viewers"));

        String previewTemplate = streamObject.getJSONObject("preview").getString("template");
        String videoPreview = previewTemplate.replaceAll("[{]width[}]", "1280").replaceAll("[{]height[}]", "720");
        vod.setVodPreviewUrl(videoPreview);

        vod.setViewCount(streamObject.getString("viewers"));
        vod.setVodGameName(streamObject.getString("game"));

        vod.setLogoUrl(channelObject.getString("logo"));

        vod.setChannelName(channelObject.getString("name"));
        vod.setDisplay_name(channelObject.getString("display_name"));

        vod.setChannel_id(channelObject.getString("_id"));

        vod.setLiveStream(true);
        return vod;
    }

    public static ClipInfo getClipInfo(JSONObject clipObject) throws Exception {
        ClipInfo clip = new ClipInfo();

        JSONObject broadcasterObject = clipObject.getJSONObject("broadcaster");
        clip.setDisplayName(broadcasterObject.getString("display_name"));
        clip.setLogoUrl(broadcasterObject.getString("logo"));

        clip.setClipSlug(clipObject.getString("slug"));
        clip.setClipGameName(clipObject.getString("game"));
        clip.setViewCount(clipObject.getString("views"));
        clip.setClipPreviewUrl(clipObject.getJSONObject("thumbnails").getString("medium"));
        clip.setClipTitle(clipObject.getString("title"));
        clip.setClipLength(String.valueOf(clipObject.getInt("duration")));
        clip.setClipDate(MeasurementUtil.convertDay(clipObject.getString("created_at")));

        if (!clipObject.isNull("vod")) {
            JSONObject vodObject = clipObject.getJSONObject("vod");
            clip.setVodId(vodObject.getString("id"));
            clip.setVodOffset(vodObject.getString("offset"));
        } else {
            clip.setVodId("Full Video Unavailable");
        }

        return clip;
    }
}
