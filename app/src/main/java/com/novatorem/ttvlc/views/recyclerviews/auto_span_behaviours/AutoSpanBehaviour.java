package com.novatorem.ttvlc.views.recyclerviews.auto_span_behaviours;

import android.content.Context;

import com.novatorem.ttvlc.service.Settings;



public interface AutoSpanBehaviour {
	String getElementSizeName(Settings settings);
	int getElementWidth(Context context);
}
