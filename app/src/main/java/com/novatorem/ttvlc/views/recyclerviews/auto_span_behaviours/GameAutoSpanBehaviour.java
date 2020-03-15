package com.novatorem.ttvlc.views.recyclerviews.auto_span_behaviours;

import android.content.Context;

import com.novatorem.ttvlc.R;
import com.novatorem.ttvlc.service.Settings;



public class GameAutoSpanBehaviour implements AutoSpanBehaviour {
	@Override
	public String getElementSizeName(Settings settings) {
		return settings.getAppearanceGameSize();
	}

	@Override
	public int getElementWidth(Context context) {
		return (int) context.getResources().getDimension(R.dimen.game_card_width) + (int) context.getResources().getDimension(R.dimen.game_card_margin);
	}
}
