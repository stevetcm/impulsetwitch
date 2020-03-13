package com.orangemuffin.impulse.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.PictureInPictureParams;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
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
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Rational;
import android.view.LayoutInflater;
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
import android.widget.NumberPicker;
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
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
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
import com.orangemuffin.impulse.receivers.MyAdminReceiver;
import com.orangemuffin.impulse.tasks.FetchVODTask;
import com.orangemuffin.impulse.utils.ConnectionUtil;
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.orangemuffin.impulse.utils.MeasurementUtil;
import com.orangemuffin.impulse.views.SimpleVideoView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/* Created by OrangeMuffin on 2018-03-22 */
public class VODActivity extends AppCompatActivity {
    private final int PLAY_PAUSE_ANIMATION_DURATION = 500;
    private final int HIDE_ANIMATION_DELAY = 3250;

    private final int ORIENTATION_NULL = -1;
    private final int ORIENTATION_PORTRAIT = 0;
    private final int ORIENTATION_LANDSCAPE = 1;
    private final int ORIENTATION_PORTRAIT_REVERSE = 2;
    private final int ORIENTATION_LANDSCAPE_REVERSE = 3;

    private ArrayList<TextView> supportedQualities = new ArrayList<>();
    HashMap<String, String> availableStreams = new HashMap<>();

    private RelativeLayout activity_vod;
    private String display_name, vodId, vodLength;

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

    SeekBar vod_seekbar;
    private int currentProgress = 0;
    TextView time_current, time_end;

    ImageView ic_rewind, ic_forward;
    private int quick_seek_time = 5;

    private final Handler delayAnimationHandler	= new Handler();
    private final Handler progressHandler = new Handler();

