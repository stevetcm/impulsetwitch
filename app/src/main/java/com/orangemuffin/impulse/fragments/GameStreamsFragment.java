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

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.adapters.StreamsListAdapter;
import com.orangemuffin.impulse.models.StreamInfo;
import com.orangemuffin.impulse.tasks.FetchGameStreamsTask;
import com.orangemuffin.impulse.utils.LocalDataUtil;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-03-18 */
public class GameStreamsFragment extends Fragment {
    private RecyclerView recyclerView;
    private StreamsListAdapter streamsListAdapter;

    private RelativeLayout linlaHeaderProgress;
    private ProgressBar pbHeaderProgress;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String gameName;

    private int sizeOffset = 0;

    private int page = 0;
    private boolean isLoading = false;
    private int itemToFetch = 50;

    /* required empty public constructor */
    public GameStreamsFragment() { }

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

        gameName = getArguments().getString("gameName").replace(" ", "%20");

        linlaHeaderProgress = (RelativeLayout) rootView.findViewById(R.id.linlaHeaderProgress);
        pbHeaderProgress = (ProgressBar) rootView.findViewById(R.id.pbHeaderProgress);
        if (!LocalDataUtil.getThemeName(getContext()).equals("White Theme")) {
            pbHeaderProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
        } else {
            pbHeaderProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.black), PorterDuff.Mode.SRC_IN);
        }

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager;
        if (getResources().getBoolean(R.bool.isTablet)) {
            layoutManager = new GridLayoutManager(getContext(), 2+sizeOffset);
        } else {
            layoutManager = new GridLayoutManager(getContext(), 1+sizeOffset);
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        streamsListAdapter = new StreamsListAdapter(getContext(), new ArrayList<StreamInfo>(), "game");
        recyclerView.setAdapter(streamsListAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() >= streamsListAdapter.getItemCount()-3) {
                    if (!isLoading) {
                        isLoading = true;
                        callFetchGameStreamsTask();
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

        callFetchGameStreamsTask(); //initial call to task on create

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

    private void callFetchGameStreamsTask() {
        new FetchGameStreamsTask(getContext(), new FetchGameStreamsTask.FetchGameStreamsCallback() {
            @Override
            public void onGameStreamsFetched(List<StreamInfo> streams) {
                linlaHeaderProgress.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                streamsListAdapter.addAll(streams);

                page++;
                isLoading = false;

                swipeRefreshLayout.setRefreshing(false);
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, gameName, String.valueOf(itemToFetch), String.valueOf(page*itemToFetch));
    }

    public void refreshRecyclerView() {
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        pbHeaderProgress.setVisibility(View.VISIBLE);

        streamsListAdapter.clearList();
        recyclerView.scrollToPosition(0);

        page = 0;

        setupEntryAnimation();

        callFetchGameStreamsTask();
    }

    public void recreateFragment() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sizeOffset = 1;
        } else {
            sizeOffset = 0;
        }

        RecyclerView.LayoutManager layoutManager;
        if (getResources().getBoolean(R.bool.isTablet)) {
            layoutManager = new GridLayoutManager(getContext(), 2+sizeOffset);
        } else {
            layoutManager = new GridLayoutManager(getContext(), 1+sizeOffset);
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        streamsListAdapter = new StreamsListAdapter(getContext(), new ArrayList<StreamInfo>(), "game");
        recyclerView.setAdapter(streamsListAdapter);

        refreshRecyclerView();
    }
}
