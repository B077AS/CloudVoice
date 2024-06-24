package user;

import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

public class BlockUserAudioMenuItem extends MenuItem{

	public BlockUserAudioMenuItem() {
		
		VBox mainBox=new VBox();
		CheckBox checkBox = new CheckBox("Block User Audio");
		mainBox.getChildren().add(checkBox);
		this.setGraphic(mainBox);
	}

}
