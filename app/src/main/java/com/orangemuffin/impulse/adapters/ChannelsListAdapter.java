package com.orangemuffin.impulse.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
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
import com.orangemuffin.impulse.models.ChannelInfo;
import com.squareup.picasso.Picasso;

import java.util.List;

/* Created by OrangeMuffin on 2018-03-19 */
public class ChannelsListAdapter extends RecyclerView.Adapter<ChannelsListAdapter.MyViewHolder> {
    private Context mContext;
    private List<ChannelInfo> channels;

    private int sizeOffset = 0;

    public ChannelsListAdapter(Context mContext,  List<ChannelInfo> channels) {
        this.mContext = mContext;
        this.channels = channels;
    }

    public void addAll(List<ChannelInfo> channels) {
        this.channels.addAll(channels);
        notifyDataSetChanged();
    }

    public void clearList() {
        channels.clear();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_channels, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        setChannelLogoLayout(holder);

        ChannelInfo channel = channels.get(position);
        holder.display_name.setText(channel.getDisplayName());

        Picasso.with(mContext).load(Uri.parse(channel.getLogoUrl())).fit().into(holder.channelLogo);
    }

    /* preset layout size to reserve space in cardview item */
    private void setChannelLogoLayout(MyViewHolder holder) {
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sizeOffset = 2;
        }

        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

        //multiply by 4 since it is a single value margin
        int marginPixels = (int) (mContext.getResources().getDimension(R.dimen.card_item_margin) * 8);

        if(mContext.getResources().getBoolean(R.bool.isTablet)) {
            holder.channelLogo.getLayoutParams().width = (metrics.widthPixels - marginPixels)/(5+sizeOffset);
            holder.channelLogo.getLayoutParams().height = (metrics.widthPixels - marginPixels)/(5+sizeOffset);
        } else {
            holder.channelLogo.getLayoutParams().width = (metrics.widthPixels - marginPixels)/(3+sizeOffset);
            holder.channelLogo.getLayoutParams().height = (metrics.widthPixels - marginPixels)/(3+sizeOffset);
        }
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView channelLogo;
        TextView display_name;
        CardView card_channels;

        public MyViewHolder(View view) {
            super(view);

            channelLogo = (ImageView) view.findViewById(R.id.channelLogo);
            display_name = (TextView) view.findViewById(R.id.display_name);

            card_channels = (CardView) view.findViewById(R.id.card_channels);
            card_channels.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    Bundle bundle = new Bundle();
                    bundle.putString("channelId", channels.get(position).getChannelId());
                    bundle.putString("display_name", channels.get(position).getDisplayName());
                    bundle.putString("channelName", channels.get(position).getChannelName());
                    bundle.putString("logoUrl", channels.get(position).getLogoUrl());

                    ((MainActivity) mContext).switchToChannelProfile(bundle);
                }
            });

            card_channels.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();

                    Toast.makeText(mContext, channels.get(position).getDisplayName(), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }
}
