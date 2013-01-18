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
package com.entertailion.android.videowall;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import com.entertailion.android.videowall.utils.Analytics;

/**
 * Handle settings for app. Invoked by the user from the menu.
 * 
 * @author leon_nicholls
 * 
 */
public class PreferencesActivity extends PreferenceActivity {
	private static final String LOG_TAG = "PreferencesActivity";
	public static final String GENERAL_EFFECT = "general.effect";
	public static final String GENERAL_BORDER = "general.border";
	public static final String GENERAL_ROWS = "general.rows";

	public static final String EFFECT_FLIP = "flip";
	public static final String EFFECT_FADE = "fade";
	public static final String EFFECT_RIGHT_LEFT = "right/left";
	public static final String EFFECT_TOP_DOWN = "top/down";

	public static final String BORDER_NONE = "none";
	public static final String BORDER_THIN = "thin";
	public static final String BORDER_THICK = "thick";

	public static final String ROWS_TWO = "2";
	public static final String ROWS_THREE = "3";
	public static final String ROWS_FOUR = "4";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		// General
		Preference preference = (Preference) findPreference(GENERAL_EFFECT);
		String effect = preference.getSharedPreferences().getString(GENERAL_EFFECT, EFFECT_FLIP);
		if (effect.equals(EFFECT_FLIP)) {
			preference.setSummary(getString(R.string.preferences_general_effect_flip));
		} else if (effect.equals(EFFECT_FADE)) {
			preference.setSummary(getString(R.string.preferences_general_effect_fade));
		} else if (effect.equals(EFFECT_RIGHT_LEFT)) {
			preference.setSummary(getString(R.string.preferences_general_effect_right_left));
		} else if (effect.equals(EFFECT_TOP_DOWN)) {
			preference.setSummary(getString(R.string.preferences_general_effect_top_down));
		}
		preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue.equals(EFFECT_FLIP)) {
					preference.setSummary(getString(R.string.preferences_general_effect_flip));
					Analytics.logEvent(Analytics.EFFECT_FLIP);
				} else if (newValue.equals(EFFECT_FADE)) {
					preference.setSummary(getString(R.string.preferences_general_effect_fade));
					Analytics.logEvent(Analytics.EFFECT_FADE);
				} else if (newValue.equals(EFFECT_RIGHT_LEFT)) {
					preference.setSummary(getString(R.string.preferences_general_effect_right_left));
					Analytics.logEvent(Analytics.EFFECT_RIGHT_LEFT);
				} else if (newValue.equals(EFFECT_TOP_DOWN)) {
					preference.setSummary(getString(R.string.preferences_general_effect_top_down));
					Analytics.logEvent(Analytics.EFFECT_TOP_DOWN);
				}
				return true;
			}

		});

		preference = (Preference) findPreference(GENERAL_BORDER);
		String border = preference.getSharedPreferences().getString(GENERAL_BORDER, BORDER_THIN);
		if (border.equals(BORDER_NONE)) {
			preference.setSummary(getString(R.string.preferences_general_border_none));
		} else if (border.equals(BORDER_THIN)) {
			preference.setSummary(getString(R.string.preferences_general_border_thin));
		} else if (border.equals(BORDER_THICK)) {
			preference.setSummary(getString(R.string.preferences_general_border_thick));
		}
		preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue.equals(BORDER_NONE)) {
					preference.setSummary(getString(R.string.preferences_general_border_none));
					Analytics.logEvent(Analytics.BORDER_NONE);
				} else if (newValue.equals(BORDER_THIN)) {
					preference.setSummary(getString(R.string.preferences_general_border_thin));
					Analytics.logEvent(Analytics.BORDER_THIN);
				} else if (newValue.equals(BORDER_THICK)) {
					preference.setSummary(getString(R.string.preferences_general_border_thick));
					Analytics.logEvent(Analytics.BORDER_THICK);
				}
				return true;
			}

		});

		preference = (Preference) findPreference(GENERAL_ROWS);
		String rows = preference.getSharedPreferences().getString(GENERAL_ROWS, ROWS_FOUR);
		if (rows.equals(ROWS_TWO)) {
			preference.setSummary(getString(R.string.preferences_general_rows_two));
		} else if (rows.equals(ROWS_THREE)) {
			preference.setSummary(getString(R.string.preferences_general_rows_three));
		} else if (rows.equals(ROWS_FOUR)) {
			preference.setSummary(getString(R.string.preferences_general_rows_four));
		}
		preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue.equals(ROWS_TWO)) {
					preference.setSummary(getString(R.string.preferences_general_rows_two));
					Analytics.logEvent(Analytics.ROWS_TWO);
				} else if (newValue.equals(ROWS_THREE)) {
					preference.setSummary(getString(R.string.preferences_general_rows_three));
					Analytics.logEvent(Analytics.ROWS_THREE);
				} else if (newValue.equals(ROWS_FOUR)) {
					preference.setSummary(getString(R.string.preferences_general_rows_four));
					Analytics.logEvent(Analytics.ROWS_FOUR);
				}
				return true;
			}

		});

		Analytics.createAnalytics(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Start Google Analytics for this activity
		Analytics.startAnalytics(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Stop Google Analytics for this activity
		Analytics.stopAnalytics(this);
	}
}