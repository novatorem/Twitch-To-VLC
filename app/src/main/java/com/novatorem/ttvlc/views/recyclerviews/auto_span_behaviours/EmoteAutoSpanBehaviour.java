package com.novatorem.ttvlc.views.recyclerviews.auto_span_behaviours;

import android.content.Context;

import com.novatorem.ttvlc.R;
import com.novatorem.ttvlc.service.Settings;



public class EmoteAutoSpanBehaviour implements AutoSpanBehaviour {
	@Override
	public String getElementSizeName(Settings settings) {
		return "";
	}

	@Override
	public int getElementWidth(Context context) {
		return (int) context.getResources().getDimension(R.dimen.chat_grid_emote_size);
	}
}
