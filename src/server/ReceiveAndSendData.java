package server;

import com.alibaba.druid.sql.visitor.functions.Hex;
import commonUtil.ByteTransfer;
import commonUtil.Crc16Util;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 什么是Socket
 * Socket的英文原义是“孔”或“插座”。在网络编程中，网络上的两个程序通过一个双向的通信连接实现数据的交换，这个连接的一端称为一个socket。
 *
 *         Socket套接字是通信的基石，是支持TCP/IP协议的网络通信的基本操作单元。它是网络通信过程中端点的抽象表示，包含进行网络通信必须的五种信息：连接使用的协议，本地主机的IP地址，本地进程的协议端口，远地主机的IP地址，远地进程的协议端口。
 *
 *         Socket本质是编程接口(API)，对TCP/IP的封装，TCP/IP也要提供可供程序员做网络开发所用的接口，这就是Socket编程接口；HTTP是轿车，提供了封装或者显示数据的具体形式；Socket是发动机，提供了网络通信的能力。
 *
 * Socket的原理
 * Socket实质上提供了进程通信的端点。进程通信之前，双方首先必须各自创建一个端点，否则是没有办法建立联系并相互通信的。正如打电话之前，双方必须各自拥有一台电话机一样。
 *
 *         套接字之间的连接过程可以分为三个步骤：服务器监听，客户端请求，连接确认。
 *
 *         1、服务器监听：是服务器端套接字并不定位具体的客户端套接字，而是处于等待连接的状态，实时监控网络状态。
 *
 *         2、客户端请求：是指由客户端的套接字提出连接请求，要连接的目标是服务器端的套接字。为此，客户端的套接字必须首先描述它要连接的服务器的套接字，指出服务器端套接字的地址和端口号，然后就向服务器端套接字提出连接请求。
 *
 *         3、连接确认：是指当服务器端套接字监听到或者说接收到客户端套接字的连接请求，它就响应客户端套接字的请求，建立一个新的线程，把服务器端套接字的描述发给客户端，一旦客户端确认了此描述，连接就建立好了。而服务器端套接字继续处于监听状态，继续接收其他客户端套接字的连接请求。
 * ————————————————
 * 版权声明：本文为CSDN博主「石硕页」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
 * 原文链接：https://blog.csdn.net/u014209205/article/details/80461122
 *
 */

public class ReceiveAndSendData {

	// DataOutputStream数据输出流允许应用程序将基本Java数据类型写到基础输出流中,
	// DataInputStream数据输入流允许应用程序以机器无关的方式从底层输入流中读取基本的Java类型.
	
	private DataInputStream in = null;
	private DataOutputStream out = null;
	private Socket socket = null;
    PrintWriter pw = null;
	
	public ReceiveAndSendData(Socket socket) throws IOException {
		this.socket = socket;
		// 从客户端接受信息
		in = new DataInputStream(socket.getInputStream());
		// 给客户端发送请求
		out = new DataOutputStream(socket.getOutputStream());
        pw = new PrintWriter(out);
	}

	/**
	 * 读取6字节16进制Mac地址 连接一旦建立云盒就会发送14字节的地址信息
	 * 其中只有第7到12字节为MAC地址即设备id
	 * @return 16位Mac地址
	 * @throws IOException
	 */

//	public String getMACAddress() throws IOException {
//
//			byte buf[] = new byte[14];
//			in.readFully(buf, 0, 14);
//
//			String macAddress ="";
//			for (int i = 6; i <= 11; i++) {
//				// 一个字节是八位 字节里存的是八位二进制数 将这些字节和 0xff(255 11111111)相与得到
//				// 为什么byte类型的数字要&0xff再赋值给int类型，其本质原因就是想保持二进制补码的一致性。
//				// 对于有符号数，从小扩展大时，需要用&0xff这样方式来确保是按补零扩展。
//				macAddress += Integer.toHexString(buf[i] & 0xff);  //把byte转化为正整数
//			}
//			return macAddress;
//	}

