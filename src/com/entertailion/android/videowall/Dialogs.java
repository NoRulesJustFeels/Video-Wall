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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.entertailion.android.videowall.playlist.PlaylistAdapter;
import com.entertailion.android.videowall.playlist.PlaylistInfo;
import com.entertailion.android.videowall.utils.Analytics;
import com.entertailion.android.videowall.utils.Utils;

/**
 * Utility class to display various dialogs for the main activity
 * 
 * @author leon_nicholls
 * 
 */
public class Dialogs {

	private static final String LOG_TAG = "Dialogs";

	// Ratings dialog configuration
	public static final String DATE_FIRST_LAUNCHED = "date_first_launched";
	public static final String DONT_SHOW_RATING_AGAIN = "dont_show_rating_again";
	private final static int DAYS_UNTIL_PROMPT = 5;

	/**
	 * Display introduction to the user for first time launch
	 * 
	 * @param context
	 */
	public static void displayIntroduction(final VideoWallActivity context) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.introduction);

		Typeface lightTypeface = ((VideoWallApplication) context.getApplicationContext()).getLightTypeface(context);

		TextView titleTextView = (TextView) dialog.findViewById(R.id.intro_title);
		titleTextView.setTypeface(lightTypeface);
		TextView textView1 = (TextView) dialog.findViewById(R.id.intro_text1);
		textView1.setTypeface(lightTypeface);
		TextView textView2 = (TextView) dialog.findViewById(R.id.intro_text2);
		textView2.setTypeface(lightTypeface);

		((Button) dialog.findViewById(R.id.intro_button)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				context.showCover(false);
				dialog.dismiss();
			}

		});
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				context.showCover(false);
			}

		});
		context.showCover(true);
		dialog.show();
		Analytics.logEvent(Analytics.DIALOG_INTRODUCTION);
	}

	/**
	 * Display about dialog to user when invoked from menu option.
	 * 
	 * @param context
	 */
	public static void displayAbout(final VideoWallActivity context) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.about);

		Typeface lightTypeface = ((VideoWallApplication) context.getApplicationContext()).getLightTypeface(context);

		TextView aboutTextView = (TextView) dialog.findViewById(R.id.about_text1);
		aboutTextView.setTypeface(lightTypeface);
		aboutTextView.setText(context.getString(R.string.about_version_title, Utils.getVersion(context)));
		aboutTextView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				context.showCover(false);
				dialog.dismiss();
				Intent intent = new Intent(context, EasterEggActivity.class);
				context.startActivity(intent);
				Analytics.logEvent(Analytics.EASTER_EGG);
				return true;
			}

		});
		TextView copyrightTextView = (TextView) dialog.findViewById(R.id.copyright_text);
		copyrightTextView.setTypeface(lightTypeface);
		TextView feedbackTextView = (TextView) dialog.findViewById(R.id.feedback_text);
		feedbackTextView.setTypeface(lightTypeface);
		TextView termsTextView = (TextView) dialog.findViewById(R.id.terms_text);
		termsTextView.setTypeface(lightTypeface);
		termsTextView.setText(Html.fromHtml(context.getString(R.string.terms_html)));
		termsTextView.setMovementMethod(LinkMovementMethod.getInstance());

		((Button) dialog.findViewById(R.id.button_web)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(context.getString(R.string.about_button_web_url)));
				context.startActivity(intent);
				Analytics.logEvent(Analytics.ABOUT_WEB_SITE);
				context.showCover(false);
				dialog.dismiss();
			}

		});

		((Button) dialog.findViewById(R.id.button_privacy_policy)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(context.getString(R.string.about_button_privacy_policy_url)));
				context.startActivity(intent);
				Analytics.logEvent(Analytics.ABOUT_PRIVACY_POLICY);
				context.showCover(false);
				dialog.dismiss();
			}

		});
		((Button) dialog.findViewById(R.id.button_more_apps)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(context.getString(R.string.about_button_more_apps_url)));
				context.startActivity(intent);
				Analytics.logEvent(Analytics.ABOUT_MORE_APPS);
				context.showCover(false);
				dialog.dismiss();
			}

		});
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				context.showCover(false);
			}

		});
		context.showCover(true);
		dialog.show();
		Analytics.logEvent(Analytics.DIALOG_ABOUT);
	}

	/**
	 * Prompt the user to rate the app.
	 * 
	 * @param context
	 */
	public static void displayRating(final VideoWallActivity context) {
		SharedPreferences prefs = context.getSharedPreferences(VideoWallActivity.PREFERENCES_NAME, Activity.MODE_PRIVATE);

		if (prefs.getBoolean(DONT_SHOW_RATING_AGAIN, false)) {
			return;
		}

		final SharedPreferences.Editor editor = prefs.edit();

		// Get date of first launch
		Long date_firstLaunch = prefs.getLong(DATE_FIRST_LAUNCHED, 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong(DATE_FIRST_LAUNCHED, date_firstLaunch);
		}

		// Wait at least n days before opening
		if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
			final Dialog dialog = new Dialog(context);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.confirmation);

			TextView confirmationTextView = (TextView) dialog.findViewById(R.id.confirmationText);
			confirmationTextView.setText(context.getString(R.string.rating_message));
			Button buttonYes = (Button) dialog.findViewById(R.id.button1);
			buttonYes.setText(context.getString(R.string.dialog_yes));
			buttonYes.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.entertailion.android.videowall"));
					context.startActivity(intent);
					if (editor != null) {
						editor.putBoolean(DONT_SHOW_RATING_AGAIN, true);
						editor.commit();
					}
					Analytics.logEvent(Analytics.RATING_YES);
					context.showCover(false);
					dialog.dismiss();
				}

			});
			Button buttonNo = (Button) dialog.findViewById(R.id.button2);
			buttonNo.setText(context.getString(R.string.dialog_no));
			buttonNo.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (editor != null) {
						editor.putBoolean(DONT_SHOW_RATING_AGAIN, true);
						editor.commit();
					}
					Analytics.logEvent(Analytics.RATING_NO);
					context.showCover(false);
					dialog.dismiss();
				}

			});
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					context.showCover(false);
				}

			});
			context.showCover(true);
			dialog.show();
		}

		editor.commit();
	}

	/**
	 * Display the list of YouTube playlists.
	 * 
	 * @param context
	 */
	public static void displayPlaylists(final VideoWallActivity context) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.playlist_list);

		ListView listView = (ListView) dialog.findViewById(R.id.list);
		final ArrayList<PlaylistInfo> playlists = Utils.getPlaylists(context);
		Collections.sort(playlists, new Comparator<PlaylistInfo>() {

			@Override
			public int compare(PlaylistInfo lhs, PlaylistInfo rhs) {
				return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
			}

		});
		listView.setAdapter(new PlaylistAdapter(context, playlists));
		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PlaylistInfo playlist = (PlaylistInfo) parent.getAdapter().getItem(position);
				context.setPlaylist(playlist.getId());
				Analytics.logEvent(Analytics.SELECT_PLAYLIST);
				context.showCover(false);
				dialog.dismiss();
			}

		});
		listView.setDrawingCacheEnabled(true);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				context.showCover(false);
			}

		});
		context.showCover(true);
		dialog.show();
		Analytics.logEvent(Analytics.DIALOG_PLAYLISTS);
	}
}
