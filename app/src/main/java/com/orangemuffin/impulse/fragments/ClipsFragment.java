package com.orangemuffin.impulse.fragments;

import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.adapters.ClipsListAdapter;
import com.orangemuffin.impulse.models.ClipInfo;
import com.orangemuffin.impulse.tasks.FetchClipsTask;
import com.orangemuffin.impulse.utils.ConnectionUtil;
import com.orangemuffin.impulse.utils.LocalDataUtil;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-04-17 */
public class ClipsFragment extends Fragment {

    private String[] type_items = new String[] {"day", "week", "month", "all"};
    private String[] compl_type = new String[] {"Top 24h", "Top 7d", "Top 30d", "Top all"};

    private AppCompatSpinner typeSpinner;

    private SwipeRefreshLayout swipeRefreshLayout;

    private int sizeOffset = 0;

    private String channelName, logoUrl;

    private RecyclerView recyclerView;
    private ClipsListAdapter clipsListAdapter;

    private RelativeLayout linlaHeaderProgress;
    private ProgressBar pbHeaderProgress;
    private TextView empty_list_result;

    private FetchClipsTask fetchClipsTask;

    private String cursorId = "";
    private boolean isLoading = false;
    private int itemToFetch = 25;

    private int currentType = 0;

    /* required empty public constructor */
    public ClipsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_clips, container, false);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sizeOffset = 1;
        }

        channelName = getArguments().getString("channelName");
        logoUrl = getArguments().getString("logoUrl");

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

        clipsListAdapter = new ClipsListAdapter(getContext(), new ArrayList<ClipInfo>(), logoUrl);
        recyclerView.setAdapter(clipsListAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() >= clipsListAdapter.getItemCount()-(3+sizeOffset)) {
                    if (!isLoading) {
                        isLoading = true;
                        callFetchClipsTask();
                    }
                }
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRecyclerView();
            }
        });

        typeSpinner = (AppCompatSpinner) rootView.findViewById(R.id.type_spinner);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_element, compl_type);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> adapterView) { }

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentType = i;
                refreshRecyclerView(); //initial call to task on create
            }
        });

        return rootView;
    }

    private void callFetchClipsTask() {
        fetchClipsTask = new FetchClipsTask(getContext(), new FetchClipsTask.FetchClipsCallback() {
            @Override
            public void onClipsFetched(List<ClipInfo> clips) {
                recyclerView.setVisibility(View.VISIBLE);

                if (clips.isEmpty()) {
                    if (!ConnectionUtil.isNetworkAvailable(getContext())) {
                        empty_list_result.setText("No Connection.");
                    } else {
                        empty_list_result.setText("No clips were created in this time period.");
                    }
                    empty_list_result.setVisibility(View.VISIBLE);
                    pbHeaderProgress.setVisibility(View.GONE);
                } else {
                    linlaHeaderProgress.setVisibility(View.GONE);
                    clipsListAdapter.addAll(clips);
                }

                cursorId = fetchClipsTask.getCursorId();
                isLoading = false;

                swipeRefreshLayout.setRefreshing(false);
            }
        });
        fetchClipsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, channelName, String.valueOf(itemToFetch), type_items[currentType], cursorId);
    }

    public void refreshRecyclerView() {
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        pbHeaderProgress.setVisibility(View.VISIBLE);
        empty_list_result.setVisibility(View.GONE);

        recyclerView.setVisibility(View.GONE);

        clipsListAdapter.clearList();

        cursorId = "";

        isLoading = true;
        callFetchClipsTask();
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

        clipsListAdapter = new ClipsListAdapter(getContext(), new ArrayList<ClipInfo>(), logoUrl);
        recyclerView.setAdapter(clipsListAdapter);

        refreshRecyclerView();
    }
}
