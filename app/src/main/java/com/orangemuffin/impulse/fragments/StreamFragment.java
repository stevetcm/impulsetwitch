package com.orangemuffin.impulse.fragments;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.orangemuffin.impulse.activities.LiveStreamActivity;
import com.orangemuffin.impulse.models.StreamInfo;
import com.orangemuffin.impulse.receivers.MyAdminReceiver;
import com.orangemuffin.impulse.services.PlayerService;
import com.orangemuffin.impulse.tasks.CheckChannelFollowedTask;
import com.orangemuffin.impulse.tasks.FetchLiveInfoTask;
import com.orangemuffin.impulse.tasks.FetchLogoBitmapTask;
import com.orangemuffin.impulse.tasks.FetchStreamTask;
import com.orangemuffin.impulse.tasks.FollowChannelTask;
import com.orangemuffin.impulse.tasks.UnfollowChannelTask;
import com.orangemuffin.impulse.utils.ConnectionUtil;
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.orangemuffin.impulse.utils.MeasurementUtil;
import com.orangemuffin.impulse.views.SimpleVideoView;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/* Created by OrangeMuffin on 2018-03-18 */
public class StreamFragment extends Fragment {
    private final int PLAY_PAUSE_ANIMATION_DURATION = 500;
    private final int HIDE_ANIMATION_DELAY = 2750;

    private final int ORIENTATION_NULL = -1;
    private final int ORIENTATION_PORTRAIT = 0;
    private final int ORIENTATION_LANDSCAPE = 1;
    private final int ORIENTATION_PORTRAIT_REVERSE = 2;
    private final int ORIENTATION_LANDSCAPE_REVERSE = 3;

    private ArrayList<TextView> supportedQualities = new ArrayList<>();
    HashMap<String, String> availableStreams = new HashMap<>();

    private RelativeLayout fragment_stream, video_wrapper;
    private String streamerName, display_name, streamStatus, channelId, logoUrl, gameName;
    private int viewerCount;
    private boolean followStatus = false;

    private TextView toolbar_title, toolbar_subtitle;
    private Menu toolbarMenu;
    private Toolbar mToolbar;

    private FetchStreamTask fetchStreamTask;

    private ImageView mShowChatIcon;
    private ImageView mChatLandscapeIcon;

    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private boolean exoplayerStatus = false;

    private SimpleVideoView videoView;
    private ProgressBar pbHeaderProgress;

    private RelativeLayout mControlToolbar;
    private FrameLayout mPlayPauseWrapper;
    private ImageView mVolumeIcon, mPauseIcon, mPlayIcon, mRotateScreenIcon;

    private RelativeLayout mAudioControlToolbar;
    private ProgressBar audio_only_loading;
    private FrameLayout mPlayPauseAudioWrapper;
    private ImageView mPauseAudioIcon, mPlayAudioIcon;
    private String currentQuality = "";
    private String currentUrl = "";
    private String quality360p;

    private View mVideoForeground, mClickIntercepter;

    private boolean videoStatus = false, isMuted = false, isFullscreen = false, isLandscape = false;
    private boolean isInterfaceShowing = true, isChatLandscapeShowing = false;

