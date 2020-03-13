package com.orangemuffin.impulse.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.tasks.FetchVideoTask;
import com.orangemuffin.impulse.tasks.ParseClipSlugTask;
import com.orangemuffin.impulse.utils.ConnectionUtil;
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.orangemuffin.impulse.utils.MeasurementUtil;
import com.orangemuffin.impulse.views.SimpleVideoView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/* Created by OrangeMuffin on 2018-04-23 */
public class ClipActivity extends AppCompatActivity {
    private final int PLAY_PAUSE_ANIMATION_DURATION = 500;
    private final int HIDE_ANIMATION_DELAY = 3250;

    private final int ORIENTATION_NULL = -1;
    private final int ORIENTATION_PORTRAIT = 0;
    private final int ORIENTATION_LANDSCAPE = 1;
    private final int ORIENTATION_PORTRAIT_REVERSE = 2;
    private final int ORIENTATION_LANDSCAPE_REVERSE = 3;

    private ArrayList<TextView> supportedQualities = new ArrayList<>();
    HashMap<String, String> availableQualities = new LinkedHashMap<>();

    private String clipTitle, clipDate, clipSlug, clipLength, vodId, vodOffset, display_name;
    private TextView toolbar_title, toolbar_subtitle;

    private Toolbar mToolbar;

    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private boolean exoplayerStatus = false;

    private SimpleVideoView videoView;
    private ProgressBar pbHeaderProgress;

    private RelativeLayout mControlToolbar;
    private FrameLayout mPlayPauseWrapper;
    private ImageView mVolumeIcon, mPauseIcon, mPlayIcon, mFullscreenIcon;

    private View mVideoForeground, mClickIntercepter;

    private boolean isMuted = false, isFullscreen = false;
    private boolean isInterfaceShowing = true;

    RelativeLayout open_vod_layout;
    TextView open_vod;
    ProgressBar pb_vod_load;

    SeekBar clip_seekbar;
    private int currentProgress = 0;
    TextView time_current, time_end;

    ImageView ic_rewind, ic_forward;
    private int quick_seek_time = 5;

    private final Handler delayAnimationHandler	= new Handler();
    private final Handler progressHandler = new Handler();

