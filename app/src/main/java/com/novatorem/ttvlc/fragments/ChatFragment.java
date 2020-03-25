package com.novatorem.ttvlc.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.transition.Transition;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.novatorem.ttvlc.R;
import com.novatorem.ttvlc.chat.ChatEmoteManager;
import com.novatorem.ttvlc.chat.ChatManager;
import com.novatorem.ttvlc.views.recyclerviews.AutoSpanRecyclerView;
import com.novatorem.ttvlc.activities.stream.LiveStreamActivity;
import com.novatorem.ttvlc.adapters.ChatAdapter;
import com.novatorem.ttvlc.misc.ResizeHeightAnimation;
import com.novatorem.ttvlc.model.ChatMessage;
import com.novatorem.ttvlc.model.Emote;
import com.novatorem.ttvlc.model.ChannelInfo;
import com.novatorem.ttvlc.service.Service;
import com.novatorem.ttvlc.service.Settings;
import com.novatorem.ttvlc.tasks.ConstructChatMessageTask;
import com.novatorem.ttvlc.tasks.GetTwitchEmotesTask;
import com.novatorem.ttvlc.tasks.SendMessageTask;
import com.novatorem.ttvlc.views.recyclerviews.ChatRecyclerView;
import com.novatorem.ttvlc.views.EditTextBackEvent;
import com.novatorem.ttvlc.views.recyclerviews.auto_span_behaviours.EmoteAutoSpanBehaviour;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;


interface EmoteKeyboardDelegate {
	void onEmoteClicked(Emote clickedEmote, View view);
}

public class ChatFragment extends Fragment implements EmoteKeyboardDelegate, ChatAdapter.ChatAdapterCallback {
	private static Integer[] supportedUnicodeEmotes = new Integer[] {
			0x1F600, 0x1F601, 0x1F602, 0x1F603, 0x1F604, 0x1F605, 0x1F606, 0x1F607, 0x1F608, 0x1F609, 0x1F60A, 0x1F60B, 0x1F60C, 0x1F60D, 0x1F60E, 0x1F60F,
			0x1F610, 0x1F611, 0x1F612, 0x1F613, 0x1F614, 0x1F615, 0x1F616, 0x1F617, 0x1F618, 0x1F619, 0x1F61A, 0x1F61B, 0x1F61C, 0x1F61D, 0x1F61E, 0x1F61F,
			0x1F620, 0x1F621, 0x1F622, 0x1F623, 0x1F624, 0x1F625, 0x1F626, 0x1F627, 0x1F628, 0x1F629, 0x1F62A, 0x1F62B, 0x1F62C, 0x1F62D, 0x1F62E, 0x1F62F,
			0x1F630, 0x1F631, 0x1F632, 0x1F633, 0x1F634, 0x1F635, 0x1F636, 0x1F637, 0x1F638, 0x1F639, 0x1F63A, 0x1F63B, 0x1F63C, 0x1F63D, 0x1F63E, 0x1F63F,
			0x1F640, 0x1F641, 0x1F642, 0x1F643, 0x1F644, 0x1F645, 0x1F646, 0x1F647, 0x1F648, 0x1F649, 0x1F64A, 0x1F64B, 0x1F64C, 0x1F64D, 0x1F64E, 0x1F64F
	};

	private static ArrayList<Emote> supportedTextEmotes, bttvEmotes, bttvChannelEmotes, twitchEmotes, subscriberEmotes;
	private static ArrayList<Emote> recentEmotes, emotesToHide;

	private final String LOG_TAG = getClass().getSimpleName();
	private final int VIBRATION_FEEDBACK = HapticFeedbackConstants.KEYBOARD_TAP;

	private boolean chatStatusBarShowing = true;

	private ChatAdapter mChatAdapter;
	private ChatManager chatManager;
	private ChannelInfo mChannelInfo;
	private Settings settings;

	private RelativeLayout 		mChatInputLayout;
	private ChatRecyclerView 	mRecyclerView;
	private EditTextBackEvent 	mSendText;
	private ImageView 	mSendButton,
			mSlowmodeIcon,
			mSubonlyIcon,
			mR9KIcon;
	private TextView 	mChatStatus;
	private View 		chatInputDivider;
	private FrameLayout mChatStatusBar;

	//Emote Keyboard
	private EmoteGridFragment textEmotesFragment, recentEmotesFragment, twitchEmotesFragment, bttvEmotesFragment, subscriberEmotesFragment;
	private ImageView mEmoteKeyboardButton, mEmoteChatBackspace;
	private ViewGroup emoteKeyboardContainer;
	private boolean isEmoteKeyboardOpen = false;
	private TabLayout mEmoteTabs;
	private ViewPager mEmoteViewPager;
	private Integer selectedTabColorRes, unselectedTabColorRes;
	private boolean hasSoftKeyboardBeenShown = false,
			hideKeyboardWhenShown = false,
			isSoftKeyboardOpen = false;
	private ColorFilter defaultBackgroundColor;
	private Vibrator vibe;

