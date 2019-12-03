package server;


import equipments.IEquipmentInstruction;
import log.LogRecorder;
import org.apache.log4j.Logger;

import java.io.IOException;

public class SenderAndReceiver implements Runnable{

	public static Logger logger = Logger.getLogger(SenderAndReceiver.class);
	
	private volatile boolean isListen = true;
	private InstructionQueue<IEquipmentInstruction> instructionQueue = new InstructionQueue<IEquipmentInstruction>();
	private ReceiveAndSendData inAndOut;

	public SenderAndReceiver(ReceiveAndSendData inAndOut, InstructionQueue<IEquipmentInstruction> instructionQueue)  {
		this.inAndOut = inAndOut;
		this.instructionQueue = instructionQueue;
	}

	public void ThreadListen() {
		isListen = false;
	}

	@Override
	public void run(){
		boolean isAlive = true;// 线程是否存活
		while(true) {
			//从指令队列中取出一个指令
			IEquipmentInstruction instruction = instructionQueue.poll();
			if(instruction !=null) {
				try {
					logger.trace("取出一条指令开始发送");
					instruction.sendOrder();
					//接受该指令的数据
					logger.trace("接收返回数据");
					byte[] data = instruction.receive();
					if(data.length == 0) {
						logger.trace("返回数据为空");
					} else {
						//处理该数据
						logger.trace("处理返回数据");
						instruction.updateRecord(data);
					}
				} catch (IOException e) {
					//socket连接失败
					//logRecorder.writeLog("the socket has disconnneted,we can't send or receive\r\n"+e.getMessage());
					e.printStackTrace();
					break;
				}
			}
			else {
				//logRecorder.writeLog("the thread is listening whether the socket is alive or not");
				int count = 0;
				// 以下代码实际上只在第一次指令为空时执行
				while(isListen) {
					logger.trace("指令为空 线程监听Socket");
					count ++;
					if((count%10) ==0)
						//logRecorder.writeLog("Listen has listened 10 times heartbeat");
						logger.trace("socket心跳10次");
					try {
						logger.trace("尝试从Socket读入数据");
                        logger.trace("清空socket输入缓冲区");
						int buff = 0;
						while(buff != -1) {
                            buff = inAndOut.read();
                        }
					} catch (IOException e) {
						//logRecorder.writeLog("the socket has disconneted,thread will stop\r\n"+e.getMessage());
						e.printStackTrace();
						isAlive = false;
						break;
					}
				}
				if(!isAlive)
					break;
				//logRecorder.writeLog("Listenning is over,now,continue to send");
			}
			try{
//				logger.trace("指令间睡眠一秒...");
				Thread.sleep(1000);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			inAndOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fromListenToSend() {
		isListen = false;
	}
	

	


}
