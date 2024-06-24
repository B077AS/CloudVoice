package serializedObjects;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class UpdateMessage implements Serializable{

	private int roomID;
	private HashMap<Integer, UserDetailsObject> usersList;

	public UpdateMessage(int roomID, HashMap<Integer, UserDetailsObject> usersList) {
		this.roomID=roomID;
		this.usersList=usersList;

	}

	public int getRoomID() {
		return roomID;
	}

	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}

	public HashMap<Integer, UserDetailsObject> getUsersList() {
		return usersList;
	}

	public void setUsersList(HashMap<Integer, UserDetailsObject> usersList) {
		this.usersList = usersList;
	}
	
	
	
}
