/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 * Copyright 2013 ENTERTAILION LLC. All Rights Reserved.
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

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.entertailion.android.videowall.animation.FadingImageAnimationView;
import com.entertailion.android.videowall.animation.FlipImageAnimationView;
import com.entertailion.android.videowall.animation.ImageAnimationListener;
import com.entertailion.android.videowall.animation.ImageAnimationView;
import com.entertailion.android.videowall.animation.RightLeftImageAnimationView;
import com.entertailion.android.videowall.animation.TopDownImageAnimationView;
import com.entertailion.android.videowall.layout.ImageWallView;
import com.entertailion.android.videowall.utils.Analytics;
import com.entertailion.android.videowall.utils.Utils;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeIntents;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.PlayerStyle;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

/*
 * The app shows a video wall of thumbnail images for videos in a YouTube playlist. 
 * One of the thumbnails will flip over and begin playing a video from the playlist, which will play until it finishes. 
 * After that, the video will be replaced with a thumnbail, and a different thumbnail will flip over to play another video.
 * 
 * @see https://developers.google.com/youtube/android/player/
 */

public class VideoWallActivity extends Activity implements ImageAnimationListener, YouTubePlayer.OnInitializedListener,
		YouTubeThumbnailView.OnInitializedListener {

	private static final String LOG_TAG = "VideoWallActivity";

	public static final String DEVELOPER_KEY = "";

	private static final int RECOVERY_DIALOG_REQUEST = 1;

	/** The player view cannot be smaller than 110 pixels high. */
	/** Google TV 960x540 dp **/
	private static final float PLAYER_VIEW_MINIMUM_HEIGHT_DP = 110;
	private static final int MAX_NUMBER_OF_ROWS_WANTED = 4;

	// Example playlist from which videos are displayed on the video wall
	private static final String PLAYLIST_ID = "PLLCXrEIzG_9H8QMNLK8znC89d1lL-Il6D";

	private static final int INTER_IMAGE_PADDING_DP = 5;

	// YouTube thumbnails have a 16 / 9 aspect ratio
	private static final double THUMBNAIL_ASPECT_RATIO = 16 / 9d;

	private static final int INITIAL_FLIP_DURATION_MILLIS = 100;
	private static final int FLIP_DURATION_MILLIS = 500;
	private static final int FLIP_PERIOD_MILLIS = 2000;

	// Identifiers for menu items
	private static final int MENU_SETTINGS = Menu.FIRST + 1;
	private static final int MENU_ABOUT = MENU_SETTINGS + 1;
	private static final int MENU_SELECT_PLAYLIST = MENU_ABOUT + 1;

	private final static float COVER_ALPHA_VALUE = 0.8f;

	public static final String PREFERENCES_NAME = "preferences";
	public static final String FIRST_INSTALL = "first_install";
	public static final String PLAYLIST = "playlist";

	private ImageWallView imageWallView;
	private Handler flipDelayHandler;

	private ImageAnimationView imageAnimationView;
	private YouTubeThumbnailView thumbnailView;
	private YouTubeThumbnailLoader thumbnailLoader;

	private YouTubePlayerFragment playerFragment;
	private View playerView;
	private YouTubePlayer player;

	private Dialog errorDialog;

	private int flippingCol;
	private int flippingRow;
	private int videoCol;
	private int videoRow;

	private boolean nextThumbnailLoaded;
	private boolean activityResumed;
	private State state;

	private enum State {
		UNINITIALIZED, LOADING_THUMBNAILS, VIDEO_FLIPPED_OUT, VIDEO_LOADING, VIDEO_CUED, VIDEO_PLAYING, VIDEO_ENDED, VIDEO_BEING_FLIPPED_OUT,
	}

	private Animation fadeOut, fadeIn;
	private ImageView menuImageView, coverImageView, youtubeImageView;
	private View overlayView;
	private SharedPreferences preferences;
	private ViewGroup viewFrame;
	private int imageWidth, imageHeight;
	private boolean firstTime = true;
	private String currentVideoId;
	private int interImagePaddingPx;
	private String currentPlaylist = PLAYLIST_ID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		state = State.UNINITIALIZED;

		viewFrame = new FrameLayout(this);

		flipDelayHandler = new FlipDelayHandler();

		setContentView(viewFrame);

		fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

		SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
		currentPlaylist = settings.getString(PLAYLIST, PLAYLIST_ID);
		Log.d(LOG_TAG, "currentPlaylist=" + currentPlaylist);

		// Set the context for Google Analytics
		Analytics.createAnalytics(this);
		Utils.logDeviceInfo(this);
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

	@Override
	public void onInitializationSuccess(YouTubeThumbnailView thumbnailView, YouTubeThumbnailLoader thumbnailLoader) {
		Log.d(LOG_TAG, "onInitializationSuccess");
		this.thumbnailLoader = thumbnailLoader;
		thumbnailLoader.setOnThumbnailLoadedListener(new ThumbnailListener());
		maybeStartDemo();
	}

	@Override
	public void onInitializationFailure(YouTubeThumbnailView thumbnailView, YouTubeInitializationResult errorReason) {
		Log.w(LOG_TAG, "onInitializationFailure: " + errorReason);
		if (errorReason.isUserRecoverableError()) {
			if (errorDialog == null || !errorDialog.isShowing()) {
				errorDialog = errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST);
				errorDialog.show();
			}
		} else {
			String errorMessage = String.format(getString(R.string.error_thumbnail_view), errorReason.toString());
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasResumed) {
		Log.d(LOG_TAG, "onInitializationSuccess");
		VideoWallActivity.this.player = player;
		player.setPlayerStyle(PlayerStyle.CHROMELESS);
		player.setPlayerStateChangeListener(new VideoListener());
		player.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
		maybeStartDemo();
	}

	@Override
	public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
		Log.w(LOG_TAG, "onInitializationFailure: " + errorReason);
		if (errorReason.isUserRecoverableError()) {
			if (errorDialog == null || !errorDialog.isShowing()) {
				errorDialog = errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST);
				errorDialog.show();
			}
		} else {
			String errorMessage = String.format(getString(R.string.error_player), errorReason.toString());
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
		}
	}

	private void maybeStartDemo() {
		Log.d(LOG_TAG, "maybeStartDemo");
		if (activityResumed && player != null && thumbnailLoader != null && state.equals(State.UNINITIALIZED)) {
			// loading the first thumbnail will kick off demo
			Log.d(LOG_TAG, "setPlaylist");
			thumbnailLoader.setPlaylist(currentPlaylist);
			state = State.LOADING_THUMBNAILS;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RECOVERY_DIALOG_REQUEST) {
			// Retry initialization if user performed a recovery action
			if (errorDialog != null && errorDialog.isShowing()) {
				errorDialog.dismiss();
			}
			errorDialog = null;
			playerFragment.initialize(DEVELOPER_KEY, this);
			thumbnailView.initialize(DEVELOPER_KEY, this);
		}
	}

	@Override
	protected void onResume() {
		Log.d(LOG_TAG, "onResume");
		super.onResume();

		createUserInterface();
	}

	public void createUserInterface() {
		if (flipDelayHandler != null) {
			flipDelayHandler.removeCallbacksAndMessages(null);
		}

		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		int padding = INTER_IMAGE_PADDING_DP;
		String paddingValue = preferences.getString(PreferencesActivity.GENERAL_BORDER, String.valueOf(INTER_IMAGE_PADDING_DP));
		Log.d(LOG_TAG, "padding=" + paddingValue);
		if (paddingValue.equals(PreferencesActivity.BORDER_NONE)) {
			padding = 0;
		} else if (paddingValue.equals(PreferencesActivity.BORDER_THIN)) {
			padding = INTER_IMAGE_PADDING_DP;
		} else if (paddingValue.equals(PreferencesActivity.BORDER_THICK)) {
			padding = INTER_IMAGE_PADDING_DP * 2;
		}

		String rowsValue = preferences.getString(PreferencesActivity.GENERAL_ROWS, String.valueOf(MAX_NUMBER_OF_ROWS_WANTED));
		Log.d(LOG_TAG, "rows=" + rowsValue);
		int rows = MAX_NUMBER_OF_ROWS_WANTED;
		try {
			rows = Integer.parseInt(rowsValue);
		} catch (NumberFormatException e) {
		}

		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		int maxAllowedNumberOfRows = (int) Math.floor((displayMetrics.heightPixels / displayMetrics.density) / PLAYER_VIEW_MINIMUM_HEIGHT_DP);
		int numberOfRows = Math.min(maxAllowedNumberOfRows, rows);
		interImagePaddingPx = (int) displayMetrics.density * padding;
		imageHeight = (displayMetrics.heightPixels / numberOfRows) - interImagePaddingPx;
		imageWidth = (int) (imageHeight * THUMBNAIL_ASPECT_RATIO);

		if (imageWallView != null) {
			viewFrame.removeView(imageWallView);
		}
		imageWallView = new ImageWallView(this, imageWidth, imageHeight, interImagePaddingPx);
		viewFrame.addView(imageWallView, MATCH_PARENT, MATCH_PARENT);

		if (thumbnailLoader != null) {
			thumbnailLoader.setOnThumbnailLoadedListener(null);
			thumbnailLoader.release();
			thumbnailLoader = null;
		}
		thumbnailView = new YouTubeThumbnailView(this);
		thumbnailView.initialize(DEVELOPER_KEY, this);

		createAnimationView(firstTime ? INITIAL_FLIP_DURATION_MILLIS : FLIP_DURATION_MILLIS);
		firstTime = false;

		if (playerView != null) {
			viewFrame.removeView(playerView);
			getFragmentManager().beginTransaction().remove(playerFragment).commit();
		}
		playerView = new FrameLayout(this);
		playerView.setId(R.id.player_view);
		playerView.setVisibility(View.INVISIBLE);
		viewFrame.addView(playerView, imageWidth, imageHeight);

		playerFragment = YouTubePlayerFragment.newInstance();
		playerFragment.initialize(DEVELOPER_KEY, this);
		getFragmentManager().beginTransaction().add(R.id.player_view, playerFragment).commit();

		if (overlayView != null) {
			viewFrame.removeView(overlayView);
		}
		LayoutInflater inflater = getLayoutInflater();
		overlayView = inflater.inflate(R.layout.overlay, null);
		viewFrame.addView(overlayView, MATCH_PARENT, MATCH_PARENT);

		menuImageView = (ImageView) findViewById(R.id.menu);
		coverImageView = (ImageView) findViewById(R.id.cover);
		youtubeImageView = (ImageView) findViewById(R.id.youtube);

		// display menu hint
		fadeOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				menuImageView.setVisibility(View.GONE);
				showCover(false);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}

		});
		menuImageView.startAnimation(fadeOut);

		// For first time install show the introduction dialog with some user
		// instructions
		SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
		boolean firstInstall = settings.getBoolean(FIRST_INSTALL, true);
		if (firstInstall) {
			try {
				Dialogs.displayIntroduction(this);

				// persist not to show introduction again
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean(FIRST_INSTALL, false);
				editor.commit();
			} catch (Exception e) {
				Log.d(LOG_TAG, "first install", e);
			}
		} else {
			Dialogs.displayRating(this);
		}

		activityResumed = true;
		state = State.UNINITIALIZED;

		Analytics.logEvent(Analytics.VIDEO_WALL);
	}

	private void createAnimationView(int duration) {
		String effect = preferences.getString(PreferencesActivity.GENERAL_EFFECT, PreferencesActivity.EFFECT_FLIP);
		Log.d(LOG_TAG, "effect=" + effect);
		if (imageAnimationView != null) {
			viewFrame.removeView(imageAnimationView);
		}
		if (effect.equals(PreferencesActivity.EFFECT_FLIP)) {
			imageAnimationView = new FlipImageAnimationView(this, this, imageWidth, imageHeight);
		} else if (effect.equals(PreferencesActivity.EFFECT_FADE)) {
			imageAnimationView = new FadingImageAnimationView(this, this, imageWidth, imageHeight);
		} else if (effect.equals(PreferencesActivity.EFFECT_RIGHT_LEFT)) {
			imageAnimationView = new RightLeftImageAnimationView(this, this, imageWidth, imageHeight);
		} else if (effect.equals(PreferencesActivity.EFFECT_TOP_DOWN)) {
			imageAnimationView = new TopDownImageAnimationView(this, this, imageWidth, imageHeight);
		}
		imageAnimationView.setDuration(duration);
		viewFrame.addView(imageAnimationView, imageWidth, imageHeight);
	}

	@Override
	protected void onPause() {
		if (player != null) {
			Log.d(LOG_TAG, "player.pause: " + currentVideoId);
			player.pause();
		}
		flipDelayHandler.removeCallbacksAndMessages(null);
		activityResumed = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (thumbnailLoader != null) {
			thumbnailLoader.release();
		}
		super.onDestroy();
	}

	private void flipNext() {
		if (!nextThumbnailLoaded || state.equals(State.VIDEO_LOADING)) {
			return;
		}

		if (state.equals(State.VIDEO_ENDED)) {
			flippingCol = videoCol;
			flippingRow = videoRow;
			state = State.VIDEO_BEING_FLIPPED_OUT;
		} else {
			// When video is cued only pick locations that are totally visible
			// otherwise player will fail.
			Pair<Integer, Integer> nextTarget = imageWallView.getNextLoadTarget(state == State.VIDEO_CUED);
			flippingCol = nextTarget.first;
			flippingRow = nextTarget.second;
		}

		imageAnimationView.setX(imageWallView.getXPosition(flippingCol, flippingRow));
		imageAnimationView.setY(imageWallView.getYPosition(flippingCol, flippingRow));
		imageAnimationView.setInDrawable(thumbnailView.getDrawable());
		imageAnimationView.setOutDrawable(imageWallView.getImageDrawable(flippingCol, flippingRow));
		imageWallView.setImageDrawable(flippingCol, flippingRow, thumbnailView.getDrawable());
		imageWallView.hideImage(flippingCol, flippingRow);
		imageAnimationView.setVisibility(View.VISIBLE);
		imageAnimationView.start();
	}

	@Override
	public void onAnimationCompleted(ImageAnimationView view) {
		imageWallView.showImage(flippingCol, flippingRow);
		imageAnimationView.setVisibility(View.INVISIBLE);

		if (activityResumed) {
			loadNextThumbnail();

			if (state.equals(State.VIDEO_BEING_FLIPPED_OUT)) {
				state = State.VIDEO_FLIPPED_OUT;
			} else if (state.equals(State.VIDEO_CUED)) {
				overlayView.setVisibility(View.GONE);
				youtubeImageView.setVisibility(View.GONE);
				videoCol = flippingCol;
				videoRow = flippingRow;
				playerView.setX(imageWallView.getXPosition(flippingCol, flippingRow));
				playerView.setY(imageWallView.getYPosition(flippingCol, flippingRow));
				imageWallView.hideImage(flippingCol, flippingRow);
				playerView.setVisibility(View.VISIBLE);
				Log.d(LOG_TAG, "player.play: " + currentVideoId);
				player.play();
				state = State.VIDEO_PLAYING;
			} else if (state.equals(State.LOADING_THUMBNAILS) && imageWallView.allImagesLoaded()) {
				state = State.VIDEO_FLIPPED_OUT; // trigger flip in of an
													// initial video
				imageAnimationView.setDuration(FLIP_DURATION_MILLIS);
				flipDelayHandler.sendEmptyMessage(0);
			}
		}
	}

	private void loadNextThumbnail() {
		nextThumbnailLoaded = false;
		if (thumbnailLoader.hasNext()) {
			Log.d(LOG_TAG, "loadNextThumbnail next");
			thumbnailLoader.next();
		} else {
			Log.d(LOG_TAG, "loadNextThumbnail first");
			thumbnailLoader.first();
		}
		Log.d(LOG_TAG, "loadNextThumbnail end");
	}

	/**
	 * A handler that periodically flips an element on the video wall.
	 */
	private final class FlipDelayHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			flipNext();
			sendEmptyMessageDelayed(0, FLIP_PERIOD_MILLIS);
		}

	}

	/**
	 * An internal listener which listens to thumbnail loading events from the
	 * {@link YouTubeThumbnailView}.
	 */
	private final class ThumbnailListener implements YouTubeThumbnailLoader.OnThumbnailLoadedListener {

		@Override
		public void onThumbnailLoaded(YouTubeThumbnailView thumbnail, String videoId) {
			nextThumbnailLoaded = true;

			if (activityResumed) {
				if (state.equals(State.LOADING_THUMBNAILS)) {
					flipNext();
				} else if (state.equals(State.VIDEO_FLIPPED_OUT)) {
					// load player with the video of the next thumbnail being
					// flipped in
					state = State.VIDEO_LOADING;
					currentVideoId = videoId;
					if (currentVideoId != null) {
						Log.d(LOG_TAG, "player.cueVideo: " + currentVideoId);
						player.cueVideo(currentVideoId);
					}
				}
			}
		}

		@Override
		public void onThumbnailError(YouTubeThumbnailView thumbnail, YouTubeThumbnailLoader.ErrorReason reason) {
			Log.w(LOG_TAG, "onThumbnailError: " + reason);
			loadNextThumbnail();
		}

	}

	private final class VideoListener implements YouTubePlayer.PlayerStateChangeListener {

		@Override
		public void onLoaded(String videoId) {
			currentVideoId = videoId;
			if (currentVideoId != null) {
				state = State.VIDEO_CUED;
			}
		}

		@Override
		public void onVideoEnded() {
			currentVideoId = null;
			state = State.VIDEO_ENDED;
			imageWallView.showImage(videoCol, videoRow);
			playerView.setVisibility(View.INVISIBLE);
			overlayView.setVisibility(View.VISIBLE);
			youtubeImageView.setVisibility(View.VISIBLE);
			youtubeImageView.startAnimation(fadeIn);
		}

		@Override
		public void onError(YouTubePlayer.ErrorReason errorReason) {
			Log.e(LOG_TAG, "player error: " + errorReason);
			currentVideoId = null;
			if (errorReason == YouTubePlayer.ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
				// player has encountered an unrecoverable error - stop the demo
				flipDelayHandler.removeCallbacksAndMessages(null);
				state = State.UNINITIALIZED;
				thumbnailLoader.release();
				thumbnailLoader = null;
				player = null;
			} else {
				state = State.VIDEO_ENDED;
			}
		}

		// ignored callbacks

		@Override
		public void onVideoStarted() {
		}

		@Override
		public void onAdStarted() {
		}

		@Override
		public void onLoading() {
		}

	}

	/**
	 * Create the menu. Invoked by the user by pressing the menu key.
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_SELECT_PLAYLIST, 0, R.string.menu_select_playlist).setIcon(R.drawable.ic_menu_playlists).setAlphabeticShortcut('P');
		menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings).setIcon(R.drawable.ic_menu_settings).setAlphabeticShortcut('S');
		menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(R.drawable.ic_menu_clipboard).setAlphabeticShortcut('A');

		return true;
	}

	/**
	 * Handle the menu selections.
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ABOUT:
			Dialogs.displayAbout(this);
			return true;
		case MENU_SELECT_PLAYLIST:
			Dialogs.displayPlaylists(this);
			return true;
		case MENU_SETTINGS:
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			Analytics.logEvent(Analytics.SETTINGS);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Display the cover layer to darken the screen for dialogs.
	 * 
	 * @param isVisible
	 */
	public void showCover(boolean isVisible) {
		if (isVisible) {
			overlayView.setVisibility(View.VISIBLE);
			coverImageView.setAlpha(COVER_ALPHA_VALUE);
			coverImageView.setVisibility(View.VISIBLE);
		} else {
			// overlayView.setVisibility(View.GONE);
			coverImageView.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER: {
			if (currentVideoId != null && YouTubeIntents.isYouTubeInstalled(this) && YouTubeIntents.canResolvePlayVideoIntent(this)) {
				Intent intent = YouTubeIntents.createPlayVideoIntentWithOptions(this, currentVideoId, true, true);
				startActivity(intent);
			}
			return true;
		}
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return event.dispatch(this, null, null);
	}

	public void setPlaylist(String playlist) {
		activityResumed = false;
		if (player != null) {
			player.pause();
		}
		if (flipDelayHandler != null) {
			flipDelayHandler.removeCallbacksAndMessages(null);
		}
		if (thumbnailLoader != null) {
			thumbnailLoader.setOnThumbnailLoadedListener(null);
			thumbnailLoader.release();
			thumbnailLoader = null;
		}
		currentVideoId = null;
		currentPlaylist = playlist;

		// persist playlist selection
		try {
			SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(PLAYLIST, playlist);
			editor.commit();
		} catch (Exception e) {
			Log.d(LOG_TAG, "setPlaylist", e);
		}

		Log.d(LOG_TAG, "currentPlaylist=" + currentPlaylist);
		createUserInterface();
	}

}
