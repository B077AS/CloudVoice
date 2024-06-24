package user;

import java.util.Map.Entry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import main.model.MainWindowModel;
import main.model.SVGCodes;
import main.view.MainWindow;
import rooms.videoRooms.screenShare.StreamingController;
import rooms.videoRooms.screenShare.StreamingModel;
import rooms.videoRooms.screenShare.StreamingView;
import serializedObjects.RequestPing;
import serializedObjects.UserDetailsObject;
import settings.CSSColorExtractor;

public class ConnectedUserBox extends HBox{

	private User user;
	private MainWindow mainWindow;
	private MainWindowModel model;
	private Label liveLabel;
	private Node avatar;
	private Label userNameLabel;
	private Button microphoneOffIcon;
	private Button audioOffIcon;
	private boolean wasLive=false;
	private CSSColorExtractor cssColorExtractor;
	private ContextMenu contextMenu;
	private MenuItem joinLiveItem;
	private ContextMenu userProfileContextMenu;
	private boolean showingPing=false;

	public ConnectedUserBox(MainWindow mainWindow, MainWindowModel model, Entry<Integer, UserDetailsObject> entry) {
		UserDB userDb = new UserDB();
		this.mainWindow=mainWindow;
		this.model=model;
		this.user = userDb.fetchMinimalUserInfoFromId(entry.getKey());
		this.user.setUserDetails(entry.getValue());
		this.cssColorExtractor=new CSSColorExtractor();
		this.contextMenu = new ContextMenu();
		this.userProfileContextMenu=new ContextMenu();

		this.setPadding(new Insets(2.5, 0, 2.5, 3));
		this.setCursor(Cursor.HAND);

		Color primaryColor=this.cssColorExtractor.findPrimaryColor();
		double red=primaryColor.getRed()*255;
		double green=primaryColor.getGreen()*255;
		double blue=primaryColor.getBlue()*255;

		this.contextMenu.setOnHidden(event -> {
			if(this.showingPing==false) {
				this.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
			}else {
				this.setStyle("-fx-background-color: rgba("+red+", "+green+", "+blue+", 0.4);" + "-fx-background-radius: 7;");
			}
		});

		this.userProfileContextMenu.setOnHidden(event -> {
			this.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
		});

		this.contextMenu.setOnShowing(event -> {
			this.setStyle("-fx-background-color: rgba("+red+", "+green+", "+blue+", 0.4);" + "-fx-background-radius: 7;");
		});

		this.setOnMouseEntered(event -> {
			this.setStyle("-fx-background-color: rgba("+red+", "+green+", "+blue+", 0.4);" + "-fx-background-radius: 7;");
		});

		this.setOnMouseExited(event -> {
			if(this.contextMenu.isShowing()==false && this.showingPing==false && this.userProfileContextMenu.isShowing()==false) {
				this.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
			}
		});

		this.setSpacing(5);
		this.setAlignment(Pos.CENTER_LEFT);

		Image userAvatar=user.getAvatar();
		if(userAvatar!=null) {
			Circle clip = new Circle(15, 15, 15);
			clip.setStroke(Color.TRANSPARENT);
			clip.setStrokeWidth(2);
			clip.setFill(new ImagePattern(userAvatar));
			avatar=clip;
		}else {
			SVGPath path=SVGCodes.createSVGGraphic(SVGCodes.defaultUserAvatarSVG, 18);
			path.setStyle("-fx-fill: -primary-color;");
			path.setTranslateX(-0.4);
			path.setTranslateY(-0.9);
			Button button = new Button();
			button.setPickOnBounds(true);
			button.setGraphic(path);
			button.setAlignment(Pos.CENTER);
			button.setMinSize(25, 25);
			button.setMaxSize(25, 25);
			button.setMouseTransparent(true);
			button.setStyle("-fx-background-color: transparent;");
			button.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
			avatar=button;
		}

		userNameLabel= new Label(user.getUsername());
		this.getChildren().addAll(avatar, userNameLabel);

		MenuItem blockAudioItem = new MenuItem();			
		HBox blockAudioBox=new HBox();			
		blockAudioBox.setSpacing(20);
		blockAudioBox.setAlignment(Pos.CENTER_LEFT);
		CheckBox blockAudioCheckBox = new CheckBox();

		if(this.user.getId()==model.getUser().getId()) {//TODO devo mettere che deve essere diverso e lo user di model.getuser deve essere admin
			Label checkBoxLabel=new Label("Block User Audio");
			checkBoxLabel.setStyle("-fx-font-size: 14px;");
			blockAudioBox.getChildren().addAll(checkBoxLabel, blockAudioCheckBox);
			HBox.setHgrow(checkBoxLabel, Priority.NEVER);
			HBox.setHgrow(blockAudioCheckBox, Priority.ALWAYS);
			blockAudioItem.setGraphic(blockAudioBox);
			contextMenu.getItems().add(blockAudioItem);
		}

		MenuItem showPingItem = new MenuItem();
		HBox pingBox=new HBox();
		pingBox.setAlignment(Pos.CENTER_LEFT);
		Label pingLabel=new Label("Show Ping");
		pingBox.getChildren().add(pingLabel);
		showPingItem.setGraphic(pingBox);
		contextMenu.getItems().add(showPingItem);

		MenuItem userInfoItem = new MenuItem();
		userInfoItem.setId("pingMenuItem");
		
		HBox userInfoContainer=new HBox();
		userInfoContainer.setAlignment(Pos.TOP_CENTER);
		
		HBox usernameBox=new HBox();
		usernameBox.setAlignment(Pos.CENTER);
		usernameBox.setPadding(new Insets(-5, 0, 0, 0));
		
		SVGPath status=SVGCodes.createSVGGraphic(SVGCodes.onlineSVG, 15);
		status.setStyle("-fx-fill: -primary-color;");
		Button statusButton = new Button();
		statusButton.setPickOnBounds(true);
		statusButton.setGraphic(status);
		statusButton.setAlignment(Pos.CENTER);
		statusButton.setMinSize(20, 20);
		statusButton.setMaxSize(20, 20);
		statusButton.setStyle("-fx-background-color: transparent;");
        Tooltip tooltip = new Tooltip("Online");
        statusButton.setTooltip(tooltip);
		
		VBox infoRightBox=new VBox();
		infoRightBox.setAlignment(Pos.TOP_CENTER);
		infoRightBox.setPadding(new Insets(0, 0, 0, 20));

		Node userInfoAvatar;

		if(model.getUser().getAvatar()!=null) {
			ImageView avatarView=new ImageView(model.getUser().getAvatar());
			avatarView.setFitHeight(100);
			avatarView.setPreserveRatio(true);		
			Rectangle clip = new Rectangle(100, 100); // Dimensioni dell'avatar quadrato
			clip.setArcWidth(7); // Angoli smussati
			clip.setArcHeight(7); // Angoli smussati
			avatarView.setClip(clip);			
			userInfoAvatar=avatarView;
		}else{
			SVGPath path=SVGCodes.createSVGGraphic(SVGCodes.defaultUserAvatarSVG, 50);
			path.setStyle("-fx-fill: -primary-color;");
			Button buttonAvatar = new Button();
			buttonAvatar.setPickOnBounds(true);
			buttonAvatar.setGraphic(path);
			buttonAvatar.setAlignment(Pos.CENTER);
			buttonAvatar.setMinSize(100, 100);
			buttonAvatar.setMaxSize(100, 100);
			buttonAvatar.setMouseTransparent(true);
			buttonAvatar.setStyle("-fx-background-color: transparent; -fx-border-color: -primary-color; -fx-border-radius: 3px; -fx-border-width: 2px;");
			buttonAvatar.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
			userInfoAvatar=buttonAvatar;
		}

		Label userInfoUsername=new Label(this.user.getUsername());
		userInfoUsername.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: -primary-color;");
		
		Label cloudUserLabel=new Label("Cloud-User since: 07-05-2024");
		cloudUserLabel.setStyle("-fx-text-fill: -primary-color;");

		usernameBox.getChildren().addAll(userInfoUsername, statusButton);
		infoRightBox.getChildren().addAll(usernameBox, cloudUserLabel);
		userInfoContainer.getChildren().addAll(userInfoAvatar, infoRightBox);
		userInfoItem.setGraphic(userInfoContainer);
		userProfileContextMenu.getItems().add(userInfoItem);

		this.setOnMouseClicked(clickEvent -> {
			if (clickEvent.getButton() == MouseButton.SECONDARY ) {
				if(this.userProfileContextMenu.isShowing()==true) {
					this.userProfileContextMenu.hide();
				}

				blockAudioItem.setOnAction(event -> {
					if(blockAudioCheckBox.isSelected()==false) {
						blockAudioCheckBox.setSelected(true);
					}else {
						blockAudioCheckBox.setSelected(false);
					}					
				});

				showPingItem.setOnAction(event -> {
					this.showingPing=true;
					RequestPing request=new RequestPing(this.model.getUser().getId(), this.getUser().getId());
					model.requestUsersPing(request);
				});


				contextMenu.show(this, clickEvent.getScreenX(), clickEvent.getScreenY());
			}else {
				if (clickEvent.getButton() == MouseButton.PRIMARY) {

					if(this.contextMenu.isShowing()==true) {
						this.contextMenu.hide();
					}

					if(this.userProfileContextMenu.isShowing()==false) {
						this.contextMenu.hide();
						this.setStyle("-fx-background-color: rgba("+red+", "+green+", "+blue+", 0.4);" + "-fx-background-radius: 7;");
						this.userProfileContextMenu.show(this, clickEvent.getScreenX(), clickEvent.getScreenY());
					}else {
						this.userProfileContextMenu.hide();
					}
				}
			}
		});

		checkDetails();
	}

