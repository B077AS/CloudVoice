package serializedObjects;

import java.io.Serializable;

public class RequestPing implements Serializable{
	
	private int userRequesting;
	private int userRequested;
	
	public RequestPing(int userRequesting, int userRequested) {
		super();
		this.userRequesting = userRequesting;
		this.userRequested = userRequested;
	}
	
	public int getUserRequesting() {
		return userRequesting;
	}
	public void setUserRequesting(int userRequesting) {
		this.userRequesting = userRequesting;
	}
	public int getUserRequested() {
		return userRequested;
	}
	public void setUserRequested(int userRequested) {
		this.userRequested = userRequested;
	}
}