package rooms;

import java.util.ArrayList;
import java.util.List;

import serializedObjects.RoomsEnum;
import user.User;

public abstract class ChatRoom {
	private int ID;
    private String name;
    private List<User> users;
    private int port;
    private int serverID;
    private RoomsEnum type;
    private boolean hasAssociatedRoom;

    public ChatRoom(int ID, String name, int serverID, int port, RoomsEnum type) {
    	this.ID=ID;
        this.name = name;
        this.port = port;
        this.serverID=serverID;
        this.type=type;
        this.hasAssociatedRoom=false;
        this.users = new ArrayList<>();
    }

    public void addUser(User user) {
        users.add(user);
        // Aggiungi il codice per notificare agli altri utenti l'aggiunta di un nuovo utente
    }

    public void removeUser(User user) {
        users.remove(user);
        // Aggiungi il codice per notificare agli altri utenti la rimozione di un utente
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public int getServerID() {
		return serverID;
	}

	public void setServerID(int serverID) {
		this.serverID = serverID;
	}

	public void setName(String name) {
		this.name = name;
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
	
	public boolean hasAssociatedRoom() {
		return hasAssociatedRoom;
	}

	public void setHasAssociatedRoom(boolean hasAssociatedRoom) {
		this.hasAssociatedRoom = hasAssociatedRoom;
	}

	@Override
	public String toString() {
		return "ChatRoom [name=" + name + "]";
	}
}
