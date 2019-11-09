package equipments.ups;

import commonUtil.ByteTransfer;
import dao.impl.IBMSDao;
import dao.BMSDaoJdbcImpl;
import equipments.IEquipmentInstruction;
import org.apache.log4j.Logger;
import server.ReceiveAndSendData;
import server.SenderAndReceiver;

import java.io.IOException;

public class UpsInstruction  implements UpsSendInstructionType, IEquipmentInstruction {
	private byte[] instruction = null;
	private String type = null;
	private int batteryId = -1;
	private int batteryGroup = -1;
	private ReceiveAndSendData inAndOut = null;
	private IBMSDao upsDao = new BMSDaoJdbcImpl();
	private String mac = null;
	private Ups ups = null;

	public static Logger logger = Logger.getLogger(UpsInstruction.class);
	
	public UpsInstruction(byte[] instruction, String type, ReceiveAndSendData inAndOut, String mac, Ups ups) {
		this.instruction = instruction;
		this.type = type;
		this.inAndOut = inAndOut;
		this.mac = mac;
		this.ups = ups;
	}

	public void setType(String type) {
		this.type = type;
	}

	public UpsInstruction(byte[] instruction, String type, int batteryId, ReceiveAndSendData inAndOut, String mac, Ups ups) {
		this(instruction,type,inAndOut,mac,ups);
		this.batteryId = batteryId;
	}

	public UpsInstruction(byte[] instruction, String type, String batteryGroup, ReceiveAndSendData inAndOut, String mac, Ups ups) {
		this(instruction,type,inAndOut,mac,ups);
		this.batteryGroup = Integer.parseInt(batteryGroup);
	}
	
	public byte[] getInstruction() {
		return instruction;
	}
	public String getType() {
		return type;
	}
	public int getBatteryId() {
		return batteryId;
	}

	@Override
	public void sendOrder() throws IOException {
		logger.trace("指令类型: " + type);
		// 发送一条具体的ups参数查询指令
		inAndOut.sendOrder(instruction);
	}

	@Override
	public byte[] receive() throws IOException {
		return inAndOut.getReceivedBytes();
	}

	@Override
	public void updateRecord(byte[] data) {

		// 一条指令读取多个电池

		for(int i = 0; i < data.length; i ++) {
			float value = -1;

			if(type.equals(get_Error)) {
				if((i + 1) % 2 == 0) {
					int battery_id =  (i + 1) / 2;
					value = ByteTransfer.transfer(data[i - 1], data[i]);
					upsDao.addRecord(mac,"error",value,battery_id,batteryGroup);
					logger.trace("ORDER_TYPE" + type + "BATTERY_ID: " + battery_id + " BATTERY_PACK: " + batteryGroup + " VALUE: " + value);
				}
			} else {
				if((i + 1) % 4 == 0) {
					int battery_id =  (i + 1) / 4;
					value = ByteTransfer.transfer(data[i - 3],data[i - 2],data[i - 1],data[i]);
					logger.trace("ORDER_TYPE" + type + "BATTERY_ID: " + battery_id + " BATTERY_PACK: " + batteryGroup + " VALUE: " + value);
					switch(type) {
						case get_Voltage:
						    upsDao.addRecord(mac,"voltage",value,battery_id,batteryGroup);
							break;
						case get_Temperature:
                            upsDao.addRecord(mac,"temprature",value,battery_id,batteryGroup);
							break;
						case get_Resistance:
                            upsDao.addRecord(mac,"resistance",value,battery_id,batteryGroup);
							break;
						case get_Capacity:
                            upsDao.addRecord(mac,"capacity",value,batteryId,batteryGroup);
							break;
						case get_OneChannel:
							int numOfBatteries = (0xff&data[0]) * 16 + (0xff&data[1]);
							ups.setOneChannelBatteries(numOfBatteries);
							break;
						case get_TwoChannel:
							int numOfBatteries2 = (0xff&data[0]) * 16 + (0xff&data[1]);
							ups.setTwoChannelBatteries(numOfBatteries2);
							break;
					}
				}
			}
		}

		// 以下为一条指令读取一个电池

//		float value = -1;
//		if(data.length == 4) {
//			value = ByteTransfer.transfer(data[0],data[1],data[2],data[3]);
//		}
//		switch(type) {
//			case get_Voltage:
////				upsDao.addRecord(mac,"voltage",value,batteryId);
//				System.out.println(type + " VALUE: " + value);
//				break;
//            case get_Temperature:
////                upsDao.addRecord(mac,"temprature",value,batteryId);
//				System.out.println(type + " VALUE: " + value);
//                break;
//            case get_Resistance:
////                upsDao.addRecord(mac,"resistance",value,batteryId);
//				System.out.println(type + " VALUE: " + value);
//                break;
//            case get_Capacity:
////                upsDao.addRecord(mac,"capacity",value,batteryId);
//				System.out.println(type + " VALUE: " + value);
//                break;
//			case get_Error:
////				upsDao.addRecord(mac,"error",value,batteryId);
//				System.out.println(type + " VALUE: " + value);
//				break;
//			case get_OneChannel:
//				int numOfBatteries = (0xff&data[0]) * 16 + (0xff&data[1]);
//				ups.setOneChannelBatteries(numOfBatteries);
//				break;
//			case get_TwoChannel:
//				int numOfBatteries2 = (0xff&data[0]) * 16 + (0xff&data[1]);
//				ups.setTwoChannelBatteries(numOfBatteries2);
//				break;
//		}
	}
	

}
