package dao.impl;



public interface IBMSDao {

	public void addRecord(String mac, String dataName, double readValue, int batteryId, int batteryPack);

	public void addError();

}
