package dao.impl;

import com.alibaba.druid.pool.DruidPooledConnection;
import dbutils.DBPoolConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public abstract class dbBaseUtil {

	protected int update(String sql,Object[] args) {
		DBPoolConnection dbp = DBPoolConnection.getInstance();    //获取数据连接池单例
		DruidPooledConnection conn = null;
		PreparedStatement ps = null;
		//ResultSet rs = null;
		try {
			conn = dbp.getConnection();
			ps = conn.prepareStatement(sql);

			for(int i=0;i<args.length;i++)
				ps.setObject(i+1, args[i]);
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			try {
				if (null != ps){
					ps.close();
				}
				if (null != conn){
					conn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
