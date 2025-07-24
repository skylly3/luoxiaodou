package com.hiflying.blelink;

import android.net.wifi.WifiInfo;

public interface OnLinkListener {

	public void onWifiConnectivityChanged(boolean connected, String ssid, WifiInfo wifiInfo);
	public void onBluetoothEnabledChanged(boolean enabled);
	public void onModuleLinked(LinkedModule module);
	public void onModuleLinkTimeOut();

	/**
	 * always invoked when link task finished, no matter the task result
	 */
	public void onFinished();
	public void onError(LinkingError error);
	public void onProgress(LinkingProgress progress);
}