    private final Runnable hideAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            //hideVideoInterface();
        }
    };
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (progressStatus) { //loop until stopped
                clip_seekbar.setProgress(currentProgress + 1);
                progressHandler.postDelayed(this, 1000);
            }
        }
    };

    private BottomSheetDialog mQualityBottomSheet;
    private String currentUrl;

    private boolean clipEnd = false, progressStatus = false, videoStatus = false;

    private OrientationEventListener orientationEventListener;
    private int lastOrientation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(LocalDataUtil.setupThemeLayout(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip);

        display_name = getIntent().getStringExtra("display_name");

        clipTitle = getIntent().getStringExtra("clipTitle");
        clipDate = getIntent().getStringExtra("clipDate");
        clipSlug = getIntent().getStringExtra("clipSlug");
        clipLength = getIntent().getStringExtra("clipLength");

        vodId = getIntent().getStringExtra("vodId");
        vodOffset = getIntent().getStringExtra("vodOffset");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        mToolbar = (Toolbar) findViewById(R.id.clip_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.bringToFront();

        toolbar_title = (TextView) findViewById(R.id.toolbar_custom_title);
        toolbar_title.setText(clipTitle);

        toolbar_subtitle = (TextView) findViewById(R.id.toolbar_custom_subtitle);
        toolbar_subtitle.setText(clipDate);

        pbHeaderProgress = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        pbHeaderProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);

        time_current = (TextView) findViewById(R.id.time_current);
        time_end = (TextView) findViewById(R.id.time_end);
        time_end.setText(MeasurementUtil.convertTime((long) (Math.floor(Float.parseFloat(clipLength)) * 1000)));

        videoView = (SimpleVideoView) findViewById(R.id.videoView);
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);

        exoplayerStatus = LocalDataUtil.getExoplayerStatus(this);
        if (exoplayerStatus) {
            videoView.setVisibility(View.GONE);
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
            simpleExoPlayerView = new SimpleExoPlayerView(this);
            simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);

            if (LocalDataUtil.getCropToFitStatus(this)) {
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
            } else {
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            }

            simpleExoPlayerView.setPlayer(player);
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
                        pbHeaderProgress.setVisibility(View.VISIBLE);
                        stopProgressTimer();
                    } else if (i == Player.STATE_READY) {
                        pbHeaderProgress.setVisibility(View.GONE);

                        if (!videoStatus) {
                            pauseStream();
                        } else {
                            startProgressTimer();
                        }
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ClipActivity.this);
                    builder.setMessage("Cannot play this stream.")
                            .setPositiveButton("OK", null);
                    builder.create();

                    AlertDialog alertDialog = builder.show();

                    if (!LocalDataUtil.getThemeName(ClipActivity.this).equals("White Theme")) {
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                    } else {
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FF000000"));
                    }
                }
            });
        } else {
            simpleExoPlayerView.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i2) {
                        if (i == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START || i == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                            pbHeaderProgress.setVisibility(View.GONE);

                            if (!videoStatus) {
                                pauseStream();
                            } else {
                                startProgressTimer();
                            }
                        }

                        else if (i == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                            pbHeaderProgress.setVisibility(View.VISIBLE);
                            stopProgressTimer();
                        }

                        return true;
                    }
                });
            } else {
                //Find a way to detect video buffering time in API 16
            }
        }

        mVolumeIcon = (ImageView) findViewById(R.id.volume_icon);
        mVolumeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetDelayHiding();
                toggleVolume();
            }
        });

        mFullscreenIcon = (ImageView) findViewById(R.id.rotate_screen_icon);
        mFullscreenIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetDelayHiding();
                toggleFullscreen();
            }
        });

        mClickIntercepter = findViewById(R.id.click_intercepter);
        mVideoForeground = findViewById(R.id.video_foreground);
        mVideoForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isVideoInterfaceShowing()) {
                    hideVideoInterface();
                } else {
                    showVideoInterface();

                    if (!isInterfaceShowing && isFullscreen && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        View decorView = getWindow().getDecorView();
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
                    }

                    resetDelayHiding();
                }
            }
        });

        open_vod_layout = (RelativeLayout) findViewById(R.id.open_vod_layout);
        open_vod = (TextView) findViewById(R.id.open_vod);
        if (vodId.equals("Full Video Unavailable")) {
            open_vod.setText(vodId);
            open_vod.setTextColor(ContextCompat.getColor(this, R.color.grey_35));
        } else {
            open_vod.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    open_vod.setText("");
                    pb_vod_load = (ProgressBar) findViewById(R.id.pb_vod_load);
                    pb_vod_load.setVisibility(View.VISIBLE);

                    new FetchVideoTask(new FetchVideoTask.FetchVideoCallback() {
                        @Override
                        public void onVideoFetched(String vodLength) {
                            pb_vod_load.setVisibility(View.GONE);
                            open_vod.setText("Continue Watching");

                            Intent intent = new Intent(getApplicationContext(), VODActivity.class);
                            intent.putExtra("display_name", display_name);
                            intent.putExtra("vodId", vodId);
                            intent.putExtra("vodLength", vodLength);
                            intent.putExtra("vodOffset", vodOffset);

                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_up, R.anim.anim_stay);
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, vodId);
                }
            });
        }

        clip_seekbar = (SeekBar) findViewById(R.id.clip_seekbar);
        clip_seekbar.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);
        clip_seekbar.getThumb().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);
        clip_seekbar.setPadding(clip_seekbar.getThumb().getIntrinsicWidth() / 2, 0, clip_seekbar.getThumb().getIntrinsicWidth() / 2, 0);
        clip_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress >= Integer.parseInt(clipLength)) {
                    pauseStream();

                    clipEnd = true;
                    pbHeaderProgress.setVisibility(View.GONE);

                    stopProgressTimer();
                } else if (progress != currentProgress+1) {
                    if ((!exoplayerStatus && videoView.isPlaying()) || (exoplayerStatus && player.getPlayWhenReady())) {
                        pbHeaderProgress.setVisibility(View.VISIBLE);
                    }
                    stopProgressTimer();

                    if (clipEnd) {
                        clipEnd = false;
                    }

                    if (exoplayerStatus) {
                        player.seekTo(progress * 1000);
                    } else {
                        videoView.seekTo(progress * 1000);
                    }
                }
                currentProgress = progress;
                time_current.setText(MeasurementUtil.convertTime(currentProgress*1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                delayAnimationHandler.removeCallbacks(hideAnimationRunnable);
            }

            @Override public void onStopTrackingTouch(SeekBar seekBar) { resetDelayHiding(); }
        });
        clip_seekbar.setMax(Integer.parseInt(clipLength));

        quick_seek_time = LocalDataUtil.getQuickSeekTime(getApplicationContext());
        ic_rewind = (ImageView) findViewById(R.id.rewind_icon);
        ic_rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetDelayHiding();
                if ((!exoplayerStatus && videoView.isPlaying()) || (exoplayerStatus && player.getPlayWhenReady())) {
                    pbHeaderProgress.setVisibility(View.VISIBLE);
                }
                stopProgressTimer();

                if (clipEnd) {
                    clipEnd = false;
                }

                clip_seekbar.setProgress(currentProgress - quick_seek_time);
            }
        });
        ic_forward = (ImageView) findViewById(R.id.forward_icon);
        ic_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetDelayHiding();
                if ((!exoplayerStatus && videoView.isPlaying()) || (exoplayerStatus && player.getPlayWhenReady())) {
                    pbHeaderProgress.setVisibility(View.VISIBLE);
                }
                stopProgressTimer();

                clip_seekbar.setProgress(currentProgress + quick_seek_time);
            }
        });

        mControlToolbar = (RelativeLayout) findViewById(R.id.control_toolbar_wrapper);
        mControlToolbar.bringToFront();

        mPauseIcon = (ImageView) findViewById(R.id.pause_icon);
        mPlayIcon = (ImageView) findViewById(R.id.play_icon);
        mPlayPauseWrapper = (FrameLayout) findViewById(R.id.play_pause_wrapper);
        mPlayPauseWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if ((!exoplayerStatus && videoView.isPlaying()) || (exoplayerStatus && player.getPlayWhenReady())) {
                        pauseStream();
                    } else {
                        pbHeaderProgress.setVisibility(View.VISIBLE);

                        stopProgressTimer();

                        if (clipEnd) {
                            clipEnd = false;
                            currentProgress = 0;
                            clip_seekbar.setProgress(currentProgress);
                        }

                        resumeStream();
                        resetDelayHiding();
                    }
                } catch (Exception e) { }
            }
        });

        new ParseClipSlugTask(new ParseClipSlugTask.ParseClipSlugCallback() {
            @Override
            public void onClipSlugParsed(HashMap<String, String> clipUrls) {
                if (clipUrls != null) {
                    availableQualities.putAll(clipUrls);
                    String clipUrl = setupStreamBottomSheet(clipUrls);
                    startVideo(clipUrl);
                } else {
                    Toast.makeText(getApplicationContext(), "Failure to retrieve clip.", Toast.LENGTH_LONG).show();
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, clipSlug);

        View view = getLayoutInflater().inflate(R.layout.bottomsheet_stream, null);
        mQualityBottomSheet = new BottomSheetDialog(this);
        mQualityBottomSheet.setContentView(view);
        final BottomSheetBehavior behavior = BottomSheetBehavior.from((View) view.getParent());
        mQualityBottomSheet.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isFullscreen = true;
            updateUI();
        }

        delayAnimationHandler.postDelayed(hideAnimationRunnable, HIDE_ANIMATION_DELAY);

        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (android.provider.Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
                    if (orientation < 0) {
                        return; // Flip screen, Not take account
                    }

                    int curOrientation = ORIENTATION_NULL;

                    if (orientation >= 0 && orientation <= 30) {
                        curOrientation = ORIENTATION_PORTRAIT;
                    } else if (orientation >= 60 && orientation <= 120) {
                        curOrientation = ORIENTATION_LANDSCAPE_REVERSE;
                    } else if (orientation >= 150 && orientation <= 210) {
                        curOrientation = ORIENTATION_PORTRAIT_REVERSE;
                    } else if (orientation >= 240 && orientation <= 300) {
                        curOrientation = ORIENTATION_LANDSCAPE;
                    } else if (orientation >= 330 && orientation <= 360) {
                        curOrientation = ORIENTATION_PORTRAIT;
                    }

                    if (curOrientation != lastOrientation) {
                        onChanged(curOrientation);
                        lastOrientation = curOrientation;
                    }
                }
            }

            public void onChanged(int current) {
                if (current == ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (current == ORIENTATION_LANDSCAPE_REVERSE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                } else if (current == ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        };
        orientationEventListener.enable();
    }

    private void startProgressTimer() {
        progressStatus = true;
        progressHandler.removeCallbacks(progressRunnable);
        progressHandler.postDelayed(progressRunnable, 1000);
    }

    private void stopProgressTimer() {
        progressStatus = false;
        progressHandler.removeCallbacks(progressRunnable);
    }

    private void resetDelayHiding() {
        if ((!exoplayerStatus && videoView.isPlaying()) || (exoplayerStatus && player.getPlayWhenReady())) {
            delayAnimationHandler.removeCallbacks(hideAnimationRunnable);
            delayAnimationHandler.postDelayed(hideAnimationRunnable, HIDE_ANIMATION_DELAY);
        }
    }

    /* Checks if the video interface is fully showing */
    public boolean isVideoInterfaceShowing() {
        return mControlToolbar.getAlpha() == 1f;
    }

    private void showVideoInterface() {
        isInterfaceShowing = true;
        mToolbar.animate().alpha(1f).start();
        mControlToolbar.animate().alpha(1f).start();
        open_vod_layout.animate().alpha(1f).start();
        changeVideoControlClickablity(true);
    }

    private void hideVideoInterface() {
        if (mToolbar != null) {
            isInterfaceShowing = false;
            mToolbar.animate().alpha(0f).start();
            mControlToolbar.animate().alpha(0f).start();
            open_vod_layout.animate().alpha(0f).start();
            changeVideoControlClickablity(false);
            setAndroidUIMode(); //hide status bar
        }
    }

    private void changeVideoControlClickablity(boolean clickable) {
        mClickIntercepter.setVisibility(clickable ? View.GONE : View.VISIBLE);
        mClickIntercepter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVideoForeground.performClick();
            }
        });
    }

    private void startVideo(final String video_link) {
        videoStatus = true; //auto start
        pbHeaderProgress.setVisibility(View.VISIBLE);

        currentUrl = video_link;

        if (exoplayerStatus) {
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Impulse-Exoplayer"));
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            ExtractorMediaSource videoSource = new ExtractorMediaSource(Uri.parse(video_link), dataSourceFactory, extractorsFactory, null, null);
            final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);
            player.prepare(loopingSource);
            player.setPlayWhenReady(true);
            player.seekTo(currentProgress * 1000);
        } else {
            videoView.setVideoURI(Uri.parse(video_link));
            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                    if (what == 100) {
                        videoView.stopPlayback();
                        startVideo(video_link);
                    }
                    return true;
                }
            });
            videoView.seekTo(currentProgress*1000);
        }

        showPauseIcon();
        keepScreenOn();
    }

    private void resumeStream() {
        videoStatus = true;
        pbHeaderProgress.setVisibility(View.VISIBLE);

        if (exoplayerStatus) {
            player.setPlayWhenReady(true);
            player.seekTo(currentProgress*1000);
        } else {
            videoView.resume();
            videoView.seekTo(currentProgress*1000);
        }

        showPauseIcon();
        keepScreenOn();
    }

    private void pauseStream() {
        videoStatus = false;
        delayAnimationHandler.removeCallbacks(hideAnimationRunnable);
        pbHeaderProgress.setVisibility(View.GONE);

        if (exoplayerStatus) {
            player.setPlayWhenReady(false);
        } else {
            videoView.pause();
        }

        stopProgressTimer();
        showPlayIcon();
        releaseScreenOn();
    }

    private void rotatePlayPauseWrapper() {
        RotateAnimation rotate = new RotateAnimation(mPlayPauseWrapper.getRotation(), 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(PLAY_PAUSE_ANIMATION_DURATION);
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());
        mPlayPauseWrapper.startAnimation(rotate);
    }

    private void showPlayIcon() {
        if (mPauseIcon.getAlpha() != 0f) {
            rotatePlayPauseWrapper();
            mPauseIcon.animate().alpha(0f).start();
            mPlayIcon.animate().alpha(1f).start();
        }
    }

    private void showPauseIcon() {
        if (mPauseIcon.getAlpha() == 0f) {
            rotatePlayPauseWrapper();
            mPauseIcon.animate().alpha(1f).start();
            mPlayIcon.animate().alpha(0f).start();
        }
    }

    public void toggleVolume() {
        isMuted = !isMuted;
        if (isMuted) {
            if (!exoplayerStatus) { videoView.mute(); }
            else { player.setVolume(0f); }
            mVolumeIcon.setImageResource(R.drawable.ic_volume_off_white_24dp);
        } else {
            if (!exoplayerStatus) {videoView.unmute(); }
            else { player.setVolume(1f); }
            mVolumeIcon.setImageResource(R.drawable.ic_volume_up_white_24dp);
        }
    }

    public void toggleFullscreen() {
        if(!isFullscreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public void updateFullscreenIcon() {
        if (isFullscreen) {
            mFullscreenIcon.setImageResource(R.drawable.ic_fullscreen_exit_white_24dp);
        } else {
            mFullscreenIcon.setImageResource(R.drawable.ic_fullscreen_white_24dp);
        }
    }

    public void updateUI() {
        setAndroidUIMode();
        updateFullscreenIcon();
    }

    private void setAndroidUIMode() {
        View decorView = getWindow().getDecorView();
        if(isFullscreen) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        } else {
            decorView.setSystemUiVisibility(0); // Remove all flags.
        }
    }

    public String setupStreamBottomSheet(HashMap<String, String> clipUrls) {
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_one));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_two));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_three));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_four));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_five));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_six));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_seven));

        String defaultQualitySaved;
        if (!ConnectionUtil.isNetworkLimited(this)) {
            defaultQualitySaved = LocalDataUtil.getDefaultQualityWifi(this, "clips");
        } else {
            defaultQualitySaved = LocalDataUtil.getDefaultQualityMobile(this, "clips");
        }

        if (defaultQualitySaved.equals("720p60")) {
            defaultQualitySaved = "721p"; //need to up the resolution number
        }

        String defaultQuality = "";
        int countQualities = 0;
        for (Map.Entry<String, String> entry : clipUrls.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            supportedQualities.get(countQualities).setText(key);
            supportedQualities.get(countQualities).setVisibility(View.VISIBLE);
            setQualityOnClick(supportedQualities.get(countQualities));

            if (defaultQualitySaved.equals("Source") && countQualities == 0) {
                defaultQuality = value;
                supportedQualities.get(countQualities).setBackgroundColor(ContextCompat.getColor(this, R.color.grey_400));
            } else if (defaultQualitySaved.equals(key)) {
                defaultQuality = value;
                supportedQualities.get(countQualities).setBackgroundColor(ContextCompat.getColor(this, R.color.grey_400));
            } else if (defaultQuality.equals("")) {
                int numInt = Integer.parseInt(defaultQualitySaved.substring(0, defaultQualitySaved.indexOf("p")));
                int tempInt = Integer.parseInt(key.substring(0, key.indexOf("p")));
                if (tempInt < numInt) {
                    defaultQuality = value;
                    supportedQualities.get(countQualities).setBackgroundColor(ContextCompat.getColor(this, R.color.grey_400));
                } else if (countQualities == clipUrls.size()-1){
                    defaultQuality = value;
                    supportedQualities.get(countQualities).setBackgroundColor(ContextCompat.getColor(this, R.color.grey_400));
                }
            }

            countQualities++;
        }

        mQualityBottomSheet.findViewById(R.id.viewing_settings_text).setVisibility(View.GONE);
        mQualityBottomSheet.findViewById(R.id.audio_only).setVisibility(View.GONE);
        mQualityBottomSheet.findViewById(R.id.chat_only).setVisibility(View.GONE);

        TextView external_play = (TextView) mQualityBottomSheet.findViewById(R.id.external_play);
        assert external_play != null;
        external_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent external = new Intent(Intent.ACTION_VIEW);
                external.setDataAndType(Uri.parse(currentUrl), "video/*");
                startActivity(Intent.createChooser(external, "Play with..."));
                mQualityBottomSheet.dismiss();
            }
        });

        //aspect ratio 0.5625(9/16) is 16:9
        if (MeasurementUtil.getAspectRatio(this) < 0.5625) {
            RelativeLayout crop_to_fit_wrapper = (RelativeLayout) mQualityBottomSheet.findViewById(R.id.crop_to_fit_wrapper);
            crop_to_fit_wrapper.setVisibility(View.VISIBLE);
        }

        CheckBox crop_to_fit_checkbox = (CheckBox) mQualityBottomSheet.findViewById(R.id.crop_to_fit_checkbox);
        crop_to_fit_checkbox.setChecked(LocalDataUtil.getCropToFitStatus(this));
        crop_to_fit_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalDataUtil.setCropToFitStatus(getApplicationContext(), ((CheckBox) view).isChecked());
                mQualityBottomSheet.dismiss();

                if (exoplayerStatus) {
                    if (((CheckBox) view).isChecked()) {
                        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
                    } else {
                        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                    }
                    simpleExoPlayerView.requestLayout();
                    simpleExoPlayerView.invalidate();
                } else {
                    videoView.requestLayout();
                    videoView.invalidate();
                }

                if (((CheckBox) view).isChecked()) {
                    Toast.makeText(getApplicationContext(), "Cropped to fit", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Set to original", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return defaultQuality;
    }

    private void setQualityOnClick(final TextView qualityView) {
        qualityView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopProgressTimer();
                String quality = String.valueOf(qualityView.getText()).toLowerCase();
                startVideo(availableQualities.get(quality));
                removeQualityViewBackground();
                setQualityViewBackground(quality);
                mQualityBottomSheet.dismiss();
            }
        });
    }

    private void removeQualityViewBackground() {
        for (TextView qualityView : supportedQualities) {
            if (qualityView != null) {
                qualityView.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
            }
        }
    }

    public void setQualityViewBackground(String quality) {
        for (TextView qualityView : supportedQualities) {
            if (qualityView != null) {
                String value = qualityView.getText().toString().toLowerCase();
                if (value.equals(quality)) {
                    qualityView.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_400));
                }
            }
        }
    }

    private void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void releaseScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isFullscreen = true;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            isFullscreen = false;
        }
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_clip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.menu_item_settings:
                mQualityBottomSheet.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (exoplayerStatus) {
            player.seekTo(currentProgress * 1000);
        } else {
            videoView.seekTo(currentProgress * 1000);
        }
    }

    @Override
    public void onBackPressed() {
        int auto_rotation = android.provider.Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        if (auto_rotation != 1 && isFullscreen) {
            toggleFullscreen();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isInterfaceShowing) {
            showVideoInterface();
        }
        pauseStream();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        orientationEventListener.disable();
        if (player != null) { player.release(); }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_down);
    }

}
