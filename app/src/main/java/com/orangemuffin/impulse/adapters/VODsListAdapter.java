package com.orangemuffin.impulse.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.activities.LiveStreamActivity;
import com.orangemuffin.impulse.activities.VODActivity;
import com.orangemuffin.impulse.models.VODInfo;
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.orangemuffin.impulse.utils.MeasurementUtil;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/* Created by OrangeMuffin on 2018-03-22 */
public class VODsListAdapter extends RecyclerView.Adapter<VODsListAdapter.MyViewHolder> {
    private Context mContext;
    private List<VODInfo> vods;

    private int sizeOffset = 0;

    public VODsListAdapter(Context mContext, List<VODInfo> vods) {
        this.mContext = mContext;
        this.vods = vods;
    }

    public void addLiveOnly(VODInfo live) {
        this.vods.add(live);
    }

    public void addAll(List<VODInfo> vods) {
        this.vods.addAll(vods);
        notifyDataSetChanged();
    }

    public void clearList() {
        vods.clear();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_vods, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        setVideoPreviewLayout(holder);

        VODInfo vod = vods.get(position);

        if (vod.isLiveStream()) {
            holder.vod_info_container_top.setVisibility(View.GONE);
            holder.vod_livestream.setVisibility(View.VISIBLE);
            holder.vod_views_icon.setImageResource(R.drawable.ic_person_white_24dp);
        } else {
            holder.vod_info_container_top.setVisibility(View.VISIBLE);
            holder.vod_livestream.setVisibility(View.GONE);
            holder.vod_views_icon.setImageResource(R.drawable.ic_eye_white_24dp);

            holder.vod_date.setText(MeasurementUtil.convertDay(vod.getRecorded_at()));

            String vodLength = "";

            int currentProgress = LocalDataUtil.getVODProgress(mContext, vod.getVodId());
            if (currentProgress != 0) {
                vodLength += MeasurementUtil.convertTime(currentProgress * 1000) + "/";
            }

            vodLength += MeasurementUtil.convertTime(Integer.parseInt(vod.getVodLength()) * 1000);
            holder.vod_length.setText(vodLength);
        }

        holder.streamerName.setText(vod.getDisplay_name());
        holder.vod_title.setText(vod.getVodTitle());
        holder.vod_game.setText(vod.getVodGameName());
        holder.vod_views.setText(NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(vod.getViewCount())));

        Picasso.with(mContext).load(Uri.parse(vod.getLogoUrl())).fit().into(holder.logo);
        Picasso.with(mContext).load(Uri.parse(vod.getVodPreviewUrl())).fit().into(holder.preview);
    }

    /* preset layout size to reserve space in cardview item */
    private void setVideoPreviewLayout(MyViewHolder holder) {
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sizeOffset = 1;
        }

        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

        //multiply by 4 since it is a single value margin
        int marginPixels = (int) (mContext.getResources().getDimension(R.dimen.card_item_margin)*4);

        holder.preview.getLayoutParams().width = metrics.widthPixels - marginPixels;

        if(mContext.getResources().getBoolean(R.bool.isTablet)) {
            holder.preview.getLayoutParams().height = (int) (((metrics.widthPixels - marginPixels)/(16/9.0))/(2+sizeOffset));
        } else {
            holder.preview.getLayoutParams().height = (int) (((metrics.widthPixels - marginPixels)/(16/9.0))/(1+sizeOffset));
        }

    }

    @Override
    public int getItemCount() {
        return vods.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout vod_info_container_top;
        ImageView preview, logo, vod_views_icon;
        TextView vod_date, vod_length, streamerName, vod_title, vod_game, vod_views, vod_livestream;
        CardView card_vods;

        public MyViewHolder(View view) {
            super(view);

            preview = (ImageView) view.findViewById(R.id.preview);
            logo = (ImageView) view.findViewById(R.id.logo);

            vod_date = (TextView) view.findViewById(R.id.vod_date);
            vod_length = (TextView) view.findViewById(R.id.vod_length);
            streamerName = (TextView) view.findViewById(R.id.streamerName);
            vod_title = (TextView) view.findViewById(R.id.vod_title);
            vod_game = (TextView) view.findViewById(R.id.vod_game);
            vod_views = (TextView) view.findViewById(R.id.vod_views);

            vod_info_container_top = (RelativeLayout) view.findViewById(R.id.vod_info_container_top);
            vod_views_icon = (ImageView) view.findViewById(R.id.vod_views_icon);
            vod_livestream = (TextView) view.findViewById(R.id.vod_livestream);

            card_vods = (CardView) view.findViewById(R.id.card_vods);
            card_vods.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    if (!vods.get(position).isLiveStream()) {
                        Intent intent = new Intent(mContext, VODActivity.class);
                        intent.putExtra("display_name", vods.get(position).getDisplay_name());
                        intent.putExtra("vodId", vods.get(position).getVodId());
                        intent.putExtra("vodLength", vods.get(position).getVodLength());

                        mContext.startActivity(intent);
                        ((Activity) mContext).overridePendingTransition(R.anim.slide_up, R.anim.anim_stay);
                    } else {
                        Intent intent = new Intent(mContext, LiveStreamActivity.class);
                        intent.putExtra("streamerName", vods.get(position).getChannelName());
                        intent.putExtra("display_name", vods.get(position).getDisplay_name());
                        intent.putExtra("streamStatus", vods.get(position).getVodTitle());
                        intent.putExtra("channelId", vods.get(position).getChannel_id());
                        intent.putExtra("logoUrl", vods.get(position).getLogoUrl());
                        intent.putExtra("gameName", vods.get(position).getVodGameName());
                        intent.putExtra("viewCount", Integer.parseInt(vods.get(position).getViewCount()));

                        ((Activity) mContext).startActivityForResult(intent, 4367);
                        ((Activity) mContext).overridePendingTransition(R.anim.slide_up, R.anim.anim_stay);
                    }
                }
            });
            card_vods.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();
                    Toast.makeText(mContext, vods.get(position).getVodTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }
}
