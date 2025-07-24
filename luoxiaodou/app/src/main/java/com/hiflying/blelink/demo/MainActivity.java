package com.hiflying.blelink.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hiflying.blelink.LinkedModule;
import com.hiflying.blelink.LinkingError;
import com.hiflying.blelink.LinkingProgress;
import com.hiflying.blelink.OnLinkListener;
import com.hiflying.blelink.v1.BleLinker;
import com.luobotec.message.AppMessage;

import androidx.annotation.NonNull;
//import android.support.annotation.NonNull;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends Activity implements OnLinkListener {

    private static final String TAG = "BleLinker.MainActivity";
   // private static final String KEY_BLELINKER_BLE_NAME = "BleLinker_ble_name";
    private static final String KEY_BLELINKER_SSID_FORMAT = "BleLinker_ssid.%s";

    private EditText mSsidEditText;
    private EditText mPasswordEditText;
   // private EditText mBleNameEditText;
    private Button mLinkButton;  //连接蓝牙
    
    private Button mBtnWifiU;  //设置wifi
    private Button mBtnWifiD;   //断开wifi
    
    private Button mBtnMoveU;  //前进
    private Button mBtnMoveD;   //后退
    private Button mBtnMoveL;  //左转
    private Button mBtnMoveR;   //右转
    
    private Button mBtnMoveS;   //停止运动
    
    private Button mBtnFace;   //变脸  
    private Button mBtnWheel;   //转圈
    
   // private Button mBtnSing;   //唱歌   
    //private Button mBtnDance;   //跳舞
    private Button mBtnStory;   //讲故事   
    
    private TextView mMessageTextView;

    private BleLinker mBleLinker;
    private String mWifiSsid;
    private boolean mWifiConnected;
    private boolean mBluetoothEnabled;
    private ProgressDialog mProgressDialog;
    private ProgressDialog mCancelingDialog;
    private AlertDialog mAlertDialog;
    private SharedPreferences mSharedPreferences;
    
    
  //双轮分别控制
  	public static AppMessage.Request.Builder func(
  			AppMessage.RobotAction.ActionDirection leftDir,
  			AppMessage.RobotAction.ActionDirection rightDir, int leftSpeed,
  			int rightSpeed, int leftUnit, int rightUnit, boolean bAvoid,
  			boolean bReset) {
  		// 左脚
  		AppMessage.RobotAction.Builder leftBuild = AppMessage.RobotAction
  				.newBuilder();
  		leftBuild.setType(AppMessage.RobotAction.ActionType.LEFT_FOOT);
  		leftBuild.setDirection(leftDir);
  		leftBuild.setSpeed(leftSpeed);
  		leftBuild.setUnits(leftUnit);
  		// 右脚
  		AppMessage.RobotAction.Builder rightBuild = AppMessage.RobotAction
  				.newBuilder();
  		rightBuild.setType(AppMessage.RobotAction.ActionType.RIGHT_FOOT);
  		rightBuild.setDirection(rightDir);
  		rightBuild.setSpeed(rightSpeed);
  		rightBuild.setUnits(rightUnit);
  		// 动作集合
  		AppMessage.ActionBundle.Builder bBuild = AppMessage.ActionBundle
  				.newBuilder();
  		// 启动时间
  		bBuild.setStartTime(0);
  		bBuild.addAction(leftBuild.build());
  		bBuild.addAction(rightBuild.build());
  		// 动作参数 是否避障
  		AppMessage.ActionParams.Builder aBuild = AppMessage.ActionParams
  				.newBuilder();
  		aBuild.setAvoid(bAvoid);
  		if (bReset)
  			aBuild.setRest(bReset);
  		aBuild.addActions(bBuild.build());
  		// 动作请求包
  		AppMessage.Request.Builder requestBuilder = AppMessage.Request
  				.newBuilder();
  		requestBuilder
  				.setType(com.luobotec.message.AppMessage.MessageType.action);
  		requestBuilder.setActions(aBuild.build());
  		return (requestBuilder);
  	}
  	//双轮同时控制
  	public static AppMessage.Request.Builder func(
  			AppMessage.RobotAction.ActionDirection Dir,
  			AppMessage.RobotAction.ActionType type, int Speed, int Unit,
  			boolean bAvoid) {
  		// 动作
  		AppMessage.RobotAction.Builder leftBuild = AppMessage.RobotAction
  				.newBuilder();
  		leftBuild.setType(type);
  		leftBuild.setDirection(Dir);
  		leftBuild.setSpeed(Speed);
  		leftBuild.setUnits(Unit);

  		// 动作集合
  		AppMessage.ActionBundle.Builder bBuild = AppMessage.ActionBundle
  				.newBuilder();
  		// 启动时间
  		bBuild.setStartTime(0);
  		bBuild.addAction(leftBuild.build());
  		// 动作参数 是否避障
  		AppMessage.ActionParams.Builder aBuild = AppMessage.ActionParams
  				.newBuilder();
  		aBuild.setAvoid(bAvoid);

  		aBuild.addActions(bBuild.build());
  		// 动作请求包
  		AppMessage.Request.Builder requestBuilder = AppMessage.Request
  				.newBuilder();
  		requestBuilder
  				.setType(com.luobotec.message.AppMessage.MessageType.action);
  		requestBuilder.setActions(aBuild.build());
  		return (requestBuilder);
  	}
  	//双轮同时控制
  	public static AppMessage.Request.Builder func(
  			AppMessage.RobotAction.ActionDirection Dir, int Speed, int Unit,
  			boolean bAvoid) {
  		return func(Dir, AppMessage.RobotAction.ActionType.FEET, Speed, Unit, bAvoid);
  	}
  	
  	//双轮平衡车
  	public static AppMessage.Request.Builder func(AppMessage.MotionMode mode
  			) {	
  		// 动作
  		AppMessage.RobotAction.Builder leftBuild = AppMessage.RobotAction
  				.newBuilder();
  		leftBuild.setType(AppMessage.RobotAction.ActionType.MOTION_SWITCH);
  		leftBuild.setMotionMode(mode);    //AppMessage.MotionMode.TWO_WHEEL  AppMessage.MotionMode.THREE_WHEEL
  		// 动作集合
  		AppMessage.ActionBundle.Builder bBuild = AppMessage.ActionBundle
  				.newBuilder();
  		// 启动时间
  		bBuild.setStartTime(0);
  		bBuild.addAction(leftBuild.build());
  		// 动作参数 是否避障
  		AppMessage.ActionParams.Builder aBuild = AppMessage.ActionParams
  				.newBuilder();
  		aBuild.setAvoid(true);

  		aBuild.addActions(bBuild.build());
  		// 动作请求包
  		AppMessage.Request.Builder requestBuilder = AppMessage.Request
  				.newBuilder();
  		requestBuilder
  				.setType(com.luobotec.message.AppMessage.MessageType.action);
  		requestBuilder.setActions(aBuild.build());
  		return (requestBuilder);
  	}

  	// playalum  测试有问题
  	public static AppMessage.Request.Builder func_playalum(int alblumId, int resindex,
  			int playmod, boolean noAction) {
  		AppMessage.Request.Builder localBuilder = AppMessage.Request
  				.newBuilder();
  		localBuilder.setType(AppMessage.MessageType.play);
  		AppMessage.PlayAlbumParams.Builder localBuilder1 = AppMessage.PlayAlbumParams
  				.newBuilder();
  		localBuilder1.setAlbumId(alblumId);
  		localBuilder1.setResIndex(resindex);
  		localBuilder1.setNoAction(noAction);
  		localBuilder1.setPlayMode(playmod);
  		localBuilder.setPlayAblum(localBuilder1.build());
  		return (localBuilder);
  	}

  	// play  测试有问题
  	public static AppMessage.Request.Builder func_play(int alblumId, int mediaid,
  			AppMessage.PlayParams.Action paramAction, String paramString,
  			boolean noAction) {
  		AppMessage.Request.Builder localBuilder = AppMessage.Request
  				.newBuilder();
  		localBuilder.setType(AppMessage.MessageType.play);
  		AppMessage.PlayParams.Builder localBuilder1 = AppMessage.PlayParams
  				.newBuilder();
  		localBuilder1.setAction(paramAction);
  		localBuilder1.setAlbumId(alblumId);
  		if (mediaid > 0) {
  			localBuilder1.setMediaId(mediaid);
  		}
  		if (!paramString.isEmpty()) {
  			localBuilder1.setName(paramString);
  		}
  		localBuilder1.setNoAction(noAction);
  		localBuilder.setPlay(localBuilder1.build());
  		return (localBuilder);
  	}

  	public static AppMessage.Request.Builder func_update(String str) {
  		AppMessage.UpgradeParams.Builder bb = AppMessage.UpgradeParams
  				.newBuilder();
  		bb.setUpgradeInfo(str);
  		bb.setType(1);

  		AppMessage.Request.Builder requestBuilder = AppMessage.Request
  				.newBuilder();
  		requestBuilder
  				.setType(com.luobotec.message.AppMessage.MessageType.upgrade);
  		requestBuilder.setUpgrade(bb);
  		return (requestBuilder);
  	}
  	public static AppMessage.Request.Builder factory_test(com.luobotec.message.AppMessage.FactoryTestParams.Command command) {
  		AppMessage.FactoryTestParams.Builder fBuild = AppMessage.FactoryTestParams
  				.newBuilder();
  		// fBuild.setNluText("今天天气");
  		fBuild.setCommand(command);

  		AppMessage.Request.Builder requestBuilder = AppMessage.Request
  				.newBuilder();
  		requestBuilder
  				.setType(com.luobotec.message.AppMessage.MessageType.factory_test);
  		requestBuilder.setFactoryTestParams(fBuild);
  		return (requestBuilder);
  	}
  	public static AppMessage.Request.Builder func_takepic() {
  		// 拍照测试 不知是否 测试成功
  		AppMessage.Request.Builder requestBuilder = factory_test(com.luobotec.message.AppMessage.FactoryTestParams.Command.TAKE_PICTURE);
  		return (requestBuilder);
  	}

  	public static AppMessage.Request.Builder func_bluetooth() {
  		AppMessage.BluetoothParams.Builder kBuild = AppMessage.BluetoothParams
  				.newBuilder();
  		kBuild.setType(AppMessage.BluetoothParams.BluetoothType.CONNECT);
  		kBuild.setName("小萝卜");

  		AppMessage.Request.Builder requestBuilder = AppMessage.Request
  				.newBuilder();
  		requestBuilder
  				.setType(com.luobotec.message.AppMessage.MessageType.bluetooth);
  		requestBuilder.setBluetooth(kBuild.build());
  		return (requestBuilder);
  	}

  	//停止
  	public static AppMessage.Request.Builder move_stop() {
  		return func(AppMessage.RobotAction.ActionDirection.KEEP,
  				AppMessage.RobotAction.ActionDirection.KEEP, 0, 0, 0, 0, false,
  				true);
  	}

  	// 右转
  	public static AppMessage.Request.Builder move_right() {
  		return func(AppMessage.RobotAction.ActionDirection.UP,
  				AppMessage.RobotAction.ActionType.LEFT_FOOT, 4, 1, false);
  	}

  	// 左转
  	public static AppMessage.Request.Builder move_left() {
  		return func(AppMessage.RobotAction.ActionDirection.UP,
  				AppMessage.RobotAction.ActionType.RIGHT_FOOT, 4, 1, false);
  	}

  	// 后退
  	public static AppMessage.Request.Builder move_back() {
  		return func(AppMessage.RobotAction.ActionDirection.DOWN, 4, 1, true);
  	}

  	// 前进
  	public static AppMessage.Request.Builder move_front() {
  		return func(AppMessage.RobotAction.ActionDirection.UP, 4, 1, true);
  	}
  	//测试有问题
  	public static AppMessage.Request.Builder func_a(int alblumId, int resindex) {
  		return func_playalum(alblumId, resindex, 1, false);

  	}
  	//测试有问题
  	public static AppMessage.Request.Builder func_x() {
  		return func_play(0, -1, AppMessage.PlayParams.Action.PLAY_ALL, "", true);

  	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSharedPreferences = getSharedPreferences(TAG, MODE_PRIVATE);
        mBleLinker = BleLinker.getInstance(this);

        if (!checkBleProvider()) {
            return;
        }

        setContentView(R.layout.activity_main);
        setupViews();

        mBleLinker.init(this);
        mBleLinker.setOnLinkListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

//        checkLocationProvider();
        MainActivityPermissionsDispatcher.requestPermissionsWithPermissionCheck(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBleLinker.destroy();
    }
    private void setupViews() {

        mSsidEditText = (EditText) findViewById(R.id.ssid);
        mPasswordEditText = (EditText) findViewById(R.id.password);
      //  mBleNameEditText = (EditText) findViewById(R.id.ble_name);
        mMessageTextView = (TextView) findViewById(R.id.message);
        
        mLinkButton = (Button) findViewById(R.id.link);
        mLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                String ssid = mSsidEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
           //     String bleName = mBleNameEditText.getText().toString();

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(String.format(KEY_BLELINKER_SSID_FORMAT, ssid), password);
           //     editor.putString(KEY_BLELINKER_BLE_NAME, bleName);
                editor.commit();

//                mBleLinker.setSsid(ssid);
//                mBleLinker.setPassword(password);
            //    mBleLinker.setBleName(bleName);
                try {
                    mBleLinker.start();
                    mLinkButton.setEnabled(false);

                    clearMessage();
                 //   String text = String.format("Start Ble Link\n  ssid: \"%s\"\n  password: \"%s\"\n  ", ssid, password);
                  //  updateMessage(text);;
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlertDialog("Start Failed!");
                }
            }
        });
        
        //wifi配置
        mBtnWifiU = (Button) findViewById(R.id.btnWifi);
        mBtnWifiU.setOnClickListener(new Button.OnClickListener(){ 
            public void onClick(View v) {    
                //构建数据包
                AppMessage.WifiParams.Builder mybuild= AppMessage.WifiParams.newBuilder();
                
                String ssid = mSsidEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                
                mybuild.setName(ssid);
                mybuild.setPwd(password);
                AppMessage.WifiParams wifipara = mybuild.build();
                AppMessage.Request.Builder requestBuilder = AppMessage.Request.newBuilder();
                requestBuilder.setId(1);
                requestBuilder.setType(com.luobotec.message.AppMessage.MessageType.wifi);
                requestBuilder.setWifi(wifipara);
                
               // String str = ssid + "---" + password;
               // Toast.makeText(MainActivity, str, Toast.LENGTH_SHORT).show(); 
               // updateMessage(str);
                
                
                mBleLinker.SendMsg(requestBuilder);
            }    
   
        });
        //wifi断开
        mBtnWifiD = (Button) findViewById(R.id.BtnWifiD);
        mBtnWifiD.setOnClickListener(new Button.OnClickListener(){ 
            public void onClick(View v) { 
            	AppMessage.Request.Builder requestBuilder = factory_test(com.luobotec.message.AppMessage.FactoryTestParams.Command.FORGET_WIFI);
                mBleLinker.SendMsg(requestBuilder);  
            }    
   
        });
        
        //前进
        mBtnMoveU = (Button) findViewById(R.id.BtnMoveU);
        mBtnMoveU.setOnClickListener(new Button.OnClickListener(){ 
            public void onClick(View v) {    
                //点击了move
            	AppMessage.Request.Builder requestBuilder = move_front();
            	mBleLinker.SendMsg(requestBuilder);
            }    
        });
        //后退
        mBtnMoveD = (Button) findViewById(R.id.btnMoveD);
        mBtnMoveD.setOnClickListener(new Button.OnClickListener(){ 
            public void onClick(View v) {    
                //点击了move
            	AppMessage.Request.Builder requestBuilder = move_back();
            	mBleLinker.SendMsg(requestBuilder);
            }    
        });
        //左转
        mBtnMoveL = (Button) findViewById(R.id.BtnMoveL);
        mBtnMoveL.setOnClickListener(new Button.OnClickListener(){ 
            public void onClick(View v) {    
                //点击了move
            	AppMessage.Request.Builder requestBuilder = move_left();
            	mBleLinker.SendMsg(requestBuilder);
            }    
        });
        //右转
        mBtnMoveR = (Button) findViewById(R.id.BtnMoveR);
        mBtnMoveR.setOnClickListener(new Button.OnClickListener(){ 
            public void onClick(View v) {    
                //点击了move
            	AppMessage.Request.Builder requestBuilder = move_right();
            	mBleLinker.SendMsg(requestBuilder);
            }    
        });
        
        mBtnMoveS = (Button) findViewById(R.id.BtnStop);
        mBtnMoveS.setOnClickListener(new Button.OnClickListener(){ 
            public void onClick(View v) {    
                //点击了move
            	AppMessage.Request.Builder requestBuilder = move_stop();
            	mBleLinker.SendMsg(requestBuilder);
            }    
        });
         
        
        
        
        //变脸
        mBtnFace = (Button) findViewById(R.id.BtnFace);
        mBtnFace.setOnClickListener(new Button.OnClickListener(){ 
            public void onClick(View v) { 
            	AppMessage.Request.Builder requestBuilder = factory_test(com.luobotec.message.AppMessage.FactoryTestParams.Command.EMOTION);
                mBleLinker.SendMsg(requestBuilder);  
    	        
            }    
        });
        //wheel
        mBtnWheel = (Button) findViewById(R.id.btnWheel);
        mBtnWheel.setOnClickListener(new Button.OnClickListener(){ 
            public void onClick(View v) { 
                //点击了move
            	AppMessage.Request.Builder requestBuilder = func(AppMessage.MotionMode.TWO_WHEEL);
            	mBleLinker.SendMsg(requestBuilder); 
    	        
            }    
        });
