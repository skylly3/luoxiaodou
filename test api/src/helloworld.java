import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.protobuf.InvalidProtocolBufferException;
import com.luobotec.message.AppMessage;

public class helloworld {

	//˫�ֱַ����
	public static AppMessage.Request func(
			AppMessage.RobotAction.ActionDirection leftDir,
			AppMessage.RobotAction.ActionDirection rightDir, int leftSpeed,
			int rightSpeed, int leftUnit, int rightUnit, boolean bAvoid,
			boolean bReset) {
		// ���
		AppMessage.RobotAction.Builder leftBuild = AppMessage.RobotAction
				.newBuilder();
		leftBuild.setType(AppMessage.RobotAction.ActionType.LEFT_FOOT);
		leftBuild.setDirection(leftDir);
		leftBuild.setSpeed(leftSpeed);
		leftBuild.setUnits(leftUnit);
		// �ҽ�
		AppMessage.RobotAction.Builder rightBuild = AppMessage.RobotAction
				.newBuilder();
		rightBuild.setType(AppMessage.RobotAction.ActionType.RIGHT_FOOT);
		rightBuild.setDirection(rightDir);
		rightBuild.setSpeed(rightSpeed);
		rightBuild.setUnits(rightUnit);
		// ��������
		AppMessage.ActionBundle.Builder bBuild = AppMessage.ActionBundle
				.newBuilder();
		// ����ʱ��
		bBuild.setStartTime(0);
		bBuild.addAction(leftBuild.build());
		bBuild.addAction(rightBuild.build());
		// �������� �Ƿ����
		AppMessage.ActionParams.Builder aBuild = AppMessage.ActionParams
				.newBuilder();
		aBuild.setAvoid(bAvoid);
		if (bReset)
			aBuild.setRest(bReset);
		aBuild.addActions(bBuild.build());
		// ���������
		AppMessage.Request.Builder requestBuilder = AppMessage.Request
				.newBuilder();
		requestBuilder.setId(1);
		requestBuilder
				.setType(com.luobotec.message.AppMessage.MessageType.action);
		requestBuilder.setActions(aBuild.build());
		return (requestBuilder.build());
	}
	//˫��ͬʱ����
	public static AppMessage.Request func(
			AppMessage.RobotAction.ActionDirection Dir,
			AppMessage.RobotAction.ActionType type, int Speed, int Unit,
			boolean bAvoid) {
		// ����
		AppMessage.RobotAction.Builder leftBuild = AppMessage.RobotAction
				.newBuilder();
		leftBuild.setType(type);
		leftBuild.setDirection(Dir);
		leftBuild.setSpeed(Speed);
		leftBuild.setUnits(Unit);

		// ��������
		AppMessage.ActionBundle.Builder bBuild = AppMessage.ActionBundle
				.newBuilder();
		// ����ʱ��
		bBuild.setStartTime(0);
		bBuild.addAction(leftBuild.build());
		// �������� �Ƿ����
		AppMessage.ActionParams.Builder aBuild = AppMessage.ActionParams
				.newBuilder();
		aBuild.setAvoid(bAvoid);

		aBuild.addActions(bBuild.build());
		// ���������
		AppMessage.Request.Builder requestBuilder = AppMessage.Request
				.newBuilder();
		requestBuilder.setId(1);
		requestBuilder
				.setType(com.luobotec.message.AppMessage.MessageType.action);
		requestBuilder.setActions(aBuild.build());
		return (requestBuilder.build());
	}
	//˫��ͬʱ����
	public static AppMessage.Request func(
			AppMessage.RobotAction.ActionDirection Dir, int Speed, int Unit,
			boolean bAvoid) {
		return func(Dir, AppMessage.RobotAction.ActionType.FEET, Speed, Unit, bAvoid);
	}
	
	//˫��ƽ�⳵
	public static AppMessage.Request func(AppMessage.MotionMode mode
			) {	
		// ����
		AppMessage.RobotAction.Builder leftBuild = AppMessage.RobotAction
				.newBuilder();
		leftBuild.setType(AppMessage.RobotAction.ActionType.MOTION_SWITCH);
		leftBuild.setMotionMode(mode);    //AppMessage.MotionMode.TWO_WHEEL  AppMessage.MotionMode.THREE_WHEEL
		// ��������
		AppMessage.ActionBundle.Builder bBuild = AppMessage.ActionBundle
				.newBuilder();
		// ����ʱ��
		bBuild.setStartTime(0);
		bBuild.addAction(leftBuild.build());
		// �������� �Ƿ����
		AppMessage.ActionParams.Builder aBuild = AppMessage.ActionParams
				.newBuilder();
		aBuild.setAvoid(true);

		aBuild.addActions(bBuild.build());
		// ���������
		AppMessage.Request.Builder requestBuilder = AppMessage.Request
				.newBuilder();
		requestBuilder.setId(1);
		requestBuilder
				.setType(com.luobotec.message.AppMessage.MessageType.action);
		requestBuilder.setActions(aBuild.build());
		return (requestBuilder.build());
	}

