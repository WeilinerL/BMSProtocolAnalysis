package commonUtil;

import java.util.Arrays;

/**
 * Modbus CRC16 循环冗余检验
 * CRC校验原理看起来比较复杂，好难懂，因为大多数书上基本上是以二进制的多项式形式来说明的。
 * 其实很简单的问题，其根本思想就是先在要发送的帧后面附加一个数（这个就是用来校验的校验码，
 * 但要注意，这里的数也是二进制序列的，下同），生成一个新帧发送给接收端。当然，这个附加的数不是随意的，
 * 它要使所生成的新帧能与发送端和接收端共同选定的某个特定数整除（注意，这里不是直接采用二进制除法，
 * 而是采用一种称之为“模2除法”）。到达接收端后，再把接收到的新帧除以（同样采用“模2除法”）这个选定的除数。
 * 因为在发送端发送数据帧之前就已通过附加一个数，做了“去余”处理（也就已经能整除了），所以结果应该是没有余数。
 * 如果有余数，则表明该帧在传输过程中出现了差错。
 */
public final class Crc16Util {
	private Crc16Util() {};

    /**
     * 生成带有校验码的数据
     * @param aa
     * @return
     */

    public static byte[] getData(byte[] aa) {
        byte[] bb = getCrc16(aa);
        byte[] cc = new byte[aa.length+bb.length];
        System.arraycopy(aa,0,cc,0,aa.length); //前aa.length位位原始数据
        System.arraycopy(bb,0,cc,aa.length,bb.length); //后aa.length+bb.length位为crc校验码
        return cc;
    }

    /**
     * java 位运算
     * ~ 按位非（NOT）（一元运算）
     * & 按位与（AND）
     * | 按位或（OR）
     * ^ 按位异或（XOR）
     * >> 右移
     * >>> 右移，左边空出的位以0填充
     * 运算符 结果
     * << 左移
     * &= 按位与赋值
     * |= 按位或赋值
     * ^= 按位异或赋值
     * >>= 右移赋值
     * >>>= 右移赋值，左边空出的位以0填充
     * <<= 左移赋值
     * 左移<<:向左移位，符号后面的数字是移了多少位，移的位用0补齐，例如2进制数01111111左移一位后变为11111110，移位是字节操作
     * 右移>>:向右移位，符号后面的数字是移了多少位，移的位用符号位补齐，例如01111111右移一位后变为00111111，而10000000右移一位后变成11000000，因为符号位是1
     * @param arr_buff
     * @return 16位crc校验码
     */
    private static byte[] getCrc16(byte[] arr_buff) {
        int len = arr_buff.length;
        int crc = 0xFFFF; //crc = 15*16*16*16+15*16*16+15*16+15 = 65535 0x 表示十六进制
        int i,j;
        for (i = 0; i < len; i++) {
            // (65535 "与"操作 15*16*16*16+15*16*16)
            crc = ((crc & 0xFF00) | (crc & 0x00FF) ^ (arr_buff[i] & 0xFF));
            for (j = 0; j < 8; j++) {
                if ((crc & 0x0001) > 0) {
                    crc = crc >> 1;
                    crc = crc ^ 0xA001;
                } else
                    crc = crc >> 1;
            }
        }
        return intToBytes(crc);
    }

    /**
     * crc校验码有两个字节
     * 将输入值右移8位后和255相"与"作为输出值的第一个字节
     * 将输入值和255相"与"作为输出值的第二个字节
     * @param value
     * @return
     */
    private static byte[] intToBytes(int value)  {
        byte[] src = new byte[2];
        src[1] =  (byte) ((value>>8) & 0xFF);
        src[0] =  (byte) (value & 0xFF);
        return src;
    }
    
    public static void main(String[] args) {
    	byte a[] = new byte[]{0x01,0x03,(byte) 0x08,(byte) 0xFA,0x00,0x01};
    	for(byte b:a)
    		System.out.print(b+" ");
        System.out.println("\n");
        for(byte c:getData(a))
            System.out.print(c+" ");
        System.out.println((byte)(((0xff & 0xa)<<8)/256));
        System.out.println(ByteTransfer.transfer((byte)0x1,(byte)(0xff&0xa)));
        System.out.println(ByteTransfer.transfer(new byte[] {0,0,0xa,0}));
    }

	public static boolean check(byte[] needCheck, byte[] crc16) {
		return Arrays.equals(getCrc16(needCheck), crc16);
	}
}