//        //唱歌
//        mBtnSing = (Button) findViewById(R.id.BtnSing);
//        mBtnSing.setOnClickListener(new Button.OnClickListener(){ 
//            public void onClick(View v) { 
//            	
//            	AppMessage.Request.Builder requestBuilder = AppMessage.Request.newBuilder();
//      	        requestBuilder.setId(1);
//    	        AppMessage.FactoryTestParams.Builder fBuild = AppMessage.FactoryTestParams.newBuilder();
//    	        fBuild.setNluText("唱歌");
//    	        //fBuild.setCommand(com.luobotec.message.AppMessage.FactoryTestParams.Command.EMOTION);
//    	        requestBuilder.setType(com.luobotec.message.AppMessage.MessageType.factory_test);
//    	        requestBuilder.setFactoryTestParams(fBuild);
//                mBleLinker.SendMsg(requestBuilder);  
//    	        
//            }    
//        });
//        //跳舞
//        mBtnDance = (Button) findViewById(R.id.BtnDance);
//        mBtnDance.setOnClickListener(new Button.OnClickListener(){ 
//            public void onClick(View v) { 
//            	
//            	AppMessage.Request.Builder requestBuilder = AppMessage.Request.newBuilder();
//      	        requestBuilder.setId(1);
//    	        AppMessage.FactoryTestParams.Builder fBuild = AppMessage.FactoryTestParams.newBuilder();
//    	        fBuild.setNluText("跳舞");
//    	        //fBuild.setCommand(com.luobotec.message.AppMessage.FactoryTestParams.Command.EMOTION);
//    	        requestBuilder.setType(com.luobotec.message.AppMessage.MessageType.factory_test);
//    	        requestBuilder.setFactoryTestParams(fBuild);
//                mBleLinker.SendMsg(requestBuilder);  
//    	        
//            }    
//        });
        //讲故事
        mBtnStory = (Button) findViewById(R.id.BtnStory);
        mBtnStory.setOnClickListener(new Button.OnClickListener(){ 
            public void onClick(View v) { 
            	
            	AppMessage.Request.Builder requestBuilder = AppMessage.Request.newBuilder();
      	        requestBuilder.setId(1);
    	        AppMessage.FactoryTestParams.Builder fBuild = AppMessage.FactoryTestParams.newBuilder();
    	        fBuild.setNluText("讲故事");
    	        //fBuild.setCommand(com.luobotec.message.AppMessage.FactoryTestParams.Command.EMOTION);
    	        requestBuilder.setType(com.luobotec.message.AppMessage.MessageType.factory_test);
    	        requestBuilder.setFactoryTestParams(fBuild);
                mBleLinker.SendMsg(requestBuilder);  
    	        
            }    
        });
        setEditTextWithSharedPreferences();

        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(!mBleLinker.isLinking() && mWifiConnected && mBluetoothEnabled)
				{
                    mLinkButton.setEnabled(true);
				
                }
					
					
					
                else {
                    mLinkButton.setEnabled(false);
					
					
										
					  // if (  !mSsidEditText.getText().toString().isEmpty() && !mPasswordEditText.getText().toString().isEmpty())
					 // {  // 
							// mBtnWifiU.setEnabled(true);
					 // }
					 // else {
						// mBtnWifiU.setEnabled(false);
					// }
                }
				

            }
        };

        mSsidEditText.addTextChangedListener(textWatcher);
        mPasswordEditText.addTextChangedListener(textWatcher);

        mCancelingDialog = new ProgressDialog(this);
        mCancelingDialog.setMessage(getString(R.string.blelinker_canceling));
        mCancelingDialog.setCanceledOnTouchOutside(false);
        mCancelingDialog.setCancelable(false);

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                ((TextView)findViewById(R.id.version)).setText("version: " + packageInfo.versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @NeedsPermission({"android.permission.ACCESS_WIFI_STATE", "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION", "android.permission.INTERNET", "android.permission.WAKE_LOCK",
            "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN"})
    public void requestPermissions() {
        Log.d(TAG, "requestPermissions: ");
        if (mWifiConnected==true){
            mWifiSsid= mBleLinker.getWifiSSID();
            Log.d(TAG, "requestPermissions: mWifiSsid="+mWifiSsid);
        }
        enableLinkUI(mWifiConnected && mBluetoothEnabled);
    }

    @OnPermissionDenied({"android.permission.ACCESS_WIFI_STATE", "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION", "android.permission.INTERNET", "android.permission.WAKE_LOCK",
            "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN"})
    public void showPermissionDenied() {
        showAlertDialog(getString(R.string.blelinker_permission_denied));
    }


    @OnNeverAskAgain({"android.permission.ACCESS_WIFI_STATE", "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION", "android.permission.INTERNET", "android.permission.WAKE_LOCK",
            "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN"})
    public void onPermissionNeverAskAgain() {
        showAlertDialog(getString(R.string.blelinker_permission_denied));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void setEditTextWithSharedPreferences() {
        mPasswordEditText.setText(mSharedPreferences.getString(String.format(KEY_BLELINKER_SSID_FORMAT, mSsidEditText.getText().toString()), null));
      //  mBleNameEditText.setText(mSharedPreferences.getString(KEY_BLELINKER_BLE_NAME, BleLinker.BLE_NAME_HIFLYING));
    }

    @Override
    public void onWifiConnectivityChanged(boolean connected, String ssid, WifiInfo wifiInfo) {
        Log.d(TAG, String.format("onWifiConnectivityChanged: connected-%s ssid-%s", connected, ssid));

        mWifiConnected = connected;
        mWifiSsid = ssid;
        enableLinkUI(mWifiConnected && mBluetoothEnabled);
    }

    @Override
    public void onBluetoothEnabledChanged(boolean enabled) {
        Log.d(TAG, "onBluetoothEnabledChanged: " + enabled);

        mBluetoothEnabled = enabled;
        enableLinkUI(mWifiConnected && mBluetoothEnabled);
    }

    @Override
    public void onModuleLinked(LinkedModule module) {
        Log.i(TAG, "onModuleLinked: " + module);

        updateMessage("onModuleLinked: " + module.getMac());
       // showAlertDialog("linked module: " + JSON.toJSONString(module));
    }

    @Override
    public void onFinished() {
        Log.i(TAG, "onFinished");

      //  updateMessage("onFinished");
        dismissProgressDialog();
        if (mCancelingDialog.isShowing()) {
            mCancelingDialog.dismiss();
        }
        mLinkButton.setEnabled(true);
    }

    @Override
    public void onModuleLinkTimeOut() {
        Log.i(TAG, "onModuleLinkTimeOut");

        updateMessage("onModuleLinkTimeOut");
        showAlertDialog("TIME OUT");
    }

    @Override
    public void onError(LinkingError error) {
        Log.i(TAG, "onError: " + error);

        updateMessage("onError: " + error);
        dismissProgressDialog();
        showAlertDialog(error.name());
    }

    @Override
    public void onProgress(LinkingProgress progress) {
        Log.i(TAG, "onProgress: " + progress);
        
        if (progress == LinkingProgress.FIND_DEVICE)
        {//找到设备
        	mBtnMoveU.setEnabled(true);
        	mBtnMoveD.setEnabled(true);    
        	mBtnMoveL.setEnabled(true);
        	mBtnMoveR.setEnabled(true);  
        	mBtnMoveS.setEnabled(true);  
        	
        	mBtnWifiU.setEnabled(true); 	
        	mBtnWifiD.setEnabled(true);
        	
        	mBtnFace.setEnabled(true);
        	mBtnWheel.setEnabled(true);        	
        	//mBtnSing.setEnabled(true);
        	//mBtnDance.setEnabled(true);
        	//mBtnStory.setEnabled(true);
        }

       // updateMessage("onProgress: " + progress);
        showProgressDialog(progress.name());
    }

    private void enableLinkUI(boolean enabled) {

        if (enabled) {

            mSsidEditText.setText(mWifiSsid);
            mPasswordEditText.setText(mSharedPreferences.getString(String.format(KEY_BLELINKER_SSID_FORMAT, mWifiSsid), null));
        //    mBleNameEditText.setText(mSharedPreferences.getString(KEY_BLELINKER_BLE_NAME, BleLinker.BLE_NAME_HIFLYING));

		//	mBtnWifiU.setEnabled(true);
			mSsidEditText.setEnabled(true);
            mPasswordEditText.setEnabled(true);
       //    mBleNameEditText.setEnabled(true);
            if(mSsidEditText.getText().toString().isEmpty()) {  // || mBleNameEditText.getText().toString().trim().isEmpty()
                mBtnWifiU.setEnabled(false);
            }else {
               // mBtnWifiU.setEnabled(true);
            }

//            if (mMessageTextView.getText().toString().equals(getString(R.string.blelinker_no_valid_wifi_connection))) {
//            if (!mBleLinker.isLinking()) {
//                clearMessage();
//            }
        }else {

            mSsidEditText.setText(null);
            mPasswordEditText.setText(null);
          //  mBleNameEditText.setText(null);
		  
		  	mSsidEditText.setEnabled(false);
            mPasswordEditText.setEnabled(false);
			
			 mBtnWifiU.setEnabled(false);
         //   mBleNameEditText.setEnabled(false);
            mLinkButton.setEnabled(false);

//            if (!mBleLinker.isLinking()) {
//
//                clearMessage();
//
//                if (!mWifiConnected) {
//                    updateMessage(getString(R.string.blelinker_no_valid_wifi_connection));
//                }
//
//                if (!mBluetoothEnabled) {
//                    updateMessage(getString(R.string.blelinker_bluetooth_disabled));
//                }
//            }
        }

        if (!mBleLinker.isLinking()) {

            clearMessage();

            if(!enabled) {

                if (!mWifiConnected) {
                    updateMessage(getString(R.string.blelinker_no_valid_wifi_connection));
                }

                if (!mBluetoothEnabled) {
                    updateMessage(getString(R.string.blelinker_bluetooth_disabled));
                }
            }
        }
    }

    private void showProgressDialog(String message) {

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle(R.string.blelinker_app_name);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mBleLinker.stop();
                    mCancelingDialog.show();
                }
            });
