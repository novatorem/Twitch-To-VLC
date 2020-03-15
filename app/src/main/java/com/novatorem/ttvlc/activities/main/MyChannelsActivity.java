package com.novatorem.ttvlc.activities.main;

import android.os.AsyncTask;

import com.novatorem.ttvlc.R;
import com.novatorem.ttvlc.activities.FollowingFetcher;
import com.novatorem.ttvlc.adapters.ChannelsAdapter;
import com.novatorem.ttvlc.adapters.MainActivityAdapter;
import com.novatorem.ttvlc.model.ChannelInfo;
import com.novatorem.ttvlc.service.TempStorage;
import com.novatorem.ttvlc.tasks.GetFollowsFromDB;
import com.novatorem.ttvlc.views.recyclerviews.auto_span_behaviours.AutoSpanBehaviour;
import com.novatorem.ttvlc.views.recyclerviews.AutoSpanRecyclerView;
import com.novatorem.ttvlc.views.recyclerviews.auto_span_behaviours.ChannelAutoSpanBehaviour;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that shows the user's Twitch Follows.
 * If there are no follows in TempStorage when the activity is created, then the activity initiates an AsyncTask that connects to Twitch, that loads and adds the follows to this activity
 */
public class MyChannelsActivity extends MainActivity implements FollowingFetcher {

	@Override
	public void refreshElements() {
		mRecyclerView.smoothScrollToPosition(0);
	}

	@Override
	protected MainActivityAdapter constructAdapter(AutoSpanRecyclerView recyclerView) {
		return new ChannelsAdapter(mRecyclerView, getBaseContext(), this);
	}

	@Override
	protected int getActivityIconRes() {
		return R.drawable.ic_channels;
	}

	@Override
	protected int getActivityTitleRes() {
		return R.string.my_channels_activity_title;
	}

	@Override
	protected AutoSpanBehaviour constructSpanBehaviour() {
		return new ChannelAutoSpanBehaviour();
	}

	@Override
	protected void customizeActivity() {
		super.customizeActivity();

		if (!TempStorage.hasLoadedStreamers()) {
			// Get all the subscriptions from internal database. This will also create and set an adaptor on the recyclerview
			GetFollowsFromDB subscriptionsTask = new GetFollowsFromDB(this);
			subscriptionsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getBaseContext());
		} else {
			// Get all the subscriptions from the static list, and set them as the adaptor for the recyclerview
			mAdapter.clearNoAnimation();
			mAdapter.addList(new ArrayList<>(TempStorage.getLoadedStreamers()));
			if (mAdapter.getItemCount() == 0) {
				showErrorView();
			}
		}
	}

	public ChannelsAdapter getAdapter() {
		return ((ChannelsAdapter) mAdapter);
	}

	// FollowingFetcher
	@Override
	public void addStreamer(ChannelInfo streamer) {
		mAdapter.add(streamer);
	}

	@Override
	public void addStreamers(List<ChannelInfo> streamers) {
		mAdapter.addList(streamers);
	}

	@Override
	public boolean isEmpty() {
		return mAdapter.getItemCount() == 0;
	}

	@Override
	public void notifyFinishedAdding() {}
}
