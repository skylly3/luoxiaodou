package com.hiflying.blelink.v1;

import com.luobotec.message.AppMessage;
import java.util.Base64;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hiflying.blelink.CRC;
import com.hiflying.blelink.GTransformer;
import com.hiflying.blelink.LinkerUtils;
import com.hiflying.blelink.LinkedModule;
import com.hiflying.blelink.LinkingError;
import com.hiflying.blelink.LinkingProgress;
import com.hiflying.blelink.OnLinkListener;
import com.hiflying.blelink.TeaEncryptor;
import com.hiflying.blelink.ble.Ble;
import com.hiflying.blelink.ble.BleCallback;
import com.hiflying.blelink.demo.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BleLinker {

    private static final String TAG = BleLinker.class.getSimpleName();
   // public static final String BLE_NAME_HIFLYING = "AZ";
    private static final String BLE_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String BLE_NOTIFY_CHARACTERISTIC_UUID = "0000ffe4-0000-1000-8000-00805f9b34fb";
    private static final String BLE_WRITE_CHARACTERISTIC_UUID = "0000ffe9-0000-1000-8000-00805f9b34fb";
    private static final String TEA_ENCRYPTION_KEY = "hiflying12345678";
    private static final String BLE_CONFIG_SUCCESS = "config_success";
    private static final String BLE_CONFIG_FAIL = "config_fail";
    private static final String BLE_CONFIG_ACK = "config_ack";

    /**
     * The udp port to receive smartlink config
     */
    private static int PORT_RECEIVE_SMART_CONFIG = 49999;
    /**
     * The udp port to send smartlinkfind broadcast
     */
    private static int PORT_SEND_SMART_LINK_FIND = 48899;
    private static final int DEFAULT_TIMEOUT_PERIOD = 60000 * 4;

    private static String SMART_LINK_FIND = "smartlinkfind";
    private static String SMART_CONFIG = "smart_config";
    private static final int RETRY_MAX_TIMES = 6;

    private Context context;
  //  private String ssid;
  //  private String password;
 //   private String userData;
   // private String bleName = BLE_NAME_HIFLYING;
    private String bleServiceUuid = BLE_SERVICE_UUID;
    private String bleNotifyCharacteristicUuid = BLE_NOTIFY_CHARACTERISTIC_UUID;
    private String bleWriteCharacteristicUuid = BLE_WRITE_CHARACTERISTIC_UUID;
    private String teaEncryptionKey = TEA_ENCRYPTION_KEY;
    private OnLinkListener onLinkListener;
    private BroadcastReceiver wifiChangedReceiver;
    private WifiManager wifiManager;
    private LinkTask linkTask;
    private LinkingProgress linkingProgress;
    private LinkedModule linkedModule;
    private int timeoutPeriod = DEFAULT_TIMEOUT_PERIOD;
    private Timer timer;
    private boolean isTimeout;
    private WifiManager.WifiLock wifiLock;

    private Ble ble;
    private BroadcastReceiver bluetoothStateChangedReceiver;
    private LinkingStatus linkingStatus = new LinkingStatus();
   // private boolean bleNameStrictMatching = true;

    /**
     * The socket to receive smart_config response
     */
    private MulticastSocket mSmartConfigSocket;

    /**
     * The flag indicates the smartlink is working
     */
    private boolean isLinking;

//    public String getSsid() {
//        return ssid;
//    }
//
//    public void setSsid(String ssid) {
//        this.ssid = ssid;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
/*
    public String getBleName() {
        return bleName;
    }

    public void setBleName(String bleName) {
        this.bleName = bleName;
    }
*/
//    public String getUserData() {
//        return userData;
//    }
//
//    public void setUserData(String userData) {
//        this.userData = userData;
//    }

    public void setTimeoutPeriod(int timeoutPeriod) {
        this.timeoutPeriod = timeoutPeriod;
    }

    public boolean isLinking() {
        return isLinking;
    }

    public void setOnLinkListener(OnLinkListener onLinkListener) {
        this.onLinkListener = onLinkListener;

        if (onLinkListener != null) {

            try {
                onLinkListener.onBluetoothEnabledChanged(ble.isAdapterOn());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
/*
    public void setBleNameStrictMatching(boolean bleNameStrictMatching) {
        this.bleNameStrictMatching = bleNameStrictMatching;
    }
*/
    private static class BleLinkerInner {
        private static final BleLinker BLE_LINKER = new BleLinker();
    }

    private class LinkingStatus {

        private boolean canceled;
        private LinkingProgress progress;
        private Map<String, Object> data = new HashMap<>();
        private static final String KEY_SCANNED_BLE = "KEY_SCANNED_BLE";
        private static final String KEY_CONNECT_BLE = "KEY_CONNECT_BLE";
        private static final String KEY_CONFIG_BLE = "KEY_CONFIG_BLE";
        private static final String KEY_CONFIG_BLE_SUCCESS = "KEY_CONFIG_BLE_SUCCESS";
        private static final String KEY_CONFIG_BLE_ACK = "KEY_CONFIG_BLE_ACK";

        public boolean isCanceled() {
            return canceled;
        }

        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }

        public Map<String, Object> getData() {
            return new HashMap<>(data);
        }

        public Object getData(String key) {
            return data.get(key);
        }

        public void setData(String key, Object value) {
            data.put(key, value);
        }

        public LinkingProgress getProgress() {
            return progress;
        }

        public void setProgress(LinkingProgress progress) {
            this.progress = progress;
        }

//        public String toJson() {
//            return JSON.toJSONString(this);
//        }

        public LinkingStatus(LinkingProgress progress) {
            this.progress = progress;
        }

        public LinkingStatus() {
        }

        public void reset() {
            canceled = false;
            data.clear();
            progress = null;
        }
    }

    public String getWifiSSID(){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null && networkInfo.isConnected()){
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo == null ? null : wifiInfo.getSSID();
            if (LinkerUtils.isEmptySsid(ssid)) {
                ssid = networkInfo.getExtraInfo();
            }
            if (LinkerUtils.isEmptySsid(ssid) && wifiInfo != null) {
                ssid = LinkerUtils.getSsid(context, wifiInfo.getNetworkId());
            }
            return LinkerUtils.getPureSsid(ssid);
        }
        return "";
    }

    private BleLinker() {

        wifiChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (networkInfo != null && onLinkListener != null) {

                    if(networkInfo.isConnected()) {

                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid = wifiInfo == null ? null : wifiInfo.getSSID();
                        if (LinkerUtils.isEmptySsid(ssid)) {
                            ssid = networkInfo.getExtraInfo();
                        }
                        if (LinkerUtils.isEmptySsid(ssid) && wifiInfo != null) {
                            ssid = LinkerUtils.getSsid(context, wifiInfo.getNetworkId());
                        }

                        try {
                            onLinkListener.onWifiConnectivityChanged(true, LinkerUtils.getPureSsid(ssid), wifiInfo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {

                        try {
                            onLinkListener.onWifiConnectivityChanged(false, null, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        bluetoothStateChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_ON) {

                    if (onLinkListener != null) {

                        try {
                            onLinkListener.onBluetoothEnabledChanged(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }else if (state == BluetoothAdapter.STATE_OFF) {

                    if (onLinkListener != null) {

                        try {
                            onLinkListener.onBluetoothEnabledChanged(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    public static BleLinker getInstance(Context context) {

        if (context == null) {
            throw new NullPointerException();
        }

        BleLinker apLinker = BleLinkerInner.BLE_LINKER;
        if (apLinker.context == null) {

            apLinker.context = context.getApplicationContext();
            apLinker.wifiManager = (WifiManager) apLinker.context.getSystemService(Context.WIFI_SERVICE);
            apLinker.wifiLock = apLinker.wifiManager.createWifiLock(apLinker.context.getPackageName());
            apLinker.ble = Ble.getInstance(apLinker.context);
        }

        return apLinker;
    }

    public WifiInfo getConnectedWifi() {
        return wifiManager.getConnectionInfo();
    }
    
    //scanRecords的格式转换
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void init(final MainActivity mainActivity) {

        context.registerReceiver(wifiChangedReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        context.registerReceiver(bluetoothStateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        ble.setCallback(new BleCallback() {

            @Override
            public void onDeviceFind(BluetoothDevice device, int rssi, byte[] scanRecord) {
                super.onDeviceFind(device, rssi, scanRecord);
                Log.v(TAG, "BleCallback.onDeviceFind: " + device.getName()/* JSON.toJSONString(device)*/);

                String deviceName = device.getName();
                String strBroadcast = bytesToHex(scanRecord);
                /*
                
                if (!TextUtils.isEmpty(deviceName) &&
                        ((bleNameStrictMatching && deviceName.equals(bleName)) ||
                                (!bleNameStrictMatching && deviceName.contains(bleName))))
                            */
                if (!TextUtils.isEmpty(strBroadcast) && strBroadcast.contains("02010016FF00004C42"))  
                 {
                	  //Toast.makeText(mainActivity, strBroadcast, Toast.LENGTH_SHORT).show(); 
                	
                    	synchronized (linkingStatus) {
                           linkingStatus.setData(LinkingStatus.KEY_SCANNED_BLE, device);
                    	}
                    ble.stopScanDevice();
                }
            }

            @Override
            public void onConnectionChanged(int status) {
                super.onConnectionChanged(status);
                
                
              //  Toast.makeText(mainActivity, "BleCallback.onConnectionChanged: ", Toast.LENGTH_SHORT).show(); 
                
                Log.d(TAG, "BleCallback.onConnectionChanged: " + status);

                if (BleCallback.STATE_CONNECTED == status) {
                    Log.d(TAG, "ble connection is created and enable notify");
                    ble.enableNotify(true);
                }
            }

            //被动  收到数据
            @Override
            public void onDataNotified(byte[] data) {
                super.onDataNotified(data);

                String text = new String(data);
                Log.d(TAG, String.format("BleCallback.onDataNotified: hex-%s text-'%s'",
                        GTransformer.bytes2HexStringWithWhitespace(data), text));
                
                Toast.makeText(mainActivity, "收到数据:" + text, Toast.LENGTH_SHORT).show(); 

                
                /*
                if (BLE_CONFIG_SUCCESS.equalsIgnoreCase(text.trim())) {

                    synchronized (linkingStatus) {

                        linkingStatus.setData(LinkingStatus.KEY_CONFIG_BLE_SUCCESS, true);
                        linkingStatus.notifyAll();
                    }
                }else if (BLE_CONFIG_FAIL.equalsIgnoreCase(text.trim())) {

                    synchronized (linkingStatus) {

                        linkingStatus.setData(LinkingStatus.KEY_CONFIG_BLE_SUCCESS, false);
                        linkingStatus.notifyAll();
                    }
                }
                */
            }

            //写入数据
            @Override
            public void onDataWritten(byte[] data, boolean success) {
                super.onDataWritten(data, success);
                Log.d(TAG, String.format("BleCallback.onDataWritten: data-%s success-%s",
                        GTransformer.bytes2HexStringWithWhitespace(data), success));

                if (success) {

                    synchronized (linkingStatus) {

                        if (BLE_CONFIG_ACK.equalsIgnoreCase(new String(data).trim())) {
                            linkingStatus.setData(LinkingStatus.KEY_CONFIG_BLE_ACK, true);
                        }else {
                            linkingStatus.setData(LinkingStatus.KEY_CONFIG_BLE, data);
                        }

                        linkingStatus.notifyAll();
                    }
                }
            }

            //启用被动读数据成功
            @Override
            public void onNotifyChanged(Boolean enabled) {
                super.onNotifyChanged(enabled);
                Log.d(TAG, "BleCallback.onNotifyChanged: " );  //+ JSON.toJSONString(enabled)

                if (Boolean.TRUE == enabled) {

                    synchronized (linkingStatus) {
                    	//准备配置BLE
                        linkingStatus.setData(LinkingStatus.KEY_CONNECT_BLE, true);
                        linkingStatus.notifyAll();
                    }
                }
            }

            //扫描结束
            @Override
            public void onScanFinished() {
                super.onScanFinished();
                Log.d(TAG, "BleCallback.onScanFinished");

                synchronized (linkingStatus) {
                    linkingStatus.notifyAll();
                }
            }
        });

        this.resetProperties();
    }

    public void destroy() {

        try {
            context.unregisterReceiver(wifiChangedReceiver);
        } catch (Exception e) {
        }

        try {
            context.unregisterReceiver(bluetoothStateChangedReceiver);
        } catch (Exception e) {
        }

        this.stop();
        if (linkTask != null) {
            linkTask.cancel(true);
        }
        this.resetProperties();
    }

    public void start() throws Exception {

//        if (TextUtils.isEmpty(ssid)) {
//            throw new Exception("ssid is empty");
//        }

      /*  if (TextUtils.isEmpty(bleName)) {
            throw new Exception("bleName is empty");
        }
*/
        if (isLinking) {
            return;
        }

        resetLinkProperties();
        isLinking = true;
        ble.setServiceUuid(bleServiceUuid);
        ble.setNotifyCharacteristicUuid(bleNotifyCharacteristicUuid);
        ble.setReadWriteCharacteristicUuid(bleWriteCharacteristicUuid);
        linkingStatus.reset();
        linkTask = new LinkTask();
        linkTask.execute();
        isTimeout = false;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "time out!");
                isTimeout = true;
                stop();
            }
        }, timeoutPeriod);
    }

    public void stop() {
        isLinking = false;
//never invoke linkTask.cancel to stop task, otherwise it will make the onFinished not be invoked
//        if (linkTask != null) {
//            linkTask.cancel(true);
//        }
        if (timer != null) {
            try {
                timer.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        synchronized (linkingStatus) {
            linkingStatus.setCanceled(true);
            linkingStatus.notifyAll();
        }

        if (mSmartConfigSocket != null) {

            try {
                mSmartConfigSocket.close();
            }catch (Exception e) {
            }
        }

        ble.close();
    }

    public boolean isBluetoothAdapterEnabled() {
        return  ble.isAdapterOn();
    }

    public boolean isBleSupported() {
        return Ble.isBleSupported(context);
    }

    public void requestEnableBluetoothAdapter() {
        Ble.requestEnableBluetoothAdapter(context);
    }

    private void resetProperties() {

      //  ssid = null;
     //   password = null;
      //  bleName = null;
     //   userData = null;
        onLinkListener = null;
     //   bleNameStrictMatching = true;
        this.resetLinkProperties();
    }

    private void resetLinkProperties() {

        isLinking = false;
        isTimeout = false;
        linkTask = null;
        linkingProgress = null;
        linkedModule = null;
        timer = null;
    }

    private class LinkTask extends AsyncTask<Void, LinkingProgress, LinkingError> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                wifiLock.acquire();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(LinkingError error) {

            Log.d(TAG, "onPostExecute: " + error);

            if (timer != null) {
                timer.cancel();
            }

            try {
                wifiLock.release();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (onLinkListener != null) {

                if (error == null) {

                    if (linkedModule != null) {
                        try {
                            onLinkListener.onModuleLinked(linkedModule);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else if (isTimeout) {
                        try {
                            onLinkListener.onModuleLinkTimeOut();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }else {

                    try {
                        onLinkListener.onError(error);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    onLinkListener.onFinished();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

            resetLinkProperties();
        }

        @Override
        protected void onProgressUpdate(LinkingProgress... values) {

            linkingProgress = values[0];
            if (onLinkListener != null) {
                try {
                    onLinkListener.onProgress(linkingProgress);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected LinkingError doInBackground(Void... voids) {

            if (!ble.isAdapterOn()) {
                return LinkingError.BLUETOOTH_DISABLED;
            }

            if (getConnectedWifi() == null) {
                return LinkingError.NO_VALID_WIFI_CONNECTION;
            }

            try {

                publishProgress(LinkingProgress.SCAN_BLE);
                BluetoothDevice scannedBleDevice = scanBle();
                Log.i(TAG, "LinkTask->scanBle: " + scannedBleDevice.getName()/*JSON.toJSONString(scannedBleDevice)*/);
                if (scannedBleDevice == null) {
                    return LinkingError.BLE_NOT_FOUND;
                }

                publishProgress(LinkingProgress.CONNECT_BLE);
                if (!connectBle(scannedBleDevice.getAddress())) {
                    Log.w(TAG, String.format("LinkTask->connect ble device mac-%s failed", scannedBleDevice.getAddress()));
                    ble.close();
                    return LinkingError.CONNECT_BLE_FAILED;
                }
                Log.i(TAG, String.format("LinkTask->connect ble device mac-%s succeed", scannedBleDevice.getAddress()));

                publishProgress(LinkingProgress.CONFIG_BLE);
                if (!configBle()) {
                    Log.w(TAG, String.format("LinkTask->config ble device mac-%s failed", scannedBleDevice.getAddress()));
                    ble.close();
                    return LinkingError.CONFIG_BLE_FAILED;
                }
                Log.i(TAG, String.format("LinkTask->config ble device mac-%s succeed", scannedBleDevice.getAddress()));
//                ble.disconnect();
               // ble.close();

              
                publishProgress(LinkingProgress.FIND_DEVICE);
                /*   linkedModule = smartLinkFind();
                Log.i(TAG, String.format("smartlink find: %s", linkedModule));
                if (linkedModule == null) {
                    return  LinkingError.FIND_DEVICE_FAILED;
                }
                */
                
            } catch (LinkingCanceledException e) {
                Log.w(TAG, "ap link task is canceled");
//                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
//                ble.disconnect();
               // ble.close();
            }

            return null;
        }
    }

    /**
     * Scan ble device with {@link #bleName} in 60 seconds
     * @return
     * @throws LinkingCanceledException
     */
    private BluetoothDevice scanBle() throws LinkingCanceledException {

        linkingStatus.setProgress(LinkingProgress.SCAN_BLE);

        for (int i = 0; i < RETRY_MAX_TIMES; i++) {

            boolean succeed = ble.scanDevice();
            Log.d(TAG, String.format("start scan ble device NO.%s time %s", i + 1, succeed ? "succeed" : "failed"));

            synchronized (linkingStatus) {

                try {
                    linkingStatus.wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (linkingStatus.isCanceled()) {
                    throw new LinkingCanceledException();
                }

                if (linkingStatus.getData(LinkingStatus.KEY_SCANNED_BLE) instanceof BluetoothDevice) {
                    return (BluetoothDevice) linkingStatus.getData(LinkingStatus.KEY_SCANNED_BLE);
                }
            }
        }

        return null;
    }

    /**
     * connect the scanned ap in 60 seconds
     * @return
     * @throws LinkingCanceledException
     * @param mac
     */
    private boolean connectBle(String mac) throws LinkingCanceledException {

        linkingStatus.setProgress(LinkingProgress.CONNECT_BLE);

        for (int i = 0; i < RETRY_MAX_TIMES * 2; i++) {

//            ble.disconnect();
//            sleep(1000);
            Log.d(TAG, String.format("start to connect ble device NO.%s time", i + 1));
            boolean succeed = ble.connectDevice(mac);
            Log.d(TAG, String.format("connect ble device NO.%s time %s", i + 1, succeed ? "succeed" : "failed"));

            synchronized (linkingStatus) {

                try {
                    linkingStatus.wait(3000);
                } catch (InterruptedException e) {
                }

                if (linkingStatus.isCanceled()) {
                    throw new LinkingCanceledException();
                }

                if (Boolean.TRUE == linkingStatus.getData(LinkingStatus.KEY_CONNECT_BLE)) {
                	//事件通知   连接成功
                    return true;
                }

                if ((i+1)%2==0){
                    ble.close();
                    try {
                        linkingStatus.wait(500);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        ble.close();
        return false;
    }
    
    public void SendMsg(AppMessage.Request.Builder requestBuilder)
    {
    	requestBuilder.setId(1);
    	AppMessage.Request request = requestBuilder.build();
    	//转换成字节数组
    	byte[] byteArray = request.toByteArray();
    	//打包
        final Base64.Encoder encoder = Base64.getEncoder();
    	final String encodedText = "MSG:" + encoder.encodeToString(byteArray) + "@";
    			
      	byte[] byteSend = encodedText.getBytes();
       ble.write(byteSend);
    }

    private boolean configBle() throws LinkingCanceledException {

        linkingStatus.setProgress(LinkingProgress.CONFIG_BLE);

//        //构建数据包
//        AppMessage.WifiParams.Builder mybuild= AppMessage.WifiParams.newBuilder();
//        mybuild.setName(ssid);
//        mybuild.setPwd(password);
//        AppMessage.WifiParams wifipara = mybuild.build();
//        AppMessage.Request.Builder requestBuilder = AppMessage.Request.newBuilder();
//        requestBuilder.setId(1);
//        requestBuilder.setType(com.luobotec.message.AppMessage.MessageType.wifi);
//        requestBuilder.setWifi(wifipara);
//        
//        AppMessage.Request request = requestBuilder.build();
//		//转换成字节数组
//		byte[] byteArray = request.toByteArray();
//		//打包
//	    final Base64.Encoder encoder = Base64.getEncoder();
//		final String encodedText = "MSG:" + encoder.encodeToString(byteArray) + "@";
//		
//		byte[] byteSend = encodedText.getBytes();
//	    ble.write(byteSend);
	    
	    
        /*
        
        
        List<byte[]> frames = new ArrayList<>();
        try {
            frames.addAll(this.getConfigFrames());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (frames.isEmpty()) {
            return false;
        }

        boolean[] framesWritten = new boolean[frames.size()];
        boolean result = false;

        int count = frames.size() * RETRY_MAX_TIMES;
        int index = 0;
        long time = 0;
        for (int i = 0; i < count; i++) {

            if (this.isAllFramesWritten(framesWritten)) {

                synchronized (linkingStatus) {

                    try {
                        linkingStatus.wait(1000);
                    } catch (InterruptedException e) {
                    }

                    if (linkingStatus.isCanceled()) {
                        throw new LinkingCanceledException();
                    }

                    Boolean configSuccess = (Boolean)linkingStatus.getData(LinkingStatus.KEY_CONFIG_BLE_SUCCESS);
                    Log.d(TAG, String.format("LinkingStatus.KEY_CONFIG_BLE_SUCCESS: %s", configSuccess));

                    if (Boolean.TRUE == configSuccess) {
                        result = true;
                        break;
                    }else if (Boolean.FALSE == configSuccess) {
                        result = false;
                        break;
                    }
                }
            }

            linkingStatus.setData(LinkingStatus.KEY_CONFIG_BLE, null);
            byte[] frame = frames.get(index);
            Log.d(TAG, String.format("write config data, frame[%s]-%s", index + 1, GTransformer.bytes2HexStringWithWhitespace(frame)));
            ble.write(frame);
            time = System.currentTimeMillis();

            synchronized (linkingStatus) {

                try {
                    linkingStatus.wait(1000);
                } catch (InterruptedException e) {
                }

                if (linkingStatus.isCanceled()) {
                    throw new LinkingCanceledException();
                }

                byte[] writtenData = (byte[])linkingStatus.getData(LinkingStatus.KEY_CONFIG_BLE);
                Log.d(TAG, String.format("LinkingStatus.KEY_CONFIG_BLE: %s",
                        GTransformer.bytes2HexStringWithWhitespace(writtenData)));

                if (!framesWritten[index] && Arrays.equals(writtenData, frame)) {
                    framesWritten[index] = true;
                }
            }

            if (framesWritten[index]) {
                index++;
            }
            if (index >= frames.size()) {
                index = 0;
            }

            if (!this.isAllFramesWritten(framesWritten)) {

                int timeInterval = 450;
                time = System.currentTimeMillis() - time;
                if (time < timeInterval) {
                    sleep(timeInterval - time);
                }
            }
        }

        if (!result) {
            Log.d(TAG, "send blelink data to device failed");
            return false;
        }
        Log.d(TAG, "send blelink data to device succeed");

        Log.d(TAG, "send config_ack data to device");
        result = false;
        for (int i = 0; i < RETRY_MAX_TIMES; i++) {

            ble.write(BLE_CONFIG_ACK.getBytes());

            synchronized (linkingStatus) {

                try {
                    linkingStatus.wait(1000);
                } catch (InterruptedException e) {
                }

                if (linkingStatus.isCanceled()) {
                    throw new LinkingCanceledException();
                }

                if (Boolean.TRUE == linkingStatus.getData(LinkingStatus.KEY_CONFIG_BLE_ACK)) {
                    result = true;
                    break;
                }
            }
        }

        if (!result) {
            Log.d(TAG, "send config ack to device failed");
            return false;
        }
        
        
        
        */
        Log.d(TAG, "send config ack to device succeed");

        return true;
    }

    private LinkedModule smartLinkFind() throws LinkingCanceledException {

        linkingStatus.setProgress(LinkingProgress.FIND_DEVICE);
        LinkedModule linkedModule = null;

        try {

            mSmartConfigSocket = createMulticastSocket();

            byte[] buffer = new byte[1024];
            DatagramPacket pack = new DatagramPacket(buffer, buffer.length);

            while (isLinking) {

                try {

                    //send smart link find
                    byte[] data = SMART_LINK_FIND.getBytes();
                    try {
                        mSmartConfigSocket.send(new DatagramPacket(data, data.length,
                                InetAddress.getByName(getBroadcastAddress(context)), PORT_SEND_SMART_LINK_FIND));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    sleep(100);

                    mSmartConfigSocket.receive(pack);
                    byte[] bytes = new byte[pack.getLength()];
                    System.arraycopy(buffer, 0, bytes, 0, bytes.length);

                    if (bytes.length >= 25) {

                        boolean ignore = true;
                        for (int i = 0; i < bytes.length; i++) {
                            ignore = bytes[i] == 5;
                            if (!ignore) {
                                break;
                            }
                        }

                        if (!ignore) {
                            StringBuffer sb = new StringBuffer();
                            for (int i = 0; i < bytes.length; i++) {
                                sb.append((char)bytes[i]);
                            }

                            String result = sb.toString().trim();
                            String mac = null, ip = null, id = null;

                            Log.d(TAG, "Smart Link Find Received: " + result);

                            if (result.startsWith(SMART_CONFIG)) {

                                result = result.replace(SMART_CONFIG, "").trim();
                                String[] items = result.split("##");

                                if (items.length > 0) {

                                    mac = items[0].trim();
                                    id = items.length > 1 && !TextUtils.isEmpty(items[1].trim())? items[1].trim() : mac;
                                }
                            }else {

                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    id = jsonObject.optString("mid");
                                    mac = jsonObject.optString("mac");
                                    ip = jsonObject.optString("ip");
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

//                            if (!TextUtils.isEmpty(mac) && isSmartLinkFoundMatched(mac)) {
                            if (!TextUtils.isEmpty(mac)) {

                                if (TextUtils.isEmpty(id) || id.trim().isEmpty()) {
                                    id = mac;
                                }

                                if (TextUtils.isEmpty(ip)) {
                                    ip = pack.getAddress().getHostAddress();
                                }

                                linkedModule = new LinkedModule(mac, ip, id);
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.v(TAG, "smartLinkSocket.receive(pack) timeout");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (mSmartConfigSocket != null) {

                try {
                    mSmartConfigSocket.disconnect();
                    mSmartConfigSocket.close();
                    mSmartConfigSocket = null;
                }catch (Exception e) {
                }
            }
        }

        if (!isLinking) {
            throw new LinkingCanceledException("ap link task is canceled when smartlink find");
        }

        return linkedModule;
    }

    private byte[] getBytes(String text) {
        return TextUtils.isEmpty(text) ? new byte[0] : text.getBytes();
    }

    private MulticastSocket createMulticastSocket() throws IOException {

        MulticastSocket socket = new MulticastSocket(PORT_RECEIVE_SMART_CONFIG);
        socket.setSoTimeout(2000);

        NetworkInterface networkInterface = LinkerUtils.getNetworkInterface(getLocalIpAddress());
        if (networkInterface != null) {
            try {
                socket.setNetworkInterface(networkInterface);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        try {
//            socket.joinGroup(InetAddress.getByName("239.0.0.0"));
            if (networkInterface == null) {
                socket.joinGroup(InetAddress.getByName("239.0.0.0"));
            }else {
                socket.joinGroup(new InetSocketAddress(InetAddress.getByName("239.0.0.0"), PORT_RECEIVE_SMART_CONFIG), networkInterface);
            }
            socket.setLoopbackMode(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return socket;
    }

    private String getLocalIpAddress() {

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = 0;
        if (wifiInfo == null || (ipAddress = wifiInfo.getIpAddress()) == 0) {
            return null;
        }
        return LinkerUtils.calculateIpAddress(ipAddress);
    }

    private String getBroadcastAddress(Context ctx)  {
        WifiManager cm = (WifiManager) ctx
                .getSystemService(Context.WIFI_SERVICE);
        DhcpInfo myDhcpInfo = cm.getDhcpInfo();
        if (myDhcpInfo == null) {
            return "255.255.255.255";
        }
        int broadcast = (myDhcpInfo.ipAddress & myDhcpInfo.netmask)
                | ~myDhcpInfo.netmask;
        byte[] quads = new byte[4];
        for (int i = 0; i < 4; i++)
            quads[i] = (byte) ((broadcast >> i * 8) & 0xFF);
        try{
            return InetAddress.getByAddress(quads).getHostAddress();
        }catch(Exception e){
            return "255.255.255.255";
        }
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    private List<byte[]> getConfigFrames() throws Exception {
//
//        byte[] ssidBytes = this.getBytes(ssid);
//        byte[] passwordBytes = this.getBytes(password);
//        byte[] userDataBytes = this.getBytes(userData);
//        int length = ssidBytes.length + passwordBytes.length + userDataBytes.length + 4;
//
//        ByteBuffer buffer = ByteBuffer.allocate(length);
//        buffer.put((byte)(ssidBytes.length & 0xFF));
//        if (ssidBytes.length > 0) {
//            buffer.put(ssidBytes);
//        }
//        buffer.put((byte)(passwordBytes.length & 0xFF));
//        if (passwordBytes.length > 0) {
//            buffer.put(passwordBytes);
//        }
//        buffer.put((byte)(userDataBytes.length & 0xFF));
//        if (userDataBytes.length > 0) {
//            buffer.put(userDataBytes);
//        }
//        buffer.position(0);
//
//        byte[] toCrc = new byte[buffer.capacity() - 1];
//        buffer.get(toCrc);
//        byte crc = CRC.crc8Maxim(toCrc);
//        buffer.put(crc);
//
//        byte[] data = TeaEncryptor.encrypt(buffer.array(), teaEncryptionKey);
//        int dataLength = data.length;
//        int frameCount = dataLength/17;
//        if (dataLength % 17 != 0) {
//            frameCount++;
//        }
//        int position = 0;
//        List<byte[]> frames = new ArrayList<>();
//        for (int i = 0; i < frameCount; i++) {
//
//            int frameDataLength = Math.min(dataLength - position, 17);
//            byte[] frame = new byte[frameDataLength + 3];
//            frame[0] = (byte)((i + 1) & 0xFF);
//            frame[1] = (byte)(frameCount & 0xFF);
//            frame[2] = (byte)(frameDataLength & 0xFF);
//            System.arraycopy(data, position, frame, 3,  frameDataLength);
//            frames.add(frame);
//
//            Log.d(TAG, String.format("getConfigFrames: NO.%s->%s", i + 1, GTransformer.bytes2HexStringWithWhitespace(frame)));
//
//            position += frameDataLength;
//        }
//
//        return frames;
//    }

    private boolean isAllFramesWritten(boolean[] framesWritten) {

        for (Boolean written : framesWritten) {
            if (!written) {
                return false;
            }
        }

        return true;
    }
}
