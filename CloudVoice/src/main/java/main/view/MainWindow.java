package main.view;

import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.model.CSSLoader;
import main.model.PingGraphBox;
import main.model.SVGCodes;
import rooms.RoomBox;
import rooms.RoomContainerBox;
import rooms.textRooms.messages.ChatContainer;
import server.ServerBox;
import server.ServerContainerBox;
import user.User;
import user.UsersListView;
import javafx.scene.image.Image;

public class MainWindow extends Application{

	private User user;
	private BorderPane mainLayout;
	private VBox chatAndRoomsPanel;
	private SplitPane topSplitPane;
	private SplitPane multipleChatSplitPane;
	private ChatContainer primaryChatContainer;
	private ChatContainer secondaryChatContainer;
	private ChatContainer userChatContainer;
	private BorderPane serverListPanel;
	private ServerContainerBox serversBox;	
	private HBox buttonsBox;
	private SplitPane splitPane;
	private ScrollPane serversScrollPane;
	private Stage primaryStage;
	private VBox userAvatarBox;
	private Node userAvatarNode;
	private Button wifiButton;
	private Button screenShareButton;
	private Button settingsButton;
	private SplitPane chatSplitPane;
	private VBox buttomSplitContainer;
	private ContextMenu currentServerEditContextMenu;
	private Button microphoneButton;
	private Button headphonesButton;
	private Button disconnectButton;
	private UsersListView usersListView;
	private ListView<RoomBox> chatRoomListView;
	private ListView<ServerBox> serverListView;
	private RoomContainerBox roomContainerBox;
	private ContextMenu pingContextMenu;
	private RotateTransition rotateTransition;
	private PingGraphBox userPingGraph;
	private Label smallPingLabel;

	public MainWindow (User user) {
		this.user=user;
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage=primaryStage;
		this.primaryStage.setTitle("CloudVoice");
		this.primaryStage.setMinHeight(555);
		this.primaryStage.setMinWidth(1000);
		this.mainLayout = new BorderPane();
		Scene scene = new Scene(mainLayout, 1200, 700);

		buttonsBox = new HBox(10);
		buttonsBox.setAlignment(Pos.CENTER);
		buttonsBox.setPadding(new Insets(10));

		microphoneButton =SVGCodes.createIconButton(SVGCodes.onMicrophoneSVG, 45, 37, 25);
		headphonesButton = SVGCodes.createIconButton(SVGCodes.onHeadphonesSVG, 45, 37, 25);
		wifiButton = SVGCodes.createIconButton(SVGCodes.wifiSVG, 45, 37, 25);
		screenShareButton = SVGCodes.createIconButton(SVGCodes.onScreenShareSVG, 45, 37, 25);
		settingsButton = SVGCodes.createIconButton(SVGCodes.settingsSVG, 45, 37, 25);
		disconnectButton=SVGCodes.createIconButton(SVGCodes.disconnectSVG, 45, 37, 25);

		rotateTransition = new RotateTransition(Duration.seconds(0.75), settingsButton.getGraphic());
		rotateTransition.setByAngle(360); // Rotate by 360 degrees
		rotateTransition.setCycleCount(1); // Do it just once		

		buttonsBox.getChildren().addAll(microphoneButton, headphonesButton, wifiButton, screenShareButton, settingsButton, disconnectButton);

		serverListPanel = new BorderPane();
		serversBox=new ServerContainerBox();
		serverListView = new ListView<>();

		VBox.setVgrow(serverListView, Priority.ALWAYS);

		serverListPanel.setBottom(buttonsBox);

		keepScrollPaneHeight();

		this.primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> {
			keepScrollPaneHeight();
		});

		chatAndRoomsPanel = new VBox();

		buttomSplitContainer =new VBox();

		multipleChatSplitPane = new SplitPane();

		splitPane = new SplitPane(serverListPanel, chatAndRoomsPanel);

		SplitPane.setResizableWithParent(chatAndRoomsPanel, Boolean.FALSE);
		SplitPane.setResizableWithParent(serverListPanel, Boolean.FALSE);

		mainLayout.setCenter(splitPane);
		