    private final Runnable hideAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            hideVideoInterface();
        }
    };
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (progressStatus) { //loop until stopped
                vod_seekbar.setProgress(currentProgress + 1);
                progressHandler.postDelayed(this, 1000);
            }
        }
    };

    private BottomSheetDialog mQualityBottomSheet;
    private String currentUrl;

    private boolean vodEnd = false, progressStatus = false, videoStatus = false;

    private OrientationEventListener orientationEventListener;
    private int lastOrientation = 0;

    private boolean mBackstackLost = false;

    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;
    private ScheduledExecutorService scheduledTaskExecutor;
    private ScheduledFuture scheduledFuture;
    Runnable quitTask = new Runnable() {
        @Override
        public void run() {
            if (LocalDataUtil.getSleepTimerHome(VODActivity.this)) { moveTaskToBack(true); }
            if (LocalDataUtil.getSleepTimerScreen(VODActivity.this)) { devicePolicyManager.lockNow(); }
            pauseStream();
        }
    };
    private CountDownTimer countDownTimer;
    private boolean isSleepTimerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(LocalDataUtil.setupThemeLayout(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyAdminReceiver.class);
        scheduledTaskExecutor = Executors.newScheduledThreadPool(1);

        display_name = getIntent().getStringExtra("display_name");
        vodId = getIntent().getStringExtra("vodId");
        vodLength = getIntent().getStringExtra("vodLength");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        activity_vod = (RelativeLayout) findViewById(R.id.activity_vod);

        mToolbar = (Toolbar) findViewById(R.id.vod_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(display_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.bringToFront();

        pbHeaderProgress = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        pbHeaderProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);

        time_current = (TextView) findViewById(R.id.time_current);
        time_end = (TextView) findViewById(R.id.time_end);
        time_end.setText(MeasurementUtil.convertTime(Integer.parseInt(vodLength) * 1000));

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

                @Override public void onPlayerError(ExoPlaybackException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(VODActivity.this);
                    builder.setMessage("Cannot play this stream.")
                            .setPositiveButton("OK", null);
                    builder.create();

                    AlertDialog alertDialog = builder.show();

                    if (!LocalDataUtil.getThemeName(VODActivity.this).equals("White Theme")) {
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

                        if (i == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
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

        vod_seekbar = (SeekBar) findViewById(R.id.vod_seekbar);
        vod_seekbar.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);
        vod_seekbar.getThumb().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);
        vod_seekbar.setPadding(vod_seekbar.getThumb().getIntrinsicWidth()/2, 0 ,vod_seekbar.getThumb().getIntrinsicWidth()/2, 0);
        vod_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress >= Integer.parseInt(vodLength)) {
                    pauseStream();

                    vodEnd = true;
                    pbHeaderProgress.setVisibility(View.GONE);

                    stopProgressTimer();
                }
                if (progress != currentProgress+1) {
                    if ((!exoplayerStatus && videoView.isPlaying()) || (exoplayerStatus && player.getPlayWhenReady())) {
                        pbHeaderProgress.setVisibility(View.VISIBLE);
                    }
                    stopProgressTimer();

                    if (vodEnd) {
                        vodEnd = false;
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

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { resetDelayHiding(); }
        });
        vod_seekbar.setMax(Integer.parseInt(vodLength));

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

                if (vodEnd) {
                    vodEnd = false;
                }

                vod_seekbar.setProgress(currentProgress-quick_seek_time);
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

                vod_seekbar.setProgress(currentProgress+quick_seek_time);
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
                        resumeStream();
                        resetDelayHiding();

                        pbHeaderProgress.setVisibility(View.VISIBLE);

                        stopProgressTimer();

                        if (vodEnd) {
                            vodEnd = false;
                            currentProgress = 0;
                            vod_seekbar.setProgress(currentProgress);
                        }
                    }
                } catch (Exception e) { }
            }
        });

        new FetchVODTask(new FetchVODTask.FetchVODCallback() {
            @Override
            public void onVODFetched(HashMap<String, String> vodUrls) {
                if (vodUrls != null) {
                    availableStreams.putAll(vodUrls);
                    String vodUrl = setupStreamBottomSheet(vodUrls);
                    startVideo(vodUrl);
                } else {
                    Toast.makeText(getApplicationContext(), "Failure to retrieve VOD.", Toast.LENGTH_LONG).show();
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, vodId);

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
        changeVideoControlClickablity(true);
    }

    private void hideVideoInterface() {
        if (mToolbar != null) {
            isInterfaceShowing = false;
            mToolbar.animate().alpha(0f).start();
            mControlToolbar.animate().alpha(0f).start();
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
            MediaSource videoSource = new HlsMediaSource(Uri.parse(video_link), dataSourceFactory, 1, null, null);
            final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);
            player.prepare(loopingSource);
            player.setPlayWhenReady(true);
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
        }

        if (getIntent().getStringExtra("vodOffset") != null) {
            currentProgress = Integer.valueOf(getIntent().getStringExtra("vodOffset"));
        } else {
            currentProgress = LocalDataUtil.getVODProgress(this, vodId);
        }

        if (exoplayerStatus) {
            player.seekTo(currentProgress * 1000);
        } else {
            videoView.seekTo(currentProgress * 1000);
        }

        showPauseIcon();
        keepScreenOn();
    }

    private void resumeStream() {
        videoStatus = true;
        pbHeaderProgress.setVisibility(View.VISIBLE);

        if (exoplayerStatus) {
            player.setPlayWhenReady(true);
            player.seekTo(LocalDataUtil.getVODProgress(this, vodId) * 1000);
        } else {
            videoView.resume();
            videoView.seekTo(LocalDataUtil.getVODProgress(this, vodId) * 1000);
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
        LocalDataUtil.setVODProgress(this, vodId, currentProgress);

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

    public String setupStreamBottomSheet(HashMap<String, String> vodUrls) {
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_one));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_two));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_three));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_four));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_five));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_six));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_seven));

        String defaultQualitySaved;
        if (!ConnectionUtil.isNetworkLimited(this)) {
            defaultQualitySaved = LocalDataUtil.getDefaultQualityWifi(this, "vod");
        } else {
            defaultQualitySaved = LocalDataUtil.getDefaultQualityMobile(this, "vod");
        }

        if (defaultQualitySaved.equals("720p60")) {
            defaultQualitySaved = "721p"; //need to up the resolution number
        }

        String defaultQuality = "";
        int countQualities = 0;
        for (Map.Entry<String, String> entry : vodUrls.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!key.equals("audio_only")) {
                if (key.equals("auto")) {
                    supportedQualities.get(countQualities).setText("Auto");
                } else {
                    supportedQualities.get(countQualities).setText(key);
                }
                supportedQualities.get(countQualities).setVisibility(View.VISIBLE);
                setQualityOnClick(supportedQualities.get(countQualities));

                if (defaultQualitySaved.equals("Source") && countQualities == 1) {
                    defaultQuality = value;
                    supportedQualities.get(countQualities).setBackgroundColor(ContextCompat.getColor(this, R.color.grey_400));
                } else if (!key.equals("auto") && defaultQualitySaved.equals(key)) {
                    defaultQuality = value;
                    supportedQualities.get(countQualities).setBackgroundColor(ContextCompat.getColor(this, R.color.grey_400));
                } else if (!key.equals("auto") && defaultQuality.equals("")) {
                    int numInt = Integer.parseInt(defaultQualitySaved.substring(0, defaultQualitySaved.indexOf("p")));
                    int tempInt = Integer.parseInt(key.substring(0, key.indexOf("p")));
                    if (tempInt < numInt) {
                        defaultQuality = value;
                        supportedQualities.get(countQualities).setBackgroundColor(ContextCompat.getColor(this, R.color.grey_400));
                    } else if (countQualities == vodUrls.size()-2) {
                        defaultQuality = value;
                        supportedQualities.get(countQualities).setBackgroundColor(ContextCompat.getColor(this, R.color.grey_400));
                    }
                }

                countQualities++;
            }
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

        final TextView sleep_timer = (TextView) mQualityBottomSheet.findViewById(R.id.sleep_timer);
        assert sleep_timer != null;
        sleep_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isDeviceAdmin = devicePolicyManager.isAdminActive(compName);

                if (isDeviceAdmin && !isSleepTimerRunning) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(VODActivity.this);
                    LayoutInflater inflater = getLayoutInflater();

                    View dialogView = inflater.inflate(R.layout.dialog_sleep_timer, null);
                    final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_number_picker);
                    numberPicker.setMaxValue(1000);
                    numberPicker.setMinValue(1);
                    numberPicker.setValue(LocalDataUtil.getSleepTimerTime(VODActivity.this));

                    View dialogTitleView = inflater.inflate(R.layout.dialog_custom_title, null);
                    TextView custom_title = (TextView) dialogTitleView.findViewById(R.id.custom_title);
                    custom_title.setText("Sleep Timer");

                    builder.setCustomTitle(dialogTitleView)
                            .setView(dialogView)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    toggleIsSleepTimerRunning();

                                    int numberValue = numberPicker.getValue();

                                    scheduledFuture = scheduledTaskExecutor.schedule(quitTask, numberValue, TimeUnit.MINUTES);

                                    countDownTimer = new CountDownTimer(numberPicker.getValue() * 60000, 1000) {

                                        public void onTick(long millisUntilFinished) {
                                            sleep_timer.setText("Set up Sleep Timer" + " (" + MeasurementUtil.calculateTimeRemaining(millisUntilFinished) + ")");
                                        }

                                        public void onFinish() {
                                            sleep_timer.setText("Set up Sleep Timer");
                                            toggleIsSleepTimerRunning();
                                        }
                                    }.start();

                                    if (numberValue == 1) {
                                        Toast.makeText(VODActivity.this, "Sleep Timer set for 1 minute", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(VODActivity.this, "Sleep Timer set for " + numberValue + " minutes", Toast.LENGTH_SHORT).show();
                                    }

                                    LocalDataUtil.setSleepTimerTime(VODActivity.this, numberValue);
                                }
                            })
                            .setNegativeButton("CANCEL", null);
                    builder.create();

                    AlertDialog alertDialog = builder.show();
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                    mQualityBottomSheet.dismiss();
                } else if (!isDeviceAdmin) {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "If you would like to UNINSTALL this app, you will have to disable it from device administrators in:" +
                            "\nIn-app Settings > Device Administrator > Uncheck\nOR\nPhone's Settings > Security > Device Administrators > Uncheck the app");
                    startActivityForResult(intent, 11);
                } else if (isSleepTimerRunning) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(VODActivity.this);
                    LayoutInflater inflater = getLayoutInflater();

                    View dialogView = inflater.inflate(R.layout.dialog_simple_text, null);
                    TextView support_me_text = (TextView) dialogView.findViewById(R.id.custom_text);
                    support_me_text.setText("Sleep Timer is currently running. Do you want to cancel it?");

                    View dialogTitleView = inflater.inflate(R.layout.dialog_custom_title, null);
                    TextView custom_title = (TextView) dialogTitleView.findViewById(R.id.custom_title);
                    custom_title.setText("Sleep Timer");

                    builder.setCustomTitle(dialogTitleView)
                            .setView(dialogView)
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    scheduledFuture.cancel(true);
                                    countDownTimer.cancel();
                                    toggleIsSleepTimerRunning();
                                    sleep_timer.setText("Set up Sleep Timer");
                                }
                            })
                            .setNegativeButton("NO", null);
                    builder.create();

                    AlertDialog alertDialog = builder.show();
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                }
            }
        });

        return defaultQuality;
    }

    private void setQualityOnClick(final TextView qualityView) {
        qualityView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalDataUtil.setVODProgress(getApplicationContext(), vodId, currentProgress);
                String quality = String.valueOf(qualityView.getText()).toLowerCase();
                startVideo(availableStreams.get(quality));
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
        getMenuInflater().inflate(R.menu.menu_vod, menu);
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
    protected void onStart() {
        super.onStart();

        if (exoplayerStatus) {
            player.seekTo(currentProgress * 1000);
        } else {
            videoView.seekTo(currentProgress * 1000);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
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

    @Override @TargetApi(26)
    public void finish() {
        if (mBackstackLost) {
            finishAndRemoveTask();
            startActivity(Intent.makeRestartActivityTask(new ComponentName(this, MainActivity.class)));
        } else {
            super.finish();
            overridePendingTransition(0, R.anim.slide_down);
        }
    }

    @Override @TargetApi(26)
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();

        if (canGoToPiPMode()) {
            Rational aspectRatio = new Rational(16,9);
            PictureInPictureParams params = new PictureInPictureParams.Builder().setAspectRatio(aspectRatio).build();
            enterPictureInPictureMode(params);
        }
    }

    @Override @TargetApi(26)
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (!isInPictureInPictureMode) {
            mBackstackLost = true;
        } else {
            hideVideoInterface();
        }
    }

    private boolean canGoToPiPMode() {
        return LocalDataUtil.getClunkyPiPStatus(getApplicationContext())
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public void toggleIsSleepTimerRunning() {
        isSleepTimerRunning = !isSleepTimerRunning;
    }
}
