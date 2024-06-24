package main.model;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
	
	private Connection connection;

	public DatabaseConnection() {
		Properties properties = new Properties();

		try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("server.properties")) {
			properties.load(input);

			String dbUrl = properties.getProperty("DB_URL");
			String dbUser = properties.getProperty("DB_USER");
			String dbPassword = properties.getProperty("DB_PASSWORD");

			Class.forName("org.mariadb.jdbc.Driver");
			//Class.forName("com.mysql.cj.jdbc.Driver");
			//Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void closeConnection() {
		if (connection != null) {
			try {
				connection.close();
				System.out.println("DB Connection Closed");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}