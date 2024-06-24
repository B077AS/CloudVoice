package main.model;

import java.io.InputStream;
import java.util.Properties;

public class VPS {
	
	public static String SERVER_ADDRESS;
	public static int SERVER_HASH_PORT;
	public static int SERVER_UPDATE_PORT;
	public static int SERVER_PORT;
	
	public static void loadVpsDetails() {
		Properties properties = new Properties();
		try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("server.properties")) {
			properties.load(input);

			SERVER_ADDRESS = properties.getProperty("SERVER_ADDRESS");
			SERVER_HASH_PORT = Integer.parseInt(properties.getProperty("SERVER_HASH_PORT"));
			SERVER_UPDATE_PORT = Integer.parseInt(properties.getProperty("SERVER_UPDATE_PORT"));
			SERVER_PORT = Integer.parseInt(properties.getProperty("SERVER_PORT"));
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}