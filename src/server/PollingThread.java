package server;

import equipments.EquipmentsFactory;
import equipments.IEquipment;
import equipments.IEquipmentInstruction;
import equipments.IEquipmentsType;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 轮巡线程
 */

public final class PollingThread implements Runnable,IEquipmentsType{

	public static Logger logger = Logger.getLogger(PollingThread.class);
	private String mac;
	private ReceiveAndSendData inAndOut = null;
	private IEquipment equipment = null;
	private SenderAndReceiver senderAndReceiver = null;
	private Thread senderAndReceiverThread = null;
	private int pollingTime = 60;// 无配置文件时默认轮训时间 单位:秒
	private InstructionQueue<IEquipmentInstruction> instructionQueue = new InstructionQueue<>();

	public PollingThread(ReceiveAndSendData inAndOut, String mac){
	    this.mac = mac;
		this.inAndOut = inAndOut;
	}

	@Override
	public void run() {
		//logRecorder.writeLog("Thread is Reading Configuration\r\n");
		//读取配置文件,获得轮询时间
		Properties prop = new Properties();
		try {
		    logger.trace("读取轮询时间配置文件");
			InputStream PollingTimePropertiesinStream = PollingThread.class.getClassLoader().getResourceAsStream("resources/pollingTime.properties");
			prop.load(PollingTimePropertiesinStream);
			PollingTimePropertiesinStream.close();
            logger.trace("读取轮询时间配置文件完成");
		} catch (IOException e2) {
			//logRecorder.writeLog("read pollingTime fail\r\n"+e2.getMessage());
			e2.printStackTrace();
			logger.trace("read pollingTime fail");
		}
		try {
			//创建数据实体类
//			System.out.println("获取设备mac地址");
//			String mac = inAndOut.getMACAddress();
			//这里写数据库查询语句查询该设备的类型
			logger.trace("创建并初始化实体类");
			// 这里要更改的话只能通过先读取设备的mac地址在创建相应的设备
			equipment = EquipmentsFactory.createEquipment(mac,ups,inAndOut,instructionQueue);
			logger.trace("初始化实体类完成");

			// 获取轮询时间
			String get_pollingTime = prop.getProperty("pollingTime");
			pollingTime = Integer.parseInt(get_pollingTime);
			//logRecorder.writeLog("pollingTime is"+pollingTime);
			logger.trace("获取轮询时间: " + pollingTime);

			//创建发送接收线程
			senderAndReceiver = new SenderAndReceiver(inAndOut,instructionQueue);
			logger.trace("启动发送接收线程");
			//启动发送接收线程
			senderAndReceiverThread = new Thread(senderAndReceiver);
			senderAndReceiverThread.start();
			logger.trace("发送接收线程启动成功");

			//当upsEmailThread还存活时，重复轮询
			while(senderAndReceiverThread.isAlive()) {

				logger.trace("装填指令");
				equipment.polling(); // 装填指令
				logger.trace("装填指令结束");

				if(senderAndReceiverThread.isAlive()) {
					//让线程从监听转变成发送
					//logRecorder.writeLog("we will let SenderAndReceiverThread wake up to send instruciton");
					logger.trace("唤醒发送接收线程");
					senderAndReceiver.fromListenToSend();
					//线程睡眠
					try {
					    logger.trace("轮询线程睡眠" + pollingTime + "秒");
						//logRecorder.writeLog("this polling instructions have be sent,the polling thread will sleep "
							//	+ pollingTime+" seconds");
						Thread.sleep(1000*pollingTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}else
					break;
			}
			logger.trace("发送接收线程已挂掉");
			//logRecorder.writeLog("because of socket is closed,the pollingThread will stop");
		}catch(NumberFormatException e){
			//logRecorder.writeLog("pollingTime is not a number \r\n"+e.getMessage());
		}catch (IOException e) {
			e.printStackTrace();
		}finally {
			inAndOut.close();
		}
	}

}
