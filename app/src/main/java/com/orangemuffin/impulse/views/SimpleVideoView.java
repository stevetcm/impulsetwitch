package com.orangemuffin.impulse.views;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.orangemuffin.impulse.utils.LocalDataUtil;

/* Created by OrangeMuffin on 2018-03-20 */
public class SimpleVideoView extends VideoView implements MediaPlayer.OnPreparedListener {
    private MediaPlayer mediaPlayer;
    private Context context;

    public SimpleVideoView(Context context, AttributeSet attributes) {
        super(context, attributes);
        this.context = context;
        this.setOnPreparedListener(this);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        this.mediaPlayer.start();
    }

    public boolean mute() {
        if (this.mediaPlayer != null) {
            this.setVolume(0);
            return true;
        }
        return false;
    }

    public boolean unmute() {
        if (this.mediaPlayer != null) {
            this.setVolume(100);
            return true;
        }
        return false;
    }

    private void setVolume(int amount) {
        final int max = 100;
        final double numerator = max - amount > 0 ? Math.log(max - amount) : 0;
        final float volume = (float) (1 - (numerator / Math.log(max)));

        this.mediaPlayer.setVolume(volume, volume);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (LocalDataUtil.getCropToFitStatus(context)) {
            int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
            this.setMeasuredDimension(parentWidth, (parentWidth/16)*9);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
