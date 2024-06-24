package serializedObjects;

import java.io.Serializable;

public class StopPingObject implements Serializable{
	
	private int userRequesting;

	public StopPingObject(int userRequesting) {
		super();
		this.userRequesting = userRequesting;
	}

	public int getUserRequesting() {
		return userRequesting;
	}

	public void setUserRequesting(int userRequesting) {
		this.userRequesting = userRequesting;
	}
}