	// playalum  ����������
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

	// play  ����������
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
		// ���ղ��� ��֪�Ƿ� ���Գɹ�
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
		// ���ղ��� ��֪�Ƿ� ���Գɹ�
		AppMessage.FactoryTestParams.Builder fBuild = AppMessage.FactoryTestParams
				.newBuilder();
		fBuild.setNluText(nlu);  	// fBuild.setNluText("��������");

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
		kBuild.setName("С�ܲ�");

		AppMessage.Request.Builder requestBuilder = AppMessage.Request
				.newBuilder();
		requestBuilder.setId(1);
		requestBuilder
				.setType(com.luobotec.message.AppMessage.MessageType.bluetooth);
		requestBuilder.setBluetooth(kBuild.build());
		return (requestBuilder.build());
	}

	//ֹͣ
	public static AppMessage.Request move_stop() {
		return func(AppMessage.RobotAction.ActionDirection.KEEP,
				AppMessage.RobotAction.ActionDirection.KEEP, 0, 0, 0, 0, false,
				true);
	}

	// ��ת
	public static AppMessage.Request move_right() {
		return func(AppMessage.RobotAction.ActionDirection.UP,
				AppMessage.RobotAction.ActionType.LEFT_FOOT, 4, 1, false);
	}

	// ��ת
	public static AppMessage.Request move_left() {
		return func(AppMessage.RobotAction.ActionDirection.UP,
				AppMessage.RobotAction.ActionType.RIGHT_FOOT, 4, 1, false);
	}

	// ����
	public static AppMessage.Request move_back() {
		return func(AppMessage.RobotAction.ActionDirection.DOWN, 4, 1, true);
	}

	// ǰ��
	public static AppMessage.Request move_front() {
		return func(AppMessage.RobotAction.ActionDirection.UP, 4, 1, true);
	}
	//����������
	public static AppMessage.Request func_a(int alblumId, int resindex) {
		return func_playalum(alblumId, resindex, 1, false);

	}
	//����������
	public static AppMessage.Request func_x() {
		return func_play(0, -1, AppMessage.PlayParams.Action.PLAY_ALL, "", true);

	}

	// ����������
	public static AppMessage.Request func_wifi(String ssid, String pwd) {
		// ����wifi���ݰ�
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



		// ����������Ϣ ����ʧ��
		// requestBuilder.setType(com.luobotec.message.AppMessage.MessageType.volume);
		// requestBuilder.setVolume(com.luobotec.message.AppMessage.VolumeType.MINUS);

		AppMessage.Request request = func_nlu("��������");      //func_takepic(); //func_wifi("play","12345678");
		// ת�����ֽ�����
		byte[] byteArray = request.toByteArray();
		// ���
		final Base64.Encoder encoder = Base64.getEncoder();
		final String encodedText = "MSG:" + encoder.encodeToString(byteArray)
				+ "@";
		System.out.println(encodedText);
		
		
		
//		String str="hello world";
//		
//		byte[ ] bytes=str.getBytes();
//		System.out.println("ѹ��ǰ���ȣ�"+bytes.length);
//		byte[ ] gzipBytes=gzip(bytes);
//		System.out.println("ѹ���󳤶ȣ�"+gzipBytes.length);
//		System.out.println("ѹ����"+byteToHexString(gzipBytes));
		
		
//		byte [] gzipBytes = hexStringToByte("7d99ae532f2f2b55b7af534fcb4a4c50b737d5544f4c2853b7572f28cdcc4a49542d2b484b4b282b53afa8e00705269808da");
//		
//		
//		byte[ ] unGzipBytes=unGzip(gzipBytes);
//		System.out.println("��ѹ��"+byteToHexString(unGzipBytes));
//		
//		String strOUt = new String(unGzipBytes);
//		System.out.println("��ѹ��"+strOUt);
 
		

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