    private final Handler delayAnimationHandler	= new Handler();
    private final Runnable hideAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            hideVideoInterface();
        }
    };

    private final Handler delayChatScrollHandler = new Handler();
    private final Runnable chatScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (getActivity() != null) {
                ((LiveStreamActivity) getActivity()).scrollRecyclerViewToBottom();
            }
        }
    };

    private BottomSheetDialog mQualityBottomSheet;

    private boolean isChatOnly = false, isAudioOnly = false;
    private CheckBox audio_only_checkbox;

    private OrientationEventListener orientationEventListener;
    private int lastOrientation = 0;

    private TextView quality_text, streamViewers;
    private LinearLayout viewCount;

    private ViewGroup mRootView;
    private boolean chatMotionSet = false;

    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;
    private ScheduledExecutorService scheduledTaskExecutor;
    private ScheduledFuture scheduledFuture;
    Runnable quitTask = new Runnable() {
        @Override
        public void run() {
            if (LocalDataUtil.getSleepTimerHome(getActivity())) { getActivity().moveTaskToBack(true); }
            if (LocalDataUtil.getSleepTimerScreen(getActivity())) { devicePolicyManager.lockNow(); }
            pauseStream();
        }
    };
    private CountDownTimer countDownTimer;
    private boolean isSleepTimerRunning = false;

    /* required empty public constructor */
    public StreamFragment() { }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stream, container, false);
        mRootView = (ViewGroup) rootView;

        devicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        compName = new ComponentName(getActivity(), MyAdminReceiver.class);
        scheduledTaskExecutor = Executors.newScheduledThreadPool(1);

        fragment_stream = (RelativeLayout) rootView.findViewById(R.id.fragment_stream);
        video_wrapper = (RelativeLayout) rootView.findViewById(R.id.video_wrapper);
        streamerName = getArguments().getString("streamerName");
        display_name = getArguments().getString("display_name");
        streamStatus = getArguments().getString("streamStatus");
        channelId = getArguments().getString("channelId");
        logoUrl = getArguments().getString("logoUrl");
        gameName = getArguments().getString("gameName");
        viewerCount = getArguments().getInt("viewCount");

        if (!LocalDataUtil.getAccessToken(getContext()).equals("NULL")) {
            new CheckChannelFollowedTask(getContext(), new CheckChannelFollowedTask.CheckChannelFollowedCallBack() {
                @Override
                public void onChannelFollowedChecked(Boolean isFollowing) {
                    if (isFollowing) {
                        updateFollowMenuIcon(followStatus = true);
                    }
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, channelId);
        }

        mToolbar = (Toolbar) rootView.findViewById(R.id.stream_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.bringToFront();

        toolbar_title = (TextView) rootView.findViewById(R.id.toolbar_custom_title);
        toolbar_title.setText(display_name);

        toolbar_subtitle = (TextView) rootView.findViewById(R.id.toolbar_custom_subtitle);
        toolbar_subtitle.setText(streamStatus);

        HorizontalScrollView scrollView = (HorizontalScrollView) rootView.findViewById(R.id.subtitle_scroll_wrapper);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        stopDelayHiding();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!isAudioOnly) {
                            resetDelayHiding();
                        }
                        return true;
                }
                return false;
            }
        });

        mShowChatIcon = (ImageView) rootView.findViewById(R.id.show_chat_icon);
        mShowChatIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetDelayHiding();
                isFullscreen = !isFullscreen;
                updateUI();
            }
        });
        mShowChatIcon.bringToFront();

        mChatLandscapeIcon = (ImageView) rootView.findViewById(R.id.chat_visibility_icon);
        mChatLandscapeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetDelayHiding();
                toggleChatLandscape();
            }
        });
        mChatLandscapeIcon.bringToFront();
        checkChatVisibilityIconVisibility();

        videoView = (SimpleVideoView) rootView.findViewById(R.id.videoView);
        simpleExoPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.player_view);

        exoplayerStatus = LocalDataUtil.getExoplayerStatus(getContext());
        if (exoplayerStatus) {
            videoView.setVisibility(View.GONE);
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector);
            simpleExoPlayerView = new SimpleExoPlayerView(getActivity());
            simpleExoPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.player_view);

            if (LocalDataUtil.getCropToFitStatus(getContext())) {
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
                    } else if (i == Player.STATE_READY) {
                        pbHeaderProgress.setVisibility(View.GONE);

                        if (!videoStatus) {
                            pauseStream();
                        }
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Cannot play this stream.");
                    builder.setPositiveButton("OK", null);
                    builder.create();

                    AlertDialog alertDialog = builder.show();

                    if (!LocalDataUtil.getThemeName(getContext()).equals("White Theme")) {
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
                            if (isMuted) { toggleVolume(); }
                            pbHeaderProgress.setVisibility(View.GONE);

                            if (!videoStatus) {
                                pauseStream();
                            }
                        }

                        if (i == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                            pbHeaderProgress.setVisibility(View.VISIBLE);
                        }

                        return true;
                    }
                });
            } else {
                //Find a way to detect video buffering time in API 16
            }
        }

        pbHeaderProgress = (ProgressBar) rootView.findViewById(R.id.pbHeaderProgress);
        pbHeaderProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
        pbHeaderProgress.setVisibility(View.GONE);

        mControlToolbar = (RelativeLayout) rootView.findViewById(R.id.control_toolbar_wrapper);
        mControlToolbar.bringToFront();

        mVolumeIcon = (ImageView) rootView.findViewById(R.id.volume_icon);
        mVolumeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetDelayHiding();
                toggleVolume();
            }
        });

        mPauseIcon = (ImageView) rootView.findViewById(R.id.pause_icon);
        mPlayIcon = (ImageView) rootView.findViewById(R.id.play_icon);
        mPlayPauseWrapper = (FrameLayout) rootView.findViewById(R.id.play_pause_wrapper);
        mPlayPauseWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if ((!exoplayerStatus && videoView.isPlaying()) || (exoplayerStatus && player.getPlayWhenReady())) {
                        pauseStream();
                    } else {
                        resumeStream();
                        resetDelayHiding();
                    }
                } catch (Exception e) { }
            }
        });

        mAudioControlToolbar = (RelativeLayout) rootView.findViewById(R.id.audio_only_toolbar_wrapper);
        audio_only_loading = (ProgressBar) rootView.findViewById(R.id.audio_only_loading);
        audio_only_loading.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
        mPauseAudioIcon = (ImageView) rootView.findViewById(R.id.audio_only_pause_icon);
        mPlayAudioIcon = (ImageView) rootView.findViewById(R.id.audio_only_play_icon);
        mPlayPauseAudioWrapper = (FrameLayout) rootView.findViewById(R.id.audio_only_play_wrapper);
        mPlayPauseAudioWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (PlayerService.getInstance().getPlayer().getPlayWhenReady()) {
                        Intent playerService = new Intent(getContext(), PlayerService.class);
                        playerService.setAction("action_pause");
                        getContext().startService(playerService);
                    } else {
                        Intent playerService = new Intent(getContext(), PlayerService.class);
                        playerService.setAction("action_play");
                        getContext().startService(playerService);
                    }
                } catch (Exception e) { }
            }
        });

        audio_only_checkbox = (CheckBox) rootView.findViewById(R.id.audio_only_checkbox);
        audio_only_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAudioOnly) {
                    for (TextView quality : supportedQualities) {
                        if (quality.getText().toString().toLowerCase().equals(currentQuality)) {
                            quality.performClick();
                        }
                    }
                }
            }
        });

        mRotateScreenIcon = (ImageView) rootView.findViewById(R.id.rotate_screen_icon);
        mRotateScreenIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetDelayHiding();
                rotateScreen();
            }
        });

        mClickIntercepter = rootView.findViewById(R.id.click_intercepter);
        mVideoForeground = rootView.findViewById(R.id.video_foreground);
        mVideoForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //normal behaviour if portrait or chat swipe disabled
                if (!isLandscape || !LocalDataUtil.getChatSwipeStatus(getActivity())) {
                    mClickIntercepterListener();
                }
            }
        });

        quality_text = (TextView) rootView.findViewById(R.id.quality_text);
        quality_text.bringToFront();

        viewCount = (LinearLayout) rootView.findViewById(R.id.viewCount);
        viewCount.bringToFront();
        streamViewers = (TextView) rootView.findViewById(R.id.streamViewers);
        streamViewers.setText(NumberFormat.getNumberInstance(Locale.US).format(viewerCount));


        View view = getActivity().getLayoutInflater().inflate(R.layout.bottomsheet_stream, null);
        mQualityBottomSheet = new BottomSheetDialog(getContext());
        mQualityBottomSheet.setContentView(view);
        final BottomSheetBehavior behavior = BottomSheetBehavior.from((View) view.getParent());
        mQualityBottomSheet.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        setLayoutSize(); //set layout before processing

        delayAnimationHandler.postDelayed(hideAnimationRunnable, HIDE_ANIMATION_DELAY);

        fetchStreamTask = new FetchStreamTask(new FetchStreamTask.FetchStreamCallback() {
            @Override
            public void onStreamFetched(HashMap<String, String> streamUrls) {
                if (streamUrls != null) {
                    availableStreams.putAll(streamUrls);
                    String streamUrl = setupStreamBottomSheet(streamUrls);

                    if (PlayerService.getInstance() != null && PlayerService.getInstance().isPlaying()) {
                        pbHeaderProgress.setVisibility(View.GONE);
                        supportedQualities.get(supportedQualities.size()-2).performClick();
                    } else {
                        startVideo(streamUrl);

                        quality_text.animate().alpha(1f).start();
                        if (!isVideoInterfaceShowing()) {
                            mVideoForeground.performClick();
                        }
                        resetDelayHiding();

                        //in case notification is showing and not playing
                        if (PlayerService.getInstance() != null) {
                            PlayerService.removeNotification();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Failure to loading livestream.", Toast.LENGTH_LONG).show();
                }
            }
        });
        fetchStreamTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, streamerName);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
            updateUI();
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        orientationEventListener = new OrientationEventListener(getActivity()) {
            @Override
            public void onOrientationChanged(int orientation) {
                int auto_rotation = android.provider.Settings.System.getInt(getActivity().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                if (auto_rotation == 1 && !isAudioOnly && !isChatOnly) {
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
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (current == ORIENTATION_LANDSCAPE_REVERSE) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                } else if (current == ORIENTATION_PORTRAIT) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        };
        orientationEventListener.enable();

        return rootView;
    }

    private void stopDelayHiding() {
        delayAnimationHandler.removeCallbacks(hideAnimationRunnable);
    }

    private void resetDelayHiding() {
        delayAnimationHandler.removeCallbacks(hideAnimationRunnable);
        delayAnimationHandler.postDelayed(hideAnimationRunnable, HIDE_ANIMATION_DELAY);
    }

    private void showVideoInterface() {
        isInterfaceShowing = true;
        mToolbar.animate().alpha(1f).start();
        mControlToolbar.animate().alpha(1f).start();
        mShowChatIcon.animate().alpha(1f).start();
        mChatLandscapeIcon.animate().alpha(1f).start();
        viewCount.animate().alpha(1f).start();
        updateViewerCount();
        changeVideoControlClickablity(true);
    }

    private void hideVideoInterface() {
        if (mToolbar != null) {
            isInterfaceShowing = false;
            mToolbar.animate().alpha(0f).start();
            mControlToolbar.animate().alpha(0f).start();
            mShowChatIcon.animate().alpha(0f).start();
            mChatLandscapeIcon.animate().alpha(0f).start();
            quality_text.animate().alpha(0f).start();
            viewCount.animate().alpha(0f).start();
            changeVideoControlClickablity(false);
            setAndroidUIMode(); //hide status bar
        }
    }

    private void changeVideoControlClickablity(boolean clickable) {
        mClickIntercepter.setVisibility(clickable ? View.GONE : View.VISIBLE);
        mClickIntercepter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //normal behaviour if portrait or chat swipe disabled
                if (!isLandscape || !LocalDataUtil.getChatSwipeStatus(getActivity())) {
                    mVideoForeground.performClick();
                }
            }
        });
    }

    private void playAudioOnly(final String audio_only_url) {
        if (audio_only_url != null) {
            if (PlayerService.getInstance() == null || !PlayerService.getInstance().isPlaying() || !PlayerService.getInstance().getDisplayName().equals(display_name)) {
                new FetchLogoBitmapTask(new FetchLogoBitmapTask.FetchLogoBitmapCallback() {
                    @Override
                    public void onLogoBitmapFetched(Bitmap bitmap) {
                        Intent playerService = new Intent(getContext(), PlayerService.class);
                        playerService.putExtra("url", audio_only_url);
                        playerService.putExtra("display_name", display_name);
                        playerService.putExtra("streamStatus", streamStatus);
                        playerService.putExtra("receiver", getAudioOnlyDelegateReceiver());
                        playerService.putExtra("logoBitmap", bitmap);
                        playerService.putExtra("gameName", gameName);
                        playerService.putExtra("actualIntent", getActivity().getIntent());

                        getContext().startService(playerService);

                        if (PlayerService.getInstance() != null && PlayerService.getInstance().isPlaying() && !PlayerService.getInstance().getDisplayName().equals(display_name)) {
                            Toast.makeText(getContext(), "Switching Audio to " + display_name + "'s Stream", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, logoUrl);
            } else {
                audio_only_loading.setVisibility(View.GONE);
            }
        }
    }

    private void stopAudioOnly() {
        Intent playerService = new Intent(getContext(), PlayerService.class);
        playerService.setAction("action_stop");
        getContext().startService(playerService);
    }

    private void startVideo(final String video_link) {
        videoStatus = true; //auto start
        pbHeaderProgress.setVisibility(View.VISIBLE);

        currentUrl = video_link;

        if (exoplayerStatus) {
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getActivity(), Util.getUserAgent(getActivity(), "Impulse-Exoplayer"));
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

        keepScreenOn();
        showPauseIcon();
    }

    private void resumeStream() {
        videoStatus = true;
        pbHeaderProgress.setVisibility(View.VISIBLE);

        if (exoplayerStatus) {
            player.setPlayWhenReady(true);
            player.seekToDefaultPosition();
        } else {
            videoView.resume();
        }

        showPauseIcon();
        keepScreenOn();
    }

    private void pauseStream() {
        videoStatus = false;
        stopDelayHiding();

        if (exoplayerStatus) {
            player.setPlayWhenReady(false);
        } else {
            videoView.pause();
        }

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

    public boolean isVideoInterfaceShowing() {
        return mControlToolbar.getAlpha() == 1f;
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

    public void rotateScreen() {
        if (!isLandscape) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public void updateUI() {
        setAndroidUIMode();
        setLayoutSize();
        updateChatPortraitIcon();
        checkChatVisibilityIconVisibility();

        if (isLandscape && !chatMotionSet) {
            chatMotionSet = true;
            setupChatLandscapeMotionEvent();
        }
    }

    private void setAndroidUIMode() {
        View decorView = getActivity().getWindow().getDecorView();
        if (isLandscape) {
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

    private void setLayoutSize() {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        if (isLandscape || isFullscreen) {
            fragment_stream.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
            fragment_stream.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
        } else {
            fragment_stream.getLayoutParams().height = (int) ((metrics.widthPixels) / (16 / 9.0));
            fragment_stream.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
        }
        fragment_stream.invalidate();
        fragment_stream.requestLayout();
    }

    public void updateChatPortraitIcon() {
        if (isFullscreen) {
            mShowChatIcon.setImageResource(R.drawable.ic_show_chat_white_24dp);
        } else {
            mShowChatIcon.setImageResource(R.drawable.ic_hide_chat_white_24dp);
        }
    }

    public void checkChatVisibilityIconVisibility() {
        if (isLandscape) {
            mChatLandscapeIcon.setVisibility(View.VISIBLE);
            mShowChatIcon.setVisibility(View.GONE);
        } else {
            mChatLandscapeIcon.setVisibility(View.GONE);
            mShowChatIcon.setVisibility(View.VISIBLE);
        }
    }

    public String setupStreamBottomSheet(final HashMap<String, String> streamUrls) {
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_one));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_two));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_three));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_four));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_five));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_six));
        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.quality_seven));

        String defaultQualitySaved;
        if (!ConnectionUtil.isNetworkLimited(getContext())) {
            defaultQualitySaved = LocalDataUtil.getDefaultQualityWifi(getContext(), "live");
        } else {
            defaultQualitySaved = LocalDataUtil.getDefaultQualityMobile(getContext(), "live");
        }

        if (defaultQualitySaved.equals("720p60")) {
            defaultQualitySaved = "721p"; //need to up the resolution number
        }

        String defaultQuality = "";
        int countQualities = 0;
        for (Map.Entry<String, String> entry : streamUrls.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!key.equals("audio_only")) {
                if (key.equals("auto")) {
                    supportedQualities.get(countQualities).setText("Auto");
                } else if (key.contains("(source)")) {
                    String newKey = key;
                    supportedQualities.get(countQualities).setText(newKey.replace("(source)", "(Source)"));
                } else {
                    supportedQualities.get(countQualities).setText(key);
                }
                supportedQualities.get(countQualities).setVisibility(View.VISIBLE);
                setQualityOnClick(supportedQualities.get(countQualities));

                if (defaultQualitySaved.equals("Source") && key.contains("(source)")) {
                    defaultQuality = value;
                    String newKey = key;
                    quality_text.setText(" " + newKey.replace("(source)", "(Source)") + " ");
                    supportedQualities.get(countQualities).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_400));
                    currentQuality = supportedQualities.get(countQualities).getText().toString().toLowerCase();
                } else if (!key.equals("auto") && defaultQualitySaved.equals(key)) {
                    defaultQuality = value;
                    quality_text.setText(" " + key + " ");
                    supportedQualities.get(countQualities).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_400));
                    currentQuality = supportedQualities.get(countQualities).getText().toString().toLowerCase();
                } else if (!key.equals("auto") && defaultQuality.equals("")) {
                    int numInt = Integer.parseInt(defaultQualitySaved.substring(0, defaultQualitySaved.indexOf("p")));

                    //source link creating conflicted selection
                    int offset = 0;
                    if (key.contains("(source)")) {
                        offset = 2;
                    }

                    int tempInt = Integer.parseInt(key.substring(0, key.indexOf("p"))) + offset;
                    if (tempInt < numInt) {
                        defaultQuality = value;
                        quality_text.setText(" " + key + " ");
                        supportedQualities.get(countQualities).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_400));
                        currentQuality = supportedQualities.get(countQualities).getText().toString().toLowerCase();
                    } else if (countQualities == streamUrls.size()-2) { //last item is "audio_only"
                        defaultQuality = value;
                        quality_text.setText(" " + key + " ");
                        supportedQualities.get(countQualities).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_400));
                        currentQuality = supportedQualities.get(countQualities).getText().toString().toLowerCase();
                    }
                }

                countQualities++;
            }

            if (key.contains("360")) {
                quality360p = value;
                ((LiveStreamActivity) getActivity()).setLowestUrl(value);
            }
        }

        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.audio_only));
        supportedQualities.get(supportedQualities.size()-1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAudioOnly) {
                    if (isChatOnly) {
                        isChatOnly = false;
                    }
                    pauseStream();
                    removeQualityViewBackground();
                    setQualityViewBackground("audio only");
                    toggleAudioOnlyMode();
                    playAudioOnly(streamUrls.get("audio_only"));
                    ((LiveStreamActivity) getActivity()).setLowestUrl(null);
                }
                mQualityBottomSheet.dismiss();
            }
        });

        supportedQualities.add((TextView) mQualityBottomSheet.findViewById(R.id.chat_only));
        supportedQualities.get(supportedQualities.size()-1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isChatOnly) {
                    if (isAudioOnly) {
                        isAudioOnly = false;
                        stopAudioOnly();
                    }
                    pauseStream();
                    removeQualityViewBackground();
                    setQualityViewBackground("chat only");
                    toggleChatOnlyMode();
                    ((LiveStreamActivity) getActivity()).setLowestUrl(null);
                }
                mQualityBottomSheet.dismiss();
            }
        });


        TextView external_play = (TextView) mQualityBottomSheet.findViewById(R.id.external_play);
        assert external_play != null;
        external_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent external = new Intent(Intent.ACTION_VIEW);
                external.setDataAndType(Uri.parse(currentUrl), "video/*");
                getActivity().startActivity(Intent.createChooser(external, "Play with..."));
                mQualityBottomSheet.dismiss();
            }
        });

        //aspect ratio 0.5625(9/16) is 16:9
        if (MeasurementUtil.getAspectRatio(getActivity()) < 0.5625) {
            RelativeLayout crop_to_fit_wrapper = (RelativeLayout) mQualityBottomSheet.findViewById(R.id.crop_to_fit_wrapper);
            crop_to_fit_wrapper.setVisibility(View.VISIBLE);
        }

        CheckBox crop_to_fit_checkbox = (CheckBox) mQualityBottomSheet.findViewById(R.id.crop_to_fit_checkbox);
        crop_to_fit_checkbox.setChecked(LocalDataUtil.getCropToFitStatus(getContext()));
        crop_to_fit_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalDataUtil.setCropToFitStatus(getContext(), ((CheckBox) view).isChecked());
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
                    Toast.makeText(getContext(), "Cropped to fit", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Set to original", Toast.LENGTH_SHORT).show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getLayoutInflater();

                    View dialogView = inflater.inflate(R.layout.dialog_sleep_timer, null);
                    final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_number_picker);
                    numberPicker.setMaxValue(1000);
                    numberPicker.setMinValue(1);
                    numberPicker.setValue(LocalDataUtil.getSleepTimerTime(getActivity()));

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
                                        Toast.makeText(getContext(), "Sleep Timer set for 1 minute", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Sleep Timer set for " + numberValue + " minutes", Toast.LENGTH_SHORT).show();
                                    }

                                    LocalDataUtil.setSleepTimerTime(getActivity(), numberValue);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                String quality = String.valueOf(qualityView.getText()).toLowerCase();
                startVideo(availableStreams.get(quality));
                removeQualityViewBackground();
                setQualityViewBackground(quality);

                if (!isChatOnly && !isAudioOnly) {
                    quality_text.setText(" " + qualityView.getText() + " ");
                    quality_text.animate().alpha(1f).start();
                    if (!isVideoInterfaceShowing()) {
                        mVideoForeground.performClick();
                    }
                    resetDelayHiding();
                }

                if (isChatOnly) { toggleChatOnlyMode(); }

                if (isAudioOnly) {
                    stopAudioOnly();
                    toggleAudioOnlyMode();
                }

                currentQuality = quality;

                ((LiveStreamActivity) getActivity()).setLowestUrl(quality360p);

                mQualityBottomSheet.dismiss();
            }
        });
    }

    private void removeQualityViewBackground() {
        for (TextView qualityView : supportedQualities) {
            if (qualityView != null) {
                qualityView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
            }
        }
    }

    public void setQualityViewBackground(String quality) {
        for (TextView qualityView : supportedQualities) {
            if (qualityView != null) {
                String value = qualityView.getText().toString().toLowerCase();
                if (value.equals(quality)) {
                    qualityView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_400));
                }
            }
        }
    }

    private void toggleAudioOnlyMode() {
        isAudioOnly = !isAudioOnly;

        keepScreenOn(); //necessary??? prevent device from sleeping

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        if (isAudioOnly) {
            int currentHeight = (int) ((metrics.widthPixels) / (16 / 9.0));
            int targetHeight = (int) getResources().getDimension(R.dimen.main_toolbar_audio_height);
            setLayoutResizeAnimation(fragment_stream, currentHeight, targetHeight, 1);
            mToolbar.setBackground(ContextCompat.getDrawable(getContext(), LocalDataUtil.getResIdFromAttribute(getActivity(), R.attr.main_tab_background)));

            mShowChatIcon.setVisibility(View.GONE);
            quality_text.setVisibility(View.GONE);
            viewCount.setVisibility(View.GONE);
            pbHeaderProgress.setVisibility(View.GONE);

            mAudioControlToolbar.setVisibility(View.VISIBLE);

            showPauseAudioIcon();
            audio_only_loading.setVisibility(View.VISIBLE);

            audio_only_checkbox.setChecked(true);

            mControlToolbar.setVisibility(View.GONE);

            delayChatScrollHandler.postDelayed(chatScrollRunnable, 1000);
        } else {
            int currentHeight = (int) getResources().getDimension(R.dimen.main_toolbar_audio_height);
            int targetHeight = (int) ((metrics.widthPixels) / (16 / 9.0));
            setLayoutResizeAnimation(fragment_stream, currentHeight, targetHeight, 1);
            mToolbar.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.info_gradient_background_top));
            mControlToolbar.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.info_gradient_background_bottom));

            checkChatVisibilityIconVisibility();
            viewCount.setVisibility(View.VISIBLE);

            mAudioControlToolbar.setVisibility(View.GONE);

            mControlToolbar.setVisibility(View.VISIBLE);
            resetDelayHiding();
        }
        showVideoInterface();
    }

    private void toggleChatOnlyMode() {
        isChatOnly = !isChatOnly;

        keepScreenOn(); //prevent device from sleeping

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        if (isChatOnly) {
            int currentHeight = (int) ((metrics.widthPixels) / (16 / 9.0));
            int targetHeight = (int) getResources().getDimension(R.dimen.main_toolbar_height);
            setLayoutResizeAnimation(fragment_stream, currentHeight, targetHeight, 1);
            mToolbar.setBackground(ContextCompat.getDrawable(getContext(), LocalDataUtil.getResIdFromAttribute(getActivity(), R.attr.main_tab_background)));
            mShowChatIcon.setVisibility(View.GONE);
            quality_text.setVisibility(View.GONE);
            pbHeaderProgress.setVisibility(View.GONE);
            mControlToolbar.setVisibility(View.GONE);
            mAudioControlToolbar.setVisibility(View.GONE);
            delayChatScrollHandler.postDelayed(chatScrollRunnable, 1000);
        } else {
            int currentHeight = (int) getResources().getDimension(R.dimen.main_toolbar_height);
            int targetHeight = (int) ((metrics.widthPixels) / (16 / 9.0));
            setLayoutResizeAnimation(fragment_stream, currentHeight, targetHeight, 1);
            mToolbar.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.info_gradient_background_top));

            viewCount.setVisibility(View.VISIBLE);
            checkChatVisibilityIconVisibility();
            mControlToolbar.setVisibility(View.VISIBLE);
            resetDelayHiding();
        }
        showVideoInterface();
    }

    private void setLayoutResizeAnimation(final View view, int current, int target, final int side) {
        ValueAnimator slideAnimator = ValueAnimator.ofInt(current, target).setDuration(300);
        slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                if (side == 0) {
                    view.getLayoutParams().width = value;
                } else if (side == 1) {
                    view.getLayoutParams().height = value;
                }
                view.requestLayout();
            }
        });
        AnimatorSet set = new AnimatorSet();
        set.play(slideAnimator);
        set.start();
    }

    public void toggleChatLandscape() {
        isChatLandscapeShowing = !isChatLandscapeShowing;
        double chatWidth = LocalDataUtil.getChatWidth(getContext())/100.0;
        if (isLandscape && isChatLandscapeShowing) {
            int currentWidth = MeasurementUtil.getRealWidth(getActivity());
            setLayoutResizeAnimation(fragment_stream, currentWidth, (int) ((currentWidth) * (1-chatWidth)), 0);
            mChatLandscapeIcon.setImageResource(R.drawable.ic_keyboard_arrow_right_white_24dp);
        } else if (isLandscape && !isChatLandscapeShowing) {
            int screenWidth = MeasurementUtil.getRealWidth(getActivity());
            int currentWidth = (int) ((screenWidth)*(1-chatWidth));
            setLayoutResizeAnimation(fragment_stream, currentWidth, screenWidth, 0);
            mChatLandscapeIcon.setImageResource(R.drawable.ic_keyboard_arrow_left_white_24dp);
        }
    }

    public void toggleChatLandscape(int currentWidth) {
        isChatLandscapeShowing = !isChatLandscapeShowing;
        double chatWidth = LocalDataUtil.getChatWidth(getContext())/100.0;
        int screenWidth = MeasurementUtil.getRealWidth(getActivity());
        if (isLandscape && isChatLandscapeShowing) {
            setLayoutResizeAnimation(fragment_stream, currentWidth, (int) ((screenWidth) * (1 - chatWidth)), 0);
            mChatLandscapeIcon.setImageResource(R.drawable.ic_keyboard_arrow_right_white_24dp);
        } else if (isLandscape && !isChatLandscapeShowing) {
            setLayoutResizeAnimation(fragment_stream, currentWidth, screenWidth, 0);
            mChatLandscapeIcon.setImageResource(R.drawable.ic_keyboard_arrow_left_white_24dp);
        }
    }

    private void setupChatLandscapeMotionEvent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final int width = MeasurementUtil.getRealWidth(getActivity());
            final double chatWidth = LocalDataUtil.getChatWidth(getContext())/100.0;

            View.OnTouchListener touchListener = new View.OnTouchListener() {
                private int downPosition = width;
                private int widthOnDown = width;

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    //if landscape and chat swipe is enabled
                    if (isLandscape && LocalDataUtil.getChatSwipeStatus(getActivity())) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fragment_stream.getLayoutParams();
                        final int X = (int) event.getRawX(); //current x-axis coordinate

                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN:
                                if (params.width > 0) { //get width on down action
                                    widthOnDown = params.width;
                                }
                                downPosition = (int) event.getRawX(); //starting x-axis coordinate
                                break;
                            case MotionEvent.ACTION_UP:
                                int upPosition = (int) event.getRawX(); //ending x-axis coordinate
                                int deltaPostion = upPosition - downPosition; //moving quantity

                                //if moving quantity around ±35, do NOTHING but reset MOVED ACTION
                                if ((deltaPostion < 35 && deltaPostion > -35)
                                        || (deltaPostion < 0 && isChatLandscapeShowing)
                                        || (deltaPostion >= 0 && !isChatLandscapeShowing)) {
                                    params.width = widthOnDown;
                                    fragment_stream.setLayoutParams(params);

                                    mClickIntercepterListener();

                                    return false;
                                }

                                int currentSize = widthOnDown + deltaPostion;

                                //limit opening size
                                if (currentSize < width - (width*chatWidth)) {
                                    currentSize = (int) (width - (width*chatWidth));
                                }

                                //condition to trigger toggle
                                if (currentSize <= width ) {
                                    toggleChatLandscape(currentSize);
                                } else { //limit closing size
                                    toggleChatLandscape(width);
                                }

                                break;
                            case MotionEvent.ACTION_MOVE:
                                int newWidth = 0;

                                //resulting amount
                                if (X > downPosition) { // Swiping right
                                    newWidth = widthOnDown + (X - downPosition);
                                } else if (!isChatLandscapeShowing) { // Swiping left
                                    newWidth = widthOnDown - (downPosition - X);
                                }

                                //condition to motion update
                                if (newWidth > width - (width*chatWidth)) {
                                    params.width = newWidth;
                                } else { //limit opening size motion
                                    params.width = (int) (width - (width*chatWidth));
                                }

                                //limit closing size motion
                                if (newWidth > width) {
                                    params.width = width;
                                }

                                fragment_stream.setLayoutParams(params);
                                break;
                        }
                        fragment_stream.invalidate();
                    }
                    return false;
                }
            };

            mVideoForeground.setOnTouchListener(touchListener);
            mClickIntercepter.setOnTouchListener(touchListener);
        }
    }

    private void mClickIntercepterListener() {
        if (isVideoInterfaceShowing()) {
            hideVideoInterface();
        } else {
            showVideoInterface();

            if (!isInterfaceShowing && isLandscape && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                View decorView = getActivity().getWindow().getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
            }

            resetDelayHiding();
        }
    }

    private void keepScreenOn() {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void releaseScreenOn() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private ResultReceiver getAudioOnlyDelegateReceiver() {
        return new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                try {
                    switch (resultCode) {
                        case PlayerService.DELEGATE_PLAY:
                            showPauseAudioIcon();
                            break;
                        case PlayerService.DELEGATE_PAUSE:
                            showPlayAudioIcon();
                            break;
                        case PlayerService.DELEGATE_STOP:
                            audio_only_checkbox.performClick();
                            break;
                        case PlayerService.DELEGATE_LOADING_START:
                            audio_only_loading.setVisibility(View.VISIBLE);
                            break;
                        case PlayerService.DELEGATE_LOADING_STOP:
                            audio_only_loading.setVisibility(View.GONE);
                            break;
                        case PlayerService.DELEGATE_STREAM_ERROR:
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage("Cannot play this stream.")
                                    .setPositiveButton("OK", null);
                            builder.create();

                            AlertDialog alertDialog = builder.show();

                            if (!LocalDataUtil.getThemeName(getContext()).equals("White Theme")) {
                                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                            } else {
                                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FF000000"));
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void registerAudioOnlyDelegate() {
        if (PlayerService.getInstance() != null) {
            PlayerService.getInstance().registerDelegate(getAudioOnlyDelegateReceiver());
        }
    }

    private void rotatePlayPauseAudioWrapper() {
        RotateAnimation rotate = new RotateAnimation(mPlayPauseAudioWrapper.getRotation(), 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(PLAY_PAUSE_ANIMATION_DURATION);
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());
        mPlayPauseAudioWrapper.startAnimation(rotate);
    }

    private void showPlayAudioIcon() {
        if (mPauseAudioIcon.getAlpha() != 0f) {
            rotatePlayPauseAudioWrapper();
            mPauseAudioIcon.animate().alpha(0f).start();
            mPlayAudioIcon.animate().alpha(1f).start();
        }
    }

    private void showPauseAudioIcon() {
        if (mPlayAudioIcon.getAlpha() != 0f) {
            rotatePlayPauseAudioWrapper();
            mPlayAudioIcon.animate().alpha(0f).start();
            mPauseAudioIcon.animate().alpha(1f).start();
        }
    }

    private void updateViewerCount() {
        new FetchLiveInfoTask(getContext(), new FetchLiveInfoTask.FetchLiveInfoCallback() {
            @Override
            public void onLiveInfoFetched(StreamInfo stream) {
                if (stream != null) {
                    streamViewers.setText(NumberFormat.getNumberInstance(Locale.US).format(stream.getViewCount()));
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, channelId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            isLandscape = false;

            isChatLandscapeShowing = false;
            mChatLandscapeIcon.setImageResource(R.drawable.ic_keyboard_arrow_left_white_24dp);
        }
        updateUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isAudioOnly && !isChatOnly) {
            resumeStream();
            resetDelayHiding();
        }
        registerAudioOnlyDelegate();
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
    public void onDestroy() {
        super.onDestroy();
        fetchStreamTask.cancel(true);
        orientationEventListener.disable();
        if (player != null) { player.release(); }
    }

    public boolean backPressed() {
        int auto_rotation = android.provider.Settings.System.getInt(getActivity().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        if (auto_rotation != 1 && isLandscape) {
            rotateScreen();
            return false;
        } else {
            videoView.setVisibility(View.GONE);
            return true;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_stream, menu);
        if (LocalDataUtil.getAccessToken(getContext()).equals("NULL")) {
            menu.getItem(0).setVisible(false);
        }
        toolbarMenu = menu;
    }

    private void updateFollowMenuIcon(boolean isFollowing) {
        try {
            MenuItem followItem = toolbarMenu.getItem(0);
            if (isFollowing) {
                followItem.setIcon(R.drawable.ic_favorite_white_24dp);
            } else {
                followItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
            }
        } catch (Exception e) { }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                mQualityBottomSheet.show();
                return true;
            case R.id.menu_item_follow:
                if (!followStatus) {
                    new FollowChannelTask(getContext(), new FollowChannelTask.FollowChannelCallBack() {
                        @Override
                        public void onFollowChannelSuccessful(Boolean onFollow) {
                            updateFollowMenuIcon(followStatus = onFollow);
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, channelId);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getLayoutInflater();

                    View dialogView = inflater.inflate(R.layout.dialog_simple_text, null);
                    TextView unfollow_text = (TextView) dialogView.findViewById(R.id.custom_text);
                    unfollow_text.setText("Unfollow " + display_name + "?");

                    builder.setView(dialogView)
                           .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface, int i) {

                                   new UnfollowChannelTask(getContext(), new UnfollowChannelTask.UnFollowChannelCallBack() {
                                       @Override
                                       public void onUnfollowChannelSuccessful(Boolean onUnfollow) {
                                           updateFollowMenuIcon(followStatus = !onUnfollow);
                                       }
                                   }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, channelId);
                               }
                           })
                           .setNegativeButton("NO", null);
                    builder.create();

                    AlertDialog alertDialog = builder.show();
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleIsSleepTimerRunning() {
        isSleepTimerRunning = !isSleepTimerRunning;
    }

    public boolean isChatOnly() {
        return isChatOnly;
    }

    public boolean isAudioOnly() {
        return isAudioOnly;
    }

    @Override @TargetApi(26)
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        hideVideoInterface();
    }
}
