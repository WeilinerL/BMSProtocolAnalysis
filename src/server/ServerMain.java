package server;

import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


public class ServerMain{

	public static Logger logger = Logger.getLogger(ServerMain.class);

	public static void main(String args[]) {

		Socket socket=null;
		ServerSocket ss=null;

		int port = 0 ;//端口号
		int timeOut = 60;//阻塞时间
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

		try {
			Properties prop = new Properties();
			InputStream inStream = null;
			inStream = ServerMain.class.getClassLoader().getResourceAsStream("resources/socket.properties");//读取端口设置配置文件
			prop.load(inStream);
			port = Integer.parseInt(prop.getProperty("port"));//端口号
			timeOut = Integer.parseInt(prop.getProperty("timeOut"));//阻塞时间
			ss = new ServerSocket(port);
			logger.trace("开始监听等待设备连接...");
			logger.trace("监听端口号：" + port);
			if(inStream!=null)
				inStream.close();
			while(true) {
				socket = ss.accept();// 接受请求
				logger.trace("云盒连接建立  " + df.format(new Date()));
				socket.setSoTimeout(30*1000);// 读取数据时阻塞链路的超时时间。
                ReceiveAndSendData inAndOut = new ReceiveAndSendData(socket);
				new Thread(new PollingThread(inAndOut)).start();//每有一个云盒建立请求就开启一个新的线程
			}
		}catch(FileNotFoundException e) {
			//找不到socket配置文件
			e.printStackTrace();
			logger.trace("can not found socket.properties");
//			LogRecorder.writePropertiesLog("can not found socket.properties\r\n"
//			+e.getMessage(), "socket");
		}catch (IOException e) {
			//socket配置文件读取出错
			e.printStackTrace();
			logger.trace("read socket.properties unsuccessfully");
//			LogRecorder.writePropertiesLog("read socket.properties unsuccessfully\r\n"
//			+e.getMessage(), "socket");
		}catch(NumberFormatException e) {
			//数据格式出错
			e.printStackTrace();
			logger.trace("port or timeout is not a number");
//			LogRecorder.writePropertiesLog("port or timeout is not a number\r\n"+
//			e.getMessage(), "socket");
		}catch(IllegalArgumentException e) {
			//端口号设置超出范围
			e.printStackTrace();
			logger.trace("port is outside range of valid,which is between 0 and 65535");
//			LogRecorder.writePropertiesLog("port is outside range of valid,which is between 0 and 65535\r\n"
//					+e.getMessage(), "socket");
		}

	}

}
