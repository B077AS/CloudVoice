package settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import main.controller.MainWindowController;
import main.model.CSSLoader;
import main.model.MainWindowModel;
import main.view.MainWindow;

public class ColorPickerBox extends VBox{

	private MainWindow mainWindow;
	private SettingsWindow settingsStage;

	public ColorPickerBox(MainWindow mainWindow, SettingsWindow settingsStage) {
		this.mainWindow=mainWindow;
		this.settingsStage=settingsStage;
		this.setSpacing(10);
		this.setAlignment(Pos.CENTER);

		Label colorPickerType=new Label("PRIMARY COLOR");
		ColorPicker colorPicker = new ColorPicker();

		CSSColorExtractor colorExtractor=new CSSColorExtractor();

		colorPicker.setValue(colorExtractor.findPrimaryColor());

		Button applyButton = new Button("Apply");		
		applyButton.setOnAction(event -> {
			Color selectedColor = colorPicker.getValue();

			// Creazione della finestra di avviso
			Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
			confirmationDialog.setTitle("Confirm Changes");
			confirmationDialog.setHeaderText("Attention: the client will be restarted");
			confirmationDialog.setContentText("To apply the changes the client will need to be restarted.");

			DialogPane dialogPane = confirmationDialog.getDialogPane();

			CSSLoader cssLoader=new CSSLoader();
			dialogPane.getStylesheets().add(cssLoader.loadCss());

			// Aggiunta del pulsante di conferma
			ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
			confirmationDialog.getButtonTypes().setAll(confirmButtonType, ButtonType.CANCEL);

			// Gestione dell'evento di conferma
			Optional<ButtonType> result = confirmationDialog.showAndWait();
			if (result.isPresent() && result.get() == confirmButtonType) {

				updatePrimaryColor(selectedColor);

				this.settingsStage.getPrimaryStage().close();

				this.mainWindow.getPrimaryStage().close();
				
				MainWindow mainWindowNew = new MainWindow(this.mainWindow.getUser());
				Stage stage = new Stage();
				mainWindowNew.start(stage);
				MainWindowModel model=new MainWindowModel(this.mainWindow.getUser());
				MainWindowController controller=new MainWindowController(model, mainWindowNew);
				controller.startController();				
			}
		});

		this.getChildren().addAll(colorPickerType, colorPicker, applyButton);

	}


	public void updatePrimaryColor(Color selectedColor) {
		String hexColor = String.format("#%02X%02X%02X",
				(int) (selectedColor.getRed() * 255),
				(int) (selectedColor.getGreen() * 255),
				(int) (selectedColor.getBlue() * 255));


		String fileName = System.getProperty("user.home") + File.separator + "customcss.css";
		File file = new File(fileName);
		if (file.exists() && file.isFile() && file.length() > 0) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				StringBuilder stringBuilder = new StringBuilder();
				String line;

				while ((line = reader.readLine()) != null) {
					// Sostituisci il colore hex nella riga
					line = line.replaceAll("-primary-color:\\s*#[0-9a-fA-F]+;", "-primary-color: " + hexColor + ";");
					stringBuilder.append(line).append("\n");
				}

				reader.close();

				// Sovrascrivi il contenuto del file con il nuovo contenuto
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(stringBuilder.toString());
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {

			try {
				file.createNewFile();
				CSSLoader cssLoader=new CSSLoader();
				InputStream cssInputStream=cssLoader.getCssStream();
				InputStreamReader inputReader = new InputStreamReader(cssInputStream, StandardCharsets.UTF_8);

				BufferedReader reader = new BufferedReader(inputReader);
				StringBuilder stringBuilder = new StringBuilder();
				String line;

				while ((line = reader.readLine()) != null) {
					// Sostituisci il colore hex nella riga
					line = line.replaceAll("-primary-color:\\s*#[0-9a-fA-F]+;", "-primary-color: " + hexColor + ";");
					stringBuilder.append(line).append("\n");
				}

				reader.close();

				// Sovrascrivi il contenuto del file con il nuovo contenuto
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(stringBuilder.toString());
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}
}
