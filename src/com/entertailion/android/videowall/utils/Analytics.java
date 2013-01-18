/*
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
package com.entertailion.android.videowall.utils;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.entertailion.android.videowall.R;
import com.google.analytics.tracking.android.EasyTracker;

/**
 * Utility class to manage Google analytics
 * 
 * @see https://developers.google.com/analytics/devguides/collection/android/v2/
 * @author leon_nicholls
 * 
 */
public class Analytics {
	private static final String LOG_CAT = "Analytics";

	public static final String ANALYTICS = "Analytics";
	public static final String VIDEO_WALL = "video.wall";
	public static final String ABOUT_PRIVACY_POLICY = "about.privacy_policy";
	public static final String ABOUT_WEB_SITE = "about.website";
	public static final String ABOUT_MORE_APPS = "about.more_apps";
	public static final String DIALOG_INTRODUCTION = "dialog.introduction";
	public static final String DIALOG_ABOUT = "dialog.about";
	public static final String DIALOG_PLAYLISTS = "dialog.playlists";
	public static final String EASTER_EGG = "easter.egg";
	public static final String RATING_YES = "rating.yes";
	public static final String RATING_NO = "rating.no";
	public static final String SETTINGS = "settings";
	public static final String EFFECT_FLIP = "effect.flip";
	public static final String EFFECT_FADE = "effect.fade";
	public static final String EFFECT_RIGHT_LEFT = "effect.right_left";
	public static final String EFFECT_TOP_DOWN = "effect.top_down";
	public static final String BORDER_NONE = "border.none";
	public static final String BORDER_THIN = "border.thin";
	public static final String BORDER_THICK = "border.thick";
	public static final String ROWS_TWO = "rows.two";
	public static final String ROWS_THREE = "rows.three";
	public static final String ROWS_FOUR = "rows.four";
	public static final String SELECT_PLAYLIST = "playlist.select";

	private static Context context;

	public static void createAnalytics(Context context) {
		try {
			Analytics.context = context;
			EasyTracker.getInstance().setContext(context);
		} catch (Exception e) {
			Log.e(LOG_CAT, "createAnalytics", e);
		}
	}

	public static void startAnalytics(final Activity activity) {
		try {
			if (activity != null && activity.getResources().getInteger(R.integer.development) == 0) {
				EasyTracker.getInstance().activityStart(activity);
			}
		} catch (Exception e) {
			Log.e(LOG_CAT, "startAnalytics", e);
		}
	}

	public static void stopAnalytics(Activity activity) {
		try {
			if (activity != null && activity.getResources().getInteger(R.integer.development) == 0) {
				EasyTracker.getInstance().activityStop(activity);
			}
		} catch (Exception e) {
			Log.e(LOG_CAT, "stopAnalytics", e);
		}
	}

	public static void logEvent(String event) {
		try {
			if (context != null && context.getResources().getInteger(R.integer.development) == 0) {
				EasyTracker.getTracker().trackEvent(ANALYTICS, event, event, 1L);
			}
		} catch (Exception e) {
			Log.e(LOG_CAT, "logEvent", e);
		}
	}

	public static void logEvent(String event, Map<String, String> parameters) {
		try {
			if (context != null && context.getResources().getInteger(R.integer.development) == 0) {
				EasyTracker.getTracker().trackEvent(ANALYTICS, event, event, 1L);
			}
		} catch (Exception e) {
			Log.e(LOG_CAT, "logEvent", e);
		}
	}
}