	public void checkDetails() {
		if (user.getUserDetails().isLive()==true && wasLive==false) {

			wasLive=true;
			liveLabel = new Label("LIVE");
			liveLabel.setStyle("-fx-background-color: red; -fx-text-fill: -text-fill-color; -fx-font-size: 10; -fx-padding: 2;"
					+ "-fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;");
			liveLabel.setMinWidth(Label.USE_PREF_SIZE); // Impedisce l'espansione del label
			this.getChildren().add(liveLabel);

			joinLiveItem = new MenuItem();
			HBox joinLiveBox=new HBox();
			joinLiveBox.setAlignment(Pos.CENTER_LEFT);
			Label joinLiveLable=new Label("Join Live");
			joinLiveBox.getChildren().add(joinLiveLable);
			joinLiveItem.setGraphic(joinLiveBox);

			this.setOnMouseClicked(clickEvent -> {
				if (clickEvent.getButton() == MouseButton.SECONDARY) {
					joinLiveItem.setOnAction(event -> {
						StreamingView newStage=new StreamingView(mainWindow);
						StreamingModel streamingModel=new StreamingModel(mainWindow, model);
						streamingModel.initialize();
						StreamingController streamingController=new StreamingController(newStage, streamingModel);						
						streamingModel.setReceiveFrameThread(new Thread(() -> streamingController.receiveVideo()));
						streamingModel.getReceiveFrameThread().setDaemon(true);
						streamingModel.getReceiveFrameThread().start();						
						newStage.show();
						mainWindow.getPrimaryStage().hide();
					});


					contextMenu.show(this, clickEvent.getScreenX(), clickEvent.getScreenY());
				}else {
					if (clickEvent.getButton() == MouseButton.PRIMARY) {
						if(this.contextMenu.isShowing()==true) {
							this.contextMenu.hide();
						}
					}
				}
			});
			contextMenu.getItems().add(joinLiveItem);
		}

		if(user.getUserDetails().isLive()==false && wasLive==true) {
			wasLive=false;
			contextMenu.getItems().remove(joinLiveItem);
			this.getChildren().remove(liveLabel);
		}

		if(user.getUserDetails().isSpeaking()==true) {
			try {
				Circle avatarCircle=(Circle)avatar;
				avatarCircle.setStroke(Color.web("#8a2be2"));
			}catch(Exception e) {
				avatar.setStyle("-fx-background-radius: 50%; -fx-background-color: transparent; -fx-border-color: -primary-color; -fx-border-width: 2px; -fx-border-style: solid; -fx-border-radius: 50%;");
			}
		}else {
			try {
				Circle avatarCircle=(Circle)avatar;
				avatarCircle.setStroke(Color.TRANSPARENT);
			}catch(Exception e) {
				avatar.setStyle("-fx-background-radius: 50%; -fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 2px; -fx-border-style: solid; -fx-border-radius: 50%;");
			}
		}

		if(user.getUserDetails().isMicrophoneOn()==false && !this.getChildren().contains(microphoneOffIcon)) {
			setMicrohoneOff();
		}

		if(user.getUserDetails().isMicrophoneOn()==true && this.getChildren().contains(microphoneOffIcon)) {
			setMicrohoneOn();
		}

		if(user.getUserDetails().isAudioOn()==false && !this.getChildren().contains(audioOffIcon)) {
			setAudioOff();
		}

		if(user.getUserDetails().isAudioOn()==true && this.getChildren().contains(audioOffIcon)) {
			setAudioOn();
		}

		if(user.getUserDetails().isDisconnect()==true) {
			//user.getUserDetails().setDisconnect(false);
			/*System.out.println("update "+user.getUserDetails().getMainRoomId());
			VoiceRoom room=(VoiceRoom)model.getCurrentRoom();
			if(user.getUserDetails().getMainRoomId()==room.getVideoRoom().getID()) {
			}else {
				mainWindow.getUserDisplayPanel().getChildren().remove(model.getCurrentConnectedUsers().get(user.getId()));
			}*/			
			mainWindow.getUsersListView().removeUser(user);
		}
	}

