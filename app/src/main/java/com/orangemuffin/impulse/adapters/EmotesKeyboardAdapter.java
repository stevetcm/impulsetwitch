package com.orangemuffin.impulse.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codewaves.stickyheadergrid.StickyHeaderGridAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.activities.LiveStreamActivity;
import com.orangemuffin.impulse.fragments.ChatFragment;
import com.orangemuffin.impulse.models.EmoteInfo;
import com.orangemuffin.impulse.utils.LocalDataUtil;
import com.orangemuffin.impulse.utils.MeasurementUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

/* Created by OrangeMuffin on 2018-05-25 */
public class EmotesKeyboardAdapter extends StickyHeaderGridAdapter {
    private List<List<EmoteInfo>> emoteList;
    private Context mContext;
    ChatFragment chatFragment;

    List<EmoteInfo> recentList = new ArrayList<>();
    List<EmoteInfo> noImageList = new ArrayList<>();
    private int recentLimit = 21;

    public EmotesKeyboardAdapter(Context mContext, ChatFragment chatFragment, List<List<EmoteInfo>> emoteList) {
        this.mContext = mContext;
        this.emoteList = emoteList;
        Collections.reverse(this.emoteList);

        if (emoteList.get(0).get(0).getOwner_id().equals("-1")) {
            recentList.addAll(emoteList.get(0));
        }

        String json = LocalDataUtil.getRecentEmotes(mContext);
        if (!json.equals("NULL")) {
            Gson gson = new Gson();
            List<EmoteInfo> recentEmotes = gson.fromJson(json, new TypeToken<List<EmoteInfo>>(){}.getType());
            noImageList.addAll(recentEmotes);
        }

        if(mContext.getResources().getBoolean(R.bool.isTablet)) {
            recentLimit = 30;
        }

        this.chatFragment = chatFragment;
    }

    public void updateRecent() {
        if (!recentList.isEmpty()) {
            if (emoteList.get(0).get(0).getOwner_id().equals("-1")) {
                emoteList.remove(0);
            }
            emoteList.add(0, new ArrayList<>(recentList));
            notifyAllSectionsDataSetChanged();
        }
    }

    public void clearAll() {
        emoteList.clear();
    }

    @Override
    public int getSectionCount() {
        return emoteList.size();
    }

