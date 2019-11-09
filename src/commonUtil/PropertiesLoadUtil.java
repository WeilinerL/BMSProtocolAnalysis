package commonUtil;

import equipments.IEquipmentsType;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public final class PropertiesLoadUtil implements IEquipmentsType {
	
	private PropertiesLoadUtil() {};

	public static Properties getProperties(String propertiesType) throws IOException {
		Properties prop = null;
		switch(propertiesType) {
		    case ups:
		    	prop = loadUpsInstructions(upsProperties);
		    	break;
			case bms:
				prop = loadUpsInstructions(bmsProperties);
		}

		return prop ;
	}


	private static Properties loadUpsInstructions(String filename) throws IOException {
		Properties prop = new Properties();
		InputStream inStream = PropertiesLoadUtil.class.getClassLoader().getResourceAsStream("resources/" + filename);
		prop.load(inStream);
		return prop;
	}

	public  static void getInstruction(String instruction,byte[] b) {
		String[] s = instruction.split("-");
		for(int i =0;i<s.length;i++)
			b[i] = (byte)Integer.parseInt(s[i],16); // s[i]作为16进制,转化为10进制
	}

	public  static byte[] getInstructionWithoutTransfer(String instruction) {
		String[] s = instruction.split("-");
		String order = "";
		for(int i =0;i<s.length;i++)
			order += s[i]; // s[i]作为16进制,转化为10进制
		return ByteTransfer.hexToByte(order);
	}

}
