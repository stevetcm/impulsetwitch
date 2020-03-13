package com.orangemuffin.impulse.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.activities.LiveStreamActivity;
import com.orangemuffin.impulse.activities.MainActivity;
import com.orangemuffin.impulse.models.StreamInfo;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/* Created by OrangeMuffin on 2018-03-17 */
public class StreamsListAdapter extends RecyclerView.Adapter<StreamsListAdapter.MyViewHolder> {
    private Context mContext;
    private List<StreamInfo> streams;

    private int sizeOffset = 0;

    private String type;

    public StreamsListAdapter(Context mContext, List<StreamInfo> streams, String type) {
        this.mContext = mContext;
        this.streams = streams;

        this.type = type;
    }

    public void addAll(List<StreamInfo> streams) {
        this.streams.addAll(streams);
        notifyDataSetChanged();
    }

    public void clearList() {
        streams.clear();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_streams, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        setVideoPreviewLayout(holder);

        StreamInfo stream = streams.get(position);

        if (stream.getStreamType().equals("rerun")) {
            holder.stream_rerun.setVisibility(View.VISIBLE);
        } else {
            holder.stream_rerun.setVisibility(View.GONE);
        }

        holder.streamerName.setText(stream.getDisplayName());
        holder.streamStatus.setText(stream.getStreamStatus());
        holder.streamGame.setText(stream.getGameName());
        holder.streamViewers.setText(NumberFormat.getNumberInstance(Locale.US).format(stream.getViewCount()));

        Picasso.with(mContext).load(Uri.parse(stream.getLogoUrl())).fit().into(holder.logo);
        Picasso.with(mContext).load(Uri.parse(stream.getVideoPreviewUrl())).fit().into(holder.videoPreview);
    }

    /* preset layout size to reserve space in cardview item */
    private void setVideoPreviewLayout(MyViewHolder holder) {
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sizeOffset = 1;
        }

        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

        //multiply by 4 since it is a single value margin
        int marginPixels = (int) (mContext.getResources().getDimension(R.dimen.card_item_margin)*4);

        holder.videoPreview.getLayoutParams().width = metrics.widthPixels - marginPixels;

        if(mContext.getResources().getBoolean(R.bool.isTablet)) {
            holder.videoPreview.getLayoutParams().height = (int) (((metrics.widthPixels - marginPixels)/(16/9.0))/(2+sizeOffset));
        } else {
            holder.videoPreview.getLayoutParams().height = (int) (((metrics.widthPixels - marginPixels)/(16/9.0))/(1+sizeOffset));
        }
    }

    @Override
    public int getItemCount() {
        return streams.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView videoPreview, logo, more_settings;
        TextView streamerName, streamStatus, streamGame, streamViewers, stream_rerun;
        CardView card_streams;

        public MyViewHolder(View view) {
            super(view);
            videoPreview = (ImageView) view.findViewById(R.id.videoPreview);
            logo = (ImageView) view.findViewById(R.id.logo);
            more_settings = (ImageView) view.findViewById(R.id.more_settings);
            more_settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position = getAdapterPosition();

                    PopupMenu channel_popup = new PopupMenu(mContext, view);
                    channel_popup.inflate(R.menu.menu_channel);

                    if (type.equals("game")) {
                        channel_popup.getMenu().getItem(1).setVisible(false);
                    }

                    channel_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.menu_item_profile:
                                    Bundle bundle = new Bundle();
                                    bundle.putString("channelId", streams.get(position).getUserId());
                                    bundle.putString("channelName", streams.get(position).getStreamerName());
                                    bundle.putString("display_name", streams.get(position).getDisplayName());
                                    bundle.putString("logoUrl", streams.get(position).getLogoUrl());

                                    ((MainActivity) mContext).switchToChannelProfile(bundle);
                                    return true;
                                case R.id.menu_item_game:
                                    ((MainActivity) mContext).switchToGameStreamsList(streams.get(position).getGameName());
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

                    channel_popup.show();
                }
            });

            streamerName = (TextView) view.findViewById(R.id.streamerName);
            streamStatus = (TextView) view.findViewById(R.id.streamStatus);
            streamGame = (TextView) view.findViewById(R.id.streamGame);
            streamViewers = (TextView) view.findViewById(R.id.streamViewers);
            stream_rerun = (TextView) view.findViewById(R.id.stream_rerun);

            card_streams = (CardView) view.findViewById(R.id.card_streams);
            card_streams.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    Intent intent = new Intent(mContext, LiveStreamActivity.class);
                    intent.putExtra("streamerName", streams.get(position).getStreamerName());
                    intent.putExtra("display_name", streams.get(position).getDisplayName());
                    intent.putExtra("streamStatus", streams.get(position).getStreamStatus());
                    intent.putExtra("channelId", streams.get(position).getUserId());
                    intent.putExtra("logoUrl", streams.get(position).getLogoUrl());
                    intent.putExtra("gameName", streams.get(position).getGameName());
                    intent.putExtra("viewCount", streams.get(position).getViewCount());

                    ((Activity) mContext).startActivityForResult(intent, 4367);
                    ((Activity) mContext).overridePendingTransition(R.anim.slide_up, R.anim.anim_stay);
                }
            });
            card_streams.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();

                    Toast.makeText(mContext, streams.get(position).getStreamStatus(),Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }
}
