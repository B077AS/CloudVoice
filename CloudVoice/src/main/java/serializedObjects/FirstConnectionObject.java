package serializedObjects;

import java.io.Serializable;

public class FirstConnectionObject implements Serializable{
	
	private int userId;


	public FirstConnectionObject(int userId) {
		super();
		this.userId = userId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
}