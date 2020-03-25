package com.novatorem.ttvlc.misc;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by novatorem on 24-01-2016.
 */
public class ResizeHeightAnimation extends Animation {
	final int startHeight;
	final int targetHeight;
	View view;

	public ResizeHeightAnimation(View view, int targetHeight) {
		this.view = view;
		this.targetHeight = targetHeight;
		this.startHeight = view.getHeight();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		view.getLayoutParams().height = (int) (startHeight + (targetHeight - startHeight) * interpolatedTime);
		view.requestLayout();
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
	}

	@Override
	public boolean willChangeBounds() {
		return true;
	}
}
