package equipments;

import equipments.BMS.BMS;
import equipments.ups.Ups;
import server.ReceiveAndSendData;
import server.InstructionQueue;

import java.io.IOException;

public class EquipmentsFactory implements IEquipmentsType {
	
	public static IEquipment createEquipment(String mac, String type, ReceiveAndSendData inAndOut, InstructionQueue<IEquipmentInstruction> instructionQueue) throws IOException {
		IEquipment equipment = null;
		switch(type) {
			case ups:
				equipment =  new Ups(inAndOut,instructionQueue,mac);
				break;
			case bms:
				equipment = new BMS();
		}
		return equipment;
	}
}
