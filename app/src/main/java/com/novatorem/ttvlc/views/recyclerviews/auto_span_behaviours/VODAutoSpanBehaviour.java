package com.novatorem.ttvlc.views.recyclerviews.auto_span_behaviours;

import android.content.Context;

import com.novatorem.ttvlc.R;
import com.novatorem.ttvlc.service.Settings;



public class VODAutoSpanBehaviour implements AutoSpanBehaviour {
	@Override
	public String getElementSizeName(Settings settings) {
		return settings.getContext().getString(R.string.card_size_large);
	}

	@Override
	public int getElementWidth(Context context) {
		return (int) context.getResources().getDimension(R.dimen.stream_card_min_width) + (int) context.getResources().getDimension(R.dimen.stream_card_left_margin);
	}
}
