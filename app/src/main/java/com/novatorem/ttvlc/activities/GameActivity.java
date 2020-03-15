package com.novatorem.ttvlc.activities;

import android.content.Intent;

import com.novatorem.ttvlc.R;
import com.novatorem.ttvlc.activities.main.LazyMainActivity;
import com.novatorem.ttvlc.adapters.MainActivityAdapter;
import com.novatorem.ttvlc.adapters.StreamsAdapter;
import com.novatorem.ttvlc.model.Game;
import com.novatorem.ttvlc.model.StreamInfo;
import com.novatorem.ttvlc.service.JSONService;
import com.novatorem.ttvlc.service.Service;
import com.novatorem.ttvlc.views.recyclerviews.AutoSpanRecyclerView;
import com.novatorem.ttvlc.views.recyclerviews.auto_span_behaviours.AutoSpanBehaviour;
import com.novatorem.ttvlc.views.recyclerviews.auto_span_behaviours.StreamAutoSpanBehaviour;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GameActivity extends LazyMainActivity<StreamInfo> {
	private Game game;

	@Override
	protected MainActivityAdapter constructAdapter(AutoSpanRecyclerView recyclerView) {
		return new StreamsAdapter(recyclerView, this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void customizeActivity() {
		Intent intent = getIntent();
		game = intent.getParcelableExtra(getResources().getString(R.string.game_intent_key));
		mTitleView.setText(game.getGameTitle());
	}

	@Override
	protected int getActivityIconRes() {
		return R.drawable.ic_game;
	}

	@Override
	protected int getActivityTitleRes() {
		return R.string.my_streams_activity_title;
	}

	@Override
	protected AutoSpanBehaviour constructSpanBehaviour() {
		return new StreamAutoSpanBehaviour();
	}

	@Override
	public void addToAdapter(List<StreamInfo> streamsToAdd) {
		mOnScrollListener.checkForNewElements(mRecyclerView);
		mAdapter.addList(streamsToAdd);
	}

	@Override
	public List<StreamInfo> getVisualElements() throws JSONException, MalformedURLException, UnsupportedEncodingException {
		String gameTitleEncoded = URLEncoder.encode(game.getGameTitle(), "UTF-8");
		String url = "https://api.twitch.tv/kraken/streams?game=" + gameTitleEncoded + "&limit=" + getLimit() + "&offset=" + getCurrentOffset();
		final String GAMES_ARRAY_KEY = "streams";

		List<StreamInfo> mResultList = new ArrayList<>();
		String jsonString = Service.urlToJSONString(url);
		JSONObject fullDataObject = new JSONObject(jsonString);
		JSONArray topStreamsArray = fullDataObject.getJSONArray(GAMES_ARRAY_KEY);

		for (int i = 0; i < topStreamsArray.length(); i++) {
			JSONObject streamObject = topStreamsArray.getJSONObject(i);
			mResultList.add(JSONService.getStreamInfo(getBaseContext(), streamObject, null, false));
		}

		return mResultList;
	}
}
