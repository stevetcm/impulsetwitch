package com.orangemuffin.impulse.fragments;

import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.adapters.VODsListAdapter;
import com.orangemuffin.impulse.models.VODInfo;
import com.orangemuffin.impulse.tasks.CheckLiveChannelTask;
import com.orangemuffin.impulse.tasks.FetchVideosTask;
import com.orangemuffin.impulse.utils.ConnectionUtil;
import com.orangemuffin.impulse.utils.LocalDataUtil;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-03-24 */
public class AllVideosFragment extends Fragment {
    private String channelId, display_name, logoUrl, channelName;

    private RecyclerView recyclerView;
    private VODsListAdapter voDsListAdapter;

    private RelativeLayout linlaHeaderProgress;
    private ProgressBar pbHeaderProgress;
    private TextView empty_list_result;

    private SwipeRefreshLayout swipeRefreshLayout;

    private int sizeOffset = 0;

    private int page = 0;
    private boolean isLoading = false;
    private int itemToFetch = 15;

    private boolean isLive = false;

    /* required empty public constructor */
    public AllVideosFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_layout, container, false);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sizeOffset = 1;
        }

        channelId = getArguments().getString("channelId");
        display_name = getArguments().getString("display_name");
        logoUrl = getArguments().getString("logoUrl");
        channelName = getArguments().getString("channelName");

        linlaHeaderProgress = (RelativeLayout) rootView.findViewById(R.id.linlaHeaderProgress);
        pbHeaderProgress = (ProgressBar) rootView.findViewById(R.id.pbHeaderProgress);
        if (!LocalDataUtil.getThemeName(getContext()).equals("White Theme")) {
            pbHeaderProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
        } else {
            pbHeaderProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.black), PorterDuff.Mode.SRC_IN);
        }
        empty_list_result = (TextView) rootView.findViewById(R.id.empty_list_result);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager linearLayoutManager;
        if (getResources().getBoolean(R.bool.isTablet)) {
            linearLayoutManager = new GridLayoutManager(getContext(), 2+sizeOffset);
        } else {
            linearLayoutManager = new GridLayoutManager(getContext(), 1+sizeOffset);
        }
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        voDsListAdapter = new VODsListAdapter(getContext(), new ArrayList<VODInfo>());
        recyclerView.setAdapter(voDsListAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() >= voDsListAdapter.getItemCount()-(3+sizeOffset)) {
                    if (!isLoading) {
                        isLoading = true;
                        callFetchVideosTask();
                    }
                }
            }
        });

        setupEntryAnimation(); //always set animation after setting adapter to activate it

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRecyclerView();
            }
        });

        callCheckLiveChannelTask(); //initial call to task on create

        return rootView;
    }

    private void setupEntryAnimation() {
        //always set animation after setting adapter to activate it
        if (getContext() != null) {
            int resId = R.anim.layout_fall_down;
            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
            recyclerView.setLayoutAnimation(animation);
        }
    }

    private void callCheckLiveChannelTask() {
        new CheckLiveChannelTask(new CheckLiveChannelTask.CheckLiveChannelCallback() {
            @Override
            public void onLiveChannelChecked(VODInfo live) {
                if (live != null) {
                    voDsListAdapter.addLiveOnly(live);
                    isLive = true;
                }
                callFetchVideosTask();
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, channelId);
    }

    private void callFetchVideosTask() {
        new FetchVideosTask(getContext(), channelId, new FetchVideosTask.FetchVideosCallback() {
            @Override
            public void onVideosFetched(List<VODInfo> vods) {
                recyclerView.setVisibility(View.VISIBLE);

                if (!ConnectionUtil.isNetworkAvailable(getContext())) {
                    empty_list_result.setText("No Connection.");
                    empty_list_result.setVisibility(View.VISIBLE);
                    pbHeaderProgress.setVisibility(View.GONE);
                } else {
                    if (vods.isEmpty() && !isLive) {
                        empty_list_result.setText("No videos found.");
                        empty_list_result.setVisibility(View.VISIBLE);
                        pbHeaderProgress.setVisibility(View.GONE);
                    } else {
                        linlaHeaderProgress.setVisibility(View.GONE);
                        voDsListAdapter.addAll(vods);
                    }

                    page++;
                    isLoading = false;
                }

                swipeRefreshLayout.setRefreshing(false);
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(itemToFetch), String.valueOf(page*itemToFetch));
    }

    public void refreshRecyclerView() {
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        pbHeaderProgress.setVisibility(View.VISIBLE);
        empty_list_result.setVisibility(View.GONE);

        recyclerView.setVisibility(View.GONE);

        voDsListAdapter.clearList();

        page = 0;

        isLoading = true;
        callCheckLiveChannelTask();
    }

    public void recreateFragment() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sizeOffset = 1;
        } else {
            sizeOffset = 0;
        }

        RecyclerView.LayoutManager linearLayoutManager;
        if (getResources().getBoolean(R.bool.isTablet)) {
            linearLayoutManager = new GridLayoutManager(getContext(), 2+sizeOffset);
        } else {
            linearLayoutManager = new GridLayoutManager(getContext(), 1+sizeOffset);
        }
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        voDsListAdapter = new VODsListAdapter(getContext(), new ArrayList<VODInfo>());
        recyclerView.setAdapter(voDsListAdapter);

        refreshRecyclerView();
    }
}
