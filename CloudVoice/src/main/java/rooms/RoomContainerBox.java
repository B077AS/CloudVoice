package rooms;

import java.util.LinkedHashMap;
import javafx.scene.layout.VBox;

public class RoomContainerBox extends VBox{
	
	private LinkedHashMap<Integer, RoomBox> roomsList=new LinkedHashMap<Integer, RoomBox>();

	public LinkedHashMap<Integer, RoomBox> getRoomsList() {
		return roomsList;
	}

	public void setRoomsList(LinkedHashMap<Integer, RoomBox> roomsList) {
		this.roomsList = roomsList;
	}
}