package rooms.voiceRooms;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.model.CSSLoader;
import main.model.MainWindowModel;
import user.UserDB;
import user.UserSettings;

public class AudioSlider extends Stage {

	private Slider slider;
	private MainWindowModel mainModel;
	private UserSettings settings;
	private static final double DEFAULT_DB=-40.0;
	private UserDB userDB;

	public AudioSlider(MainWindowModel mainModel) {

		this.mainModel=mainModel;
		// Valori minimi e massimi per lo slider (personalizzabili)
		double minValue = -70.0;
		double maxValue = 0.0;

		// Crea uno slider con valori compresi tra minValue e maxValue
		slider = new Slider(minValue, maxValue, minValue);
		slider.setMinorTickCount(4); // Imposta il numero di tacche minori a 4 (mostrerà valori ogni 5)
		slider.setMajorTickUnit(5.0); // Imposta il passo delle tacche principali a 5
		slider.setBlockIncrement(1);
		slider.setSnapToTicks(true);
		slider.setShowTickMarks(true);
		slider.setShowTickLabels(true);


		userDB=new UserDB();
		settings=userDB.fetchSettings(mainModel.getUser());
		if(settings.getDbNum()!=0.0) {
			slider.setValue(settings.getDbNum());
		}else {
			slider.setValue(DEFAULT_DB);
		}

		// Layout per contenere lo slider e il pulsante
		VBox root = new VBox();
		root.setAlignment(Pos.CENTER);
		root.getChildren().add(slider);

		// Crea un pulsante per confermare e salvare il valore
		Button saveButton = new Button("Save");
		saveButton.setOnAction(event -> saveValue());
		root.getChildren().add(saveButton);

		// Crea la scena
		Scene scene = new Scene(root, 600, 200);
		
		CSSLoader cssLoader=new CSSLoader();
		scene.getStylesheets().add(cssLoader.loadCss());
		
		setScene(scene);

		// Imposta il titolo della finestra
		setTitle("Configure VAD");
	}

	private void saveValue() {
		// Ottieni il valore impostato dallo slider
		double selectedValue = slider.getValue();
		try {
			mainModel.getAudioManager().getVad().setDb(selectedValue);
			userDB.updateDbNum(selectedValue, mainModel.getUser());
		}catch(Exception e){
			userDB.updateDbNum(selectedValue, mainModel.getUser());
		}
		// Chiudi la finestra
		close();
	}
}
