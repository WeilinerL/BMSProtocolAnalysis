package equipments;

import java.io.IOException;

public interface IEquipmentInstruction {
	public abstract void sendOrder() throws IOException; // 发送指令
	public abstract byte[] receive() throws IOException; // 接收数据
	public abstract void updateRecord(byte data[]); //数据库更新

}
