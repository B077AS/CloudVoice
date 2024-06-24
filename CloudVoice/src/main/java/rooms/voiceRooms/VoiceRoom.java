package rooms.voiceRooms;

import rooms.ChatRoom;
import rooms.textRooms.TextRoom;
import rooms.videoRooms.VideoRoom;
import serializedObjects.RoomsEnum;

public class VoiceRoom extends ChatRoom{

	private VideoRoom videoRoom;
	private TextRoom textRoom;

	public VoiceRoom(int ID, String name, int serverID, int port, RoomsEnum type) {
		super(ID, name, serverID, port, type);
		// TODO Auto-generated constructor stub
	}

	public VideoRoom getVideoRoom() {
		return videoRoom;
	}

	public void setVideoRoom(VideoRoom videoRoom) {
		this.videoRoom = videoRoom;
	}

	public TextRoom getTextRoom() {
		return textRoom;
	}

	public void setTextRoom(TextRoom textRoom) {
		this.textRoom = textRoom;
	}
}