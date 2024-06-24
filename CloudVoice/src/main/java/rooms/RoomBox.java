package rooms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import server.Server;
import settings.CSSColorExtractor;

public class RoomBox extends HBox{
	
	private ChatRoom room;
	private Server server;
	private CSSColorExtractor cssColorExtractor;
	private Label chatName;
	private boolean connected=false;

	public RoomBox(ChatRoom room, Server server) {
		this.room = room;
		this.server=server;
		this.cssColorExtractor=new CSSColorExtractor();
		this.setAlignment(Pos.CENTER);
		this.setPadding(new Insets(3, 0, 3, 0));		
		this.setCursor(Cursor.HAND);

		Color primaryColor=this.cssColorExtractor.findPrimaryColor();
		double red=primaryColor.getRed()*255;
		double green=primaryColor.getGreen()*255;
		double blue=primaryColor.getBlue()*255;
		
		VBox mainContainer=new VBox();
		mainContainer.setAlignment(Pos.CENTER);				
		this.getChildren().add(mainContainer);
		
		chatName=new Label(room.getName());
		mainContainer.getChildren().add(chatName);

		this.setOnMouseEntered(event -> {
			if(this.connected==false) {
				this.setStyle("-fx-background-color: rgba("+red+", "+green+", "+blue+", 0.4);" + "-fx-background-radius: 10;");
			}
		});

		this.setOnMouseExited(event -> {
			if(this.connected==false /*&& this.serverEditContextMenu.isShowing()==false*/) {
				this.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
			}
		});
		
	}
	
	public void setBoxFocused() {
		this.setStyle("-fx-background-color: -primary-color;" + "-fx-background-radius: 10;");
		this.chatName.setStyle("-fx-text-fill: -text-fill-color;");
	}
	
	
	public void disconnect() {
		this.connected=false;
		this.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
		this.chatName.setStyle("-fx-text-fill: -primary-color;");
		//this.chatName.setStyle("-fx-text-fill: -text-fill-color;");//TODO DARK MODE
	}

	public ChatRoom getRoom() {
		return room;
	}

	public void setRoom(ChatRoom room) {
		this.room = room;
	}

	public boolean amIConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}
}