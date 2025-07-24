package com.hiflying.blelink.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;

public class BleCallback {
	
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
	public void onDeviceFind(boolean found) {
	}
	/**
	 * invoked when a new device is found. In most case, you may display the found device in a list, please make sure
	 * the adapter.addDevice(BluetoothDevice device) and adapter.notifyDataSetChanged() run on UI thread.
	 * <pre>
	 * Sample code:
	 *  runOnUiThread(new Runnable() {
	 *  	
	 *  	public void run() {
	 *
	 *  		mAdapter.addDevice(device);
	 *  		mAdapter.notifyDataSetChanged();
	 *  		mDeviceListView.scrollTo(0, mScrollHeight);
	 *  	}
	 *  });
	 * </pre>
     *
     * @param device Identifies the remote device
     * @param rssi The RSSI value for the remote device as reported by the
     *             Bluetooth hardware. 0 if no RSSI value is available.
     * @param scanRecord The content of the advertisement record offered by
     *                   the remote device.
	 */
	public void onDeviceFind(BluetoothDevice device, int rssi, byte[] scanRecord) {
	}
	public void onDeviceFind(String mac, Bundle data) {
	}
	/**
	 * 
	 * @param status one of {@link #STATE_CONNECTING}, {@link #STATE_CONNECTED}, {@link #STATE_DISCONNECTED}
	 */
	public void onConnectionChanged(int status) {
	}
	public void onDataRead(BluetoothGatt agatt, BluetoothGattCharacteristic characteristic, byte[] data) {
	}
	public void onDataNotified(byte[] data) {
	}
	public void onDataWritten(byte[] data, boolean success) {
	}
	public void onNotifyChanged(Boolean enabled) {
	}
	public void onNotifyRead(Boolean enabled) {
	}
	public void onScanFinished() {
	}
	public void onSystemError() {
	}
}