package server;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import main.model.MainWindowModel;
import main.model.SVGCodes;
import settings.CSSColorExtractor;

public class ServerBox extends HBox{

	private Server server;
	private MainWindowModel model;
	private CSSColorExtractor cssColorExtractor;
	private boolean connected=false;
	private boolean isEdiServerDialogOpen=false;
	private Label serverLabel;
	private Button connectedIcon;
	private ContextMenu serverEditContextMenu;

	public ServerBox(Server server, MainWindowModel model) {
		this.server=server;
		this.model=model;
		this.cssColorExtractor=new CSSColorExtractor();

		Color primaryColor=this.cssColorExtractor.findPrimaryColor();
		double red=primaryColor.getRed()*255;
		double green=primaryColor.getGreen()*255;
		double blue=primaryColor.getBlue()*255;

		this.setSpacing(10);
		this.setAlignment(Pos.CENTER_LEFT);
		this.setPadding(new Insets(5, 0, 5, 10));
		this.setCursor(Cursor.HAND);

		this.serverEditContextMenu = new ContextMenu();
		MenuItem editServerItem = new MenuItem("Edit Server");
		this.serverEditContextMenu.setOnHidden(event -> {
			if(this.connected==false && this.isEdiServerDialogOpen==false) {
				this.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
			}
		});

		this.setOnMouseEntered(event -> {
			if(this.connected==false || this.isEdiServerDialogOpen==true && this.connected!=true) {
				this.setStyle("-fx-background-color: rgba("+red+", "+green+", "+blue+", 0.4);" + "-fx-background-radius: 10;");
			}
		});

		this.setOnMouseExited(event -> {
			if(this.connected==false && this.serverEditContextMenu.isShowing()==false && this.isEdiServerDialogOpen==false) {

				this.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
			}
		});

		Node avatarNode=null;

		Image avatar=this.server.getAvatar();
		if(avatar!=null) {
			ImageView avatarView = new ImageView(avatar);
			avatarView.setFitWidth(50); // Larghezza
			avatarView.setFitHeight(50); // Altezza
			Rectangle clip = new Rectangle(50, 50); // Dimensioni dell'avatar quadrato
			clip.setArcWidth(10); // Angoli smussati
			clip.setArcHeight(10); // Angoli smussati
			avatarView.setClip(clip);
			avatarNode=avatarView;
		}else {

			Button button=SVGCodes.createIconButton(SVGCodes.defaultServerAvatarSVG, 50, 50, 27);
			button.setMouseTransparent(true);
			button.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
			avatarNode=button;
		}

		serverLabel = new Label(this.server.getName());

		this.getChildren().addAll(avatarNode, serverLabel);

		setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY) {
				Platform.runLater(() -> {
					model.notifyObserverServerSelection(this.server);
				});
				
				if(this.serverEditContextMenu.isShowing()==true) {
					this.serverEditContextMenu.hide();
				}
				
			} else if (event.getButton() == MouseButton.SECONDARY ) {

				editServerItem.setOnAction(actionEvent -> {
					this.serverEdit();
				});
				serverEditContextMenu.getItems().setAll(editServerItem);
				serverEditContextMenu.show(this, event.getScreenX(), event.getScreenY());
			}
		});
	}

	public void connectBox() {
		SVGPath path=SVGCodes.createSVGGraphic(SVGCodes.defaultUserAvatarSVG, 18);
		path.setTranslateX(-0.4);
		path.setTranslateY(-0.5);
		connectedIcon = new Button();
		connectedIcon.setPickOnBounds(true);
		connectedIcon.setGraphic(path);
		connectedIcon.setAlignment(Pos.CENTER);
		connectedIcon.setMinSize(25, 25);
		connectedIcon.setMaxSize(25, 25);
		connectedIcon.setMouseTransparent(true);
		connectedIcon.setStyle("-fx-background-color: transparent;");
		connectedIcon.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
		Circle circle = new Circle(12.5); 
		connectedIcon.setShape(circle);
		this.connected=true;

		this.getChildren().add(connectedIcon);
	}

	public void setBoxFocused() {
		this.setStyle("-fx-background-color: -primary-color;" + "-fx-background-radius: 10;");
		this.serverLabel.setStyle("-fx-text-fill: -text-fill-color;"+"-fx-font-weight: bold; ");
	}

	public void disconnect() {
		this.connected=false;
		this.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");

		if(this.connectedIcon!=null) {
			this.serverLabel.setStyle("-fx-text-fill: -primary-color;"+"-fx-font-weight: bold; ");
			//this.serverLabel.setStyle("-fx-text-fill: -text-fill-color;"+"-fx-font-weight: bold; ");//TODO DARK MODE
			this.connectedIcon.setStyle("-fx-background-color: -primary-color;");
		}else {
			this.serverLabel.setStyle("-fx-text-fill: -primary-color;");
			//this.serverLabel.setStyle("-fx-text-fill: -text-fill-color;");//TODO DARK MODE
		}
	}

	public void serverEdit() {
		if (isEdiServerDialogOpen) {
			return; // La finestra è già aperta, esci
		}
		isEdiServerDialogOpen = true;

		ServerEditWindow dialog = new ServerEditWindow(this);
		dialog.show();
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public boolean amIConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public Label getServerLabel() {
		return serverLabel;
	}

	public void setServerLabel(Label serverLabel) {
		this.serverLabel = serverLabel;
	}

	public MainWindowModel getModel() {
		return model;
	}

	public void setModel(MainWindowModel model) {
		this.model = model;
	}

	public boolean isEdiServerDialogOpen() {
		return isEdiServerDialogOpen;
	}

	public void setEdiServerDialogOpen(boolean isEdiServerDialogOpen) {
		this.isEdiServerDialogOpen = isEdiServerDialogOpen;
	}

	public Button getConnectedIcon() {
		return connectedIcon;
	}

	public void setConnectedIcon(Button connectedIcon) {
		this.connectedIcon = connectedIcon;
	}
}