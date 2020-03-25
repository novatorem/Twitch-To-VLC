package com.novatorem.ttvlc.tasks;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class CheckFollowingTask extends AsyncTask<String, Void, Boolean> {
	private final int USER_NOT_FOLLOWING_CODE = 404;
	private String LOG_TAG = getClass().getSimpleName();
	private TaskCallBack callBack;

	public CheckFollowingTask(TaskCallBack callBack) {
		this.callBack = callBack;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		URL url = null;
		try {
			String urlString = params[0];
			url = new URL(urlString);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setRequestMethod("GET");
			httpCon.connect();

			int response = httpCon.getResponseCode();

			return response != USER_NOT_FOLLOWING_CODE;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	protected void onPostExecute(Boolean aBoolean) {
		super.onPostExecute(aBoolean);
		callBack.onTaskDone(aBoolean);
	}

	public interface TaskCallBack {
		void onTaskDone(Boolean result);
	}
}
