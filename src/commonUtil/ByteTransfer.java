package commonUtil;

public class ByteTransfer {

	/**
	 * 4字节byte数组转float
	 * @param b
	 * @return
	 */
	public static float transfer(byte... b) {
		int accum = 0;
		accum = accum | (b[3] & 0xff) << 0;
		accum = accum | (b[2] & 0xff) << 8;
		accum = accum | (b[1] & 0xff) << 16;
		accum = accum | (b[0] & 0xff) << 24;
		return Float.intBitsToFloat(accum);
	}

	/**
	 * 2两字节转int
	 * @param high
	 * @param low
	 * @return
	 */
	public static int transfer(byte high, byte low) {
		return ((high & 0xff) << 8 ) + low ;
	}

	/**
	 * hex转byte数组
	 * @param hex
	 * @return
	 */
	public static byte[] hexToByte(String hex){
		int m = 0, n = 0;
		int byteLen = hex.length() / 2; // 每两个字符描述一个字节
		byte[] ret = new byte[byteLen];
		for (int i = 0; i < byteLen; i++) {
			m = i * 2 + 1;
			n = m + 1;
			int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
			ret[i] = Byte.valueOf((byte)intVal);
		}
		return ret;
	}

}
