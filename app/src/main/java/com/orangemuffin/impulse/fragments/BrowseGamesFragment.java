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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.adapters.BrowseGamesAdapter;
import com.orangemuffin.impulse.models.BrowseGeneric;
import com.orangemuffin.impulse.models.GameInfo;
import com.orangemuffin.impulse.tasks.FetchAllGamesTask;
import com.orangemuffin.impulse.tasks.FetchMyGamesTask;
import com.orangemuffin.impulse.utils.ConnectionUtil;
import com.orangemuffin.impulse.utils.LocalDataUtil;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-03-18 */
public class BrowseGamesFragment extends Fragment {
    private RecyclerView recyclerView;
    private BrowseGamesAdapter browseGamesAdapter;

    private RelativeLayout linlaHeaderProgress;
    private ProgressBar pbHeaderProgress;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int sizeOffset = 0;

    private TextView empty_list_result;

    private List<BrowseGeneric> browseList = new ArrayList<>();

    private int page = 0;
    private boolean isLoading = true;
    private int itemToFetch = 50;
    private int newitemToFetch = 5;

    private FetchMyGamesTask fetchMyGamesTask;

    private boolean allgamestitlestatus = false;

    /* required empty public constructor */
    public BrowseGamesFragment() { }

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

        final int layoutSpanSize;
        if(getContext().getResources().getBoolean(R.bool.isTablet)) {
            layoutSpanSize = 4+sizeOffset;
        } else {
            layoutSpanSize = 2+sizeOffset;
        }

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), layoutSpanSize);
        ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (!browseList.isEmpty()) {
                    return browseList.get(position).getType() == 2 ? 1 : layoutSpanSize;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        browseGamesAdapter = new BrowseGamesAdapter(getContext(), browseList, sizeOffset, this);
        recyclerView.setAdapter(browseGamesAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() >= browseGamesAdapter.getItemCount()-(25+sizeOffset)) {
                    if (!isLoading) {
                        isLoading = true;
                        callFetchAllGamesTask();
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

        if (!LocalDataUtil.getAccessToken(getContext()).equals("NULL")) {
            callFetchMyGamesTask();
        } else {
            callFetchAllGamesTask();
        }

        return rootView;
    }

    public void callFetchMyGamesTask() {
        fetchMyGamesTask = new FetchMyGamesTask(getContext(), new FetchMyGamesTask.FetchMyGamesCallback() {
            @Override
            public void onMyGamesFetched(List<GameInfo> games) {
                if (!games.isEmpty()) {
                    BrowseGeneric followed_title = new BrowseGeneric("section_title");
                    followed_title.setSection_title("Followed Games");
                    browseGamesAdapter.addItem(followed_title);

                    BrowseGeneric mygames_list = new BrowseGeneric("horizontal");
                    mygames_list.setMygames_list(games);
                    browseGamesAdapter.addItem(mygames_list);
                }

                callFetchAllGamesTask();
            }
        });
        fetchMyGamesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(newitemToFetch), String.valueOf(0));
    }

    public void callFetchAllGamesTask() {
        new FetchAllGamesTask(new FetchAllGamesTask.FetchAllGamesCallback() {
            @Override
            public void onAllGamesFetched(List<GameInfo> games) {
                recyclerView.setVisibility(View.VISIBLE);

                if (!ConnectionUtil.isNetworkAvailable(getContext())) {
                    empty_list_result.setText("No Connection.");
                    empty_list_result.setVisibility(View.VISIBLE);
                    pbHeaderProgress.setVisibility(View.GONE);
                } else {
                    linlaHeaderProgress.setVisibility(View.GONE);

                    if (!allgamestitlestatus) {
                        BrowseGeneric allgames_title = new BrowseGeneric("section_title");
                        allgames_title.setSection_title("All Games");
                        browseGamesAdapter.addItem(allgames_title);
                        allgamestitlestatus = true;
                    }

                    List<BrowseGeneric> newgames = new ArrayList<>();
                    for (GameInfo game : games) {
                        BrowseGeneric gameInfo = new BrowseGeneric("vertical");
                        gameInfo.setGameInfo(game);
                        newgames.add(gameInfo);
                    }

                    browseGamesAdapter.addAll(newgames);

                    page++;
                    isLoading = false;
                }

                swipeRefreshLayout.setRefreshing(false);
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(itemToFetch), String.valueOf(page * itemToFetch));
    }

    public void refreshRecyclerView() {
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        pbHeaderProgress.setVisibility(View.VISIBLE);
        empty_list_result.setVisibility(View.GONE);

        recyclerView.setVisibility(View.GONE);

        browseGamesAdapter.clearList();

        page = 0;

        isLoading = true;

        if (!LocalDataUtil.getAccessToken(getContext()).equals("NULL")) {
            callFetchMyGamesTask();
        } else {
            callFetchAllGamesTask();
        }
    }

    public void recreateFragment() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sizeOffset = 2;
        } else {
            sizeOffset = 0;
        }

        final int layoutSpanSize;
        if(getContext().getResources().getBoolean(R.bool.isTablet)) {
            layoutSpanSize = 4+sizeOffset;
        } else {
            layoutSpanSize = 2+sizeOffset;
        }

        browseList.clear();

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), layoutSpanSize);
        ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return browseList.get(position).getType() == 2 ? 1 : layoutSpanSize;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        browseGamesAdapter = new BrowseGamesAdapter(getContext(), browseList, sizeOffset, this);
        recyclerView.setAdapter(browseGamesAdapter);

        refreshRecyclerView();
    }
}