    @Override
    public int getSectionItemCount(int section) {
        return emoteList.get(section).size();
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emotes_keyboard_header, parent, false);
        return new MyHeaderViewHolder(view);
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int headerType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emotes_keyboard_element, parent, false);
        return new MyItemViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder headerViewHolder, int section) {
        final MyHeaderViewHolder holder = (MyHeaderViewHolder) headerViewHolder;

        String groupText = "";
        if (!emoteList.get(section).isEmpty()) {
            if (emoteList.get(section).get(0).getOwner_id().equals("-4")) {
                groupText = "BetterTTV Global Emotes";
            } else if (emoteList.get(section).get(0).getOwner_id().equals("-3")) {
                groupText = "FrankerFaceZ Channel Emotes";
            } else if (emoteList.get(section).get(0).getOwner_id().equals("-2")) {
                groupText = "BetterTTV Channel Emotes";
            } else if (emoteList.get(section).get(0).getOwner_id().equals("-1")) {
                groupText = "Recently Used Emotes";
            } else if (emoteList.get(section).get(0).getOwner_id().equals("19194")) {
                groupText = "Twitch Unlocked Emotes";
            } else if (emoteList.get(section).get(0).getOwner_id().equals("0")) {
                groupText = "Twitch Standard Emotes";
            } else {
                for (int i = 0; i < emoteList.get(section).size(); i++) {
                    if (i < emoteList.get(section).size() - 1) {
                        groupText += emoteList.get(section).get(i).getCode() + ", ";
                    } else {
                        groupText += emoteList.get(section).get(i).getCode();
                    }
                }
            }
        }

        holder.emote_group.setText(groupText);
    }

    @Override
    public void onBindItemViewHolder(ItemViewHolder itemViewHolder, final int section, final int position) {
        final MyItemViewHolder holder = (MyItemViewHolder) itemViewHolder;

        setEmoteLayout(holder);

        final EmoteInfo emote = emoteList.get(section).get(position);

        if (emote.getType() != null && emote.getType().equals("gif") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                GifDrawable gifDrawable = new GifDrawable(emote.getGifByte());
                holder.emote_image.setImageDrawable(gifDrawable);
            } catch (Exception e) { }
        } else {
            holder.emote_image.setImageBitmap(emote.getImage());
        }

        if (!emote.isAllowed()) {
            holder.emote_allowed.setVisibility(View.VISIBLE);
        } else {
            holder.emote_allowed.setVisibility(View.GONE);
        }

        if (emote.getOwner_id().equals("-1")) {
            holder.emote_image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    removeEmoteDialog(emote.getCode(), position);
                    return true;
                }
            });
        }
    }

    private void setEmoteLayout(MyItemViewHolder itemViewHolder) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

        double sizeOffset = 1;
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sizeOffset = LocalDataUtil.getChatWidth(mContext)/100.0;
        }

        itemViewHolder.emote_image_container.getLayoutParams().width = (int) ((metrics.widthPixels/chatFragment.getEmoteQuantity()) * sizeOffset);
        itemViewHolder.emote_image_container.getLayoutParams().height = MeasurementUtil.dpToPixel(48);
    }

    public class MyHeaderViewHolder extends HeaderViewHolder {
        TextView emote_group;

        MyHeaderViewHolder(View itemView) {
            super(itemView);

            emote_group = (TextView) itemView.findViewById(R.id.emote_group);
        }
    }

    public class MyItemViewHolder extends ItemViewHolder {
        RelativeLayout emote_image_container;
        ImageView emote_image, emote_allowed;

        MyItemViewHolder(View itemView) {
            super(itemView);

            emote_image_container = (RelativeLayout) itemView.findViewById(R.id.emote_image_container);
            emote_image = (ImageView) itemView.findViewById(R.id.emote_image);
            emote_allowed = (ImageView) itemView.findViewById(R.id.not_allowed);

            emote_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int section = getAdapterPositionSection(getAdapterPosition());
                    final int offset = getItemSectionOffset(section, getAdapterPosition());

                    EmoteInfo current = emoteList.get(section).get(offset);
                    processRecentEmotes(current);

                    chatFragment.handleEmoteSelected(emoteList.get(section).get(offset));
                }
            });
        }
    }



    private void processRecentEmotes(EmoteInfo current) {
        EmoteInfo temp = copyEmoteForRecent(current, "temp");
        EmoteInfo dummy = copyEmoteForRecent(current, "dummy");

        for (int i = 0; i < recentList.size(); i++) {
            if (current.getCode().equals(recentList.get(i).getCode())) {
                recentList.remove(i);
                noImageList.remove(i);
                break;
            }
        }

        if (recentList.size() >= recentLimit) {
            recentList.remove(recentList.size()-1);
            noImageList.remove(noImageList.size()-1);

        }

        noImageList.add(0, dummy);
        recentList.add(0 ,temp);

        Gson gson = new Gson();
        String json = gson.toJson(noImageList, new TypeToken<List<EmoteInfo>>(){}.getType());
        LocalDataUtil.setRecentEmotes(mContext, json);
    }

    private void removeRecentEmote(int position) {
        recentList.remove(position);
        noImageList.remove(position);

        updateRecent();

        Gson gson = new Gson();
        String json = gson.toJson(noImageList, new TypeToken<List<EmoteInfo>>(){}.getType());
        LocalDataUtil.setRecentEmotes(mContext, json);
    }

    private EmoteInfo copyEmoteForRecent(EmoteInfo emote, String type) {
        EmoteInfo temp = new EmoteInfo();
        temp.setId(emote.getId());
        temp.setCode(emote.getCode());
        temp.setOwner_id("-1");
        temp.setType(emote.getType());
        temp.setAllowed(emote.isAllowed());

        if (type.equals("temp")) {
            if (emote.getType().equals("gif")) {
                temp.setGifByte(emote.getGifByte());
            } else {
                temp.setImage(emote.getImage());
            }
        }

        return temp;
    }

    private void removeEmoteDialog(String code, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder((mContext));
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_simple_text, null);
        TextView emote_remove_text = (TextView) dialogView.findViewById(R.id.custom_text);
        emote_remove_text.setText("Remove '" + code + "' from Recently Used Emotes?");

        builder.setView(dialogView)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeRecentEmote(position);
                    }
                })
                .setNegativeButton("NO", null);
        builder.create();

        AlertDialog alertDialog = builder.show();
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFFFFFFF"));
    }
}
