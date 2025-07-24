package com.hiflying.blelink.ble;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.hiflying.blelink.GTransformer;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Ble {

	private static final String TAG = "Ble";
	
	private static final int BLE_DISCOVERY_PERIOD = 10 * 1000;
//	private static final String BT_BOX_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
//	private static final String TRANSFOR_NOTIFY = "0000fff4-0000-1000-8000-00805f9b34fb";
//	private static final String TRANSFOR_READ_WRITE = "0000fff3-0000-1000-8000-00805f9b34fb";
 //  private static final String CLIENT_CHARACTERISTIC_CONFIG = "0000ffe4-0000-1000-8000-00805f9b34fb";
    
    
  //  private static final String TRANSFOR_READ = "0000ffe4-0000-1000-8000-00805f9b34fb";
  //  private static final String TRANSFOR_WRITE = "0000ffe9-0000-1000-8000-00805f9b34fb";
    
	private Context mContext;
	private BluetoothAdapter mAdapter;
	private LeScanCallback mLeScanCallback;
	private BluetoothGatt mBluetoothGatt;
	private BluetoothGattCallback mBluetoothGattCallback;
	private BluetoothGattCharacteristic mNotifyGattCharacteristic;   //被动 读操作的特征值
	private BluetoothGattCharacteristic mReadWriteGattCharacteristic;  //写操作的特征值
	private BleCallback mTransferCallback;
	private Handler mHandler = new Handler();
	private Runnable mCancelScanRunnable;
	private String mMac;
	private boolean mStartScan;
	private HashSet<String> mScanDevices = new HashSet<String>();
	
	private String serviceUuid;
	private String readWriteCharacteristicUuid;
	private String notifyCharacteristicUuid;
	
	/**
	 * @param mTransferCallback the mTransferCallback to set
	 */
	public void setCallback(BleCallback mTransferCallback) {
		this.mTransferCallback = mTransferCallback;
	}

	/**
	 * @return the serviceUuid
	 */
	public String getServiceUuid() {
		return serviceUuid;
	}

	/**
	 * @param serviceUuid the serviceUuid to set
	 */
	public void setServiceUuid(String serviceUuid) {
		this.serviceUuid = serviceUuid;
	}

	/**
	 * @return the readWriteCharacteristicUuid
	 */
	public String getReadWriteCharacteristicUuid() {
		return readWriteCharacteristicUuid;
	}

	/**
	 * @param readWriteCharacteristicUuid the readWriteCharacteristicUuid to set
	 */
	public void setReadWriteCharacteristicUuid(String readWriteCharacteristicUuid) {
		this.readWriteCharacteristicUuid = readWriteCharacteristicUuid;
	}

	/**
	 * @return the notifyCharacteristicUuid
	 */
	public String getNotifyCharacteristicUuid() {
		return notifyCharacteristicUuid;
	}

	/**
	 * @param notifyCharacteristicUuid the notifyCharacteristicUuid to set
	 */
	public void setNotifyCharacteristicUuid(String notifyCharacteristicUuid) {
		this.notifyCharacteristicUuid = notifyCharacteristicUuid;
	}

	public static Ble getInstance(Context context) {
		
		if (BleTransferInner.instance.mContext == null) {
			BleTransferInner.instance.init(context);
		}
		return BleTransferInner.instance;
	}
	
	private static class BleTransferInner {
		private static final Ble instance = new Ble();
	}
	
	private Ble init(Context context) {
		this.mContext = context;
		BluetoothManager manager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
		mAdapter = manager.getAdapter();
		mLeScanCallback = new LeScanCallback() {
			
			@Override
			public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
				// TODO Auto-generated method stub

				Log.v(TAG, "onLeScan: " + device);

				if (TextUtils.isEmpty(mMac)) {
					
					if (!mScanDevices.contains(device.getAddress())) {

						mScanDevices.add(device.getAddress());
						if (mTransferCallback != null) {
							mTransferCallback.onDeviceFind(device, rssi, scanRecord);
						}	
					}
				}else {

					if (device.getAddress().equals(mMac)) {
						stopScanDevice();
						if (mTransferCallback != null) {
							mTransferCallback.onDeviceFind(true);
						}
					}
				}
			}
		};
		mBluetoothGattCallback = new BluetoothGattCallback() {

			//连接状态改变
			@Override
			public void onConnectionStateChange(BluetoothGatt gatt, int status,
					int newState) {
				// TODO Auto-generated method stub
				super.onConnectionStateChange(gatt, status, newState);
				Log.i(TAG, String.format("onConnectionStateChange: status-%s newState-%s", status, newState));

				Integer state = null;
				
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					Log.i(TAG, "onConnectionStateChange: STATE_CONNECTED, discover services...");
					
					if (mBluetoothGatt != null) {
						mBluetoothGatt.discoverServices();	
					}
				}else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					state = BleCallback.STATE_DISCONNECTED;
				}
				
				if (state != null && mTransferCallback != null) {
					mTransferCallback.onConnectionChanged(state);
				}
			}

			//发现设备回调
			@Override
			public void onServicesDiscovered(BluetoothGatt gatt, int status) {
				// TODO Auto-generated method stub
				super.onServicesDiscovered(gatt, status);
				Log.v(TAG, "onServicesDiscovered: status-" + status);
				
				int state = -1;
				if (status == BluetoothGatt.GATT_SUCCESS) {
					
					for (BluetoothGattService service : gatt.getServices()) {
						
						Log.d(TAG, "onServicesDiscovered:" + service.getUuid());
						
						if (service.getUuid().equals(UUID.fromString(serviceUuid))) 
						{//找到读写服务
							if (!TextUtils.isEmpty(notifyCharacteristicUuid)) {

								mNotifyGattCharacteristic = service.getCharacteristic(
										UUID.fromString(notifyCharacteristicUuid));
							}
							
							if (!TextUtils.isEmpty(readWriteCharacteristicUuid)) {

								mReadWriteGattCharacteristic = service.getCharacteristic(
										UUID.fromString(readWriteCharacteristicUuid));
							}
							
						/*	if (mNotifyGattCharacteristic != null && mReadWriteGattCharacteristic != null) {
								
								state = BleCallback.STATE_CONNECTED;
								BluetoothGattDescriptor descriptor = mNotifyGattCharacteristic.getDescriptor(
										UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
								if (descriptor != null && 
										descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) && 
										mBluetoothGatt.writeDescriptor(descriptor) &&
										mBluetoothGatt.setCharacteristicNotification(mNotifyGattCharacteristic, true)) {

									state = BleTransferCallback.STATE_CONNECTED;
								}
								break;
							}*/
								
							state = BleCallback.STATE_CONNECTED;
							break;
						}
					}
					
					if (state == BleCallback.STATE_CONNECTED && mTransferCallback != null) {
						mTransferCallback.onConnectionChanged(state);
					}else {
						Log.w(TAG, "Not find service " + serviceUuid);
						disconnect();
					}
					
				}else {
					Log.w(TAG, "onServicesDiscovered status: " + status);
//					mBluetoothGatt.disconnect();
					mBluetoothGatt.close();
				}
			}

			//被动收数据回调  用的多
			@Override
			public void onCharacteristicChanged(BluetoothGatt gatt,
					BluetoothGattCharacteristic characteristic) {
				// TODO Auto-generated method stub
				super.onCharacteristicChanged(gatt, characteristic);

				Log.i(TAG, "onCharacteristicChanged: address-" + gatt.getDevice().getAddress() +
						" uuid-" + characteristic.getUuid());
				if (mTransferCallback != null) {
					mTransferCallback.onDataNotified(characteristic.getValue());
				}
			}
			
			//写操作回调
			@Override
			public void onCharacteristicWrite(BluetoothGatt gatt,
					BluetoothGattCharacteristic characteristic, int status) {
				// TODO Auto-generated method stub
				super.onCharacteristicWrite(gatt, characteristic, status);
				Log.i(TAG, "onCharacteristicWrite: address-" + gatt.getDevice().getAddress() +
						" uuid-" + characteristic.getUuid() + " status-" + status);
				if (mTransferCallback != null) {
					mTransferCallback.onDataWritten(characteristic.getValue(), status == BluetoothGatt.GATT_SUCCESS);
				}
			}

			//主动读数据回调  用的少
			@Override
			public void onCharacteristicRead(BluetoothGatt gatt,
					BluetoothGattCharacteristic characteristic, int status) {
				// TODO Auto-generated method stub
				super.onCharacteristicRead(gatt, characteristic, status);

				Log.i(TAG, "onCharacteristicRead: address-" + gatt.getDevice().getAddress() +
						" characteristic-" + Arrays.toString(characteristic.getValue()) + " status-" + status);
				
				if (status == BluetoothGatt.GATT_SUCCESS && mTransferCallback != null) {
					mTransferCallback.onDataRead(gatt, characteristic, characteristic.getValue());
				}
			}
			
			//写操作回调
			@Override
			public void onDescriptorWrite(BluetoothGatt gatt,
					BluetoothGattDescriptor descriptor, int status) {
				// TODO Auto-generated method stub
				super.onDescriptorWrite(gatt, descriptor, status);

				Log.i(TAG, "onDescriptorWrite: address-" + gatt.getDevice().getAddress() +
						" descriptor-" + descriptor.getUuid() + " status-" + status);

				if (mTransferCallback != null)
				{
					
					if (status == BluetoothGatt.GATT_SUCCESS) {
						
						boolean enable = Arrays.equals(descriptor.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						if (enable) {
							mTransferCallback.onNotifyChanged(true);
						}else {
							enable = Arrays.equals(descriptor.getValue(), BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
							if (enable) {
								mTransferCallback.onNotifyChanged(false);
							}else {
								mTransferCallback.onNotifyChanged(null);
							}
						}
					}else {
						mTransferCallback.onNotifyChanged(null);
					}
				}
				
//				if (descriptor.getUuid().toString().equals(CLIENT_CHARACTERISTIC_CONFIG) && mTransferCallback != null) {
//					
//					if (status == BluetoothGatt.GATT_SUCCESS) {
//						
//						boolean enable = Arrays.equals(descriptor.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//						if (enable) {
//							mTransferCallback.onNotifyChanged(true);
//						}else {
//							enable = Arrays.equals(descriptor.getValue(), BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//							if (enable) {
//								mTransferCallback.onNotifyChanged(false);
//							}else {
//								mTransferCallback.onNotifyChanged(null);
//							}
//						}
//					}else {
//						mTransferCallback.onNotifyChanged(null);
//					}
//				}
				
//				if (status == BluetoothGatt.GATT_SUCCESS) {
//					mTransferCallback.onNotifyChanged(true);
//				}else {
//					mTransferCallback.onNotifyChanged(null);
//				}
			}
			
			@Override
			public void onDescriptorRead(BluetoothGatt gatt,
					BluetoothGattDescriptor descriptor, int status) {
				// TODO Auto-generated method stub
				super.onDescriptorRead(gatt, descriptor, status);

				Log.i(TAG, "onDescriptorRead: address-" + gatt.getDevice().getAddress() +
						" descriptor-" + descriptor.getUuid() + " status-" + status);
//				if (descriptor.getUuid().toString().equals(CLIENT_CHARACTERISTIC_CONFIG) && mTransferCallback != null) {
//					
//					if (status == BluetoothGatt.GATT_SUCCESS) {
//						
//						boolean enable = Arrays.equals(descriptor.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//						if (enable) {
//							mTransferCallback.onNotifyRead(true);
//						}else {
//							enable = Arrays.equals(descriptor.getValue(), BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//							if (enable) {
//								mTransferCallback.onNotifyRead(false);
//							}else {
//								mTransferCallback.onNotifyRead(null);
//							}
//						}
//					}else {
//						mTransferCallback.onNotifyRead(null);
//					}
//				}
			}
		};
		mCancelScanRunnable = new Runnable() {
			
			@Override
			public void run() {
				mAdapter.stopLeScan(mLeScanCallback);
				if (mTransferCallback != null) {
					mTransferCallback.onDeviceFind(false);
				}
				if (mStartScan) {

					mStartScan = false;
					if (mTransferCallback != null) {
						mTransferCallback.onScanFinished();
					}
				}
			}
		};
		return this;
	}

	private Ble() {}
	
	public boolean hasAdapter() {
		return mAdapter != null;
	}
	
	public boolean isAdapterOn() {
		return mAdapter.isEnabled();
	}
	
	/**
     * Get the current state of the local Bluetooth adapter.
     * <p>Possible return values are
     * {@link BluetoothAdapter#STATE_OFF},
     * {@link BluetoothAdapter#STATE_TURNING_ON},
     * {@link BluetoothAdapter#STATE_ON},
     * {@link BluetoothAdapter#STATE_TURNING_OFF}.
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH}
     *
     * @return current state of Bluetooth adapter
     */
	public int getAdapterState() {
		return mAdapter.getState();
	}
	
	public static boolean hasAdapter(Context context) {
		return getAdapter(context) != null;
	}
	
	public static boolean isAdapterOn(Context context) {
		
		BluetoothAdapter adapter = getAdapter(context);
		return adapter != null &&  adapter.isEnabled();
	}

	/**
    * Starts a scan for a Bluetooth LE device with its MAC address.
    *
    * <p>Results of the scan are reported using the
    * {@link BleCallback#onDeviceFind(BluetoothDevice, int, byte[])} callback.
    *
    * <p>Requires {@link android.Manifest.permission#BLUETOOTH_ADMIN} permission.
    *
    * @param mac the MAC address of Bluetooth LE device to scan
    * @return true, if the scan was started successfully
    */
	public synchronized boolean scanDevice(final String mac) {
		
		if (GTransformer.toMac(mac) == null) {
			throw new IllegalArgumentException("parameter mac is not invalid mac address: " + mac);
		}
		
		if (!mStartScan) {
			
			mStartScan = true;
			mMac = mac;
//			stopScanDevice();
			mHandler.postDelayed(mCancelScanRunnable, BLE_DISCOVERY_PERIOD);
			
			boolean success = mAdapter.startLeScan(mLeScanCallback);
			Log.i(TAG, "scanDevice:" + mac + " success-" + success);
			return success;
		}else {
			return false;
		}
	}
	
	 /**
     * Starts a scan for Bluetooth LE devices.
     *
     * <p>Results of the scan are reported using the
     * {@link BleCallback#onDeviceFind(BluetoothDevice, int, byte[])} callback.
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH_ADMIN} permission.
     *
     * @return true, if the scan was started successfully
     */
	public synchronized boolean scanDevice() {
		
		if (!mStartScan) {
			
			mMac = null;
			mScanDevices.clear();
			
			Log.i(TAG, "scanDevice:");
//			stopScanDevice();
			mStartScan = true;
			mHandler.postDelayed(mCancelScanRunnable, BLE_DISCOVERY_PERIOD);
			return mAdapter.startLeScan(mLeScanCallback);
		}else {
			return false;
		}
	}

    /**
     * Stops an ongoing Bluetooth LE device scan.
     *
     * <p>reported using the
     * {@link BleCallback#onScanFinished()} callback when scan stoped.
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH_ADMIN} permission.
     */
	public synchronized void stopScanDevice() {
		Log.i(TAG, "stopScanDevice");
		if (mStartScan) {

			mStartScan = false;
			mHandler.removeCallbacks(mCancelScanRunnable);
			mAdapter.stopLeScan(mLeScanCallback);
			if (mTransferCallback != null) {
				mTransferCallback.onScanFinished();
			}
		}
	}
	
	public synchronized boolean connectDevice(String mac) {
		
		mMac = mac;
		
		if (mMac == null) {
			throw new RuntimeException("mac is null");
		}
	
		/*	if (mBluetoothGatt != null) {
			
	 	//	throw new RuntimeException("mac is not null");
	 		
	 	
			BluetoothDevice device = mAdapter.getRemoteDevice(mMac);
			if (device == null) {
				Log.i(TAG, "Not find device with mac address: " + mMac);
				onConnectDeviceFailed();
				return false;
			}

			if (mBluetoothGatt != null && mBluetoothGatt.connect()) {

				Log.i(TAG, "a previous mBluetoothGatt is exist, connect it");
				if (mTransferCallback != null) {
					mTransferCallback.onConnectionChanged(BleCallback.STATE_CONNECTING);
				}
				return true;
			}else {
				onConnectDeviceFailed();
				return false;
			}
			
		}*/
		
		BluetoothDevice device = mAdapter.getRemoteDevice(mMac);
		if (device == null) {
			Log.i(TAG, "Not find device with mac address: " + mMac);
			onConnectDeviceFailed();
			return false;
		}
		if (mBluetoothGatt != null)
		{//已经连接  则先断开
		//	mBluetoothGatt.connect();
			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
		//开始连接 异步过程
		mBluetoothGatt = device.connectGatt(mContext, false, mBluetoothGattCallback);
		if (mBluetoothGatt != null && mTransferCallback != null) {
			mTransferCallback.onConnectionChanged(BleCallback.STATE_CONNECTING);
		}
		
		return true; 
	}
	
	public synchronized boolean connectDevice() {
		return connectDevice(mMac);
	}
	
	public synchronized void disconnect() {
		
		Log.i(TAG, "disconnect");
		if (mBluetoothGatt != null) {
			Log.i(TAG, "mBluetoothGatt.disconnect");
			mBluetoothGatt.disconnect();
		}
	}
	
	public synchronized void close() {
		
		Log.i(TAG, "close");
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
			mBluetoothGatt = null;
			Log.i(TAG, "mBluetoothGatt.close");
		}
	}
	
	/**
	 * Write the data to characteristic with uuid({@link #readWriteCharacteristicUuid}). 
	 * A {@link BleCallback#onDataWritten(byte[], boolean)}  callback is triggered
	 *  to report the result of the write operation. 
	 * @param data
	 */
	public void write(final byte[] data) {
		
		Log.i(TAG, "write: " + GTransformer.bytes2HexStringWithWhitespace(data));
		if (mReadWriteGattCharacteristic != null && mBluetoothGatt != null) {
			Log.i(TAG, "setValue:" + mReadWriteGattCharacteristic.setValue(data));
			mReadWriteGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
			Log.i(TAG, "writeCharacteristic:" + mBluetoothGatt.writeCharacteristic(mReadWriteGattCharacteristic));
		}
	}
	
	/**
	 * Write the data on characteristic with uuid({@link #readWriteCharacteristicUuid}). 
	 * A {@link BleCallback#onDataRead(BluetoothGatt, BluetoothGattCharacteristic, byte[])} callback is triggered
	 *  to report the result of the read operation. 
	 */
	public void read() {
		Log.i(TAG, "read");
		mBluetoothGatt.readCharacteristic(mReadWriteGattCharacteristic);
	}
	
	//启用被动读数据的接口
	/**
	 * Enable the notify. A {@link BleCallback#onNotifyChanged(Boolean)} callback is triggered
	 *  to report the result of the enable operation. 
	 * @param enable
	 */
	public void enableNotify(boolean enable) {
		
		if (mBluetoothGatt != null && mNotifyGattCharacteristic != null) {
			
			mBluetoothGatt.setCharacteristicNotification(mNotifyGattCharacteristic, enable);

			//遍历descriptors, 设置enable
			for(BluetoothGattDescriptor dp:mNotifyGattCharacteristic.getDescriptors()) {
				dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				mBluetoothGatt.writeDescriptor(dp);
				}
			
			
			
			if (mTransferCallback != null)
			{
			   //通知上层 连接上了
				mTransferCallback.onNotifyChanged(enable);
			}
			
			
			/*
			
			BluetoothGattDescriptor descriptor = mNotifyGattCharacteristic.getDescriptor(
					UUID.fromString(TRANSFOR_WRITE));
			if (descriptor != null) {
				descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : 
					BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
				mBluetoothGatt.writeDescriptor(descriptor);
			}
			
			*/
		}
	}
	
	//返回 被动读接口是否启用
	/**
	 * Read the notify enabled status, A {@link BleCallback#onNotifyRead(Boolean)} callback is triggered
	 *  to report the result of the read operation. 
	 */
	public void readNotifyEnabled() {
		
		Log.i(TAG, "readNotifyEnabled:mNotifyGattCharacteristic-" + mNotifyGattCharacteristic);
		
		if (mBluetoothGatt != null && mNotifyGattCharacteristic != null) {

			//暂未实现
//			BluetoothGattDescriptor descriptor = mNotifyGattCharacteristic.getDescriptor(
//					UUID.fromString(TRANSFOR_READ));
//			if (descriptor != null) {
//				mBluetoothGatt.readDescriptor(descriptor);
//			}
		}
	}
	
	public BluetoothDevice getBluetoothDevice() {
		if (mAdapter != null) {
			return mAdapter.getRemoteDevice(mMac);
		}
		return null;
	}
	
	public static boolean isBleSupported(Context context) {
		return context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE);
	}

	/**
	 * @return the mAdapter
	 */
	public BluetoothAdapter getAdapter() {
		return mAdapter;
	}
	
	public static BluetoothManager getBluetoothManager(Context context) {
		return (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
	}

	/**
	 * @return the mAdapter
	 */
	public static BluetoothAdapter getAdapter(Context context) {
		return getBluetoothManager(context).getAdapter();
	}
	
	private void onConnectDeviceFailed() {

		if (mTransferCallback != null) {
			mTransferCallback.onConnectionChanged(BleCallback.STATE_DISCONNECTED);
		}
		disconnect();
		close();
	}
	
	public static void requestEnableBluetoothAdapter(Context context) {
		context.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
	}
	
	public static void registerBluetoothStateChangedListener(Context context, OnBluetoothStateChangedListener listener) {
		context.registerReceiver(listener, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
	}
	
	public static void unregisterBluetoothStateChangedListener(Context context, OnBluetoothStateChangedListener listener) {
		try {
			context.unregisterReceiver(listener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
