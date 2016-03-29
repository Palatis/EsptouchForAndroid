package com.espressif.iot.esptouch.demo_activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class EspWifiAdminSimple {
	private final Context mContext;

	public EspWifiAdminSimple(Context context) {
		mContext = context;
	}

	public String getWifiConnectedSsid() {
		WifiInfo mWifiInfo = getConnectionInfo();
		String ssid = null;
		if (mWifiInfo != null && isWifiConnected()) {
			int len = mWifiInfo.getSSID().length();
			if (mWifiInfo.getSSID().startsWith("\"") && mWifiInfo.getSSID().endsWith("\"")) {
				ssid = mWifiInfo.getSSID().substring(1, len - 1);
			} else {
				ssid = mWifiInfo.getSSID();
			}
		}
		return ssid;
	}

	public String getWifiConnectedBssid() {
		final WifiInfo info = getConnectionInfo();
		return (info != null && isWifiConnected()) ? info.getBSSID() : null;
	}

	// get the wifi info which is "connected" in wifi-setting
	private WifiInfo getConnectionInfo() {
		final WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		return wifi.getConnectionInfo();
	}

	private boolean isWifiConnected() {
		final NetworkInfo info = getWifiNetworkInfo();
		return info != null && info.isConnected();
	}

	private NetworkInfo getWifiNetworkInfo() {
		final ConnectivityManager conn = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		return conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	}
}
