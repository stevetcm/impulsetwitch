package com.orangemuffin.impulse.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.activities.MainActivity;
import com.orangemuffin.impulse.fragments.BrowseGamesFragment;
import com.orangemuffin.impulse.models.BrowseGeneric;
import com.orangemuffin.impulse.models.GameInfo;
import com.orangemuffin.impulse.tasks.FetchMyGamesTask;
import com.orangemuffin.impulse.utils.MeasurementUtil;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/* Created by OrangeMuffin on 2018-12-14 */
public class BrowseGamesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_TEXTVIEW = 0;
    private final int VIEW_TYPE_HORIZONTAL = 1;
    private final int VIEW_TYPE_VERTICAL = 2;
    private final int VIEW_TYPE_EMPTY = -1;

    private Context mContext;
    private List<BrowseGeneric> itemList;
    private BrowseGamesFragment parentFragment;

    private int sizeOffset = 0;

    private boolean mygames_refresh = false;

    public BrowseGamesAdapter(Context mContext, List<BrowseGeneric> itemList, int sizeOffset, BrowseGamesFragment parentFragment) {
        this.mContext = mContext;
        this.itemList = itemList;
        this.parentFragment = parentFragment;

        this.sizeOffset = sizeOffset;
    }

    public void addItem(BrowseGeneric item) {
        this.itemList.add(item);
        notifyDataSetChanged();
    }

    public void addAll(List<BrowseGeneric> games) {
        this.itemList.addAll(games);
        notifyDataSetChanged();
    }

    public void clearList() {
        itemList.clear();
        mygames_refresh = true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_TEXTVIEW) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_textview, parent, false);
            return new MyTextViewHolder(itemView);
        } else if (viewType == VIEW_TYPE_HORIZONTAL) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_recyclerview, parent, false);
            return new MyHorizontalViewHolder(itemView);
        } else if (viewType == VIEW_TYPE_VERTICAL) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_games, parent, false);
            return new MyVerticalItemViewHolder(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyTextViewHolder) {
            ((MyTextViewHolder) holder).text.setText(itemList.get(position).getSection_title());
        } else if (holder instanceof MyHorizontalViewHolder) {
            if (mygames_refresh) {
                ((MyHorizontalViewHolder) holder).refreshRecyclerView();
                mygames_refresh = false;
            }
        } else if (holder instanceof MyVerticalItemViewHolder) {
            setGamePosterLayout(((MyVerticalItemViewHolder) holder));

            GameInfo game = itemList.get(position).getGameInfo();
            ((MyVerticalItemViewHolder) holder).gameName.setText(game.getGameName());

            ((MyVerticalItemViewHolder) holder).gameViewers.setText(NumberFormat.getNumberInstance(Locale.US).format(game.getViewCount()) + " Viewers");
            Picasso.with(mContext).load(Uri.parse(game.getPosterUrl())).fit().into(((MyVerticalItemViewHolder) holder).gamePoster);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class MyTextViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public MyTextViewHolder(View view) {
            super(view);

            text = (TextView) view.findViewById(R.id.text);
        }
    }

    public class MyHorizontalViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        GamesListAdapter adapter;

        private int page = 1;
        private boolean isLoading = false;
        private int itemToFetch = 5;

        public MyHorizontalViewHolder(View view) {
            super(view);

            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);

            adapter = new GamesListAdapter(mContext, new ArrayList<GameInfo>(), 1+sizeOffset, "mygames");
            recyclerView.setAdapter(adapter);

            final List<GameInfo> mygame_list = new ArrayList<>();
            if (itemList.size() >= 2 && itemList.get(1).getType() == VIEW_TYPE_HORIZONTAL) {
                mygame_list.addAll(itemList.get(1).getMygames_list());
            }

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!itemList.isEmpty()) {
                        if (((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() >= mygame_list.size()-3) {
                            if (!isLoading) {
                                isLoading = true;
                                callFetchMyGamesTask();
                            }
                        }
                    }
                }
            });

            adapter.addAll(mygame_list); //first call
        }

        public void callFetchMyGamesTask() {
            new FetchMyGamesTask(mContext, new FetchMyGamesTask.FetchMyGamesCallback() {
                @Override
                public void onMyGamesFetched(List<GameInfo> games) {
                    if (itemList.size() >= 2 && itemList.get(1).getType() == VIEW_TYPE_HORIZONTAL) {
                        itemList.get(1).getMygames_list().addAll(games);
                        adapter.addAll(games);
                    }

                    page++;
                    isLoading = false;
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(itemToFetch), String.valueOf(page*itemToFetch));
        }

        public void refreshRecyclerView() {
            adapter.clearList();
            recyclerView.smoothScrollToPosition(0);

            if (itemList.size() >= 2 && itemList.get(1).getType() == VIEW_TYPE_HORIZONTAL) {
                adapter.addAll(itemList.get(1).getMygames_list()); //first call
            }

            page = 1;
            isLoading = false;
        }
    }

    public class MyVerticalItemViewHolder extends RecyclerView.ViewHolder {
        ImageView gamePoster;
        TextView gameName, gameViewers;
        CardView card_games;

        public MyVerticalItemViewHolder(View view) {
            super(view);

            gamePoster = (ImageView) view.findViewById(R.id.gamePoster);
            gameName = (TextView) view.findViewById(R.id.gameName);
            gameViewers = (TextView) view.findViewById(R.id.gameViewers);

            card_games = (CardView) view.findViewById(R.id.card_games);
            card_games.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    ((MainActivity) mContext).switchToGameStreamsList(itemList.get(position).getGameInfo().getGameName());
                }
            });
            card_games.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();
                    Toast.makeText(mContext, itemList.get(position).getGameInfo().getGameName(), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }

    /* preset layout size to reserve space in cardview item */
    private void setGamePosterLayout(MyVerticalItemViewHolder holder) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

        //multiply by 4 since it is a single value margin
        int marginPixels = (int) (mContext.getResources().getDimension(R.dimen.card_item_margin) * 6);

        if(mContext.getResources().getBoolean(R.bool.isTablet)) {
            holder.gamePoster.getLayoutParams().width = (metrics.widthPixels - marginPixels)/(4+sizeOffset);
            holder.gamePoster.getLayoutParams().height = (int) (((metrics.widthPixels - marginPixels)/(4+sizeOffset))/(272/380.0));
        } else {
            holder.gamePoster.getLayoutParams().width = (metrics.widthPixels - marginPixels)/(2+sizeOffset);
            holder.gamePoster.getLayoutParams().height = (int) (((metrics.widthPixels - marginPixels)/(2+sizeOffset))/(272/380.0));
        }

        holder.gameName.getLayoutParams().width = ((metrics.widthPixels - marginPixels)/(2+sizeOffset))- MeasurementUtil.dpToPixel(10);
    }
}
