package serializedObjects;

import java.io.Serializable;

public class PingObject implements Serializable{
	
	private int ping;
	private int pingedUserId;
	
	public PingObject(int ping, int pingedUserId) {
		super();
		this.ping = ping;
		this.pingedUserId = pingedUserId;
	}
	public int getPing() {
		return ping;
	}
	public void setPing(int ping) {
		this.ping = ping;
	}
	public int getPingedUserId() {
		return pingedUserId;
	}
	public void setPingedUserId(int pingedUserId) {
		this.pingedUserId = pingedUserId;
	}
}
