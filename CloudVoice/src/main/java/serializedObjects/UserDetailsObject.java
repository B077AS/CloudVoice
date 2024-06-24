package serializedObjects;

import java.io.Serializable;

public class UserDetailsObject implements Serializable{
	
	private int userId;
	private int roomId;
	private boolean microphoneOn;
	private boolean audioOn;
	private boolean live=false;
	private boolean disconnect;
	private boolean speaking;
	
	public UserDetailsObject(int id, int roomId) {
		this.userId=id;
		this.roomId=roomId;
	}
	
	public UserDetailsObject(int userId) {
		this.userId=userId;
	}
	
	public boolean isMicrophoneOn() {
		return microphoneOn;
	}
	public void setMicrophoneOn(boolean muted) {
		this.microphoneOn = muted;
	}
	public boolean isAudioOn() {
		return audioOn;
	}
	public void setAudioOn(boolean noAudio) {
		this.audioOn = noAudio;
	}
	public boolean isLive() {
		return live;
	}
	public void setLive(boolean live) {
		this.live = live;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int id) {
		this.userId = id;
	}

	public int getMainRoomId() {
		return roomId;
	}

	public void setMainRoomId(int mainRoomId) {
		this.roomId = mainRoomId;
	}

	public boolean isDisconnect() {
		return disconnect;
	}

	public void setDisconnect(boolean disconnect) {
		this.disconnect = disconnect;
	}

	public boolean isSpeaking() {
		return speaking;
	}

	public void setSpeaking(boolean speaking) {
		this.speaking = speaking;
	}

	@Override
	public String toString() {
		return "UserDetailsObject [userId=" + userId + ", roomId=" + roomId + ", microphoneOn=" + microphoneOn
				+ ", audioOn=" + audioOn + ", live=" + live + ", disconnect=" + disconnect + ", speaking=" + speaking
				+ "]";
	}
}