    public String getMACAddress() throws IOException {
        String order = "MAC"; // 发送获取mac地址的命令
		//将字符转换成字节数组，并且指定UTF-8编码
		byte[] dataByteArr = order.getBytes("UTF-8");
        out.write(dataByteArr);
        out.flush();
        byte buf[] = new byte[6];
        in.readFully(buf, 0, 6);
        String macAddress ="";
        for (int i = 0; i <= 5; i++) {
            // 一个字节是八位 字节里存的是八位二进制数 将这些字节和 0xff(255 11111111)相与得到
            // 为什么byte类型的数字要&0xff再赋值给int类型，其本质原因就是想保持二进制补码的一致性。
            // 对于有符号数，从小扩展大时，需要用&0xff这样方式来确保是按补零扩展。
            macAddress += Integer.toHexString(buf[i] & 0xff);  //把byte转化为正整数
        }
        return macAddress;
    }

	/**
	 * 向客户端发送指令请求
	 * @param instruction
	 * @throws IOException
	 */

	public void sendOrder(byte[] instruction) throws IOException {
		// 发送包含两字节crc校验码的数据
		out.write(Crc16Util.getData(instruction));
		out.flush(); // 清空缓冲区的数据流
	}

	/**
	 * 接收响应数据
	 * @return 字节数组,每两个个字节代表一个具体数值(不包含地址码功能码等，只返回具体数据)
	 * @throws IOException
	 */

//	public byte[] getReceivedBytes() throws IOException{
//		byte[] checkBuf = new byte[3];
//		in.readFully(checkBuf, 0, 3);
//		//modbus协议 响应报头之后三个字节为地址码、功能码和字节数量
//		byte[] dataBuf = new byte[3];
//		in.readFully(dataBuf, 0, 3);
//		byte[] realData;
//		// 响应报头前三个字节为 fa 01 01
//		if((checkBuf[0]&0xff) == 0xfa && (checkBuf[1]&0xff) == 0x01 && (checkBuf[2]&0xff) == 0x01) {
//			int byteLength = dataBuf[2] & 0xff;
//			realData = new byte[byteLength];
//			for(int i = 0; i < byteLength; i ++) {
//				realData[i] = in.readByte();
//			}
//			//crc检查
//			byte[] crc = new byte[2];
//			in.readFully(crc,0,2);
//			byte[] allData = new byte[3 + 3 + realData.length];
//			System.arraycopy(checkBuf,0,allData,0,3);
//			System.arraycopy(dataBuf,0,allData,3,3);
//			System.arraycopy(realData,0,allData,6, realData.length);
//			byte[] needCheck = new byte[3 + realData.length];
//			System.arraycopy(allData,3,needCheck,0,3 + realData.length);
//			if(Crc16Util.check(needCheck,crc)) {
//				return realData;
//			}
//		}
//		return new byte[0];
//	}

    public byte[] getReceivedBytes() throws IOException{
        byte[] header = new byte[3];
        in.readFully(header, 0, 3);
         //modbus协议 响应报头之后三个字节为模块地址、功能码和字节数量
        int byteLength = (header[2] & 0xff);
        byte[] realData = new byte[byteLength];
        for(int i = 0; i < byteLength; i ++) {
            realData[i] = in.readByte();
        }
        //crc检查
        byte[] crc = new byte[2];
        in.readFully(crc,0,2);
        byte[] allData = new byte[3 + realData.length];
        System.arraycopy(header,0,allData,0,3);
        System.arraycopy(realData,0,allData,3, realData.length);
        byte[] needCheck = new byte[3 + realData.length];
        System.arraycopy(allData,0,needCheck,0,3 + realData.length);
        if(Crc16Util.check(needCheck,crc)) {
            return realData;
        }
        return new byte[0];
    }

	/**
	 * 关闭IO
	 */

	public void close() {
		try {
			// 关闭输入流
			if (in != null)
				in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭输出流
				if (out != null)
					out.close();pw.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					// 关闭发动机
					if (!socket.isClosed())
						socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public  int read() throws IOException {
		return in.read();
	}

}
