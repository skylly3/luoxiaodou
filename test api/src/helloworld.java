import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.protobuf.InvalidProtocolBufferException;
import com.luobotec.message.AppMessage;

public class helloworld {

	//双轮分别控制
	public static AppMessage.Request func(
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
		requestBuilder.setId(1);
		requestBuilder
				.setType(com.luobotec.message.AppMessage.MessageType.action);
		requestBuilder.setActions(aBuild.build());
		return (requestBuilder.build());
	}
	//双轮同时控制
	public static AppMessage.Request func(
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
		requestBuilder.setId(1);
		requestBuilder
				.setType(com.luobotec.message.AppMessage.MessageType.action);
		requestBuilder.setActions(aBuild.build());
		return (requestBuilder.build());
	}
	//双轮同时控制
	public static AppMessage.Request func(
			AppMessage.RobotAction.ActionDirection Dir, int Speed, int Unit,
			boolean bAvoid) {
		return func(Dir, AppMessage.RobotAction.ActionType.FEET, Speed, Unit, bAvoid);
	}
	
	//双轮平衡车
	public static AppMessage.Request func(AppMessage.MotionMode mode
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
		requestBuilder.setId(1);
		requestBuilder
				.setType(com.luobotec.message.AppMessage.MessageType.action);
		requestBuilder.setActions(aBuild.build());
		return (requestBuilder.build());
	}

	// playalum  测试有问题
	public static AppMessage.Request func_playalum(int alblumId, int resindex,
			int playmod, boolean noAction) {
		AppMessage.Request.Builder localBuilder = AppMessage.Request
				.newBuilder();
		localBuilder.setId(1);
		localBuilder.setType(AppMessage.MessageType.play);
		AppMessage.PlayAlbumParams.Builder localBuilder1 = AppMessage.PlayAlbumParams
				.newBuilder();
		localBuilder1.setAlbumId(alblumId);
		localBuilder1.setResIndex(resindex);
		localBuilder1.setNoAction(noAction);
		localBuilder1.setPlayMode(playmod);
		localBuilder.setPlayAblum(localBuilder1.build());
		return (localBuilder.build());
	}

	// play  测试有问题
	public static AppMessage.Request func_play(int alblumId, int mediaid,
			AppMessage.PlayParams.Action paramAction, String paramString,
			boolean noAction) {
		AppMessage.Request.Builder localBuilder = AppMessage.Request
				.newBuilder();
		localBuilder.setId(1);
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
		return (localBuilder.build());
	}

	public static AppMessage.Request func_update(String str) {
		AppMessage.UpgradeParams.Builder bb = AppMessage.UpgradeParams
				.newBuilder();
		bb.setUpgradeInfo(str);
		bb.setType(1);

		AppMessage.Request.Builder requestBuilder = AppMessage.Request
				.newBuilder();
		requestBuilder.setId(1);
		requestBuilder
				.setType(com.luobotec.message.AppMessage.MessageType.upgrade);
		requestBuilder.setUpgrade(bb);
		return (requestBuilder.build());
	}

	public static AppMessage.Request func_takepic() {
		// 拍照测试 不知是否 测试成功
		AppMessage.FactoryTestParams.Builder fBuild = AppMessage.FactoryTestParams
				.newBuilder();
	
		fBuild.setCommand(com.luobotec.message.AppMessage.FactoryTestParams.Command.TAKE_PICTURE);

		AppMessage.Request.Builder requestBuilder = AppMessage.Request
				.newBuilder();
		requestBuilder.setId(1);
		requestBuilder
				.setType(com.luobotec.message.AppMessage.MessageType.factory_test);
		requestBuilder.setFactoryTestParams(fBuild);
		return (requestBuilder.build());
	}
	
	public static AppMessage.Request func_nlu(String nlu) {
		// 拍照测试 不知是否 测试成功
		AppMessage.FactoryTestParams.Builder fBuild = AppMessage.FactoryTestParams
				.newBuilder();
		fBuild.setNluText(nlu);  	// fBuild.setNluText("今天天气");

		AppMessage.Request.Builder requestBuilder = AppMessage.Request
				.newBuilder();
		requestBuilder.setId(1);
		requestBuilder
				.setType(com.luobotec.message.AppMessage.MessageType.factory_test);
		requestBuilder.setFactoryTestParams(fBuild);
		return (requestBuilder.build());
	}

	public static AppMessage.Request func_bluetooth() {
		AppMessage.BluetoothParams.Builder kBuild = AppMessage.BluetoothParams
				.newBuilder();
		kBuild.setType(AppMessage.BluetoothParams.BluetoothType.CONNECT);
		kBuild.setName("小萝卜");

		AppMessage.Request.Builder requestBuilder = AppMessage.Request
				.newBuilder();
		requestBuilder.setId(1);
		requestBuilder
				.setType(com.luobotec.message.AppMessage.MessageType.bluetooth);
		requestBuilder.setBluetooth(kBuild.build());
		return (requestBuilder.build());
	}

	//停止
	public static AppMessage.Request move_stop() {
		return func(AppMessage.RobotAction.ActionDirection.KEEP,
				AppMessage.RobotAction.ActionDirection.KEEP, 0, 0, 0, 0, false,
				true);
	}

	// 右转
	public static AppMessage.Request move_right() {
		return func(AppMessage.RobotAction.ActionDirection.UP,
				AppMessage.RobotAction.ActionType.LEFT_FOOT, 4, 1, false);
	}

	// 左转
	public static AppMessage.Request move_left() {
		return func(AppMessage.RobotAction.ActionDirection.UP,
				AppMessage.RobotAction.ActionType.RIGHT_FOOT, 4, 1, false);
	}

