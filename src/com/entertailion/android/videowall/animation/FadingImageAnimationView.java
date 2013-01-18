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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * A view which animates from one ImageView to another view using animation.
 */
public class FadingImageAnimationView extends ImageAnimationView {

	private static final String LOG_TAG = "FadingImageAnimationView";

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
	public FadingImageAnimationView(Context context, final ImageAnimationListener listener, int width, int height) {
		super(context, listener, width, height);
	}

	protected Animation createInAnimation(int width, int height) {
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new AccelerateInterpolator());
		return fadeIn;
	}

	protected Animation createOutAnimation(int width, int height) {
		return null;
	}

}
