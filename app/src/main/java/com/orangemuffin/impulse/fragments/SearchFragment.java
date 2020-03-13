package com.orangemuffin.impulse.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.adapters.GamesListAdapter;
import com.orangemuffin.impulse.adapters.ChannelsListAdapter;
import com.orangemuffin.impulse.models.ChannelInfo;
import com.orangemuffin.impulse.models.GameInfo;
import com.orangemuffin.impulse.tasks.FetchSearchChannelsTask;
import com.orangemuffin.impulse.tasks.FetchSearchGamesTask;
import com.orangemuffin.impulse.utils.ConnectionUtil;
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.rey.material.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-04-15 */
public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;

    private ChannelsListAdapter channelsListAdapter;
    private GamesListAdapter gamesListAdapter;

    private RelativeLayout linlaHeaderProgress;
    private ProgressBar pbHeaderProgress;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView empty_list_result;

    private FetchSearchChannelsTask fetchSearchChannelsTask;
    private FetchSearchGamesTask fetchSearchGamesTask;

    private int sizeOffset = 0;

    private int page = 0;
    private boolean isLoading = false;
    private int itemToFetch = 50;
    private boolean isAdapterSet = false;

    private EditText search_edittext;
    private String query = "";

    private Spinner search_spinner;
    private String[] search_types = {"Channels", "Games"};
    private String current_type = "Channels";

    /* required empty public constructor */
    public SearchFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

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

        search_spinner = (Spinner) rootView.findViewById(R.id.search_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_search_element, search_types);
        search_spinner.setAdapter(adapter);
        search_spinner.setSelection(0);
        search_spinner.setOnItemClickListener(new Spinner.OnItemClickListener() {
            @Override
            public boolean onItemClick(Spinner spinner, View view, int i, long l) {
                current_type = search_types[i];
                refreshRecyclerView();
                return true;
            }
        });

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

        //sizeOffset is 1 more than default size
        gamesListAdapter = new GamesListAdapter(getContext(), new ArrayList<GameInfo>(), sizeOffset+1, "search");

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

        search_edittext = (EditText) rootView.findViewById(R.id.search_edittext);

        search_edittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(search_edittext, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    InputMethodManager imm =  (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        search_edittext.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
            @Override public void afterTextChanged(Editable editable) { }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                query = text.toString().replace(" ", "%20");
                refreshRecyclerView();
            }
        });

        if (!ConnectionUtil.isNetworkAvailable(getContext())) {
            refreshRecyclerView();
        }

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
                        callFetchSearchTask();
                    }
                }
            }
        });
        isAdapterSet = true;
    }

    private void callFetchSearchTask() {
        if (fetchSearchChannelsTask != null) {
            fetchSearchChannelsTask.cancel(true);
        }

        if (fetchSearchGamesTask != null) {
            fetchSearchGamesTask.cancel(true);
        }

        if (current_type.equals("Channels")) {
            fetchSearchChannelsTask = new FetchSearchChannelsTask(getContext(), new FetchSearchChannelsTask.FetchSearchChannelsCallback() {
                @Override
                public void onSearchChannelsFetched(List<ChannelInfo> channels) {
                    recyclerView.setVisibility(View.VISIBLE);

                    if (channels.isEmpty()) {
                        if (!ConnectionUtil.isNetworkAvailable(getContext())) {
                            empty_list_result.setText("No Connection.");
                        } else if (!search_edittext.getText().toString().equals("")) {
                            empty_list_result.setText("No Search Found.");
                        } else {
                            empty_list_result.setText("");
                        }
                        empty_list_result.setVisibility(View.VISIBLE);
                        pbHeaderProgress.setVisibility(View.GONE);
                    } else {
                        linlaHeaderProgress.setVisibility(View.GONE);

                        if (!isAdapterSet) {
                            setupPagination(fetchSearchChannelsTask.getTotalSize());
                        }

                        channelsListAdapter.addAll(channels);

                        page++;
                        isLoading = false;
                    }

                    swipeRefreshLayout.setRefreshing(false);
                }
            });

            fetchSearchChannelsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query, String.valueOf(itemToFetch), String.valueOf(page * itemToFetch));
        } else if (current_type.equals("Games")) {
            fetchSearchGamesTask = new FetchSearchGamesTask(getContext(), new FetchSearchGamesTask.FetchSearchGamesCallback() {
                @Override
                public void onSearchGamesFetched(List<GameInfo> games) {
                    recyclerView.setVisibility(View.VISIBLE);

                    if (games.isEmpty()) {
                        if (!ConnectionUtil.isNetworkAvailable(getContext())) {
                            empty_list_result.setText("No Connection.");
                        } else if (!search_edittext.getText().toString().equals("")) {
                            empty_list_result.setText("No Search Found.");
                        } else {
                            empty_list_result.setText("");
                        }
                        empty_list_result.setVisibility(View.VISIBLE);
                        pbHeaderProgress.setVisibility(View.GONE);
                    } else {
                        linlaHeaderProgress.setVisibility(View.GONE);

                        gamesListAdapter.addAll(games);
                    }

                    swipeRefreshLayout.setRefreshing(false);
                }
            });

            fetchSearchGamesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
        }


    }

    public void refreshRecyclerView() {
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        pbHeaderProgress.setVisibility(View.VISIBLE);
        empty_list_result.setVisibility(View.GONE);

        recyclerView.setVisibility(View.GONE);

        if (current_type.equals("Channels")) {
            isAdapterSet = false;

            channelsListAdapter.clearList();
            recyclerView.setAdapter(channelsListAdapter);

            page = 0;

            isLoading = true;
            callFetchSearchTask();
        } else if (current_type.equals("Games")) {
            gamesListAdapter.clearList();
            recyclerView.setAdapter(gamesListAdapter);
            callFetchSearchTask();
        }
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

        gamesListAdapter = new GamesListAdapter(getContext(), new ArrayList<GameInfo>(), sizeOffset+1, "search");
        channelsListAdapter = new ChannelsListAdapter(getContext(), new ArrayList<ChannelInfo>());

        refreshRecyclerView();
    }
}