	public static ChatFragment getInstance(Bundle args) {
		ChatFragment fragment = new ChatFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		final View mRootView = inflater.inflate(R.layout.fragment_chat, container, false);
		LinearLayoutManager llm = new LinearLayoutManager(getContext());
		llm.setStackFromEnd(true);
		settings = new Settings(getContext());

		mSendText = mRootView.findViewById(R.id.send_message_textview);
		mSendButton = mRootView.findViewById(R.id.chat_send_ic);
		mSlowmodeIcon = mRootView.findViewById(R.id.slowmode_ic);
		mSubonlyIcon = mRootView.findViewById(R.id.subsonly_ic);
		mR9KIcon = mRootView.findViewById(R.id.r9k_ic);
		mRecyclerView = mRootView.findViewById(R.id.ChatRecyclerView);
		chatInputDivider = mRootView.findViewById(R.id.chat_input_divider);
		mChatInputLayout = mRootView.findViewById(R.id.chat_input); mChatInputLayout.bringToFront();
		mChatStatus = mRootView.findViewById(R.id.chat_status_text);
		mChatAdapter = new ChatAdapter(mRecyclerView, getActivity(), this);
		mChatStatusBar = mRootView.findViewById(R.id.chat_status_bar);

		mEmoteKeyboardButton = mRootView.findViewById(R.id.chat_emote_keyboard_ic);
		mEmoteChatBackspace = mRootView.findViewById(R.id.emote_backspace);
		emoteKeyboardContainer = mRootView.findViewById(R.id.emote_keyboard_container);
		mEmoteTabs = mRootView.findViewById(R.id.tabs);
		mEmoteViewPager = mRootView.findViewById(R.id.tabs_viewpager);
		selectedTabColorRes = Service.getColorAttribute(R.attr.textColor, R.color.black_text, getContext());
		unselectedTabColorRes = Service.getColorAttribute(R.attr.disabledTextColor, R.color.black_text_disabled, getContext());
		vibe = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

		defaultBackgroundColor = mSendButton.getColorFilter();
		mRecyclerView.setAdapter(mChatAdapter);
		mRecyclerView.setLayoutManager(llm);
		mRecyclerView.setItemAnimator(null);

		mChannelInfo = getArguments().getParcelable(getString(R.string.stream_fragment_streamerInfo));// intent.getParcelableExtra(getResources().getString(R.string.intent_key_streamer_info));

		if (!settings.isLoggedIn()) {
			userNotLoggedIn();
		} else {
			setupChatInput();
			loadRecentEmotes();
			setupEmoteViews();
		}

		setupKeyboardShowListener();

		setupTransition();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
		final ChatFragment instance = this;
		chatManager = new ChatManager(getContext(), mChannelInfo.getStreamerName(), mChannelInfo.getUserId(), new ChatManager.ChatCallback() {
			private boolean connected = false;

			private boolean isFragmentActive() {
				return instance != null && !instance.isDetached() && instance.isAdded();
			}

			@Override
			public void onMessage(ChatMessage message) {
				mRecyclerView.bringToFront();
				if (isFragmentActive())
					addMessage(message);
			}

			@Override
			public void onConnecting() {
				if (isFragmentActive()) {
					ChatFragment.this.showChatStatusBar();
					mChatStatus.setText(getString(R.string.chat_status_connecting));
				}
			}

			@Override
			public void onReconnecting() {
				if (isFragmentActive()) {
					ChatFragment.this.showChatStatusBar();
					mChatStatus.setText(getString(R.string.chat_status_reconnecting));
				}
			}

			@Override
			public void onConnected() {
				if (isFragmentActive()) {
					Log.d(LOG_TAG, "Chat connected");
					this.connected = true;
					ChatFragment.this.showThenHideChatStatusBar();
					mChatStatus.setText(getString(R.string.chat_status_connected));
				}
			}

			@Override
			public void onConnectionFailed() {
				if (isFragmentActive()) {
					this.connected = false;
					ChatFragment.this.showChatStatusBar();
					mChatStatus.setText(getString(R.string.chat_status_connection_failed));
				}
			}

			@Override
			public void onRoomstateChange(boolean isR9K, boolean isSlow, boolean isSubsOnly) {
				if (isFragmentActive()) {
					if (this.connected) {
						ChatFragment.this.showThenHideChatStatusBar();
					} else {
						ChatFragment.this.showChatStatusBar();
					}

					Log.d(LOG_TAG, "Roomstate has changed");
					this.roomStateIconChange(isR9K, mR9KIcon);
					this.roomStateIconChange(isSlow, mSlowmodeIcon);
					this.roomStateIconChange(isSubsOnly, mSubonlyIcon);
				}
			}

			@Override
			public void onBttvEmoteIdFetched(List<Emote> bttvChannel, List<Emote> bttvGlobal) {
				try {
					if (isFragmentActive()) {
						bttvEmoteInfoLoaded(bttvChannel, bttvGlobal);
					}
				} catch (IllegalAccessError e) {
					e.printStackTrace();
				}
			}

			private void roomStateIconChange(boolean isOn, ImageView icon) {
				if (isFragmentActive()) {
					if(!isOn) {
						icon.setVisibility(View.GONE);
					} else {
						icon.setVisibility(View.VISIBLE);
					}
				}
			}
		});

		if (supportedTextEmotes == null) {
			supportedTextEmotes = new ArrayList<>();
			for (Integer supportedUnicodeEmote : supportedUnicodeEmotes) {
				supportedTextEmotes.add(new Emote(Service.getEmijoByUnicode(supportedUnicodeEmote)));
			}
		}
	}


