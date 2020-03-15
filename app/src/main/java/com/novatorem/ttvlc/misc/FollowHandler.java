package com.novatorem.ttvlc.misc;

import android.content.Context;
import android.os.AsyncTask;

import com.novatorem.ttvlc.model.ChannelInfo;
import com.novatorem.ttvlc.service.Service;
import com.novatorem.ttvlc.service.Settings;
import com.novatorem.ttvlc.tasks.FollowTask;
import com.novatorem.ttvlc.tasks.UnfollowTask;

public class FollowHandler {
	private ChannelInfo mChannelInfo;
	private Context mContext;
	private Delegate mDelegate;
	private boolean isStreamerFollowed;

	public FollowHandler(ChannelInfo mChannelInfo, Context mContext, Delegate mDelegate) {
		this.mChannelInfo = mChannelInfo;
		this.mContext = mContext;
		this.mDelegate = mDelegate;
		this.isStreamerFollowed = false;

		init();
	}

	private void init() {
		if (!new Settings(mContext).isLoggedIn()) {
			mDelegate.userIsNotLoggedIn();
		} else {
			boolean isUserFollowed = Service.isUserFollowingStreamer(mChannelInfo.getStreamerName(), mContext);
			if (isUserFollowed) {
				mDelegate.streamerIsFollowed();
				isStreamerFollowed = true;
			} else {
				mDelegate.streamerIsNotFollowed();
			}
		}
	}

	public boolean isStreamerFollowed() {
		return isStreamerFollowed;
	}

	public void followStreamer() {
		String urlString = getBaseFollowString();

		FollowTask followTask = new FollowTask(new FollowTask.FollowResult() {
			@Override
			public void onTaskDone(Boolean result) {
				isStreamerFollowed = result;
				if(result) {
					Service.insertStreamerInfoToDB(mContext, mChannelInfo);
					mDelegate.followSuccess();
				} else {
					mDelegate.followFailure();
				}
			}
		});
		followTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, urlString);
	}

	public void unfollowStreamer() {
		String url = getBaseFollowString();

		UnfollowTask unfollowTask = new UnfollowTask(new UnfollowTask.UnFollowResult() {
			@Override
			public void onTaskDone(Boolean result) {
				isStreamerFollowed = !result;

				if(result) {
					Service.deleteStreamerInfoFromDB(mContext, mChannelInfo.getUserId());
					mDelegate.unfollowSuccess();
				} else {
					mDelegate.unfollowFailure();
				}
			}
		});
		unfollowTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, url);
	}

	/**
	 * Returns the URL string we need to connect to.
	 * Both if the user wants to follow AND unfollow the current streamer
	 */
	private String getBaseFollowString() {
		Settings settings = new Settings(mContext);
		return "https://api.twitch.tv/kraken/users/" + settings.getGeneralTwitchUserID() + "/follows/channels/" + mChannelInfo.getUserId() + "?oauth_token=" + settings.getGeneralTwitchAccessToken();
	}

	public interface Delegate {
		void streamerIsFollowed();
		void streamerIsNotFollowed();
		void userIsNotLoggedIn();

		void followSuccess();
		void followFailure();
		void unfollowSuccess();
		void unfollowFailure();
	}
}
