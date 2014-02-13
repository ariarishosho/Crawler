package org.crawler;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * 
 * @author arichi
 */
public class DataTransaction {

	/**
	 * データソース
	 */
	private static BasicDataSource dataSource;
	/**
	 * DBコネクション
	 */
	public static Connection connection = null;
	// 環境変数
	private final static String username = "root";
	private final static String password = "#d33h1144";
	private final static String url = "jdbc:mysql://localhost:3306/crawler?useUnicode=true&characterEncoding=utf8";

	public static int connectionCount = 0;

	/**
	 * トランジションセット
	 * 
	 * @param setCon
	 */
	public DataTransaction() {
		try {
			setConnection();
		} catch (Exception e) {
			System.out.println("Error in Connection:" + e.toString());
		}
	}

	private void setConnection() throws SQLException {
		try {
			if (dataSource == null) {
				dataSource = new BasicDataSource();
				String driver = "com.mysql.jdbc.Driver";
				try {
					dataSource.setDriverClassName(driver);
					dataSource.setUrl(url);
					dataSource.setUsername(username);
					dataSource.setPassword(password);
					dataSource.setMaxActive(100);
					dataSource.setMaxWait(10000);
					dataSource.setMaxIdle(10);
					if (connection == null || connection.isClosed()) {
						System.out
								.println(" requeition CONNECTION WITH FIRST SERVER.");
						connection = dataSource.getConnection();
						connectionCount++;
					}
				} catch (SQLException e) {
					System.out
							.println("***Connection Requisition*** Could not connect to the database msg :"
									+ e.getMessage());
				}
			} else {
				connection = dataSource.getConnection();
				connectionCount++;
			}
		} catch (Exception e) {
			System.out.println("open connection exception" + e);
		}
	}

	/**
	 * データソースを取得する
	 * 
	 * @return
	 */
	public BasicDataSource getDataSorce() {
		if (dataSource == null) {
			try {
				setConnection();
			} catch (Exception e) {
				System.out.println("Error in Connection:" + e.toString());
			}
		}
		return DataTransaction.dataSource;
	}

	/**
	 * DBコネクションを取得する
	 * 
	 * @return
	 */
	public Connection getConnection() {
		try {
			if (connection == null || connection.isClosed()) {
				setConnection();
			}
		} catch (SQLException e) {
			System.out
					.println("***Connection Requisition*** Could not connect to the database msg :"
							+ e.getMessage());
		}
		return DataTransaction.connection;
	}
}