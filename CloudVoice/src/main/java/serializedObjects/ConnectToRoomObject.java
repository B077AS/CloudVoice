package serializedObjects;

import java.io.Serializable;

public class ConnectToRoomObject implements Serializable{

	private Room mainRoom;
	private Room textRoom;
	private Room videoRoom;
	private UserDetailsObject user;

	public ConnectToRoomObject(Room mainRoom, UserDetailsObject user) {
		this.user = user;
		this.mainRoom=mainRoom;
	}

	public ConnectToRoomObject(Room mainRoom, Room textRoom, Room videoRoom, UserDetailsObject user) {
		super();
		this.mainRoom = mainRoom;
		this.user = user;
		this.textRoom = textRoom;
		this.videoRoom = videoRoom;
	}

	public Room getMainRoom() {
		return mainRoom;
	}

	public void setMainRoom(Room mainRoom) {
		this.mainRoom = mainRoom;
	}

	public Room getTextRoom() {
		return textRoom;
	}

	public void setTextRoom(Room textRoom) {
		this.textRoom = textRoom;
	}

	public Room getVideoRoom() {
		return videoRoom;
	}

	public void setVideoRoom(Room videoRoom) {
		this.videoRoom = videoRoom;
	}

	public UserDetailsObject getUser() {
		return user;
	}
	
	public void setUser(UserDetailsObject userId) {
		this.user = userId;
	}
}