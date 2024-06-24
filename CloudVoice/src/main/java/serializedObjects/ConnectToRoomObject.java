package serializedObjects;

import java.io.Serializable;

public class ConnectToRoomObject implements Serializable{

	private int id;
	private int port;
	private RoomsEnum type;
	private UserDetailsObject user;

	public ConnectToRoomObject(int id, int port, RoomsEnum type, UserDetailsObject user) {
		super();
		this.id = id;
		this.port = port;
		this.type = type;
		this.user = user;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public RoomsEnum getType() {
		return type;
	}
	
	public void setType(RoomsEnum type) {
		this.type = type;
	}
	
	public UserDetailsObject getUser() {
		return user;
	}
	
	public void setUser(UserDetailsObject userId) {
		this.user = userId;
	}
}