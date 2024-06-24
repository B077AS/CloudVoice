package serializedObjects;

import java.io.Serializable;

public class RequestUserListObject implements Serializable{
	
	private int roomId;
	
	public RequestUserListObject(int roomId) {
		this.roomId=roomId;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}
}