//            mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    mBleLinker.stop();
//                }
//            });
        }
        mProgressDialog.setMessage(message);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    private void showAlertDialog(String message) {

        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.blelinker_app_name)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }
        mAlertDialog.setMessage(message);
        if (!mAlertDialog.isShowing()) {
            mAlertDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void clearMessage() {

        mMessageTextView.setText(null);
    }

    private void updateMessage(String message) {

        mMessageTextView.setText(mMessageTextView.getText().toString().concat("\n").concat(message));
    }

    private boolean checkBleProvider() {

        if (mBleLinker.isBleSupported()) {
            return true;
        }else {
            showSimpleDialog(R.string.blelinker_ble_not_supported, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    finish();
                }
            });
            return false;
        }
    }

    private void checkLocationProvider() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            LocationManager locManager = (LocationManager)getSystemService(LOCATION_SERVICE);
            if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                Log.w(TAG, String.format("The android version sdk is %s and its location provider is disabled!", Build.VERSION.SDK_INT));

                new AlertDialog.Builder(this)
                        .setTitle(R.string.blelinker_app_name)
                        .setMessage(R.string.blelinker_location_prodiver_disabled)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent =  new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        })
                        .create()
                        .show();
            }
        }
    }

    private void showSimpleDialog(int messageResId, DialogInterface.OnClickListener onClickListener) {

        new AlertDialog.Builder(this)
                .setTitle(R.string.blelinker_app_name)
                .setMessage(messageResId)
                .setPositiveButton(android.R.string.ok, onClickListener)
                .setCancelable(false)
                .create()
                .show();
    }

    private void showSimpleDialog(int messageResId) {
        showSimpleDialog(messageResId, null);
    }
}
