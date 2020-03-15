package com.novatorem.ttvlc.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.novatorem.ttvlc.model.StreamInfo;
import com.novatorem.ttvlc.service.Settings;
import com.novatorem.ttvlc.service.StreamsService;

import java.util.List;



/***
 * This task will fetch all currently live streams for the logged in user.
 * This task should not be executed for time critical task.
 */
public class GetFollowedLiveStreamsTask extends AsyncTask<Void, Void, List<StreamInfo>> {
	private Context context;
	private FetchLiveStreamsCallback callback;

	public GetFollowedLiveStreamsTask(Context context, FetchLiveStreamsCallback callback) {
		this.context = context;
		this.callback = callback;
	}

	@Override
	protected List<StreamInfo> doInBackground(Void... voids) {
		Settings settings = new Settings(context);
		return StreamsService.fetchAllLiveStreams(context, settings.getGeneralTwitchAccessToken());
	}

	@Override
	protected void onPostExecute(List<StreamInfo> streams) {
		super.onPostExecute(streams);
		callback.onLiveStreamFetched(streams);
	}

	public interface FetchLiveStreamsCallback {
		void onLiveStreamFetched(List<StreamInfo> streams);
	}
}
