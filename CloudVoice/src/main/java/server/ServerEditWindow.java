package server;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import main.model.CSSLoader;
import main.model.SVGCodes;
import java.io.File;

public class ServerEditWindow extends Stage {

	private final int WIDTH = 900;
	private final int HEIGHT = 500;
	private ServerBox server;
	private Image selectedImage;
	private Node avatar;

	public ServerEditWindow(ServerBox server) {
		this.server = server;
		setTitle("Edit Server Profile");
		setOnHidden(event -> {
			server.setEdiServerDialogOpen(false);
			if(server.amIConnected()==false) {
				server.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
			}
			/*ListCell<ServerBox> cell=(ListCell)server.getParent();
        	ListView<ServerBox> list=cell.getListView();
        	ServerContainerBox box=(ServerContainerBox)list.getParent();*/
		});

		VBox dialogLayout = new VBox();
		dialogLayout.setSpacing(10);

		Scene dialogScene = new Scene(dialogLayout, WIDTH, HEIGHT);

		if(server.getServer().getAvatar()!=null) {
			ImageView avatarPreview = new ImageView(server.getServer().getAvatar());
			avatarPreview.setFitWidth(200);
			avatarPreview.setFitHeight(200);
			Rectangle clip = new Rectangle(200, 200);
			clip.setArcWidth(10);
			clip.setArcHeight(10);
			avatarPreview.setClip(clip);
			avatar=avatarPreview;
		}else {
			Button buttonAvatar=SVGCodes.createIconButton(SVGCodes.defaultServerAvatarSVG, 200, 200, 100);
			buttonAvatar.setMouseTransparent(true);
			buttonAvatar.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
			avatar=buttonAvatar;
		}


		VBox serverAvatarBox = new VBox();
		serverAvatarBox.setSpacing(10);
		serverAvatarBox.setAlignment(Pos.CENTER); 

		Button editServerAvatarButton = new Button("Upload");
		editServerAvatarButton.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg")
					);

			Window ownerWindow = getScene().getWindow();
			File selectedFile = fileChooser.showOpenDialog(ownerWindow);
			if (selectedFile != null) {
				selectedImage = new Image(selectedFile.toURI().toString());
				
				Button cancelButton=new Button("Cancel");
				cancelButton.setOnAction(e ->{
					
				});
				
				
				if(avatar instanceof ImageView) {
					((ImageView) avatar).setImage(selectedImage);
					serverAvatarBox.getChildren().remove(editServerAvatarButton);
					serverAvatarBox.getChildren().add(cancelButton);
				}else {
					serverAvatarBox.getChildren().removeAll(avatar, editServerAvatarButton);
					ImageView newAvatar = new ImageView(selectedImage);
					newAvatar.setFitWidth(200);
					newAvatar.setFitHeight(200);
					Rectangle clip = new Rectangle(200, 200);
					clip.setArcWidth(10);
					clip.setArcHeight(10);
					newAvatar.setClip(clip);
					avatar=newAvatar;
					serverAvatarBox.getChildren().addAll(avatar, cancelButton);
				}		
			}
		});

		serverAvatarBox.getChildren().addAll(avatar, editServerAvatarButton);

		Button saveChangesButton = new Button("Save Changes");
		saveChangesButton.setOnAction(e -> {

			ServerDB serverDb=new ServerDB();
			serverDb.updateAvatar(selectedImage, new Server(this.server.getServer().getId(), null, 0, 0, null, 0, 0));
		});

		VBox buttonAndPreviewBox = new VBox(10);
		buttonAndPreviewBox.getChildren().addAll(saveChangesButton);
		buttonAndPreviewBox.setAlignment(Pos.CENTER);
		// Creazione di un HBox per il secondo set di elementi (Label e TextField)
		HBox serverNameBox = new HBox(10);
		Label serverNameLabel = new Label("Edit Server Name");
		TextField serverNameField = new TextField();
		serverNameBox.getChildren().addAll(serverNameLabel, serverNameField);
		serverNameBox.setAlignment(Pos.CENTER); // Centra i contenuti orizzontalmente


		// Aggiunta degli HBox al layout principale (VBox)
		dialogLayout.getChildren().addAll(serverAvatarBox, serverNameBox, buttonAndPreviewBox);
		dialogLayout.setAlignment(Pos.CENTER); // Centra gli HBox verticalmente

		CSSLoader cssLoader=new CSSLoader();
		dialogScene.getStylesheets().add(cssLoader.loadCss());

		setScene(dialogScene);
	}
}
