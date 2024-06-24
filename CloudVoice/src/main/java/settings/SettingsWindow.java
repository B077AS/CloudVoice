package settings;

import java.io.File;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import main.model.CSSLoader;
import main.model.MainWindowModel;
import main.model.SVGCodes;
import main.view.CustomAlert;
import main.view.FXWinUtil;
import main.view.MainWindow;
import user.UserDB;

public class SettingsWindow extends Application{

	private static final int WIDTH = 900;
	private static final int HEIGHT = 500;
	private MainWindow mainWindow;
	private MainWindowModel model;
	private AudioDeviceSelector audioDeviceSelector;
	private Stage primaryStage;
	private Node avatar;

	public SettingsWindow(MainWindow mainWindow, MainWindowModel model) {	
		this.model=model;
		this.mainWindow=mainWindow;
		audioDeviceSelector=new AudioDeviceSelector();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {		
		
		this.primaryStage=primaryStage;		
		primaryStage.setOnCloseRequest(event -> {
			model.setSettingsOpen(false);
		});
		primaryStage.setTitle("Settings");

		TabPane tabPane=new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		tabPane.setTabDragPolicy(TabDragPolicy.FIXED);
		Tab micrphoneTab=new Tab("Microphone");
		ScrollPane micrphoneScrollPane=new ScrollPane();
		micrphoneTab.setContent(micrphoneScrollPane);
		Tab speakersTab=new Tab("Speakers");
		ScrollPane speakersScrollPane=new ScrollPane();
		speakersTab.setContent(speakersScrollPane);
		Tab userTab=new Tab("User Profile");
		ScrollPane userScrollPane=new ScrollPane();
		userTab.setContent(userScrollPane);
		Tab appearenceTab=new Tab("Appearence");
		ScrollPane appearenceScrollPane=new ScrollPane();
		appearenceTab.setContent(appearenceScrollPane);

		Scene mainScene = new Scene(tabPane, WIDTH, HEIGHT);
		
		//MIC TAB
		VBox mainMicrophoneBox=new VBox();
		mainMicrophoneBox.setSpacing(10);

		VBox mainSliderBox=new VBox();
		mainSliderBox.setSpacing(10);
		mainSliderBox.setAlignment(Pos.CENTER);
		HBox sliderBox=new HBox();
		sliderBox.setSpacing(10);
		sliderBox.setAlignment(Pos.CENTER);
		Label microphoneSensitivityLabel=new Label("Microphone Sensitivity");

		VoiceActivationSlider voiceActivationSlider=new VoiceActivationSlider(model);
		sliderBox.getChildren().add(voiceActivationSlider);

		Button saveVoiceActivationButton = new Button("Apply");
		saveVoiceActivationButton.setOnAction(event -> voiceActivationSlider.saveVoiceActivationSliderValue());
		sliderBox.getChildren().add(saveVoiceActivationButton);
		Separator firstSeparator=new Separator();
		firstSeparator.setPrefWidth(WIDTH);
		mainSliderBox.getChildren().addAll(microphoneSensitivityLabel, sliderBox);

		HBox mainNoiseReductionBox=new HBox();
		mainNoiseReductionBox.setSpacing(10);
		mainNoiseReductionBox.setAlignment(Pos.CENTER);

		Label noiseReductionLabel=new Label("Noise Reduction");


		RadioButton filterOnOff=setNoiseReductionStatus();
		filterOnOff.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue==true) {
				filterOnOff.setText("On");
				model.getUser().getSettings().setFilter(true);
				model.getUser().getUserDb().updateFilter(true, model.getUser());
			}else {
				filterOnOff.setText("Off");
				model.getUser().getSettings().setFilter(false);
				model.getUser().getUserDb().updateFilter(false, model.getUser());
			}

		});

		Separator secondSpacer=new Separator();
		secondSpacer.setPrefWidth(WIDTH);
		mainNoiseReductionBox.getChildren().addAll(noiseReductionLabel, filterOnOff);

		HBox mainPushToTalkBox=new HBox();
		mainPushToTalkBox.setSpacing(10);
		mainPushToTalkBox.setAlignment(Pos.CENTER);

		Label pushToTalkLabel=new Label("Push To Talk");

		RadioButton pushToTalkOnOff=setPushToTalkStatus();
		pushToTalkOnOff.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue==true) {
				pushToTalkOnOff.setText("On");
				model.getUser().getUserDb().updatePushToTalk(true, model.getUser());
				model.getUser().getSettings().setPushToTalk(true);
				if(model.getAudioManager()!=null) {
					model.getAudioManager().pauseMicrophoneTransmission();
				}
				model.addPushToTalk();

			}else {
				pushToTalkOnOff.setText("Off");
				model.getUser().getUserDb().updatePushToTalk(false, model.getUser());
				model.getUser().getSettings().setPushToTalk(false);
				model.getPushToTalk().stop();
				if(model.getAudioManager()!=null) {

					if(model.getAudioManager().isSendingLoop()==true) {
						model.getAudioManager().startMicrophoneTransmission();
					}else {
						model.getAudioManager().resumeMicrophoneTransmission();
					}
				}
			}

		});

		Button configurePushToTalkButton=new Button("Configure");
		CustomKeyButton displayButton=new CustomKeyButton(model.getUser().getSettings());

		configurePushToTalkButton.setOnAction(event -> {
			displayButton.setText("Press a Key");

			GlobalKeyListener keyListener=new GlobalKeyListener();
			Button resetButton=new Button("Reset");
			Button saveButton=new Button("Save");

			resetButton.setOnAction(resetEvent -> {
				displayButton.setText("P");		
				model.getUser().getUserDb().updatePushToTalkKey(-1, model.getUser());
				model.getUser().getSettings().setPushToTalkKey(-1);

				if(model.getPushToTalk()!=null) {
					keyListener.stop();
					model.getPushToTalk().stop();
					model.addPushToTalk();
				}

				mainPushToTalkBox.getChildren().removeAll(pushToTalkLabel, pushToTalkOnOff, displayButton, resetButton, saveButton);
				mainPushToTalkBox.getChildren().addAll(pushToTalkLabel, pushToTalkOnOff, displayButton, configurePushToTalkButton);
			});

			saveButton.setOnAction(saveEvent -> {
				if(keyListener.getKeyCode()>0) {
					model.getUser().getUserDb().updatePushToTalkKey(keyListener.getKeyCode(), model.getUser());
					model.getUser().getSettings().setPushToTalkKey(keyListener.getKeyCode());

					if(model.getPushToTalk()!=null) {
						keyListener.stop();
						model.getPushToTalk().stop();
						model.addPushToTalk();
					}

					mainPushToTalkBox.getChildren().removeAll(pushToTalkLabel, pushToTalkOnOff, displayButton, resetButton, saveButton);
					mainPushToTalkBox.getChildren().addAll(pushToTalkLabel, pushToTalkOnOff, displayButton, configurePushToTalkButton);
				}else {
					CustomAlert notInARoomAlert=new CustomAlert();
					notInARoomAlert.createAlert("Error", "No Key Selected");
					mainPushToTalkBox.getChildren().removeAll(pushToTalkLabel, pushToTalkOnOff, displayButton, resetButton, saveButton);
					mainPushToTalkBox.getChildren().addAll(pushToTalkLabel, pushToTalkOnOff, displayButton, configurePushToTalkButton);

					if(model.getPushToTalk()!=null) {
						keyListener.stop();
						model.getPushToTalk().stop();
						model.addPushToTalk();
					}
				}
			});

			mainPushToTalkBox.getChildren().removeAll(pushToTalkLabel, pushToTalkOnOff, displayButton, configurePushToTalkButton);
			mainPushToTalkBox.getChildren().addAll(pushToTalkLabel, pushToTalkOnOff, displayButton, resetButton, saveButton);

			keyListener.addKeyObserver(displayButton);
			try {
				Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
				logger.setLevel(Level.OFF);
				logger.setUseParentHandlers(false);
				GlobalScreen.registerNativeHook();
				GlobalScreen.addNativeKeyListener(keyListener);
			} catch (NativeHookException e) {
				e.printStackTrace();
			}
		});

		Separator fourthSpacer=new Separator();
		fourthSpacer.setPrefWidth(WIDTH);
		mainPushToTalkBox.getChildren().addAll(pushToTalkLabel, pushToTalkOnOff, displayButton, configurePushToTalkButton);

		VBox microphonesSelectBox=new VBox();
		microphonesSelectBox.setPadding(new Insets(10, 0, 0, 0));
		microphonesSelectBox.setSpacing(10);
		microphonesSelectBox.setPrefWidth(WIDTH);
		microphonesSelectBox.setAlignment(Pos.CENTER);

		ComboBox<String> microphonesComboBox = new ComboBox<>(FXCollections.observableArrayList(audioDeviceSelector.getInputDevices().keySet()));
		microphonesComboBox.setVisibleRowCount(5);
		String microphoneName=model.getUser().getSettings().getPrefMic();
		ObservableMap<String, TargetDataLine> inputDevices=audioDeviceSelector.getInputDevices();
		if(inputDevices.containsKey(microphoneName)) {
			microphonesComboBox.setValue(microphoneName);
		}else {
			List<Map.Entry<String, TargetDataLine>> entryList = new ArrayList<>(inputDevices.entrySet());
			Map.Entry<String, TargetDataLine> entry = entryList.get(0);
			microphonesComboBox.setValue(entry.getKey());
		}
		microphonesComboBox.setOnAction(event -> {
			UserDB userDb=model.getUser().getUserDb();
			userDb.updatePrefMic(microphonesComboBox.getValue(), model.getUser());

			if(model.getAudioManager()!=null) {
				model.getUser().getSettings().setPrefMic(microphonesComboBox.getValue());
				model.getAudioManager().getSettings().setPrefMic(microphonesComboBox.getValue());
				model.getAudioManager().changeMicrophone();

				if(model.getUser().getSettings().isPushToTalk()!=true) {
					model.getAudioManager().startMicrophoneTransmission();
				}
			}
		});

		Label selectMicrophoneLabel=new Label("Microphone");
		Separator thirdSpacer=new Separator();
		thirdSpacer.setPrefWidth(WIDTH);

		microphonesSelectBox.getChildren().addAll(selectMicrophoneLabel, microphonesComboBox);

		mainMicrophoneBox.getChildren().addAll(microphonesSelectBox, thirdSpacer, mainNoiseReductionBox, secondSpacer, mainSliderBox, firstSeparator, mainPushToTalkBox, fourthSpacer);
		micrphoneScrollPane.setContent(mainMicrophoneBox);

		//SPEAKERS TAB
		VBox mainSpeakersBox=new VBox();
		mainSpeakersBox.setSpacing(10);
		mainSpeakersBox.setPrefWidth(WIDTH);
		mainSpeakersBox.setAlignment(Pos.CENTER);

		VBox speakersSelectBox=new VBox();
		speakersSelectBox.setPadding(new Insets(10, 0, 0, 0));
		speakersSelectBox.setSpacing(10);
		speakersSelectBox.setPrefWidth(WIDTH);
		speakersSelectBox.setAlignment(Pos.CENTER);

		ComboBox<String> speakersComboBox = new ComboBox<>(FXCollections.observableArrayList(audioDeviceSelector.getOutputDevices().keySet()));
		speakersComboBox.setVisibleRowCount(5);
		String speakerName=model.getUser().getSettings().getPrefSpeaker();
		ObservableMap<String, SourceDataLine> outputDevices=audioDeviceSelector.getOutputDevices();
		if(outputDevices.containsKey(speakerName)) {
			speakersComboBox.setValue(speakerName);
		}else {
			List<Map.Entry<String, SourceDataLine>> entryList = new ArrayList<>(outputDevices.entrySet());
			Map.Entry<String, SourceDataLine> entry = entryList.get(0);
			speakersComboBox.setValue(entry.getKey());
		}

		speakersComboBox.setOnAction(event -> {
			UserDB userDb=model.getUser().getUserDb();
			userDb.updatePrefSpeaker(speakersComboBox.getValue(), model.getUser());

			if(model.getAudioManager()!=null) {
				model.getUser().getSettings().setPrefSpeaker(speakersComboBox.getValue());
				model.getAudioManager().getSettings().setPrefSpeaker(speakersComboBox.getValue());
				model.getAudioManager().changeSpeakers();
			}
		});

		Label selectSpeakerLabel=new Label("Speaker");
		Separator firstSpeakerSpacer=new Separator();
		firstSpeakerSpacer.setPrefWidth(WIDTH);
		speakersSelectBox.getChildren().addAll(selectSpeakerLabel, speakersComboBox);

		mainSpeakersBox.getChildren().addAll(speakersSelectBox, firstSpeakerSpacer);
		speakersScrollPane.setContent(mainSpeakersBox);


		//USER TAB
		VBox userBox=new VBox();
		userBox.setPrefWidth(WIDTH);
		userBox.setSpacing(10);
		userBox.setAlignment(Pos.CENTER);

		if(model.getUser().getAvatar()!=null) {
			ImageView avatarView=new ImageView(model.getUser().getAvatar());
			avatarView.setFitHeight(250);
			avatarView.setPreserveRatio(true);		
			avatar=avatarView;
		}else{
			Button buttonAvatar=SVGCodes.createIconButton(SVGCodes.defaultUserAvatarSVG, 250, 250, 100);
			buttonAvatar.setMouseTransparent(true);
			buttonAvatar.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
			avatar=buttonAvatar;
		}
		
		VBox avatarBox=new VBox();
		avatarBox.setPrefWidth(WIDTH);
		avatarBox.setSpacing(5);
		avatarBox.setPadding(new Insets(20, 0, 5, 0));
		avatarBox.setAlignment(Pos.CENTER);
		Button uploadAvatarButton=new Button("Upload");

		uploadAvatarButton.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg")
					);

			Window ownerWindow = primaryStage.getScene().getWindow();
			File selectedFile = fileChooser.showOpenDialog(ownerWindow);
			if (selectedFile != null) {
				Image selectedImage = new Image(selectedFile.toURI().toString());
				ImageView selectedImageView=new ImageView(selectedImage);
				selectedImageView.setFitHeight(250);
				selectedImageView.setPreserveRatio(true);		

				avatarBox.getChildren().removeAll(avatar, uploadAvatarButton);
				avatar=selectedImageView;			

				HBox saveCancelBox=new HBox();
				saveCancelBox.setSpacing(10);
				saveCancelBox.setAlignment(Pos.CENTER);

				Button saveAvatar=new Button("Save");
				saveAvatar.setOnAction(saveEvent -> {
					model.getUser().getUserDb().updateAvatar(selectedImage, model.getUser());
					mainWindow.getUserAvatarBox().getChildren().remove(mainWindow.getUserAvatarNode());
					ImageView userAvatarImageView=new ImageView(selectedImage);
					userAvatarImageView.setFitWidth(50);
					userAvatarImageView.setFitHeight(50);
					mainWindow.setUserAvatarNode(userAvatarImageView);
					mainWindow.getUserAvatarBox().getChildren().add(mainWindow.getUserAvatarNode());
					avatarBox.getChildren().remove(saveCancelBox);
					avatarBox.getChildren().add(uploadAvatarButton);

				});
				saveCancelBox.getChildren().add(saveAvatar);
				Button cancelUploadAvatar=new Button("Cancel");
				cancelUploadAvatar.setOnAction(saveEvent -> {
					try {
						ImageView oldAvatar=(ImageView)avatar;
						oldAvatar.setImage(model.getUser().getAvatar());
						avatarBox.getChildren().remove(saveCancelBox);
						avatarBox.getChildren().add(uploadAvatarButton);
					}catch(Exception e) {
						Button buttonAvatar=SVGCodes.createIconButton(SVGCodes.defaultUserAvatarSVG, 250, 250, 100);
						buttonAvatar.setMouseTransparent(true);
						buttonAvatar.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
						avatarBox.getChildren().removeAll(avatar, saveCancelBox);
						avatar=buttonAvatar;
						avatarBox.getChildren().addAll(avatar, uploadAvatarButton);
					}
				});
				saveCancelBox.getChildren().add(cancelUploadAvatar);
				avatarBox.getChildren().addAll(avatar, saveCancelBox);
			}
		});
		avatarBox.getChildren().addAll(avatar, uploadAvatarButton);
		Separator avatarSeparator=new Separator();
		avatarSeparator.setPrefWidth(WIDTH);

		HBox usernameBox=new HBox();
		usernameBox.setAlignment(Pos.CENTER);
		usernameBox.setSpacing(10);
		Label username=new Label(model.getUser().getUsername()+"#"+Integer.toString(model.getUser().getId()));
		username.setStyle("-fx-font-size: 25px;");

		Button copyUsernameButton=new Button("Copy");
		copyUsernameButton.setOnAction(e -> {
			Clipboard clipboard = Clipboard.getSystemClipboard();
			ClipboardContent content = new ClipboardContent();
			content.putString(username.getText());
			clipboard.setContent(content);
		});
		Button editUsernameButton=new Button("Change");
		editUsernameButton.setOnAction(event ->{
			TextField changeUsernameField=new TextField();
			changeUsernameField.setText(model.getUser().getUsername());
			usernameBox.getChildren().removeAll(username, editUsernameButton, copyUsernameButton);

			HBox cancelSaveBox=new HBox();
			cancelSaveBox.setSpacing(10);

			Label tagLabel=new Label("#"+Integer.toString(model.getUser().getId()));
			tagLabel.setStyle("-fx-font-size: 25px;");

			Button saveUsernameButton=new Button("Save");
			saveUsernameButton.setOnAction(saveEvent ->{
				model.getUser().getUserDb().updateUsername(changeUsernameField.getText(), model.getUser());
				model.getUser().setUsername(changeUsernameField.getText());
				username.setText(model.getUser().getUsername()+"#"+Integer.toString(model.getUser().getId()));

				usernameBox.getChildren().removeAll(changeUsernameField, tagLabel, cancelSaveBox);
				usernameBox.getChildren().addAll(username, editUsernameButton, copyUsernameButton);
			});
			Button cancelEditUsernameButton=new Button("Cancel");
			cancelEditUsernameButton.setOnAction(cancelEvent ->{
				usernameBox.getChildren().removeAll(changeUsernameField, tagLabel, cancelSaveBox);
				usernameBox.getChildren().addAll(username, editUsernameButton, copyUsernameButton);
			});
			cancelSaveBox.getChildren().addAll(saveUsernameButton, cancelEditUsernameButton);
			usernameBox.getChildren().addAll(changeUsernameField, tagLabel, cancelSaveBox);
		});
		usernameBox.getChildren().addAll(username, editUsernameButton, copyUsernameButton);
		userBox.getChildren().addAll(avatarBox, avatarSeparator, usernameBox);
		userScrollPane.setContent(userBox);

		//APPEARENCE TAB
		ColorPickerBox primaryColorPicker=new ColorPickerBox(mainWindow, this);
		HBox primaryColorPickerBox=new HBox();
		primaryColorPickerBox.setMinWidth(WIDTH);
		primaryColorPickerBox.getChildren().add(primaryColorPicker);
		primaryColorPickerBox.setAlignment(Pos.CENTER);
		primaryColorPickerBox.setPadding(new Insets(50, 0, 0, 0));
		appearenceScrollPane.setContent(primaryColorPickerBox);

		tabPane.getTabs().addAll(micrphoneTab, speakersTab, userTab, appearenceTab);

		CSSLoader cssLoader=new CSSLoader();
		mainScene.getStylesheets().add(cssLoader.loadCss());

		primaryStage.setScene(mainScene);
		primaryStage.show();
		
		String osName = System.getProperty("os.name").toLowerCase();
		if (!osName.contains("linux")) {
			FXWinUtil.setDarkMode(primaryStage, true);
			FXWinUtil.forceRedrawOfWindowTitleBar(primaryStage);
		}
		
		primaryStage.setResizable(false);
	}

	public RadioButton setNoiseReductionStatus() {
		RadioButton button=new RadioButton();
		if(model.getUser().getSettings().isFilter()==true) {
			button.setText("On");
			button.setSelected(true);
		}else {
			button.setText("Off");
			button.setSelected(false);
		}
		return button;
	}

	public RadioButton setPushToTalkStatus() {
		RadioButton button=new RadioButton();
		if(model.getUser().getSettings().isPushToTalk()==true) {
			button.setText("On");
			button.setSelected(true);
		}else {
			button.setText("Off");
			button.setSelected(false);
		}
		return button;
	}
	
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
}