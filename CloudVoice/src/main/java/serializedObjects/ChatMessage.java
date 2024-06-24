package serializedObjects;

import java.io.Serializable;

public class ChatMessage implements Serializable{

	private long id;
	private int roomId;
	private String content;
	private String time;
	private int senderId;
	private String username;
	private byte[] image;
	private long replyTo;
	
	public ChatMessage(long id, int roomId, String content, String time, int senderId, String username) {
		super();
		this.id = id;
		this.roomId = roomId;
		this.content = content;
		this.time = time;
		this.senderId = senderId;
		this.username = username;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getRoomId() {
		return roomId;
	}
	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public int getSenderId() {
		return senderId;
	}
	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	public long getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(long replyTo) {
		this.replyTo = replyTo;
	}
}