	public void setMicrohoneOff() {
		SVGPath path=SVGCodes.createSVGGraphic(SVGCodes.offMicrophoneSVG, 15);
		path.setStyle("-fx-fill: -primary-color;");
		microphoneOffIcon = new Button();
		microphoneOffIcon.setPickOnBounds(true);
		microphoneOffIcon.setGraphic(path);
		microphoneOffIcon.setAlignment(Pos.CENTER);
		microphoneOffIcon.setStyle("-fx-background-color: transparent;");
		microphoneOffIcon.setMinSize(25, 25);
		microphoneOffIcon.setMaxSize(25, 25);
		microphoneOffIcon.setMouseTransparent(true);
		microphoneOffIcon.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
		this.getChildren().add(microphoneOffIcon);
	}

	public void setMicrohoneOn() {
		this.getChildren().remove(microphoneOffIcon);
	}

	public void setAudioOff() {
		SVGPath path=SVGCodes.createSVGGraphic(SVGCodes.offHeadphonesSVG, 15);
		path.setStyle("-fx-fill: -primary-color;");
		audioOffIcon = new Button();
		audioOffIcon.setPickOnBounds(true);
		audioOffIcon.setGraphic(path);
		audioOffIcon.setAlignment(Pos.CENTER);
		audioOffIcon.setStyle("-fx-background-color: transparent;");
		audioOffIcon.setMinSize(25, 25);
		audioOffIcon.setMaxSize(25, 25);
		audioOffIcon.setMouseTransparent(true);
		audioOffIcon.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
		audioOffIcon.setPadding(new Insets(0, 0, 0, -15));
		this.getChildren().add(audioOffIcon);
	}

	public void setAudioOn() {
		this.getChildren().remove(audioOffIcon);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isShowingPing() {
		return showingPing;
	}

	public void setShowingPing(boolean showingPing) {
		this.showingPing = showingPing;
	}
}