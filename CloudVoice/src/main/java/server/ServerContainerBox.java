package server;

import java.util.LinkedHashMap;

import javafx.scene.layout.VBox;

public class ServerContainerBox extends VBox{
	
	private LinkedHashMap<Integer, ServerBox> serversList=new LinkedHashMap<Integer, ServerBox>();
	private ServerBox fakeConnected;

	public LinkedHashMap<Integer, ServerBox> getServersList() {
		return serversList;
	}

	public void setServersList(LinkedHashMap<Integer, ServerBox> serversList) {
		this.serversList = serversList;
	}

	public ServerBox getFakeConnected() {
		return fakeConnected;
	}

	public void setFakeConnected(ServerBox fakeConnected) {
		this.fakeConnected = fakeConnected;
	}
}
