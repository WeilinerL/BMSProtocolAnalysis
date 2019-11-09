package equipments.ups;


import com.alibaba.druid.sql.visitor.functions.Hex;
import commonUtil.ByteTransfer;
import equipments.IEquipment;
import equipments.IEquipmentInstruction;
import org.apache.log4j.Logger;
import server.PollingThread;
import server.ReceiveAndSendData;
import server.InstructionQueue;

import java.io.IOException;
import java.util.Arrays;

public class Ups  implements UpsSendInstructionType, IEquipment {

    public static Logger logger = Logger.getLogger(Ups.class);

	private String mac = null;//mac地址
	private int oneChannelBatteries;// 一通道单体数
	private int twoChannelBatteries;// 二通道单体数
	private static UpsSendInstruction upsSendInstruction = null; // 所有类的指令都一样 类加载时初始化
	private InstructionQueue<IEquipmentInstruction> instructionQueue = null; // 不同ups有不同的轮训规则 类实例化时初始化
	private ReceiveAndSendData inAndOut = null;


	/**
	 * static{}(即static块)，会在类被加载的时候执行且仅会被执行一次，一般用来初始化静态变量和调用静态方法，下面我们详细的讨论一下该语句块的特性及应用。
	 *
	 *
	 *
	 * 类加载:Java命令的作用是启动虚拟机，虚拟机通过输入流，从磁盘上将字节码文件(.class文件)中的内容读入虚拟机，并保存起来的过程就是类加载。
	 *
	 *
	 *
	 *  类加载特性 :
	 *       *在虚拟机的生命周期中一个类只被加载一次。
	 *       *类加载的原则：延迟加载，能少加载就少加载，因为虚拟机的空间是有限的。
	 *       *类加载的时机：
	 *       1）第一次创建对象要加载类.
	 *       2）调用静态方法时要加载类,访问静态属性时会加载类。
	 *       3）加载子类时必定会先加载父类。
	 *       4）创建对象引用不加载类.
	 *       5) 子类调用父类的静态方法时
	 *           (1)当子类没有覆盖父类的静态方法时，只加载父类，不加载子类
	 *           (2)当子类有覆盖父类的静态方法时，既加载父类，又加载子类
	 *       6）访问静态常量，如果编译器可以计算出常量的值，则不会加载类,例如:public static final int a =123;否则会加载类,例如:public static final int a = math.PI。
	 */

	static {
		try {
			upsSendInstruction = new UpsSendInstruction(); //类加载时初始化 读取指令配置文件
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}

	}
	
	public Ups(ReceiveAndSendData inAndOut, InstructionQueue<IEquipmentInstruction> instructionQueue, String mac) throws IOException{
		this.inAndOut = inAndOut;
		this.instructionQueue = instructionQueue;

		// 云盒建立连接后会发送包含该云盒mac地址的字节数组
		// 初始化

		this.mac = mac;
		logger.trace("读取一通道单体数量...");
		oneChannelBatteries = readBatteries(upsSendInstruction.read_oneChannel);
		logger.trace("一通道单体数量: " + oneChannelBatteries);
		logger.trace("读取二通道单体数量...");
		twoChannelBatteries = readBatteries(upsSendInstruction.read_twoChannel);
        logger.trace("二通道单体数量: " + twoChannelBatteries);
	}
	
	public void setOneChannelBatteries(int i) {
		this.oneChannelBatteries = i;
	}
	
