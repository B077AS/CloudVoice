package rooms.videoRooms.screenShare;

import java.util.HashMap;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class AppsListView extends ListView<AppsBox>{
	
	private HashMap<String, AppsBox> boxes;
	
	public AppsListView() {
		this.boxes=new HashMap<String, AppsBox>();
		this.setFocusTraversable(false);
		this.setCellFactory(new Callback<ListView<AppsBox>, ListCell<AppsBox>>() {
			@Override
			public ListCell<AppsBox> call(ListView<AppsBox> param) {
				return new ListCell<AppsBox>() {
					@Override	
					protected void updateItem(AppsBox app, boolean empty) {
						//int currentIndex = getIndex();
						super.updateItem(app, empty);
						setStyle("-fx-padding: 1 0 1px 0;");
						if (app != null && !empty) {
							setGraphic(app);
						} else {
							setGraphic(null);
						}
					}
				};
			}
		});		
	}

	public HashMap<String, AppsBox> getBoxes() {
		return boxes;
	}

	public void setBoxes(HashMap<String, AppsBox> boxes) {
		this.boxes = boxes;
	}
}