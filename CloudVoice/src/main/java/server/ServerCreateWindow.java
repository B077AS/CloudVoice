package server;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.model.CSSLoader;
import main.model.MainWindowModel;
import main.model.SVGCodes;
import user.User;
import java.io.File;

public class ServerCreateWindow extends Application{

	private final int WIDTH = 900;
	private final int HEIGHT = 500;
	private File selectedFile;
	private Image selectedImage;
	private ServerDB serverDB;
	private MainWindowModel model;
	private User user;
	
	public ServerCreateWindow(MainWindowModel model, User user) {
		this.model=model;
		this.user=user;
		this.serverDB=new ServerDB();		
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Create New Server");
		primaryStage.setOnHidden(event -> {
			model.setCreateServerDialogOpen(false);
		});

		VBox mainBox = new VBox();
		mainBox.setSpacing(10);

		HBox nameBox=new HBox();
		nameBox.setPadding(new Insets(5, 0, 5, 0));
		nameBox.setSpacing(10);
		nameBox.setAlignment(Pos.CENTER);
		Label serverNameLabel = new Label("Server Name:");
		TextField serverNameField = new TextField();
		serverNameField.setPrefWidth(250);
		serverNameField.setPromptText("Name");
		nameBox.getChildren().addAll(serverNameLabel, serverNameField);

		VBox avatarBox=new VBox();
		avatarBox.setSpacing(10);
		avatarBox.setAlignment(Pos.CENTER);
		avatarBox.setPadding(new Insets(-50, 0, 25, 0));
		
		FileChooser fileChooser = new FileChooser();
		
		Button uploadAvatarButton = new Button("Upload Avatar");
		//uploadAvatarButton.setPadding(new Insets(2, 5, 2, 5));
		
		Button buttonAvatar=SVGCodes.createIconButton(SVGCodes.defaultServerAvatarSVG, 200, 200, 100);
		buttonAvatar.setMouseTransparent(true);
		buttonAvatar.addEventFilter(MouseEvent.ANY, MouseEvent::consume);		

		Button createServerButton = new Button("Create");
		createServerButton.setPadding(new Insets(2, 10, 2, 10));
		createServerButton.setStyle("-fx-font-size: 20px;");

		uploadAvatarButton.setOnAction(event -> {
			selectedFile = fileChooser.showOpenDialog(primaryStage);
			if (selectedFile != null) {
				selectedImage = new Image(selectedFile.toURI().toString());
				ImageView avatarView = new ImageView(selectedImage);
				avatarView.setFitWidth(200); // Larghezza
				avatarView.setFitHeight(200); // Altezza
				Rectangle clip = new Rectangle(200, 200); // Dimensioni dell'avatar quadrato
				clip.setArcWidth(10); // Angoli smussati
				clip.setArcHeight(10); // Angoli smussati
				avatarView.setClip(clip);
				avatarBox.getChildren().clear();
				avatarBox.getChildren().addAll(avatarView, uploadAvatarButton);
			}
		});

		createServerButton.setOnAction(event -> {
			String serverName = serverNameField.getText();     
			primaryStage.close();

			int[] array=serverDB.insertServer(new Server(0, serverName, user.getId(), 1, selectedImage, 0, 0));

			Thread sendPortsThread = new Thread(new Runnable() {
				public void run() {
					model.sendPorts(array[0], array[1]);
				}
			});
			sendPortsThread.setDaemon(true);
			sendPortsThread.start();

			Server newServer=serverDB.fetchServerFromId(array[2]);
			model.notifyAddServerToServerListView(newServer);			
		});
		
		avatarBox.getChildren().addAll(buttonAvatar, uploadAvatarButton);
		
		Separator spacerOne=new Separator();
		spacerOne.setPrefWidth(WIDTH);
		Separator spacerTwo=new Separator();
		spacerTwo.setPrefWidth(WIDTH);

		mainBox.getChildren().addAll(avatarBox, spacerOne, nameBox, spacerTwo, createServerButton);
		mainBox.setAlignment(Pos.CENTER);

		Scene dialogScene = new Scene(mainBox, WIDTH, HEIGHT);

		CSSLoader cssLoader=new CSSLoader();
		dialogScene.getStylesheets().add(cssLoader.loadCss());
		
		primaryStage.setResizable(false);
		primaryStage.setScene(dialogScene);
		primaryStage.show();		
	}
}