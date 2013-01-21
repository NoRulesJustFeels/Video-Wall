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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * A view which animates from one ImageView to another view using animation.
 */
public class TopDownImageAnimationView extends ImageAnimationView {

	private static final String LOG_TAG = "TopDownImageAnimationView";

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
	public TopDownImageAnimationView(Context context, final ImageAnimationListener listener, int width, int height) {
		super(context, listener, width, height);
	}

	protected Animation createInAnimation(int width, int height) {
		Animation inFromTop = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
				-1.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromTop.setInterpolator(new AccelerateInterpolator());
		return inFromTop;
	}

	protected Animation createOutAnimation(int width, int height) {
		Animation outtoBottom = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
				0.0f, Animation.RELATIVE_TO_PARENT, 1.0f);
		outtoBottom.setInterpolator(new AccelerateInterpolator());
		return outtoBottom;
	}

	/**
	 * Override so that animations run at the same time and not delayed
	 * 
	 * @see com.entertailion.android.videowall.animation.ImageAnimationView#setDuration(int)
	 */
	@Override
	public void setDuration(int duration) {
		if (outAnimation != null) {
			outAnimation.setDuration(duration);
		}
		if (inAnimation != null) {
			inAnimation.setDuration(duration);
			inAnimation.setStartOffset(0);
		}
	}

}
