package main.view;

import javafx.scene.control.Alert;
public class CustomAlert{
	
	
	public Alert createAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
		return alert;
	}

}
