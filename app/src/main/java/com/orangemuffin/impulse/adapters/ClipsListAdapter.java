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
import android.widget.TextView;
import android.widget.Toast;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.activities.ClipActivity;
import com.orangemuffin.impulse.models.ClipInfo;
import com.orangemuffin.impulse.utils.MeasurementUtil;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/* Created by OrangeMuffin on 2018-04-21 */
public class ClipsListAdapter extends RecyclerView.Adapter<ClipsListAdapter.MyViewHolder> {
    private Context mContext;
    private List<ClipInfo> clips;

    private int sizeOffset = 0;

    private String logoUrl;

    public ClipsListAdapter(Context mContext, List<ClipInfo> clips, String logoUrl) {
        this.mContext = mContext;
        this.clips = clips;

        this.logoUrl = logoUrl;
    }

    public void addAll(List<ClipInfo> clips) {
        this.clips.addAll(clips);
        notifyDataSetChanged();
    }

    public void clearList() {
        clips.clear();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_clips, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        setVideoPreviewLayout(holder);

        ClipInfo clip = clips.get(position);

        holder.display_name.setText(clip.getDisplayName());
        holder.clip_views.setText(NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(clip.getViewCount())));

        holder.clip_game.setText(clip.getClipGameName());

        Picasso.with(mContext).load(Uri.parse(logoUrl)).fit().into(holder.logo);
        Picasso.with(mContext).load(Uri.parse(clip.getClipPreviewUrl())).fit().into(holder.clipPreview);


        holder.clip_date.setText(clip.getClipDate());
        holder.clip_title.setText(clip.getClipTitle());
        holder.clip_length.setText(MeasurementUtil.convertTimeClip(Integer.valueOf(clip.getClipLength()) * 1000));
    }

    /* preset layout size to reserve space in cardview item */
    private void setVideoPreviewLayout(MyViewHolder holder) {
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sizeOffset = 1;
        }

        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

        //multiply by 4 since it is a single value margin
        int marginPixels = (int) (mContext.getResources().getDimension(R.dimen.card_item_margin)*4);

        holder.clipPreview.getLayoutParams().width = metrics.widthPixels - marginPixels;

        if(mContext.getResources().getBoolean(R.bool.isTablet)) {
            holder.clipPreview.getLayoutParams().height = (int) (((metrics.widthPixels - marginPixels)/(16/9.0))/(2+sizeOffset));
        } else {
            holder.clipPreview.getLayoutParams().height = (int) (((metrics.widthPixels - marginPixels)/(16/9.0))/(1+sizeOffset));
        }

    }

    @Override
    public int getItemCount() {
        return clips.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView clipPreview, logo;
        TextView display_name, clip_views, clip_game;
        TextView clip_date, clip_title, clip_length;
        CardView card_clips;

        public MyViewHolder(View view) {
            super(view);

            clipPreview = (ImageView) view.findViewById(R.id.clipPreview);
            logo = (ImageView) view.findViewById(R.id.logo);

            display_name = (TextView) view.findViewById(R.id.display_name);
            clip_views = (TextView) view.findViewById(R.id.clip_views);
            clip_game = (TextView) view.findViewById(R.id.clip_game);

            clip_date = (TextView) view.findViewById(R.id.clip_date);
            clip_title = (TextView) view.findViewById(R.id.clip_title);
            clip_length = (TextView) view.findViewById(R.id.clip_length);

            card_clips = (CardView) view.findViewById(R.id.card_clips);
            card_clips.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    Intent intent = new Intent(mContext, ClipActivity.class);

                    intent.putExtra("display_name", clips.get(position).getDisplayName());

                    intent.putExtra("clipTitle", clips.get(position).getClipTitle());
                    intent.putExtra("clipDate", clips.get(position).getClipDate());
                    intent.putExtra("clipSlug", clips.get(position).getClipSlug());
                    intent.putExtra("clipLength", clips.get(position).getClipLength());

                    intent.putExtra("vodId", clips.get(position).getVodId());
                    intent.putExtra("vodOffset", clips.get(position).getVodOffset());

                    mContext.startActivity(intent);
                    ((Activity) mContext).overridePendingTransition(R.anim.slide_up, R.anim.anim_stay);
                }
            });

            card_clips.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();

                    Toast.makeText(mContext, clips.get(position).getClipTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }

}