	public void setTwoChannelBatteries(int i) {
		this.twoChannelBatteries = i;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	/**
	 * 读取电池数量
	 * @param instructionOfReadBatteries 读取指令
	 * @return 电池数量
	 * @throws IOException IO异常
	 */
	private int readBatteries(byte[] instructionOfReadBatteries) throws IOException {
		inAndOut.sendOrder(instructionOfReadBatteries); //发送查询指令
		byte[] in_buf = inAndOut.getReceivedBytes();
		if(in_buf.length != 2) {
			return 0;
		}
		return ByteTransfer.transfer(in_buf[0],in_buf[1]); //这里应当返回的是两个字节，所以默认为前两位
	}


	/**
	 * 一条指令读取一个电池
	 * @param orderSend 指令
	 * @param num 电池编号
	 * @param type 指令读取类型
	 */
	private void inInstructionQueue(byte[] orderSend, int num,String type) {
		byte[] realSend = Arrays.copyOf(orderSend, orderSend.length);
		// 左移八位将8位字节转换为10进制数 左移四位是因为该字节为地址高位
		// 相当于十六进制数转十进制数后乘以2的8次方
		int address = ((0xff & realSend[2])<<8) + (0xff & realSend[3]) + num;
		Integer high = (address / 256);  // 一组电池最多有128个 但是地址是偶数
		Integer low = (address % 256);
		String highStr = high.toHexString(high);
		String lowStr = low.toHexString(low);
		String order = "";
		for(int i = 0; i < realSend.length; i ++) {
			Integer x = null;
			if(i == 2) {
				if(highStr.length() == 1) {
					highStr = "0" + highStr;
				}
				order += highStr;
			}else if(i == 3) {
				if(lowStr.length() == 1) {
					lowStr = "0" + lowStr;
				}
				order += lowStr;
			}else {
				x = Integer.valueOf(realSend[i]);
				String hex = x.toHexString(x).toUpperCase();
				if(hex.length() == 1) {
					hex = "0" + hex;
				}
				order += hex;
			}
		}
		instructionQueue.add(new UpsInstruction(ByteTransfer.hexToByte(order),type,num/2+1,inAndOut,mac,this));
	}

	/**
	 * 一条指令读取多个电池
	 * @param orderSend 指令
	 * @param num 读取数量
	 * @param type 指令类型
	 */

	private void inInstructionQueueAll(byte[] orderSend, int startAddress, int num,String type) {
		byte[] realSend = Arrays.copyOf(orderSend, orderSend.length);
		int address = ((0xff & realSend[2])<<8) + (0xff & realSend[3]) + startAddress;
		Integer high = (address / 256);  // 一组电池最多有128个 但是地址是偶数
		Integer low = (address % 256);
		String highStr = high.toHexString(high);
		String lowStr = low.toHexString(low);
		String order = "";
		for(int i = 0; i < realSend.length; i ++) {
			Integer x = null;
			if(i == 2) {
				if(highStr.length() == 1) {
					highStr = "0" + highStr;
				}
				order += highStr;
			}else if(i == 3) {
				if(lowStr.length() == 1) {
					lowStr = "0" + lowStr;
				}
				order += lowStr;
			}else if(i == 4) {
				String numOfBatteries = "";
				if(type.equals(get_Error)) {
					numOfBatteries = Integer.toHexString(num);
				} else {
					numOfBatteries = Integer.toHexString(num * 2);
				}
				if(numOfBatteries.length() == 1) {
					order += "000" + numOfBatteries;
				}else if(numOfBatteries.length() == 2){
					order += "00" + numOfBatteries;
				}else if(numOfBatteries.length() == 3){
					order += "0" + numOfBatteries;
				}else{
					order += numOfBatteries;
				}
			}else if(i == 5) {
				break;
			}else {
				x = Integer.valueOf(realSend[i]);
				String hex = x.toHexString(x).toUpperCase();
				if(hex.length() == 1) {
					hex = "0" + hex;
				}
				order += hex;
			}
		}
		if(startAddress == 0) {
            instructionQueue.add(new UpsInstruction(ByteTransfer.hexToByte(order),type,String.valueOf(1),inAndOut,mac,this));
        }else {
            instructionQueue.add(new UpsInstruction(ByteTransfer.hexToByte(order),type,String.valueOf(2),inAndOut,mac,this));
        }
	}
	
	private void inInstructionQueue(byte[] send,String type) {
		instructionQueue.add(new UpsInstruction(send,type,inAndOut,mac,this));
	}

	// 轮询 指令入队
	@Override
	public void polling() throws IOException {
		// 整体指令
		inInstructionQueue(upsSendInstruction.read_oneChannel,get_OneChannel); //读取单通道电池数量
		inInstructionQueue(upsSendInstruction.read_twoChannel,get_TwoChannel); // 读取双通道电池数量

		// 单个指令
//		for(int i = 0 ; i < oneChannelBatteries*2; i += 2) { // 第一组电池
//			inInstructionQueue(upsSendInstruction.read_voltage,i,get_Voltage);
//			inInstructionQueue(upsSendInstruction.read_temperature,i,get_Temperature);
//			inInstructionQueue(upsSendInstruction.read_resistance,i,get_Resistance);
//			inInstructionQueue(upsSendInstruction.read_capacity,i,get_Capacity);
//			inInstructionQueue(upsSendInstruction.read_error,i,get_Error);
//		}
//		for(int i = 256; i < twoChannelBatteries*2; i += 2) { // 第二组电池
//			inInstructionQueue(upsSendInstruction.read_voltage,i,get_Voltage);
//			inInstructionQueue(upsSendInstruction.read_temperature,i,get_Temperature);
//			inInstructionQueue(upsSendInstruction.read_resistance,i,get_Resistance);
//			inInstructionQueue(upsSendInstruction.read_capacity,i,get_Capacity);
//			inInstructionQueue(upsSendInstruction.read_error,i,get_Error);
//		}
		//一条指令读多个电池
		inInstructionQueueAll(upsSendInstruction.read_voltage,0,oneChannelBatteries,get_Voltage);
		inInstructionQueueAll(upsSendInstruction.read_voltage,256,twoChannelBatteries,get_Voltage);
		inInstructionQueueAll(upsSendInstruction.read_temperature,0,oneChannelBatteries,get_Temperature);
		inInstructionQueueAll(upsSendInstruction.read_temperature,256,twoChannelBatteries,get_Temperature);
		inInstructionQueueAll(upsSendInstruction.read_resistance,0,oneChannelBatteries,get_Resistance);
		inInstructionQueueAll(upsSendInstruction.read_resistance,256,twoChannelBatteries,get_Resistance);
		inInstructionQueueAll(upsSendInstruction.read_capacity,0,oneChannelBatteries,get_Capacity);
		inInstructionQueueAll(upsSendInstruction.read_capacity,256,twoChannelBatteries,get_Capacity);
		inInstructionQueueAll(upsSendInstruction.read_error,0,oneChannelBatteries,get_Error);
		inInstructionQueueAll(upsSendInstruction.read_error,128,twoChannelBatteries,get_Error);
	}

}