		/*chatRoomListPanel=new VBox();
		usersListView=new UsersListView();
		topSplitPane=new SplitPane(chatRoomListPanel, usersListView);
		buttomSplitContainer=new VBox();
		chatSplitPane=new SplitPane(topSplitPane);
		chatSplitPane.setOrientation(Orientation.VERTICAL);
		chatAndRoomsPanel.getChildren().add(chatSplitPane);*/		

		Image userAvatar=user.getAvatar();

		if(userAvatar==null) {
			Button button=SVGCodes.createIconButton(SVGCodes.defaultUserAvatarSVG, 50, 50, 28);
			button.setMouseTransparent(true);
			button.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
			userAvatarNode=button;
		}else {
			ImageView userAvatarView = new ImageView(userAvatar);
			userAvatarView.setFitWidth(50);
			userAvatarView.setFitHeight(50);
			Rectangle clip = new Rectangle(50, 50);
			clip.setArcWidth(10);
			clip.setArcHeight(10);
			userAvatarView.setClip(clip);
			userAvatarNode=userAvatarView;
		}

		userAvatarBox = new VBox();
		userAvatarBox.setSpacing(5);
		userAvatarBox.setPadding(new Insets(5, 5, 5, 5));
		userAvatarBox.getChildren().add(userAvatarNode);
		userAvatarBox.setAlignment(Pos.TOP_RIGHT);

		mainLayout.setRight(userAvatarBox);

