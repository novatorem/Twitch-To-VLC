package com.novatorem.ttvlc.misc;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by novatorem on 24-01-2016.
 */
public class ResizeWidthAnimation extends Animation {
	final int startWidth;
	final int targetWidth;
	View view;

	public ResizeWidthAnimation(View view, int targetWidth) {
		this.view = view;
		this.targetWidth = targetWidth;
		startWidth = view.getWidth();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		view.getLayoutParams().width = (int) (startWidth + (targetWidth - startWidth) * interpolatedTime);
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
