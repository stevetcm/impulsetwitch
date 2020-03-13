package com.orangemuffin.impulse.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.os.ResultReceiver;
import android.view.KeyEvent;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.orangemuffin.impulse.R;

/* Created by OrangeMuffin on 2018-05-23 */
public class PlayerService extends Service {
    private String url;

    private String display_name, streamStatus, gameName;

    private Bitmap logoBitmap = null;

    public static final int DELEGATE_PLAY = 1;
    public static final int DELEGATE_PAUSE = 2;
    public static final int DELEGATE_STOP = 3;

    public static final int DELEGATE_LOADING_START = 10;
    public static final int DELEGATE_LOADING_STOP = 11;

    public static final int DELEGATE_STREAM_ERROR = 12;

    private ResultReceiver mResultReceiver;

    private static PlayerService instance = null;
    private static Context context = null;

    private Intent mNotificationIntent;

    private SimpleExoPlayer player;

    private MediaSessionCompat mediaSession;
    private MediaSessionCompat.Callback mediaCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
        }

        @Override
        public void onPause() {
            super.onPause();

            if (player == null || mediaSession == null) {
                return;
            }

            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());

            player.setPlayWhenReady(false);
            buildNotification(generateAction(R.drawable.ic_play_arrow_white_24dp, "PLAY", "action_play"));

            if (mResultReceiver != null) {
                mResultReceiver.send(DELEGATE_PAUSE, null);
            }
        }

        @Override
        public void onPlay() {
            super.onPlay();

            if (player == null || mediaSession == null) {
                return;
            }

            buildPlayer();

            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                    .build());

            buildNotification(generateAction(R.drawable.ic_pause_white_24dp, "PAUSE", "action_pause"));

            if (mResultReceiver != null) {
                mResultReceiver.send(DELEGATE_PLAY, null);
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            stopSession();

            if (mResultReceiver != null) {
                mResultReceiver.send(DELEGATE_STOP, null);
            }
        }

        @Override
        public boolean onMediaButtonEvent(final Intent mediaButtonEvent) {
            String intentAction = mediaButtonEvent.getAction();
            if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (player != null && player.getPlayWhenReady()) {
                        this.onPause();
                    } else if (player != null) {
                        this.onPlay();
                    }
                }
            }
            return true;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (mediaSession == null || intent.hasExtra("url")) {
            try {
                initMediaSession(intent);
            } catch (Exception e) { }
        } else {
            handleIntent(intent);
        }

        return START_STICKY;
    }

    @Override public IBinder onBind(Intent intent) {
        return null;
    }

    private void initMediaSession(Intent intent) throws Exception {
        instance = this;
        context = getApplicationContext();

        url = intent.getStringExtra("url");

        display_name = intent.getStringExtra("display_name");
        streamStatus = intent.getStringExtra("streamStatus");
        gameName = intent.getStringExtra("gameName");

        mNotificationIntent = intent.getParcelableExtra("actualIntent");

        mResultReceiver = intent.getParcelableExtra("receiver");

        logoBitmap = intent.getParcelableExtra("logoBitmap");

        if (mediaSession != null && mediaSession.isActive() && url != null && player != null) {
            stopSession();
        }

        buildPlayer();

        mediaSession = new MediaSessionCompat(this, "PlayerService_AUDIO", null, null);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build());

        buildNotification(generateAction(R.drawable.ic_pause_white_24dp, "PAUSE", "action_pause"));

        mediaSession.setCallback(mediaCallback);

        mediaSession.setActive(true);
    }

    private void stopSession() {
        if (player == null || mediaSession == null) {
            return;
        }

        player.stop();
        player.release();

        mediaSession.release();
        mediaSession.setActive(false);

        player = null;

        stopForeground(true);
    }

    private void handleIntent(Intent intent) {
        if(intent == null || intent.getAction() == null || mediaCallback == null) {
            return;
        }

        String action = intent.getAction();

        if (action.equalsIgnoreCase("action_play")) {
            mediaCallback.onPlay();
        } else if( action.equalsIgnoreCase("action_pause")) {
            mediaCallback.onPause();
        } else if(action.equalsIgnoreCase("action_stop")) {
            mediaCallback.onStop();
        }
    }

    private void buildPlayer() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), "Impulse-Exoplayer"));
        MediaSource videoSource = new HlsMediaSource(Uri.parse(url), dataSourceFactory, 1, null, null);
        final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);
        player.prepare(loopingSource);
        player.setPlayWhenReady(true);
        player.seekToDefaultPosition();

        player.addListener(new ExoPlayer.EventListener() {
            @Override public void onTimelineChanged(Timeline timeline, Object o) { }
            @Override public void onTracksChanged(TrackGroupArray trackGroupArray, TrackSelectionArray trackSelectionArray) { }
            @Override public void onLoadingChanged(boolean b) { }
            @Override public void onRepeatModeChanged(int i) { }
            @Override public void onPositionDiscontinuity() { }
            @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) { }

            @Override
            public void onPlayerStateChanged(boolean b, int i) {
                if (i == Player.STATE_BUFFERING) {
                    if (mResultReceiver != null) {
                        mResultReceiver.send(DELEGATE_LOADING_START, null);
                    }
                } else if (i == Player.STATE_READY) {
                    if (mResultReceiver != null) {
                        mResultReceiver.send(DELEGATE_LOADING_STOP, null);
                    }
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException e) {
                if (mResultReceiver != null) {
                    mResultReceiver.send(DELEGATE_PLAY, null);
                }
            }
        });
    }

    private void buildNotification(NotificationCompat.Action action) {
        Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        intent.setAction("action_stop");
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this).addNextIntentWithParentStack(mNotificationIntent);
        PendingIntent onClickPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this, "1010")
                                            .setSmallIcon(R.drawable.ic_logo_white_24dp)
                                            .setLargeIcon(logoBitmap)
                                            .setContentTitle(display_name)
                                            .setContentText(streamStatus)
                                            .setSubText("Playing " + gameName)
                                            .setDeleteIntent(pendingIntent)
                                            .addAction(action)
                                            .addAction(generateAction(R.drawable.ic_close_white_24dp, "STOP", "action_stop"))
                                            .setStyle(new MediaStyle()
                                                    .setMediaSession(mediaSession.getSessionToken())
                                                    .setShowCancelButton(true)
                                                    .setShowActionsInCompactView(0, 1))
                                            .setShowWhen(false)
                                            .setAutoCancel(false)
                                            .setContentIntent(onClickPendingIntent);

        startForeground(1, builder.build());
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    public static void removeNotification() {
        if (context != null) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(1);
        }
    }

    public static PlayerService getInstance() {
        return instance;
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    public String getDisplayName() {
        return display_name;
    }

    public boolean isPlaying() {
        if (player != null) {
            return player.getPlayWhenReady();
        }
        return false;
    }

    public void registerDelegate(ResultReceiver resultReceiver) {
        this.mResultReceiver = resultReceiver;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSession();
    }
}
