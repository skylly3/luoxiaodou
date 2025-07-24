package com.hiflying.blelink;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class GTransformer {

	public static int edgeInt(int data, int min, int max) {
	
		if (data > max) {
			data = max;
		}else if (data < min) {
			data = min;
		}
		
		return data;
	}
	
	public static byte[] int2bytes(int data) {
		
		byte[] datas = new byte[4];
		datas[0] = ((byte)((data & 0xFF000000) >> 24));
		datas[1] = ((byte)((data & 0xFF0000) >> 16));
		datas[2] = ((byte)((data & 0xFF00) >> 8));
		datas[3] = ((byte)(data & 0xFF));
		
		return datas;
	}

	public static int[] bytes2ints(byte[] datas) {

		if (datas == null) {
			return null;
		}

		int length = datas.length;
		if (length == 0) {
			return new int[0];
		}

		IntBuffer buffer = ByteBuffer.wrap(datas).asIntBuffer();
		int[] out = new int[buffer.remaining()];
		buffer.get(out);

		return out;
	}
	
	public static String bytes2HexString(byte[] datas) {
		return bytes2HexStringWithSplit(datas, "");
	}
	
	public static String bytes2HexStringWithWhitespace(byte[] datas) {
		return bytes2HexStringWithSplit(datas, " ");
	}
	
	public static String bytes2Characters(byte[] datas) {
		
		StringBuffer sb = new StringBuffer();
		for (byte b : datas) {
			sb.append((char)b);
		}
		return sb.toString();
	}
	
	public static int byte2Int(byte data) {
		return data & 0xFF;
	}
	
	public static String bytes2HexStringWithSplit(byte[] datas, String splitString) {
		
		if (datas == null) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		String format = "%02X";
		
		for (int i = 0; i < datas.length; i++) {
			sb.append(String.format(format, datas[i] & 0xFF));
			if (i < datas.length - 1) {
				sb.append(splitString);
			}
		}
		
		return sb.toString();
	}
	
	public static List<String> bytes2HexStrings(List<byte[]> data) {
		
		if (data == null) {
			return null;
		}
		
		if (data.isEmpty()) {
			Collections.emptyList();
		}
		ArrayList<String> hexes = new ArrayList<String>();
		for (byte[] item : data) {
			hexes.add(bytes2HexString(item));
		}
		
		return hexes;
	}
	
	public static boolean isByteEquals(byte data1, int data2) {
		return (data1 & 0xFF) == data2;
	}
	
	public static boolean isHexString(String hexString) {
		return isHexStringWithSplit(hexString, "");
	}

	public static boolean isHexStringWithWhitespace(String hexString) {
		return isHexStringWithSplit(hexString, " ");
	}
	
	public static boolean isHexStringWithSplit(String hexString, String splitString) {
		
		if (hexString == null) {
			return false;
		}
		
		if (splitString != null) {
			hexString = hexString.replaceAll(splitString, "");	
		}
		if (hexString.length() % 2 != 0) {
			return false;
		}
		
		Pattern pattern = Pattern.compile("([0-9]|[a-f]|[A-F]){2}");
		int size = hexString.length() / 2;
		for (int i = 0; i < size; i++) {
			if (!pattern.matcher(hexString.substring(i * 2, (i + 1) * 2)).matches()) {
				return false;
			}
		}
		
		return true;
	}
	
	public static byte[] hexStringWithSplit2bytes(String hexString, String split) {
		
		if (!isHexStringWithSplit(hexString, split)) {
			return null;
		}
		
		hexString = hexString.replaceAll(split, "");
		int size = hexString.length() / 2;
		byte[] array = new byte[size];
		for (int i = 0; i < size; i++) {
			array[i] = (byte)Integer.valueOf(hexString.substring(i * 2, (i + 1) * 2), 16).intValue();
		}
		
		return array;
	}
	
	public static byte[] hexStringWithWhitespace2bytes(String hexString) {
		return hexStringWithSplit2bytes(hexString, " ");
	}
	
	public static byte[] hexString2bytes(String hexString) {
		return hexStringWithSplit2bytes(hexString, "");
	}

	public static String toMac(String address) {
		
		if (address == null) {
			return null;
		}
		
		address = address.toUpperCase(Locale.US);
		
		String regex1 = "[0-9A-F]{2}(:[0-9A-F]{2}){5}$";
		String regex2 = "[0-9A-F]{2}(-[0-9A-F]{2}){5}$";
		String regex3 = "[0-9A-F]{2}([0-9A-F]{2}){5}$";
		
		if (Pattern.matches(regex1, address)) {
			return address;
		}
		
		if (Pattern.matches(regex2, address)) {
			return address.replaceAll("-", ":");
		}
		
		if (Pattern.matches(regex3, address)) {
			StringBuffer stringBuffer = new StringBuffer();
			
			int length = address.length()/2;
			for (int i = 0; i < length; i++) {
				stringBuffer.append(address.substring(2 * i, 2 * i + 2));
				if (i < length - 1) {
					stringBuffer.append(":");
				}
			}
			
			return stringBuffer.toString();
		}
		
		return null;
	}
	
	public static String getStackTrace(Throwable throwable) {
		
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		
		String text = "";
		try {
			throwable.printStackTrace(printWriter);
			text = stringWriter.toString();
		} catch (Exception e) {
		} finally {
			printWriter.close();
		}
		
		return text;
	}
}
