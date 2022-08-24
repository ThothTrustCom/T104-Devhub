package org.thothtrust.sc.t104.util;

public class StringUtils {

	public static boolean checkName(byte[] b, int limit) {
		int lower = 32;
		int upper = 126;
		int i;
		
		// Check length
		if (b.length > limit) {
			return false;
		}
		
		// Check chars
		for (byte a : b) {
			i = (int) (a & 0xFF);
			if (i < lower || i > upper) {
				return false;
			}
		}
		return true;
	}
	
	public static String versionToString(int version) {
		byte[] ba = new byte[2];
		BinUtils.shortToBytes((short) version, ba, (short) 0);
		byte[] major = new byte[1];
		major[0] = ba[0];
		byte[] minor = new byte[1];
		minor[0] = ba[1];
		String strMajor = BinUtils.toHexString(major);
		if (strMajor.startsWith("0")) {
			char[] chars = strMajor.toCharArray();
			char[] charsFinal = new char[chars.length - 1];
			System.arraycopy(chars, 1, charsFinal, 0, charsFinal.length);
			strMajor = new String(charsFinal);
		}
		String strMinor = BinUtils.toHexString(minor);
		if (strMinor.startsWith("0")) {
			char[] chars = strMinor.toCharArray();
			char[] charsFinal = new char[chars.length - 1];
			System.arraycopy(chars, 1, charsFinal, 0, charsFinal.length);
			strMinor = new String(charsFinal);
		}
		return strMajor + "." + strMinor;
	}
	
}
