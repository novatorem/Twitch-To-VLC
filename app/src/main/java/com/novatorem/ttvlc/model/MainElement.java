package com.novatorem.ttvlc.model;

import android.content.Context;


public interface MainElement {
	String getHighPreview();
	String getMediumPreview();
	String getLowPreview();

	int getPlaceHolder(Context context);
}
