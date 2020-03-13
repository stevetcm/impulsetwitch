package com.orangemuffin.impulse.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.fragments.ChatFragment;
import com.orangemuffin.impulse.models.ChatMessage;
import com.orangemuffin.impulse.models.ImgType;
import com.orangemuffin.impulse.utils.MeasurementUtil;
import com.orangemuffin.impulse.views.DrawableCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.MultiCallback;

/* Created by OrangeMuffin on 2018-03-20 */
public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesAdapter.MyViewHolder> {
    private Context context;
    private List<ChatMessage> chatList = new ArrayList<>();

    private boolean isScrollable = false;
    private RecyclerView recyclerView;

    private TextView scroll_to_last;

    private ChatFragment chatFragment;

    public ChatMessagesAdapter(Context context, RecyclerView recyclerView, TextView scroll_to_last, ChatFragment chatFragment) {
        this.recyclerView = recyclerView;
        this.context = context;

        this.scroll_to_last = scroll_to_last;

        this.chatFragment = chatFragment;
    }

    public void add(ChatMessage message) {
        chatList.add(message);
        notifyItemInserted(getItemCount()-1);

        //minimal buffer
        if (getItemCount() > 300) {
            if (isScrollable) {
                remove();
            }
        }

        if (isScrollable) {
            recyclerView.smoothScrollToPosition(getItemCount());
        }

        //scrolled up buffer (max)
        if (getItemCount() > 750) {
            remove();
        }
    }

    public void remove() {
        chatList.remove(0);
        notifyItemRemoved(0);
    }

    public void banMessage(String displayName) {
        int limit = Math.min(getItemCount(), getItemCount());
        if (getItemCount() != 0) {
            for (int i = 1; i < limit; i++) {
                ChatMessage chatMessage = chatList.get(getItemCount() - i);
                if (chatMessage.getUserName() != null && chatMessage.getUserName().toLowerCase().equals(displayName)) {
                    chatMessage.setMessage("<message deleted>");
                    notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    public void userScrolledUp() {
        isScrollable = false;
        scroll_to_last.setVisibility(View.VISIBLE);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final MyViewHolder final_holder = holder;

        if (position == getItemCount()-1) {
            isScrollable = true;
            scroll_to_last.setVisibility(View.GONE);
        }

        final ChatMessage message = chatList.get(position);

        final SpannableStringBuilder ssb = new SpannableStringBuilder();
        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);

        if (message.getUserName() != null) {
            for (int i = 0, count = 0; i < message.getBadges().size(); i++) {
                Drawable drawable = new BitmapDrawable(context.getResources(), message.getBadges().get(i));
                if (drawable.getIntrinsicWidth() != 0) {
                    int width = MeasurementUtil.dpToPixel(18) * drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
                    drawable.setBounds(0, 0, width, MeasurementUtil.dpToPixel(18));
                    ssb.append("  ").setSpan(new ImageSpan(drawable), count, count + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    count += 2;
                }
            }

            final String nickname = message.getUserName();
            ssb.append(nickname);

            final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.parseColor(message.getColor()));

            ssb.setSpan(fcs, 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(bss, 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            int current = ssb.length() + 2;

            String content;
            int prefixAction = 0;
            String[] arr = message.getMessage().split(" ", 2);
            if ((arr[0].contains("ACTION") && arr[0].length() < 8 && arr.length > 1)
                    || (arr[0].equals("/me") && arr.length > 1)) {
                content = " " + arr[1];
                ssb.append(content);
                ssb.setSpan(fcs, 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                prefixAction = 1;
            } else {
                content = ": " + message.getMessage();
                ssb.append(content);
            }

            if (!message.getMessage().equals("<message deleted>")) {
                Iterator it = message.getEmotes().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();

                    try {
                        if (((ImgType) pair.getValue()).getType().equals("gif") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            holder.message.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                            GifDrawable gifDrawable = new GifDrawable(((ImgType) pair.getValue()).getGifByte());
                            if (gifDrawable.getIntrinsicWidth() != 0) {
                                int height = MeasurementUtil.dpToPixel(22);
                                int width = height * gifDrawable.getIntrinsicWidth() / gifDrawable.getIntrinsicHeight();
                                gifDrawable.setBounds(0, 0, width, height);
                                gifDrawable.setCallback(holder.drawableCallback);
                            }

                            String[] split = ((String) pair.getKey()).split(",");
                            for (int i = 0; i < split.length; i++) {
                                String[] innerSplit = split[i].split("-");
                                int start = Integer.parseInt(innerSplit[0]);
                                int end = Integer.parseInt(innerSplit[1]);

                                try {
                                    ssb.setSpan(new ImageSpan(gifDrawable), current + start - prefixAction, current + end + 1 - prefixAction, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                } catch (Exception e) { }
                            }
                        } else {
                            Drawable drawable = new BitmapDrawable(context.getResources(), ((ImgType) pair.getValue()).getBitmap());
                            if (drawable.getIntrinsicWidth() != 0) {
                                int height = MeasurementUtil.dpToPixel(22);
                                int width = height * drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
                                drawable.setBounds(0, 0, width, height);
                            }

                            String[] split = ((String) pair.getKey()).split(",");
                            for (int i = 0; i < split.length; i++) {
                                String[] innerSplit = split[i].split("-");
                                int start = Integer.parseInt(innerSplit[0]);
                                int end = Integer.parseInt(innerSplit[1]);

                                try {
                                    ssb.setSpan(new ImageSpan(drawable), current + start - prefixAction, current + end + 1 - prefixAction, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                } catch (Exception e) { }
                            }
                        }
                    } catch (Exception e) { }
                }
            }

            holder.message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chatFragment.handleMessageOnClick(ssb, nickname, message.getMessage());
                }
            });
        } else {
            //setting up system text
            ssb.append(message.getMessage());
            final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.parseColor("#959595"));
            ssb.setSpan(fcs, 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.message.setOnClickListener(null);

            /* //background for twitch notice
            if (message.isNotice()) {
                final BackgroundColorSpan bcs = new BackgroundColorSpan(Color.YELLOW);
                ssb.setSpan(bcs, 0, ssb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }*/
        }

        holder.message.setText(ssb);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView message;
        private DrawableCallback drawableCallback;

        public MyViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.message);

            drawableCallback = new DrawableCallback(message);
        }
    }
}
