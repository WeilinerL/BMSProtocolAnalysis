package dao;


import dao.impl.IBMSDao;
import dao.impl.dbBaseUtil;

public class BMSDaoJdbcImpl extends dbBaseUtil implements IBMSDao {

	@Override
	public void addRecord(String mac, String dataName, double readValue, int batteryId, int batteryPack) {
			String sql = "insert into t_data ("
                    + "int_data_id,"
                    + "str_mac_address,"
                    + "str_data_name,"
                    + "double_read_values,"
                    + "date_read_time,"
                    + "int_battery_id,"
                    + "int_battery_pack) "
			+ "values(?,?,?,?,?,?,?)";
		Object args[] = new Object[7];
		args[0] = null;
		args[1] = mac;
		args[2] = dataName;
		args[3] = readValue;
		args[4] = new java.sql.Timestamp(System.currentTimeMillis());
		args[5] = batteryId;
        args[6] = batteryPack;
		super.update(sql, args);
	}

	@Override
	public void addError() {

	}

	public static void main(String[] args) {
		BMSDaoJdbcImpl bmsDaoJdbc = new BMSDaoJdbcImpl();
		bmsDaoJdbc.addRecord("123456","测试",1.00,-1, 1);
	}


}
