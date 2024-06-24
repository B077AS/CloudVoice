package serializedObjects;

import java.io.Serializable;

public class DatabaseDetailsObject implements Serializable{
	
    private String DB_URL;
    private String DB_USER;
    private String DB_PASSWORD;
    
	public DatabaseDetailsObject(String dB_URL, String dB_USER, String dB_PASSWORD) {
		super();
		DB_URL = dB_URL;
		DB_USER = dB_USER;
		DB_PASSWORD = dB_PASSWORD;
	}
	
	public String getDB_URL() {
		return DB_URL;
	}
	
	public void setDB_URL(String dB_URL) {
		DB_URL = dB_URL;
	}
	
	public String getDB_USER() {
		return DB_USER;
	}
	
	public void setDB_USER(String dB_USER) {
		DB_USER = dB_USER;
	}
	
	public String getDB_PASSWORD() {
		return DB_PASSWORD;
	}
	
	public void setDB_PASSWORD(String dB_PASSWORD) {
		DB_PASSWORD = dB_PASSWORD;
	}
}