	// 后退
	public static AppMessage.Request move_back() {
		return func(AppMessage.RobotAction.ActionDirection.DOWN, 4, 1, true);
	}

	// 前进
	public static AppMessage.Request move_front() {
		return func(AppMessage.RobotAction.ActionDirection.UP, 4, 1, true);
	}
	//测试有问题
	public static AppMessage.Request func_a(int alblumId, int resindex) {
		return func_playalum(alblumId, resindex, 1, false);

	}
	//测试有问题
	public static AppMessage.Request func_x() {
		return func_play(0, -1, AppMessage.PlayParams.Action.PLAY_ALL, "", true);

	}

	// 测试有问题
	public static AppMessage.Request func_wifi(String ssid, String pwd) {
		// 构建wifi数据包
		AppMessage.WifiParams.Builder mybuild = AppMessage.WifiParams
				.newBuilder();
		mybuild.setName(ssid);
		mybuild.setPwd(pwd);

		AppMessage.Request.Builder requestBuilder = AppMessage.Request
				.newBuilder();
		requestBuilder.setId(1);
		requestBuilder
				.setType(com.luobotec.message.AppMessage.MessageType.wifi);
		requestBuilder.setWifi(mybuild);
		return (requestBuilder.build());

	}
	
	public static byte[] gzip(byte[] content) throws IOException{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		GZIPOutputStream gos=new GZIPOutputStream(baos);
		
		ByteArrayInputStream bais=new ByteArrayInputStream(content);
		byte[ ] buffer=new byte[1024];
		int n;
		while((n=bais.read(buffer))!=-1){
			gos.write(buffer, 0, n);
		}
		gos.flush();
		gos.close();
		return baos.toByteArray();
	}
	
	public static byte[] unGzip(byte[] content) throws IOException{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		GZIPInputStream gis=new GZIPInputStream(new ByteArrayInputStream(content));
		byte[] buffer=new byte[1024];
		int n;
		while((n=gis.read(buffer))!=-1){
			baos.write(buffer, 0, n);
		}
		
		return baos.toByteArray();
	}
	
	public static String byteToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length);
        String sTemp;
        for (int i = 0; i < bytes.length; i++) {
            sTemp = Integer.toHexString(0xFF & bytes[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
	}
 
	public static byte toByte(char c){
		 final String hexString = "0123456789ABCDEF";
		  byte b = (byte) hexString.indexOf(c);
		  return b;
		 }
	/**
	 * convert HexString to byte[]
	 * 
	 * @param hex
	 * @return
	 */
	public static byte[] hexStringToByte(String hex)
	{
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
 
		for (int i = 0; i < len; i++)
		{
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
 
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Hello World!");



		// 发送音量消息 测试失败
		// requestBuilder.setType(com.luobotec.message.AppMessage.MessageType.volume);
		// requestBuilder.setVolume(com.luobotec.message.AppMessage.VolumeType.MINUS);

		AppMessage.Request request = func_nlu("今天天气");      //func_takepic(); //func_wifi("play","12345678");
		// 转换成字节数组
		byte[] byteArray = request.toByteArray();
		// 打包
		final Base64.Encoder encoder = Base64.getEncoder();
		final String encodedText = "MSG:" + encoder.encodeToString(byteArray)
				+ "@";
		System.out.println(encodedText);
		
		
		
//		String str="hello world";
//		
//		byte[ ] bytes=str.getBytes();
//		System.out.println("压缩前长度："+bytes.length);
//		byte[ ] gzipBytes=gzip(bytes);
//		System.out.println("压缩后长度："+gzipBytes.length);
//		System.out.println("压缩后："+byteToHexString(gzipBytes));
		
		
//		byte [] gzipBytes = hexStringToByte("7d99ae532f2f2b55b7af534fcb4a4c50b737d5544f4c2853b7572f28cdcc4a49542d2b484b4b282b53afa8e00705269808da");
//		
//		
//		byte[ ] unGzipBytes=unGzip(gzipBytes);
//		System.out.println("解压后："+byteToHexString(unGzipBytes));
//		
//		String strOUt = new String(unGzipBytes);
//		System.out.println("解压后："+strOUt);
 
		

//		final Base64.Decoder decoder = Base64.getDecoder();
//		byte bbc[] = decoder.decode("GK0FIAA=");
//
//		AppMessage.Response response = AppMessage.Response.getDefaultInstance();
//		try {
//			response = AppMessage.Response.parseFrom(bbc);
//		} catch (InvalidProtocolBufferException paramString) {
//			paramString.printStackTrace();
//			paramString = null;
//		}
//		if (response.hasRobotState()) {
//			AppMessage.RobotState paramRobotState = response.getRobotState();
//			if (paramRobotState.hasWifiState()) {
//				// return new f();
//			}
//			if (paramRobotState.hasPlayState()) {
//				// return new c();
//			}
//			if (paramRobotState.hasRobotInfo()) {
//				// return new d();
//			}
//			if (paramRobotState.hasAvchatState()) {
//				// return new a();
//			}
//			if (paramRobotState.hasMotionMode()) {
//				// return new b();
//			}
//			// return null;
//			System.out.println("111");
//		} else if (response.hasUpgradeParams()) {
//			AppMessage.UpgradeParams paramRobotState = response
//					.getUpgradeParams();
//			System.out.println("222");
//		} else {
//			System.out.println(response.getType());
//			System.out.println(response.getErrCode());
//		
//			// System.out.println("err");
//		}

	}
}
