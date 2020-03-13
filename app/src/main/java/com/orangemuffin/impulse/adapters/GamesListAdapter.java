package com.orangemuffin.impulse.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.activities.MainActivity;
import com.orangemuffin.impulse.models.GameInfo;
import com.orangemuffin.impulse.utils.MeasurementUtil;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/* Created by OrangeMuffin on 2018-03-18 */
public class GamesListAdapter extends RecyclerView.Adapter<GamesListAdapter.MyViewHolder> {
    private Context mContext;
    private List<GameInfo> games;

    private String type;

    private int sizeOffset = 0;

    public GamesListAdapter(Context mContext, List<GameInfo> games, int sizeOffset, String type) {
        this.mContext = mContext;
        this.games = games;

        this.sizeOffset = sizeOffset;

        this.type = type;
    }

    public void addAll(List<GameInfo> games) {
        this.games.addAll(games);
        notifyDataSetChanged();
    }

    public void clearList() {
        games.clear();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_games, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        setGamePosterLayout(holder);

        GameInfo game = games.get(position);
        holder.gameName.setText(game.getGameName());

        if (type.equals("search")) {
            holder.gameViewers.setVisibility(View.GONE);
        } else if (type.equals("mygames")) {
            holder.gameViewers.setText(NumberFormat.getNumberInstance(Locale.US).format(game.getViewCount()));
        } else {
            holder.gameViewers.setText(NumberFormat.getNumberInstance(Locale.US).format(game.getViewCount()) + " Viewers");
        }

        Picasso.with(mContext).load(Uri.parse(game.getPosterUrl())).fit().into(holder.gamePoster);
    }

    /* preset layout size to reserve space in cardview item */
    private void setGamePosterLayout(MyViewHolder holder) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

        //multiply by 4 since it is a single value margin
        int marginPixels = (int) (mContext.getResources().getDimension(R.dimen.card_item_margin) * 6);

        if (type.equals("mygames")) {
            if (mContext.getResources().getBoolean(R.bool.isTablet)) {
                holder.gamePoster.getLayoutParams().width = ((metrics.widthPixels - marginPixels) / (4 + sizeOffset)) - MeasurementUtil.dpToPixel(9);
                holder.gamePoster.getLayoutParams().height = (int) ((((metrics.widthPixels - marginPixels) / (4 + sizeOffset)) - MeasurementUtil.dpToPixel(9)) / (272 / 380.0));
                holder.games_info_container.getLayoutParams().width = ((metrics.widthPixels - marginPixels) / (4 + sizeOffset)) - MeasurementUtil.dpToPixel(9);
            } else {
                holder.gamePoster.getLayoutParams().width = ((metrics.widthPixels - marginPixels) / (2 + sizeOffset)) - MeasurementUtil.dpToPixel(9);
                holder.gamePoster.getLayoutParams().height = (int) ((((metrics.widthPixels - marginPixels) / (2 + sizeOffset)) - MeasurementUtil.dpToPixel(9)) / (272 / 380.0));
                holder.games_info_container.getLayoutParams().width = ((metrics.widthPixels - marginPixels) / (2 + sizeOffset)) - MeasurementUtil.dpToPixel(9);
            }

            holder.gameName.setTextSize(14);
            holder.gameViewers.setTextSize(12);
            holder.dot_circle.setVisibility(View.VISIBLE);
        } else {
            if (mContext.getResources().getBoolean(R.bool.isTablet)) {
                holder.gamePoster.getLayoutParams().width = (metrics.widthPixels - marginPixels) / (4 + sizeOffset);
                holder.gamePoster.getLayoutParams().height = (int) (((metrics.widthPixels - marginPixels) / (4 + sizeOffset)) / (272 / 380.0));
            } else {
                holder.gamePoster.getLayoutParams().width = (metrics.widthPixels - marginPixels) / (2 + sizeOffset);
                holder.gamePoster.getLayoutParams().height = (int) (((metrics.widthPixels - marginPixels) / (2 + sizeOffset)) / (272 / 380.0));
            }

            holder.games_info_container.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
            holder.gameName.setTextSize(16);
            holder.gameViewers.setTextSize(13);
            holder.dot_circle.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView gamePoster;
        TextView gameName, gameViewers;
        CardView card_games;
        RelativeLayout games_info_container;
        ImageView dot_circle;

        public MyViewHolder(View view) {
            super(view);

            gamePoster = (ImageView) view.findViewById(R.id.gamePoster);
            gameName = (TextView) view.findViewById(R.id.gameName);
            gameViewers = (TextView) view.findViewById(R.id.gameViewers);

            card_games = (CardView) view.findViewById(R.id.card_games);
            card_games.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    ((MainActivity) mContext).switchToGameStreamsList(games.get(position).getGameName());
                }
            });
            card_games.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();
                    Toast.makeText(mContext, games.get(position).getGameName(), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            games_info_container = (RelativeLayout) view.findViewById(R.id.games_info_container);
            dot_circle = (ImageView) view.findViewById(R.id.dot_circle);
        }
    }

}
