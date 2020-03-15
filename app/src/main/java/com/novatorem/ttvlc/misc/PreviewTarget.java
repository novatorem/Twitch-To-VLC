package com.novatorem.ttvlc.misc;

import android.graphics.Bitmap;

import com.squareup.picasso.Target;


public abstract class PreviewTarget implements Target {
	private Bitmap preview;

	public Bitmap getPreview() {
		return preview;
	}

	public void setPreview(Bitmap preview) {
		this.preview = preview;
	}
}
