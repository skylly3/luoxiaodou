package com.hiflying.blelink.ble;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class OnBluetoothStateChangedListener extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
		onBluetoothStateChanged(state);
	}

	public abstract void onBluetoothStateChanged(int state);
}
