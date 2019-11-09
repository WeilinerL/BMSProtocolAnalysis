package equipments.ups;

import commonUtil.PropertiesLoadUtil;
import equipments.IEquipmentsType;

import java.io.IOException;
import java.util.Properties;

public class UpsSendInstruction implements UpsSendInstructionType, IEquipmentsType {
	public byte[] read_oneChannel = new byte[6];
	public byte[] read_twoChannel = new byte[6];
	public byte[] read_voltage = new byte[6];
	public byte[] read_temperature = new byte[6];
	public byte[] read_resistance = new byte[6];
	public byte[] read_capacity = new byte[6];
	public byte[] read_error = new byte[6];
	
	public UpsSendInstruction() throws IOException {
		Properties prop = PropertiesLoadUtil.getProperties(ups); // 加载ups配置文件
		// 读取配置文件的指令 这些红字皆为UpsSendInstructionType接口的属性 也即为ups配置文件指令集
		String get_onechannel = prop.getProperty(get_OneChannel);
		String get_twochannel = prop.getProperty(get_TwoChannel);
		String get_voltage = prop.getProperty(get_Voltage);
		String get_temperature = prop.getProperty(get_Temperature);
		String get_resistance = prop.getProperty(get_Resistance);
		String get_capacity = prop.getProperty(get_Capacity);
		String get_error = prop.getProperty(get_Error);
		// 装载指令到字节数组 这里的getInstruction 是用于将配置文件的指令格式化保存到 类属性中
		read_oneChannel = PropertiesLoadUtil.getInstructionWithoutTransfer(get_onechannel);
		read_twoChannel = PropertiesLoadUtil.getInstructionWithoutTransfer(get_twochannel);
		PropertiesLoadUtil.getInstruction(get_voltage,read_voltage);
		PropertiesLoadUtil.getInstruction(get_temperature,read_temperature);
		PropertiesLoadUtil.getInstruction(get_resistance,read_resistance);
		PropertiesLoadUtil.getInstruction(get_capacity,read_capacity);
		PropertiesLoadUtil.getInstruction(get_error,read_error);
	}
	
}
