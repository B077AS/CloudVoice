package rooms.textRooms.messages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Rotate;
import main.model.MainWindowModel;
import main.model.SVGCodes;
import serializedObjects.ChatMessage;

public class ReplyMessageBox extends MessageBox{
	
	private MessageBox mainMessage;

	public ReplyMessageBox(MainWindowModel model, ChatMessage message, ChatContainer container, MessageBox mainMessage) {
		super(model, message, container);
		this.mainMessage=mainMessage;
		
		VBox mainBox=new VBox();
		mainBox.setSpacing(5);
		Node avatar=this.getChildren().get(0);
		Node rightBox=this.getChildren().get(1);
		this.getChildren().clear();
		
		HBox bottomBox=new HBox(avatar, rightBox);
		bottomBox.setSpacing(10);		
		
		Node originalAvatar=null;
		Image userAvatar=ReplyMessageBox.getAvatarcache().get(this.mainMessage.getMessage().getSenderId());
		if(userAvatar!=null) {
			Circle clip = new Circle(13, 13, 13);
			clip.setStroke(Color.TRANSPARENT);
			clip.setStrokeWidth(2);
			clip.setFill(new ImagePattern(userAvatar));
			originalAvatar=clip;
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
			originalAvatar=button;
		}
		originalAvatar.setTranslateY(-1);
		
		HBox topBox=new HBox();
		Label topText=new Label(mainMessage.getMessage().getContent());
		
		HBox topTextContainerBox=new HBox();
		topTextContainerBox.getChildren().add(topText);
		topTextContainerBox.setPadding(new Insets(-1, 0, 0, 0));
		
		SVGPath verticalPath=SVGCodes.createSVGGraphic(SVGCodes.lineSVG, 18);
		verticalPath.setStyle("-fx-fill: #404040;");
		HBox verticalBox=new HBox();
		verticalBox.setPadding(new Insets(-21, 0, 0, 11));
		verticalBox.setScaleY(1.5);
		verticalBox.getChildren().add(verticalPath);		
        Rotate rotate = new Rotate(90);
        verticalBox.getTransforms().add(rotate);
        
		SVGPath horizontalPath=SVGCodes.createSVGGraphic(SVGCodes.lineSVG, 18);
		horizontalPath.setScaleX(1.5);
		horizontalPath.setStyle("-fx-fill: #404040;");
		HBox horizontalBox=new HBox();
		HBox.setMargin(horizontalPath, new Insets(11, 0, 0, -8));
		horizontalBox.getChildren().add(horizontalPath);
		
		HBox exMessage=new HBox();
		exMessage.setSpacing(5);
		exMessage.setAlignment(Pos.CENTER_LEFT);
		exMessage.getChildren().addAll(originalAvatar, topTextContainerBox);
		
		topBox.getChildren().addAll(verticalBox, horizontalPath, exMessage);
		
		mainBox.getChildren().addAll(topBox, bottomBox);
		this.getChildren().add(mainBox);
	}
}