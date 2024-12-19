package util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBUtil {

    private static DataSource dataSource;

    static {
        try {
            // 获取 JNDI 初始上下文
            Context context = new InitialContext();
            // 查找资源并初始化数据源
            dataSource = (DataSource) context.lookup("java:comp/env/MarketSphereConn");
        } catch (NamingException e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("初始化数据源失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库连接
     *
     * @return 数据库连接
     * @throws SQLException 如果获取连接失败
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("数据源未初始化");
        }
        return dataSource.getConnection();
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("连接成功: " + connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