		CSSLoader cssLoader=new CSSLoader();
		scene.getStylesheets().add(cssLoader.loadCss());

		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);        
		primaryStage.show();

		String osName = System.getProperty("os.name").toLowerCase();
		if (!osName.contains("linux")) {
			FXWinUtil.setDarkMode(primaryStage, true);
			FXWinUtil.forceRedrawOfWindowTitleBar(primaryStage);
		}
	}

	/*private void requestPassword(String ash) {
		try {
			Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
			String request =ash;
			byte[] requestData = request.getBytes();

			socket.getOutputStream().write(requestData);
			socket.getOutputStream().flush();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	public void keepScrollPaneHeight() {
		Platform.runLater(() -> {
			serverListView.setPrefHeight(primaryStage.getHeight()-buttonsBox.getHeight()-40);
		});
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public SplitPane getSplitPane() {
		return splitPane;
	}

	public void setSplitPane(SplitPane splitPane) {
		this.splitPane = splitPane;
	}

	public HBox getButtonsBox() {
		return buttonsBox;
	}

	public void setButtonsBox(HBox buttonsBox) {
		this.buttonsBox = buttonsBox;
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	public BorderPane getServerListPanel() {
		return serverListPanel;
	}

	public void setServerListPanel(BorderPane serverListPanel) {
		this.serverListPanel = serverListPanel;
	}

	public SplitPane getTopSplitPane() {
		return topSplitPane;
	}

	public void setTopSplitPane(SplitPane topSplitPane) {
		this.topSplitPane = topSplitPane;
	}


	public ListView<RoomBox> getChatRoomListView() {
		return chatRoomListView;
	}

	public void setChatRoomListView(ListView<RoomBox> chatRoomListView) {
		this.chatRoomListView = chatRoomListView;
	}

	public Button getWifiButton() {
		return wifiButton;
	}

	public Node getUserAvatarNode() {
		return userAvatarNode;
	}

	public void setUserAvatarNode(Node userAvatarNode) {
		this.userAvatarNode = userAvatarNode;
	}

	public void setWifiButton(Button wifiButton) {
		this.wifiButton = wifiButton;
	}

	public Button getScreenShareButton() {
		return screenShareButton;
	}

	public void setScreenShareButton(Button screenShareButton) {
		this.screenShareButton = screenShareButton;
	}

	public Button getSettingsButton() {
		return settingsButton;
	}

	public void setSettingsButton(Button settingsButton) {
		this.settingsButton = settingsButton;
	}

	public VBox getChatAndRoomsPanel() {
		return chatAndRoomsPanel;
	}

	public void setChatAndRoomsPanel(VBox chatAndRoomsPanel) {
		this.chatAndRoomsPanel = chatAndRoomsPanel;
	}

	public ChatContainer getPrimaryChatContainer() {
		return primaryChatContainer;
	}

	public void setPrimaryChatContainer(ChatContainer primaryChatContainer) {
		this.primaryChatContainer = primaryChatContainer;
	}

	public ChatContainer getSecondaryChatContainer() {
		return secondaryChatContainer;
	}

	public void setSecondaryChatContainer(ChatContainer secondaryChatContainer) {
		this.secondaryChatContainer = secondaryChatContainer;
	}

	public ChatContainer getUserChatContainer() {
		return userChatContainer;
	}

	public void setUserChatContainer(ChatContainer userChatContainer) {
		this.userChatContainer = userChatContainer;
	}

	public SplitPane getMultipleChatSplitPane() {
		return multipleChatSplitPane;
	}

	public void setMultipleChatSplitPane(SplitPane multipleChatSplitPane) {
		this.multipleChatSplitPane = multipleChatSplitPane;
	}

	public SplitPane getChatSplitPane() {
		return chatSplitPane;
	}

	public void setChatSplitPane(SplitPane chatSplitPane) {
		this.chatSplitPane = chatSplitPane;
	}

	public VBox getButtomSplitContainer() {
		return buttomSplitContainer;
	}

	public void setButtomSplitContainer(VBox buttomSplitContainer) {
		this.buttomSplitContainer = buttomSplitContainer;
	}

	public ContextMenu getCurrentServerEditContextMenu() {
		return currentServerEditContextMenu;
	}

	public void setCurrentServerEditContextMenu(ContextMenu currentServerEditContextMenu) {
		this.currentServerEditContextMenu = currentServerEditContextMenu;
	}

	public Button getMicrophoneButton() {
		return microphoneButton;
	}

	public void setMicrophoneButton(Button microphoneButton) {
		this.microphoneButton = microphoneButton;
	}

	public Button getHeadphonesButton() {
		return headphonesButton;
	}

	public void setHeadphonesButton(Button headphonesButton) {
		this.headphonesButton = headphonesButton;
	}

	public VBox getUserAvatarBox() {
		return userAvatarBox;
	}

	public void setUserAvatarBox(VBox userAvatarBox) {
		this.userAvatarBox = userAvatarBox;
	}

	public Button getDisconnectButton() {
		return disconnectButton;
	}

	public void setDisconnectButton(Button disconnectButton) {
		this.disconnectButton = disconnectButton;
	}

	public ServerContainerBox getServersBox() {
		return serversBox;
	}

	public void setServersBox(ServerContainerBox serversBox) {
		this.serversBox = serversBox;
	}

	public ScrollPane getServersScrollPane() {
		return serversScrollPane;
	}

	public void setServersScrollPane(ScrollPane serversScrollPane) {
		this.serversScrollPane = serversScrollPane;
	}

	public ListView<ServerBox> getServerListView() {
		return serverListView;
	}

	public void setServerListView(ListView<ServerBox> serverListView) {
		this.serverListView = serverListView;
	}

	public RoomContainerBox getRoomContainerBox() {
		return roomContainerBox;
	}

	public void setRoomContainerBox(RoomContainerBox roomContainerBox) {
		this.roomContainerBox = roomContainerBox;
	}

	public UsersListView getUsersListView() {
		return usersListView;
	}

	public void setUsersListView(UsersListView usersListView) {
		this.usersListView = usersListView;
	}

	public ContextMenu getPingContextMenu() {
		return pingContextMenu;
	}

	public void setPingContextMenu(ContextMenu pingContextMenu) {
		this.pingContextMenu = pingContextMenu;
	}

	public RotateTransition getRotateTransition() {
		return rotateTransition;
	}

	public void setRotateTransition(RotateTransition rotateTransition) {
		this.rotateTransition = rotateTransition;
	}

	public PingGraphBox getUserPingGraph() {
		return userPingGraph;
	}

	public void setUserPingGraph(PingGraphBox userPingGraph) {
		this.userPingGraph = userPingGraph;
	}

	public Label getSmallPingLabel() {
		return smallPingLabel;
	}

	public void setSmallPingLabel(Label smallPingLabel) {
		this.smallPingLabel = smallPingLabel;
	}

	public BorderPane getMainLayout() {
		return mainLayout;
	}

	public void setMainLayout(BorderPane mainLayout) {
		this.mainLayout = mainLayout;
	}
}