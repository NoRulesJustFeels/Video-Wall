/*
 * Copyright (C) 2012 ENTERTAILION LLC
 * Copyright (C) 2008 The Android Open Source Project
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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.entertailion.android.videowall.R;
import com.entertailion.android.videowall.playlist.PlaylistInfo;
import com.google.android.youtube.player.YouTubeIntents;

/**
 * Utility class.
 * 
 * @author leon_nicholls
 */
public class Utils {
	private static final String LOG_TAG = "Utils";

	/**
	 * Clean a string so it can be used for a file name
	 * 
	 * @param value
	 * @return
	 */
	public static final String clean(String value) {
		return value.replaceAll(":", "_").replaceAll("/", "_").replaceAll("\\\\", "_").replaceAll("\\?", "_").replaceAll("#", "_");
	}

	/**
	 * Escape XML entities
	 * 
	 * @param aText
	 * @return
	 */
	public static final String escapeXML(String aText) {
		if (null == aText) {
			return "";
		}
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(aText);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			if (character == '<') {
				result.append("&lt;");
			} else if (character == '>') {
				result.append("&gt;");
			} else if (character == '\"') {
				result.append("&quot;");
			} else if (character == '\'') {
				result.append("&#039;");
			} else if (character == '&') {
				result.append("&amp;");
			} else {
				// the char is not a special one
				// add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

	public static final String getVersion(Context context) {
		String versionString = context.getString(R.string.unknown_build);
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionString = info.versionName;
		} catch (Exception e) {
			// do nothing
		}
		return versionString;
	}

	public static final void logDeviceInfo(Context context) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			Log.i(LOG_TAG, "Version=" + pi.versionName);
			Log.i(LOG_TAG, "IP Address=" + Utils.getLocalIpAddress());
			Log.i(LOG_TAG, "android.os.Build.VERSION.RELEASE=" + android.os.Build.VERSION.RELEASE);
			Log.i(LOG_TAG, "android.os.Build.VERSION.INCREMENTAL=" + android.os.Build.VERSION.INCREMENTAL);
			Log.i(LOG_TAG, "android.os.Build.DEVICE=" + android.os.Build.DEVICE);
			Log.i(LOG_TAG, "android.os.Build.MODEL=" + android.os.Build.MODEL);
			Log.i(LOG_TAG, "android.os.Build.PRODUCT=" + android.os.Build.PRODUCT);
			Log.i(LOG_TAG, "android.os.Build.MANUFACTURER=" + android.os.Build.MANUFACTURER);
			Log.i(LOG_TAG, "android.os.Build.BRAND=" + android.os.Build.BRAND);
			Log.i(LOG_TAG, "android.os.Build.BRAND=" + android.os.Build.BRAND);
			Log.i(LOG_TAG, "YouTube Version=" + YouTubeIntents.getInstalledYouTubeVersionName(context));
		} catch (Exception e) {
			Log.e(LOG_TAG, "logDeviceInfo", e);
		}
	}

	public static final String getLocalIpAddress() {
		InetAddress inetAddress = Utils.getLocalInetAddress();
		if (inetAddress != null) {
			return inetAddress.getHostAddress().toString();
		}
		return null;
	}

	public static final InetAddress getLocalInetAddress() {
		InetAddress selectedInetAddress = null;
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				if (intf.isUp()) {
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							if (inetAddress instanceof Inet4Address) { // only
																		// want
																		// ipv4
																		// address
								if (inetAddress.getHostAddress().toString().charAt(0) != '0') {
									if (selectedInetAddress == null) {
										selectedInetAddress = inetAddress;
									} else if (intf.getName().startsWith("eth")) { // prefer
																					// wired
																					// interface
										selectedInetAddress = inetAddress;
									}
								}
							}
						}
					}
				}
			}
			return selectedInetAddress;
		} catch (Throwable e) {
			Log.e(LOG_TAG, "Failed to get the IP address", e);
		}
		return null;
	}

	public static final boolean isUsa() {
		return Locale.getDefault().equals(Locale.US);
	}

	public static ArrayList<PlaylistInfo> getPlaylists(Context context) {
		ArrayList<PlaylistInfo> playlists = new ArrayList<PlaylistInfo>();
		XmlResourceParser parser = null;
		try {
			Resources r = context.getResources();
			int resourceId = r.getIdentifier("playlists", "xml", "com.entertailion.android.videowall");
			if (resourceId != 0) {
				parser = context.getResources().getXml(resourceId); // R.xml.playlists
				parser.next();
				int eventType = parser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG) {
						switch (eventType) {
						case XmlPullParser.START_DOCUMENT:
							break;
						case XmlPullParser.START_TAG:
							String tagName = parser.getName();
							if (tagName.equalsIgnoreCase("playlist")) {
								String name = parser.getAttributeValue(null, "name");
								String id = parser.getAttributeValue(null, "id");
								playlists.add(new PlaylistInfo(id, name));
							}
							break;
						}
					}
					eventType = parser.next();
				}
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "getPlaylists", e);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
		return playlists;
	}

}
