package serializedObjects;

import java.io.Serializable;
import java.util.HashMap;

public class Room implements Serializable{
	
    private int id;
    private int port;
    private int associatedRoom;
    private RoomsEnum type;
    private HashMap<Integer, UserDetailsObject> users;
    private int associatedTextRoom;
    private int associatedVideoRoom;

    public Room(int id, int port, RoomsEnum type) {
    	this.users = new HashMap<Integer, UserDetailsObject>();
        this.id = id;
        this.port = port;
        this.type=type;
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

    public int getAssociatedRoom() {
		return associatedRoom;
	}

	public void setAssociatedRoom(int associatedRoom) {
		this.associatedRoom = associatedRoom;
	}

	public HashMap<Integer, UserDetailsObject> getUsers() {
		return users;
	}

	public void setUsers(HashMap<Integer, UserDetailsObject> users) {
		this.users = users;
	}

	public void addUser(UserDetailsObject user) {
        users.put(user.getUserId(), user);
    }

    public void removeUser(int userId) {
        if(users.containsKey(userId)) {
        	users.remove(userId);
        }
    }

	public int getAssociatedTextRoom() {
		return associatedTextRoom;
	}

	public void setAssociatedTextRoom(int associatedTextRoom) {
		this.associatedTextRoom = associatedTextRoom;
	}

	public int getAssociatedVideoRoom() {
		return associatedVideoRoom;
	}

	public void setAssociatedVideoRoom(int associatedVideoRoom) {
		this.associatedVideoRoom = associatedVideoRoom;
	}

}