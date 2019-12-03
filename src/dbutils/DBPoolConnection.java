package dbutils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

/**
 * author: https://blog.csdn.net/xiaozaq/article/details/79663907
 */

public class DBPoolConnection {

    private static DBPoolConnection dbPoolConnection = null;
    private static DruidDataSource druidDataSource = null;


    static {
        Properties properties = loadPropertiesFile("resources/mysql.properties");
        try {
            druidDataSource = (DruidDataSource)DruidDataSourceFactory.createDataSource(properties);    //DruidDataSrouce工厂模式
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 数据库连接池单例
     * @return
     */
    public static synchronized DBPoolConnection getInstance(){
        if (null == dbPoolConnection){
            dbPoolConnection = new DBPoolConnection();
        }
        return dbPoolConnection;
    }

    /**
     * 返回druid数据库连接
     * @return
     * @throws SQLException
     */
    public DruidPooledConnection getConnection() throws SQLException{
        return druidDataSource.getConnection();
    }
    /**
     * @param fullFile 配置文件名
     * @return Properties对象
     */
    private static Properties loadPropertiesFile(String fullFile) {
        String webRootPath = null;
        if (null == fullFile || fullFile.equals("")){
            throw new IllegalArgumentException("Properties file path can not be null" + fullFile);
        }
//        webRootPath = DBPoolConnection.class.getClassLoader().getResource("").getPath();
//        webRootPath = new File(webRootPath).getParent();
        InputStream inputStream = null;
        Properties p = null;
        try {
//            inputStream = new FileInputStream(new File(webRootPath + File.separator + "BMSProtocolAnalysis/" + fullFile));
            inputStream = DBPoolConnection.class.getClassLoader().getResourceAsStream(fullFile);
            p = new Properties();
            p.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inputStream){
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return p;
    }

}