	@Override
	public void onPause() {
		super.onPause();
		saveRecentEmotes();
	}

	@Override
	public void onStop() {
		super.onStop();
		chatManager.stop();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void setupTransition() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			return;
		}

		getActivity().getWindow().getReturnTransition().addListener(new Transition.TransitionListener() {

			@Override
			public void onTransitionEnd(Transition transition) {
				mChatStatusBar.setVisibility(View.GONE);
				mChatStatus.setVisibility(View.GONE);
			}

			@Override
			public void onTransitionCancel(Transition transition) {
				onTransitionEnd(transition);
			}

			public void onTransitionStart(Transition transition) {
				mChatStatusBar.setVisibility(View.GONE);
				mChatStatus.setVisibility(View.GONE);


			}
			public void onTransitionPause(Transition transition) {}
			public void onTransitionResume(Transition transition) {}
		});

	}

	private void showThenHideChatStatusBar() {
		this.showChatStatusBar();
		this.hideChatStatusBar(1000);
	}

	/**
	 * Shows the chat status bar with an animation
	 */
	private void showChatStatusBar() {
		if (!this.chatStatusBarShowing) {
			ResizeHeightAnimation heightAnimation = new ResizeHeightAnimation(this.mChatStatusBar, (int) getResources().getDimension(R.dimen.chat_status_bar_height));
			heightAnimation.setDuration(240);
			heightAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
			this.mChatStatusBar.startAnimation(heightAnimation);

			this.chatStatusBarShowing = true;
		}
	}

	/**
	 * Hides the chat status bar with an animation
	 */
	private void hideChatStatusBar(int delay) {
		if (this.chatStatusBarShowing) {
			ResizeHeightAnimation heightAnimation = new ResizeHeightAnimation(this.mChatStatusBar, (int) getResources().getDimension(R.dimen.chat_input_divider_height));
			heightAnimation.setStartOffset(delay);
			heightAnimation.setDuration(140);
			heightAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
			this.mChatStatusBar.startAnimation(heightAnimation);

			this.chatStatusBarShowing = false;
		}
	}

	/**
	 * Save the recently used emotes
	 */
	private void saveRecentEmotes() {
		if (recentEmotes != null && !recentEmotes.isEmpty()) {
			settings.setRecentEmotes(recentEmotes);
		}
	}

	/**
	 * Load the previously used emotes.
	 */
	private void loadRecentEmotes() {
		if (recentEmotes == null) {
			recentEmotes = new ArrayList<>();
			ArrayList<Emote> emotesFromSettings = settings.getRecentEmotes();
			if (emotesFromSettings != null) {
				recentEmotes.addAll(emotesFromSettings);
			} else {
				Log.e(LOG_TAG, "Failed to load recent emotes");
			}
		}
	}

	/**
	 * Checks the recently used emotes and removes any emotes that the user doesn't have access to.
	 */
	private void checkRecentEmotes() {
		if (recentEmotes != null) {
			List<Emote> emotesToRemove = new ArrayList<>();
			emotesToHide = new ArrayList<>();

			for (Emote emote : recentEmotes) {
				if (subscriberEmotes != null && emote.isSubscriberEmote() && !subscriberEmotes.contains(emote)) {
					emotesToRemove.add(emote);
				} else if (bttvChannelEmotes != null && emote.isBetterTTVChannelEmote() && !bttvChannelEmotes.contains(emote)) {
					emotesToHide.add(emote);
				}
			}

			if (emotesToHide.size() > 0 && recentEmotesFragment.mAdapter != null) {
				recentEmotesFragment.mAdapter.hideEmotes();
			}

			if (emotesToRemove.size() > 0 && recentEmotesFragment != null) {
				recentEmotes.removeAll(emotesToRemove);

				if (recentEmotesFragment.mAdapter != null) {
					recentEmotesFragment.mAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	/**
	 * Notify this fragment that back was pressed. Returns true if the super should be called. Else returns false
	 * @return
	 */
	public boolean notifyBackPressed() {
		if (isEmoteKeyboardOpen) {
			hideEmoteKeyboard();
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Notifies the ChatFragment that the twitch emotes have been fetched.
	 * The emotes are added the the twitch emote fragment.
	 * @param emotesLoaded The loaded twitch emotes
	 */
	protected void twitchEmotesLoaded(List<Emote> emotesLoaded) {
		twitchEmotes = new ArrayList<>(emotesLoaded);
		if (settings.isLoggedIn() && twitchEmotesFragment != null) {
			twitchEmotesFragment.addTwitchEmotes();
		}
	}

	protected void subscriberEmotesLoaded(List<Emote> subscriberEmotesLoaded, EmotesPagerAdapter adapter) {
		if (subscriberEmotesLoaded.size() > 0 && adapter != null && getContext() != null) {
			Log.d(LOG_TAG, "Adding subscriber emotes: " + subscriberEmotesLoaded.size());

			Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_usd_48dp);
			icon.setColorFilter(unselectedTabColorRes, PorterDuff.Mode.SRC_IN);

			TabLayout.Tab newTab = mEmoteTabs.newTab();
			newTab.setIcon(icon);
			mEmoteTabs.addTab(newTab, adapter.SUBSCRIBE_POSITION, false);
			adapter.showSubscriberEmote = true;
			adapter.notifyDataSetChanged();

			subscriberEmotes = new ArrayList<>(subscriberEmotesLoaded);
			if (settings.isLoggedIn() && subscriberEmotesFragment != null) {
				subscriberEmotesFragment.addSubscriberEmotes();
			}
		}
		checkRecentEmotes();
	}

	/**
	 * Notifies the ChatFragment that the bttvEmotes have been loaded from the API.
	 * Emotes are made and added to the EmoteKeyboard;
	 */
	protected void bttvEmoteInfoLoaded(List<Emote> bttvChannel, List<Emote> bttvGlobal) {
		Log.d(LOG_TAG, "Bttv Emotes loaded: " + bttvGlobal.size());
		bttvChannelEmotes = new ArrayList<>(bttvChannel);
		bttvEmotes = new ArrayList<>(bttvGlobal);
		bttvEmotes.addAll(bttvChannel);
		Collections.sort(bttvEmotes);

		checkRecentEmotes();
		if (settings.isLoggedIn() && bttvEmotesFragment != null) {
			bttvEmotesFragment.addBttvEmotes();
		}
	}

	private void setInitialKeyboardHeight() {
		int recordedHeight = settings.getKeyboardHeight();

		if (recordedHeight != 0 && recordedHeight > 200) {
			ViewGroup.LayoutParams lp = emoteKeyboardContainer.getLayoutParams();
			lp.height = recordedHeight;
			emoteKeyboardContainer.setLayoutParams(lp);
		}
	}

	private void notifyKeyboardHeightRecorded(int keyboardHeight) {
		Log.d(LOG_TAG, "Keyboard height: " + keyboardHeight);
		settings.setKeyboardHeight(keyboardHeight);

		ViewGroup.LayoutParams lp = emoteKeyboardContainer.getLayoutParams();
		lp.height = keyboardHeight;
		emoteKeyboardContainer.setLayoutParams(lp);

		hasSoftKeyboardBeenShown = true;
	}

	private void emoteButtonClicked(View clickedView) {
		clickedView.performHapticFeedback(VIBRATION_FEEDBACK);

		if (hasSoftKeyboardBeenShown) {
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		}

		if (!isEmoteKeyboardOpen) {
			if (!hasSoftKeyboardBeenShown) {
				Log.d(LOG_TAG, "SHOW SOFT KEYBOARD");
				hideKeyboardWhenShown = true;
				if (mSendText.requestFocus()) {
					openSoftKeyboard();
				}
			}

			showEmoteKeyboard();

		} else {
			if (isSoftKeyboardOpen) {
				closeSoftKeyboard();
			} else {
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
				openSoftKeyboard();
			}
		}
	}

	private void showEmoteKeyboard() {
		Log.d(LOG_TAG, "Show emote keyboard");

		closeSoftKeyboard();
		isEmoteKeyboardOpen = true;
		emoteKeyboardContainer.setVisibility(View.VISIBLE);
		mEmoteKeyboardButton.setColorFilter(Service.getAccentColor(getContext()));
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
	}

	private void hideEmoteKeyboard() {
		Log.d(LOG_TAG, "Hide emote keyboard");
		isEmoteKeyboardOpen = false;

		emoteKeyboardContainer.setVisibility(View.GONE);
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		mEmoteKeyboardButton.setColorFilter(defaultBackgroundColor);
	}

	public void openSoftKeyboard() {
		Service.showKeyboard(getActivity());
		isSoftKeyboardOpen = true;
		mEmoteKeyboardButton.setColorFilter(defaultBackgroundColor);
	}

	public void closeSoftKeyboard() {
		isSoftKeyboardOpen = false;
		Service.hideKeyboard(getActivity());

		if (isEmoteKeyboardOpen) {
			mEmoteKeyboardButton.setColorFilter(Service.getAccentColor(getContext()));
		}
	}

	private void setupEmoteViews() {
		setInitialKeyboardHeight();
		mEmoteKeyboardButton.setOnClickListener(this::emoteButtonClicked);

		mEmoteChatBackspace.setOnClickListener(view -> {
			mSendText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
			view.performHapticFeedback(VIBRATION_FEEDBACK);
		});

		setupEmoteTabs();
	}

	private void setupEmoteTabs() {
		final EmotesPagerAdapter pagerAdapter = new EmotesPagerAdapter(getActivity().getSupportFragmentManager());

		for (int i = 0; i < mEmoteTabs.getTabCount(); i++) {
			TabLayout.Tab tab = mEmoteTabs.getTabAt(i);
			Drawable icon = tab != null ? tab.getIcon() : null;

			if (icon != null) {
				if (i == 0) {
					icon.setColorFilter(selectedTabColorRes, PorterDuff.Mode.SRC_IN);
				} else {
					icon.setColorFilter(unselectedTabColorRes, PorterDuff.Mode.SRC_IN);
				}
			}
		}

		mEmoteViewPager.setAdapter(pagerAdapter);
		mEmoteViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				if (mEmoteTabs.getTabCount() - 1 >= position) {
					TabLayout.Tab tab = mEmoteTabs.getTabAt(position);
					if (tab != null)
						tab.select();
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		mEmoteTabs.addOnTabSelectedListener(
				new TabLayout.ViewPagerOnTabSelectedListener(mEmoteViewPager) {
					@Override
					public void onTabSelected(TabLayout.Tab tab) {
						super.onTabSelected(tab);

						if (tab.getIcon() != null)
							tab.getIcon().setColorFilter(selectedTabColorRes, PorterDuff.Mode.SRC_IN);
					}

					@Override
					public void onTabUnselected(TabLayout.Tab tab) {
						super.onTabUnselected(tab);
						if (tab.getIcon() != null)
							tab.getIcon().setColorFilter(unselectedTabColorRes, PorterDuff.Mode.SRC_IN);
					}

					@Override
					public void onTabReselected(TabLayout.Tab tab) {
						super.onTabReselected(tab);
					}
				}
		);

		GetTwitchEmotesTask getTwitchEmotesTask = new GetTwitchEmotesTask(new GetTwitchEmotesTask.Delegate() {
			@Override
			public void onEmotesLoaded(List<Emote> twitchEmotes, List<Emote> subscriberEmotes) {
				twitchEmotesLoaded(twitchEmotes);
				subscriberEmotesLoaded(subscriberEmotes, pagerAdapter);
			}
		}, getContext());
		getTwitchEmotesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void setupChatInput() {
		mChatInputLayout.bringToFront();
		chatInputDivider.bringToFront();
		mSendText.bringToFront();

		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
		mSendText.setOnEditTextImeBackListener(new EditTextBackEvent.EditTextImeBackListener() {
			@Override
			public void onImeBack(EditTextBackEvent ctrl, String text) {
				if (isEmoteKeyboardOpen && isSoftKeyboardOpen) {
					hideEmoteKeyboard();
				}

				setMentionSuggestions(new ArrayList<String>());
			}
		});
		mSendText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
			if (actionId == EditorInfo.IME_ACTION_SEND) {
				sendMessage();
				return true;
			}
			return false;
		});

		final Pattern mentionPattern = Pattern.compile("@(\\w+)$");
		mSendText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			@Override
			public void afterTextChanged(Editable editable) {
				Matcher mInputMatcher = mentionPattern.matcher(mSendText.getText());

				String userName = null;
				while (mInputMatcher.find()) {
					userName = mInputMatcher.group(1);
				}

				if (userName != null && !userName.isEmpty()) {
					setMentionSuggestions(mChatAdapter.getNamesThatMatches(userName));
				} else {
					setMentionSuggestions(new ArrayList<String>());
				}
			}
		});

		mSendText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				if (mSendText.getText().length() > 0) {
					mSendButton.setColorFilter(Service.getAccentColor(getContext()));
					mSendButton.setClickable(true);
				} else {
					mSendButton.setColorFilter(defaultBackgroundColor);
					mSendButton.setClickable(false);
				}
			}
		});
		mSendText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (isEmoteKeyboardOpen) {
					getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
					isSoftKeyboardOpen = true;
					mEmoteKeyboardButton.setColorFilter(defaultBackgroundColor);
				}
			}
		});
	}

	public void insertMentionSuggestion(String mention) {
		String currentInputText = mSendText.getText().toString();
		int mentionStart = currentInputText.lastIndexOf('@');
		String newInputText = currentInputText.substring(0, mentionStart + 1) + mention + " ";
		mSendText.setText(newInputText);
		mSendText.setSelection(newInputText.length());
	}

	private void setMentionSuggestions(List<String> suggestions) {
		if (getActivity() instanceof LiveStreamActivity && getActivity() != null) {
			Rect mInputRect = new Rect();
			mSendText.getGlobalVisibleRect(mInputRect);
			((LiveStreamActivity) getActivity()).setMentionSuggestions(suggestions, mInputRect);
		}
	}

	private void userNotLoggedIn() {
		mChatInputLayout.setVisibility(View.GONE);
		chatInputDivider.setVisibility(View.GONE);
	}

	private void setupKeyboardShowListener() {
		final Window mRootWindow = getActivity().getWindow();
		View mRootView2 = mRootWindow.getDecorView().findViewById(android.R.id.content);
		mRootView2.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					Integer lastBottom = -1;

					public void onGlobalLayout(){
						try {
							if (ChatFragment.this.isAdded()) {
								Rect r = new Rect();
								View view = mRootWindow.getDecorView();
								view.getWindowVisibleDisplayFrame(r);

								if (lastBottom > r.bottom && (lastBottom - r.bottom) > 200 && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
									Log.d(LOG_TAG, "Soft Keyboard shown");
									if (hideKeyboardWhenShown) {
										closeSoftKeyboard();
										hideKeyboardWhenShown = false;
									}

									notifyKeyboardHeightRecorded(lastBottom - r.bottom);
								}
								lastBottom = r.bottom;
							}
						} catch (IllegalStateException e) {
							e.printStackTrace();
						}
					}
				});
	}


	/**
	 * Construct and sends a message through the twitch bot and adds it to the chat recyclerview
	 */
	private void sendMessage() {
		final String message = mSendText.getText() + "";
		if (message.isEmpty()) {
			hideEmoteKeyboard();
			closeSoftKeyboard();
			return;
		}

		mSendButton.performHapticFeedback(VIBRATION_FEEDBACK);

		Log.d(LOG_TAG, "Sending Message: " + message);
		ConstructChatMessageTask getMessageTask = new ConstructChatMessageTask(
				new ConstructChatMessageTask.Callback() {
					@Override
					public void onMessageConstructed(ChatMessage chatMessage) {
						if (chatMessage != null) {
							try {
								addMessage(chatMessage);
								Log.d(LOG_TAG, "Message added");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				},
				bttvEmotes,
				twitchEmotes,
				subscriberEmotes,
				chatManager,
				message
		);
		getMessageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		hideEmoteKeyboard();
		closeSoftKeyboard();
		mSendText.setText("");

		SendMessageTask sendMessageTask = new SendMessageTask(chatManager, message);
		sendMessageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * Adds a Twitch-message to the recyclerview
	 * @param message
	 */
	public void addMessage(ChatMessage message) {
		mChatAdapter.add(message);
	}

	/**
	 * Called from EmoteGridFragments when an Emote in the emotekeyboard has been clicked
	 * @param clickedEmote
	 */
	@Override
	public void onEmoteClicked(Emote clickedEmote, View view) {
		view.performHapticFeedback(VIBRATION_FEEDBACK);

		if (clickedEmote != null) {
			int startPosition = mSendText.getSelectionStart();
			String emoteKeyword = clickedEmote.getKeyword();

			if (startPosition != 0 && mSendText.getText().charAt(startPosition - 1) != ' ') {
				emoteKeyword = " " + emoteKeyword;
			}

			mSendText.getText().insert(startPosition, emoteKeyword);

			if (recentEmotesFragment != null) {
				recentEmotesFragment.addEmote(clickedEmote);
			}
		}
	}

	@Override
	public void onMessageClicked(SpannableStringBuilder formattedMessage, final String userName, final String message) {
		View v = LayoutInflater.from(getContext()).inflate(R.layout.chat_message_options, null);
		final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
		bottomSheetDialog.setContentView(v);
		final BottomSheetBehavior behavior = BottomSheetBehavior.from((View) v.getParent());
		behavior.setPeekHeight(getContext().getResources().getDisplayMetrics().heightPixels/3);

		bottomSheetDialog.setOnDismissListener(dialogInterface -> behavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

		TextView mMessage = v.findViewById(R.id.text_chat_message);
		TextView mMention = v.findViewById(R.id.text_mention);
		TextView mDuplicateMessage = v.findViewById(R.id.text_duplicate_message);

		mMessage.setText(formattedMessage);
		mMention.setOnClickListener(view -> {
			insertSendText("@" + userName);
			bottomSheetDialog.dismiss();
		});
		mDuplicateMessage.setOnClickListener(view -> {
			insertSendText(message);
			bottomSheetDialog.dismiss();
		});

		bottomSheetDialog.show();
	}

	private void insertSendText(String message) {
		int insertPosition = mSendText.getSelectionStart();
		String textBefore = mSendText.getText().toString().substring(0, insertPosition);
		String textAfter = mSendText.getText().toString().substring(insertPosition);

		mSendText.setText(textBefore + message + textAfter);
		mSendText.setSelection(mSendText.length() - textAfter.length());
	}

	private class EmotesPagerAdapter extends FragmentPagerAdapter {
		public boolean showSubscriberEmote = false;

		public final int 	RECENT_POSITION = 0,
				TWITCH_POSITION = 1,
				SUBSCRIBE_POSITION = 2,
				BTTV_POSITION = 3,
				EMOJI_POSITION = 4;

		public EmotesPagerAdapter(FragmentManager fm) {
			super(fm);
			EmoteKeyboardDelegate delegate = ChatFragment.this;

			textEmotesFragment = EmoteGridFragment.newInstance(EmoteFragmentType.UNICODE, delegate);
			recentEmotesFragment = EmoteGridFragment.newInstance(EmoteFragmentType.ALL, delegate);
			twitchEmotesFragment = EmoteGridFragment.newInstance(EmoteFragmentType.TWITCH, delegate);
			subscriberEmotesFragment = EmoteGridFragment.newInstance(EmoteFragmentType.SUBSCRIBER, delegate);
			bttvEmotesFragment = EmoteGridFragment.newInstance(EmoteFragmentType.BTTV, delegate);
		}

		@Override
		public Fragment getItem(int position) {
			if (!showSubscriberEmote && position >= SUBSCRIBE_POSITION) {
				position++;
			}

			switch (position) {
				case RECENT_POSITION: return recentEmotesFragment;
				case TWITCH_POSITION: return twitchEmotesFragment;
				case SUBSCRIBE_POSITION: return subscriberEmotesFragment;
				case BTTV_POSITION: return bttvEmotesFragment;
				case EMOJI_POSITION: return textEmotesFragment;
				default: return EmoteGridFragment.newInstance();
			}
		}

		@Override
		public int getCount() {
			int count = EMOJI_POSITION + 1;
			if (!showSubscriberEmote) {
				count--;
			}
			return count;
		}
	}

	protected enum EmoteFragmentType {
		UNICODE,
		BTTV,
		TWITCH,
		SUBSCRIBER,
		ALL
	}

	public static class EmoteGridFragment extends Fragment {
		private String LOG_TAG = getClass().getSimpleName();
		private EmoteFragmentType fragmentType;
		private EmoteAdapter mAdapter, mPromotedAdapter;
		private EmoteKeyboardDelegate callback;

		@BindView(R.id.emote_recyclerview)
		protected AutoSpanRecyclerView mEmoteRecyclerView;

		@BindView(R.id.promoted_emotes_recyclerview)
		protected AutoSpanRecyclerView mPromotedEmotesRecyclerView;

		public static EmoteGridFragment newInstance(EmoteFragmentType fragmentType, EmoteKeyboardDelegate callback) {
			EmoteGridFragment emoteGridFragment = new EmoteGridFragment();
			emoteGridFragment.fragmentType = fragmentType;
			emoteGridFragment.callback = callback;
			return emoteGridFragment;
		}

		public static EmoteGridFragment newInstance() {
			return new EmoteGridFragment();
		}

		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View mRootView = inflater.inflate(R.layout.fragment_emote_grid, null);
			ButterKnife.bind(this, mRootView);

			mEmoteRecyclerView.setBehaviour(new EmoteAutoSpanBehaviour());
			mPromotedEmotesRecyclerView.setBehaviour(new EmoteAutoSpanBehaviour());

			mEmoteRecyclerView.setHasFixedSize(false);
			mPromotedEmotesRecyclerView.setHasFixedSize(false);

			mAdapter = new EmoteAdapter();
			mPromotedAdapter = new EmoteAdapter();

			mEmoteRecyclerView.setAdapter(mAdapter);
			mPromotedEmotesRecyclerView.setAdapter(mPromotedAdapter);

			if (fragmentType != null) {
				switch (fragmentType) {
					case UNICODE:
						addUnicodeEmotes();
						break;
					case ALL:
						addRecentEmotes();
						break;
					case TWITCH:
						addTwitchEmotes();
						break;
					case BTTV:
						addBttvEmotes();
						break;
					case SUBSCRIBER:
						addSubscriberEmotes();
						break;
				}
			}

			return mRootView;
		}

		private void addSubscriberEmotes() {
			if (mAdapter != null && subscriberEmotes != null && mAdapter.getItemCount() == 0) {
				Log.d(LOG_TAG, "Adding subscriber emotes");
				mAdapter.addEmotes(subscriberEmotes);
			}
		}

		private void addUnicodeEmotes() {
			if (supportedTextEmotes != null && mAdapter != null) {
				mAdapter.addEmotes(supportedTextEmotes);
			}
		}

		private void addBttvEmotes() {
			if (bttvEmotes != null && mAdapter != null && mAdapter.getItemCount() == 0) {
				mAdapter.addEmotes(bttvEmotes);
			}
		}

		private void addTwitchEmotes() {
			if (twitchEmotes != null && mAdapter != null) {
				mAdapter.addEmotes(twitchEmotes);
			}
		}

		private void addRecentEmotes() {
			if (recentEmotes != null && mAdapter != null) {
				mAdapter.addEmotes(recentEmotes);
			}
		}

		public void addEmote(Emote emote) {
			if (mAdapter != null)
				mAdapter.addEmote(emote);
		}

		public void addEmotes(List<Emote> emotes) {
			if (mAdapter != null)
				mAdapter.addEmotes(emotes);
		}

		public class EmoteAdapter extends RecyclerView.Adapter<EmoteAdapter.EmoteViewHolder> {
			private final int EMOTE_SIZE = 2;
			private ArrayList<Emote> emotes;
			private Settings settings;
			private HashMap<String, Target> picassoTargets;

			private View.OnClickListener emoteClickListener = new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					int itemPosition = mEmoteRecyclerView.getChildLayoutPosition(view);
					Emote emoteClicked = emotes.get(itemPosition);

					if (callback != null) {
						callback.onEmoteClicked(emoteClicked, view);
					}
				}
			};

			private View.OnLongClickListener emoteLongClickListener = new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					int itemPosition = mEmoteRecyclerView.getChildLayoutPosition(view);
					Emote emoteClicked = emotes.get(itemPosition);

					Toast.makeText(getContext(), emoteClicked.getKeyword(), Toast.LENGTH_SHORT).show();
					return false;
				}
			};

			public EmoteAdapter() {
				emotes = new ArrayList<>();
				settings = new Settings(getContext());
				picassoTargets = new HashMap<>();
			}

			@Override
			public EmoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
				View itemView = LayoutInflater
						.from(parent.getContext())
						.inflate(R.layout.view_emote_showcase, parent, false);

				itemView.setOnClickListener(emoteClickListener);
				itemView.setOnLongClickListener(emoteLongClickListener);
				return new EmoteViewHolder(itemView);
			}

			@Override
			public void onBindViewHolder(final EmoteViewHolder holder, int position) {
				final Emote emoteAtPosition = emotes.get(position);

				if (emoteAtPosition.isTextEmote()) {
					holder.mTextEmote.setText(emoteAtPosition.getKeyword());
				} else {
					final String emoteKey = ChatEmoteManager.getEmoteStorageKey(emoteAtPosition.getEmoteId(), EMOTE_SIZE);
					if (Service.doesStorageFileExist(emoteKey, getContext())) {
						try {
							Bitmap emote = Service.getImageFromStorage(emoteKey, getContext());
							holder.mImageEmote.setImageBitmap(emote);
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						String emoteUrl = ChatEmoteManager.getEmoteUrl(emoteAtPosition, EMOTE_SIZE);

						if (settings.getSaveEmotes()) {
							Target target = picassoTargets.containsKey(emoteAtPosition.getEmoteId())
									? picassoTargets.get(emoteAtPosition.getEmoteId())
									: new Target() {

								@Override
								public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
									holder.mImageEmote.setImageBitmap(bitmap);

									if (bitmap.getConfig() != null) {
										Bitmap emote = bitmap.copy(bitmap.getConfig(), true);
										Service.saveImageToStorage(ChatEmoteManager.constructMediumSizedEmote(emote, getContext()), emoteKey, getContext());
									}
								}

								@Override
								public void onBitmapFailed(Drawable errorDrawable) {}

								@Override
								public void onPrepareLoad(Drawable placeHolderDrawable) {}
							};

							if (!picassoTargets.containsKey(emoteAtPosition.getEmoteId()))
								picassoTargets.put(emoteAtPosition.getEmoteId(), target);

							Picasso.with(getContext()).load(emoteUrl).into(target);
						} else {
							Picasso.with(getContext()).load(emoteUrl).into(holder.mImageEmote);
						}
					}
				}
			}

			@Override
			public int getItemCount() {
				return emotes.size();
			}

			public void hideEmotes() {
				List<Emote> emotesToRemove = new ArrayList<>();
				for (Emote emote : emotes) {
					if (emotesToHide.contains(emote)) {
						emotesToRemove.add(emote);
					}
				}

				emotes.removeAll(emotesToRemove);
				notifyDataSetChanged();
			}

			public void addEmote(Emote emote) {
				if (fragmentType == EmoteFragmentType.ALL && emotesToHide != null && emotesToHide.contains(emote)) {
					return;
				}

				if (!emotes.contains(emote)) {
					int position = fragmentType == EmoteFragmentType.ALL ? 0 : emotes.size();
					emotes.add(position, emote);
					notifyItemInserted(position);

					if (fragmentType == EmoteFragmentType.ALL && recentEmotes != null && !recentEmotes.contains(emote)) {
						recentEmotes.add(position, emote);
					}
				} else if (!isVisible()) {
					int position = emotes.indexOf(emote);
					emotes.remove(position);
					notifyItemRemoved(position);
					addEmote(emote);
				}
			}

			public void addEmotes(List<Emote> emoteList) {
				emotes.addAll(emoteList);
				if (fragmentType == EmoteFragmentType.ALL && emotesToHide != null && emotes != null) {
					emotes.removeAll(emotesToHide);
				}

				notifyDataSetChanged();
			}

			protected class EmoteViewHolder extends RecyclerView.ViewHolder{
				protected ImageView mImageEmote;
				protected TextView mTextEmote;
				protected FrameLayout mEmoteContainer;

				public EmoteViewHolder(View itemView) {
					super(itemView);
					mImageEmote = itemView.findViewById(R.id.imageEmote);
					mTextEmote = itemView.findViewById(R.id.textEmote);
					mEmoteContainer = itemView.findViewById(R.id.emote_container);
				}
			}
		}
	}

}
