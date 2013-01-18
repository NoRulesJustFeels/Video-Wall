/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 * Copyright (C) 2013 ENTERTAILION LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.entertailion.android.videowall.animation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * A view which animates from one ImageView to another view using animation.
 */
public abstract class ImageAnimationView extends FrameLayout {

	private static final String LOG_TAG = "ImageAnimationView";

	public static final int DEFAULT_DURATION = 500;

	protected final ImageView outImageView;
	protected final ImageView inImageView;
	protected final ImageAnimationListener listener;
	protected Animation inAnimation, outAnimation;

	/**
	 * Create a view which performs an animation from one view to another.
	 * 
	 * @param context
	 *            The context associated with this View.
	 * @param listener
	 *            A listener which is informed of when the view has completed
	 *            animating.
	 * @param width
	 *            The view's width.
	 * @param height
	 *            The view's height.
	 */
	public ImageAnimationView(Context context, final ImageAnimationListener listener, int width, int height) {
		super(context);

		this.listener = listener;

		this.outImageView = new ImageView(context);
		this.inImageView = new ImageView(context);

		addView(outImageView, width, height);
		addView(inImageView, width, height);

		outAnimation = createOutAnimation(width, height);
		if (outAnimation != null) {
			outAnimation.setFillAfter(true);
			outAnimation.setDuration(DEFAULT_DURATION);
		}

		inAnimation = createInAnimation(width, height);
		if (inAnimation != null) {
			inAnimation.setFillAfter(true);
			inAnimation.setDuration(DEFAULT_DURATION);
			inAnimation.setStartOffset(DEFAULT_DURATION);
			inAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					listener.onAnimationCompleted(ImageAnimationView.this);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationStart(Animation animation) {
				}

			});
		}
	}

	protected Animation createInAnimation(int width, int height) {
		float centerX = width / 2.0f;
		float centerY = height / 2.0f;
		Animation inAnimation = new FlipAnimation(-90, 0, centerX, centerY, FlipAnimation.SCALE_DEFAULT, FlipAnimation.ScaleUpDownEnum.SCALE_UP);
		inAnimation.setFillAfter(true);
		inAnimation.setInterpolator(new AccelerateInterpolator());
		return inAnimation;
	}

	protected Animation createOutAnimation(int width, int height) {
		float centerX = width / 2.0f;
		float centerY = height / 2.0f;
		Animation outAnimation = new FlipAnimation(0, 90, centerX, centerY, FlipAnimation.SCALE_DEFAULT, FlipAnimation.ScaleUpDownEnum.SCALE_DOWN);
		outAnimation.setDuration(DEFAULT_DURATION);
		outAnimation.setFillAfter(true);
		outAnimation.setInterpolator(new AccelerateInterpolator());
		return outAnimation;
	}

	public void setInDrawable(Drawable drawable) {
		inImageView.setImageDrawable(drawable);
	}

	public void setOutDrawable(Drawable drawable) {
		outImageView.setImageDrawable(drawable);
	}

	public void setDuration(int duration) {
		if (outAnimation != null) {
			outAnimation.setDuration(duration);
		}
		if (inAnimation != null) {
			inAnimation.setDuration(duration);
			inAnimation.setStartOffset(duration);
		}
	}

	public void start() {
		if (outAnimation != null) {
			outImageView.startAnimation(outAnimation);
		}
		if (inAnimation != null) {
			inImageView.startAnimation(inAnimation);
		}
	}

}
