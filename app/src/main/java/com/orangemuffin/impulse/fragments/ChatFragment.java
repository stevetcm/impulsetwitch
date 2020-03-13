package com.orangemuffin.impulse.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codewaves.stickyheadergrid.StickyHeaderGridLayoutManager;
import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.adapters.ChatMessagesAdapter;
import com.orangemuffin.impulse.adapters.EmotesKeyboardAdapter;
import com.orangemuffin.impulse.models.ChatMessage;
import com.orangemuffin.impulse.models.EmoteInfo;
import com.orangemuffin.impulse.tasks.FetchChatTask;
import com.orangemuffin.impulse.tasks.FetchUserEmotesTask;
import com.orangemuffin.impulse.tasks.SendMessageTask;
import com.orangemuffin.impulse.utils.LocalDataUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/* Created by OrangeMuffin on 2018-03-18 */
public class ChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatMessagesAdapter chatMessagesAdapter;

    private String streamerName;
    private String channelId;

    private FetchChatTask fetchChatTask = null;
    private FetchUserEmotesTask fetchUserEmotesTask = null;

    private View chat_divider;
    private LinearLayout chat_container;
    private RelativeLayout emote_container;
    private EditText mSendEditText;
    private ImageView mSendIcon, mEmoteIcon;

    private TextView emote_keyboard_loading_text;
    private RecyclerView emotes_keyboard_list;
    private EmotesKeyboardAdapter adapter;

    private final int EMOTE_QUANTITY_PORTRAIT = 7;
    private final int EMOTE_QUANTITY_LANDSCAPE = 4;
    private final int EMOTE_QUANTITY_OFFSET = 3;

    private boolean isEmoteKeyboardShown = false;

    private final Handler delayChatScrollHandler = new Handler();
    private final Runnable chatScrollRunnable = new Runnable() {
        @Override
        public void run() {
            scrollRecyclerViewToBottom();
        }
    };

    private TextView scroll_to_last;

    private HashMap<String, String> userEmoteFinder = new HashMap<>();
    private List<EmoteInfo> gifEmoteList = new ArrayList<>();

    private BottomSheetDialog mChatMessageBottomSheet;

    /* required empty public constructor */
    public ChatFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context contextThemeWrapper;
        contextThemeWrapper = new ContextThemeWrapper(getActivity(), LocalDataUtil.setupThemeLayout(getActivity()));
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        View rootView = localInflater.inflate(R.layout.fragment_chat, container, false);

        streamerName = getArguments().getString("streamerName");
        channelId = getArguments().getString("channelId");

        scroll_to_last = (TextView) rootView.findViewById(R.id.scroll_to_last);
        scroll_to_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollRecyclerViewToBottom();
            }
        });

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        chatMessagesAdapter = new ChatMessagesAdapter(getContext(), recyclerView, scroll_to_last, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(chatMessagesAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy < 0) {
                    chatMessagesAdapter.userScrolledUp();
                }
            }
        });

        chat_divider = rootView.findViewById(R.id.chat_divider);

        chat_container = (LinearLayout) rootView.findViewById(R.id.chat_container);
        if (LocalDataUtil.getAccessToken(getContext()).equals("NULL")) {
            chat_container.setVisibility(View.GONE);
            chat_divider.setVisibility(View.GONE);
        }

        mSendEditText = (EditText) rootView.findViewById(R.id.send_edittext);
        mSendEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    handleSendMessage();
                    handled = true;
                }
                return handled;
            }
        });
        mSendEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    hideEmoteKeyboard();
                }
            }
        });

        mSendIcon = (ImageView) rootView.findViewById(R.id.send_icon);
        mSendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSendMessage();
            }
        });

        emote_container = (RelativeLayout) rootView.findViewById(R.id.emote_keyboard_container);

        mEmoteIcon = (ImageView) rootView.findViewById(R.id.emote_icon);
        mEmoteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isEmoteKeyboardShown) {
                    isEmoteKeyboardShown = true;
                    hideKeyboard(getActivity());
                    mSendEditText.clearFocus();
                    emote_container.setVisibility(View.VISIBLE);
                    delayChatScrollHandler.postDelayed(chatScrollRunnable, 1000);
                } else {
                    hideEmoteKeyboard();
                }
            }
        });

        emotes_keyboard_list = (RecyclerView) rootView.findViewById(R.id.emotes_keyboard_list);

        emote_keyboard_loading_text = (TextView) rootView.findViewById(R.id.emote_keyboard_loading_text);

        if (!LocalDataUtil.getEKeyboardStatus(getContext())) {
            emote_keyboard_loading_text.setText("Loading Emotes Keyboard...Might Take Some Time (Once)");
            emote_keyboard_loading_text.setVisibility(View.VISIBLE);
        } else {
            emote_keyboard_loading_text.setText("Checking Emotes... Please Wait...");
            emote_keyboard_loading_text.setVisibility(View.VISIBLE);
        }

        View view = getActivity().getLayoutInflater().inflate(R.layout.bottomsheet_chat, null);
        mChatMessageBottomSheet = new BottomSheetDialog(getContext());
        mChatMessageBottomSheet.setContentView(view);
        final BottomSheetBehavior behavior = BottomSheetBehavior.from((View) view.getParent());
        mChatMessageBottomSheet.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        return rootView;
    }

    public void handleSendMessage() {
        final String message = "" + mSendEditText.getText();
        if (!message.equals("")) {
            SendMessageTask sendMessageTask = new SendMessageTask(fetchChatTask, userEmoteFinder, message);
            sendMessageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mSendEditText.setText("");
            hideEmoteKeyboard();
            hideKeyboard(getActivity());
            adapter.updateRecent();
            emotes_keyboard_list.scrollToPosition(0);
            delayChatScrollHandler.postDelayed(chatScrollRunnable, 1000);
        }
    }

    public void addEmoteToFinder(String code, String id) {
        userEmoteFinder.put(code, id);
    }

    public boolean userMapContainsKey(String code) {
        return userEmoteFinder.containsKey(code);
    }

    public void addGifToList(EmoteInfo emote) {
        gifEmoteList.add(emote);
    }

    public void handleEmoteSelected(EmoteInfo emote) {
        String currentUserMessage = "" + mSendEditText.getText();

        if (currentUserMessage.equals("")) {
            mSendEditText.setText(emote.getCode());
        } else {
            if (currentUserMessage.substring(currentUserMessage.length() - 1).equals(" ")) {
                mSendEditText.setText(currentUserMessage + emote.getCode());
            } else {
                mSendEditText.setText(currentUserMessage + " " + emote.getCode());
            }
        }

        mSendEditText.setSelection(mSendEditText.getText().length());
    }

    public static void hideKeyboard(Activity activity) {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void scrollRecyclerViewToBottom() {
        recyclerView.smoothScrollToPosition(chatMessagesAdapter.getItemCount());
        scroll_to_last.setVisibility(View.GONE);
    }

    public boolean isEmoteKeyboardShown() {
        return isEmoteKeyboardShown;
    }

    public void hideEmoteKeyboard() {
        isEmoteKeyboardShown = false;
        emote_container.setVisibility(View.GONE);
        delayChatScrollHandler.postDelayed(chatScrollRunnable, 1000);
    }

    public int getEmoteQuantity() {
        if(getActivity().getResources().getBoolean(R.bool.isTablet)) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return (EMOTE_QUANTITY_LANDSCAPE + EMOTE_QUANTITY_OFFSET);
            } else {
                return EMOTE_QUANTITY_PORTRAIT + EMOTE_QUANTITY_OFFSET;
            }
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return EMOTE_QUANTITY_LANDSCAPE;
            } else {
                return EMOTE_QUANTITY_PORTRAIT;
            }
        }
    }

    public void setEmoteKeyboardUI() {
        if (adapter != null) {
            adapter.clearAll();
        }
        StickyHeaderGridLayoutManager mLayoutManager = new StickyHeaderGridLayoutManager(getEmoteQuantity());
        emotes_keyboard_list.setLayoutManager(mLayoutManager);
    }

    public void handleMessageOnClick(SpannableStringBuilder ssb, final String userName, final String message) {
        TextView mChatMessage = (TextView) mChatMessageBottomSheet.findViewById(R.id.message);
        mChatMessage.setText(ssb);

        TextView mentionUser = (TextView) mChatMessageBottomSheet.findViewById(R.id.mention_user);
        assert mentionUser != null;
        mentionUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentUserMessage = "" + mSendEditText.getText();

                if (currentUserMessage.equals("")) {
                    mSendEditText.setText("@" + userName + " ");
                } else {
                    if (currentUserMessage.substring(currentUserMessage.length() - 1).equals(" ")) {
                        mSendEditText.setText(currentUserMessage + "@" + userName + " ");
                    } else {
                        mSendEditText.setText(currentUserMessage + " " + "@" + userName + " ");
                    }
                }

                mSendEditText.setSelection(mSendEditText.getText().length());
                mChatMessageBottomSheet.dismiss();
            }
        });

        TextView copyMessage = (TextView) mChatMessageBottomSheet.findViewById(R.id.copy_message);
        assert copyMessage != null;
        copyMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentUserMessage = "" + mSendEditText.getText();

                String temp = message;

                String[] arr = message.split(" ", 2);
                if ((arr[0].equals("ACTION") && arr.length > 1)
                        || (arr[0].equals("/me") && arr.length > 1)) {
                    temp = arr[1];
                }

                if (currentUserMessage.equals("")) {
                    mSendEditText.setText(temp);
                } else {
                    if (currentUserMessage.substring(currentUserMessage.length() - 1).equals(" ")) {
                        mSendEditText.setText(currentUserMessage + temp);
                    } else {
                        mSendEditText.setText(currentUserMessage + " " + temp);
                    }
                }

                mSendEditText.setSelection(mSendEditText.getText().length());
                mChatMessageBottomSheet.dismiss();
            }
        });

        mChatMessageBottomSheet.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recyclerView.smoothScrollToPosition(chatMessagesAdapter.getItemCount());
        hideEmoteKeyboard();
        hideKeyboard(getActivity());
        callUserEmotesTask();
    }

    public void backPressed() {
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fetchChatTask != null) {
            fetchChatTask.stop();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        callChatTask();
        callUserEmotesTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fetchChatTask != null) {
            fetchChatTask.cancel(true);
        }
        fetchUserEmotesTask.cancel(true);
    }

    private void callChatTask() {
        fetchChatTask = new FetchChatTask(getContext(), new FetchChatTask.FetchChatCallback() {
            @Override
            public void onChatFetched(ChatMessage chatMessage) {
                chatMessagesAdapter.add(chatMessage);
            }

            @Override
            public void onBanChatFetched(String displayName) {
                chatMessagesAdapter.banMessage(displayName);
            }
        }, streamerName, channelId);
        fetchChatTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void callUserEmotesTask() {
        fetchUserEmotesTask = new FetchUserEmotesTask(getContext(), ChatFragment.this, new FetchUserEmotesTask.FetchUserEmotesCallback() {
            @Override
            public void onUserEmotesFetched(List<List<EmoteInfo>> emotes) {
                if (!emotes.isEmpty()) {
                    emote_keyboard_loading_text.setVisibility(View.GONE);
                    emotes_keyboard_list.setVisibility(View.VISIBLE);
                    setEmoteKeyboardUI();
                    adapter = new EmotesKeyboardAdapter(getContext(), ChatFragment.this, emotes);
                    emotes_keyboard_list.setAdapter(adapter);

                    if (fetchChatTask != null) {
                        fetchChatTask.addBttvEmotes(userEmoteFinder);
                        fetchChatTask.addGifEmoteList(gifEmoteList);
                    }

                    emote_keyboard_loading_text.setVisibility(View.GONE);
                }
            }
        });
        fetchUserEmotesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, streamerName);
    }
}
