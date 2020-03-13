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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.adapters.ChannelsListAdapter;
import com.orangemuffin.impulse.models.ChannelInfo;
import com.orangemuffin.impulse.tasks.FetchFollowedTask;
import com.orangemuffin.impulse.utils.ConnectionUtil;
import com.orangemuffin.impulse.utils.LocalDataUtil;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-03-19 */
public class FollowedFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChannelsListAdapter channelsListAdapter;

    private RelativeLayout linlaHeaderProgress;
    private ProgressBar pbHeaderProgress;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView empty_list_result;

    private FetchFollowedTask fetchFollowedTask;

    private int sizeOffset = 0;

    private int page = 0;
    private boolean isLoading = false;
    private int itemToFetch = 50;
    private boolean isAdapterSet = false;

    /* required empty public constructor */
    public FollowedFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_layout, container, false);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sizeOffset = 2;
        }

        linlaHeaderProgress = (RelativeLayout) rootView.findViewById(R.id.linlaHeaderProgress);
        pbHeaderProgress = (ProgressBar) rootView.findViewById(R.id.pbHeaderProgress);
        if (!LocalDataUtil.getThemeName(getContext()).equals("White Theme")) {
            pbHeaderProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
        } else {
            pbHeaderProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.black), PorterDuff.Mode.SRC_IN);
        }

        empty_list_result = (TextView) rootView.findViewById(R.id.empty_list_result);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        final RecyclerView.LayoutManager layoutManager;
        if(getContext().getResources().getBoolean(R.bool.isTablet)) {
            layoutManager = new GridLayoutManager(getContext(), 5+sizeOffset);
        } else {
            layoutManager = new GridLayoutManager(getContext(), 3+sizeOffset);
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        channelsListAdapter = new ChannelsListAdapter(getContext(), new ArrayList<ChannelInfo>());
        recyclerView.setAdapter(channelsListAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRecyclerView();
            }
        });

        setupEntryAnimation(); //always set animation after setting adapter to activate it

        callFetchFollowedTask();

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

    private void setupPagination(final int total) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() >= channelsListAdapter.getItemCount()-(6+sizeOffset)) {
                    if (!isLoading && page <= Math.floor(total / itemToFetch)) {
                        isLoading = true;
                        callFetchFollowedTask();
                    }
                }
            }
        });
        isAdapterSet = true;
    }

    private void callFetchFollowedTask() {
        fetchFollowedTask = new FetchFollowedTask(getContext(), new FetchFollowedTask.FetchFollowedCallback() {
            @Override
            public void onFollowedFetched(List<ChannelInfo> channels) {
                recyclerView.setVisibility(View.VISIBLE);

                if (channels.isEmpty()) {
                    if (!ConnectionUtil.isNetworkAvailable(getContext())) {
                        empty_list_result.setText("No Connection.");
                    } else {
                        empty_list_result.setText("No Channel Followed Yet.");
                    }
                    empty_list_result.setVisibility(View.VISIBLE);
                    pbHeaderProgress.setVisibility(View.GONE);
                } else {
                    linlaHeaderProgress.setVisibility(View.GONE);

                    if (!isAdapterSet) {
                        setupPagination(fetchFollowedTask.getTotalSize());
                    }

                    channelsListAdapter.addAll(channels);

                    page++;
                    isLoading = false;
                }

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        fetchFollowedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(itemToFetch), String.valueOf(page * itemToFetch));
    }

    public void refreshRecyclerView() {
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        pbHeaderProgress.setVisibility(View.VISIBLE);
        empty_list_result.setVisibility(View.GONE);

        recyclerView.setVisibility(View.GONE);

        channelsListAdapter.clearList();

        page = 0;

        setupEntryAnimation();

        isLoading = true;
        callFetchFollowedTask();
    }

    public void recreateFragment() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sizeOffset = 2;
        } else {
            sizeOffset = 0;
        }

        final RecyclerView.LayoutManager layoutManager;
        if(getContext().getResources().getBoolean(R.bool.isTablet)) {
            layoutManager = new GridLayoutManager(getContext(), 5+sizeOffset);
        } else {
            layoutManager = new GridLayoutManager(getContext(), 3+sizeOffset);
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        channelsListAdapter = new ChannelsListAdapter(getContext(), new ArrayList<ChannelInfo>());
        recyclerView.setAdapter(channelsListAdapter);

        refreshRecyclerView();
    }
}
