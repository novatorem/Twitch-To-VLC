package com.novatorem.ttvlc.tasks;

import android.os.AsyncTask;
import androidx.recyclerview.widget.RecyclerView;

import com.novatorem.ttvlc.misc.UniversalOnScrollListener;


public class ScrollToStartPositionTask extends AsyncTask<Void, Void, Void> {
	private PositionCallBack callBack;
	private RecyclerView recyclerView;
	private UniversalOnScrollListener mScrollListener;

	public ScrollToStartPositionTask(PositionCallBack callBack, RecyclerView recyclerView, UniversalOnScrollListener mScrollListener) {
		this.callBack = callBack;
		this.recyclerView = recyclerView;
		this.mScrollListener = mScrollListener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		recyclerView.smoothScrollToPosition(0);
	}

	@Override
	protected Void doInBackground(Void... params) {
		while(mScrollListener.getAmountScrolled() != 0) {

		}

		return null;
	}

	@Override
	protected void onPostExecute(Void aVoid) {
		super.onPostExecute(aVoid);
		callBack.positionReached();
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		callBack.cancelled();
	}

	public interface PositionCallBack {
		void positionReached();
		void cancelled();
	}
}
