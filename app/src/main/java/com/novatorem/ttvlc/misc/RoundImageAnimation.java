package com.novatorem.ttvlc.misc;

import android.graphics.Bitmap;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

import com.novatorem.ttvlc.service.Service;


public class RoundImageAnimation extends Animation {
	int fromRounded, toRounded;
	ImageView view;
	Bitmap imageBitmap;

	public RoundImageAnimation(int fromRounded, int toRounded, ImageView view, Bitmap imageBitmap) {
		this.fromRounded = fromRounded;
		this.toRounded = toRounded;
		this.view = view;
		this.imageBitmap = imageBitmap;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		int rounded = (int) (fromRounded + (toRounded - fromRounded) * interpolatedTime);
		view.setImageBitmap(Service.getRoundedCornerBitmap(imageBitmap, rounded));
		view.requestLayout();
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
	}

	@Override
	public boolean willChangeBounds() {
		return false;
	}
}
