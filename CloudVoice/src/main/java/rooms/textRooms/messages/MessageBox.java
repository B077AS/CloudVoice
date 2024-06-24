package rooms.textRooms.messages;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import main.model.ImageToBlob;
import main.model.MainWindowModel;
import main.model.SVGCodes;
import serializedObjects.ChatMessage;
import serializedObjects.UpdateChatMessageObject;
import settings.CSSColorExtractor;
import user.UserDB;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageBox extends HBox {
	
	private static final Map<Integer, Image> avatarCache = new HashMap<>();
	private MainWindowModel model;
	private ChatContainer container;
	private ChatMessage message;
	private TextArea messageText;
	private VBox messageBox;
	private final int maxPictureHeight=300;
	private CSSColorExtractor cssColorExtractor;
	private ContextMenu userContextMenu;
	private ImageView imageView;
	private boolean editMode=false;
	private ArrayList<String> urlList;

	public MessageBox(MainWindowModel model, ChatMessage message, ChatContainer container) {
		this.container=container;
		this.model=model;
		this.message=message;
		this.cssColorExtractor=new CSSColorExtractor();
		this.urlList=new ArrayList<>();
		Color primaryColor=this.cssColorExtractor.findPrimaryColor();
		double red=primaryColor.getRed()*255;
		double green=primaryColor.getGreen()*255;
		double blue=primaryColor.getBlue()*255;

		extractAndCheckURLs(message.getContent());

		this.setAlignment(Pos.TOP_LEFT);
		this.setPadding(new Insets(5, 0, 3, 15));

		this.userContextMenu = new ContextMenu();
		MenuItem editMenuItem = new MenuItem("Edit");
		MenuItem deleteMenuItem = new MenuItem("Delete");
		MenuItem copyMenuItem = new MenuItem("Copy");
		//MenuItem copyImageMenuItem = new MenuItem("Copy Image");
		MenuItem replyMenuItem = new MenuItem("Reply");
		this.userContextMenu.getItems().addAll(replyMenuItem, editMenuItem, copyMenuItem);

		if(this.message.getSenderId()==model.getUser().getId()) {//TODO DA METTERE !=
			this.userContextMenu.getItems().add(deleteMenuItem);
		}

		this.userContextMenu.setOnHidden(event -> {
			if (!this.isHover() && this.editMode==false) {
				this.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
				this.messageText.lookup(".content").setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
			}
		});

		editMenuItem.setOnAction(editEvent -> {
			if(this.message.getSenderId()==this.model.getUser().getId() &&this.editMode==false) {
				this.editMessage(model.getMessageManager().getSocket());						
				this.container.getMessages().replace(this.message.getId(), this);
			}
		});				

		deleteMenuItem.setOnAction(deleteEvent -> {
			this.deleteMessage(this.model.getMessageManager().getSocket());					
			this.container.getMessagesBox().getChildren().remove(this);
			this.container.getMessages().remove(this.message.getId());
		});

		copyMenuItem.setOnAction(copyEvent -> {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Clipboard clipboard = toolkit.getSystemClipboard();
			StringSelection stringSelection = new StringSelection(this.message.getContent());
			if(this.messageText.getSelectedText().equals("")) {
				stringSelection = new StringSelection(this.message.getContent());
			}else {
				stringSelection = new StringSelection(this.messageText.getSelectedText());
			}		
			clipboard.setContents(stringSelection, null);
		});

		replyMenuItem.setOnAction(replyEvent -> {
			this.container.setReplying(this.message.getId());
		});

		this.setOnMouseClicked(clickEvent -> {
			if (clickEvent.getButton() == MouseButton.SECONDARY) {
				if(this.editMode==false) {
					userContextMenu.show(this, clickEvent.getScreenX(), clickEvent.getScreenY());
				}
			}else {
				if(this.editMode==false) {
					userContextMenu.hide();
				}
			}
		});

		this.userContextMenu.setOnShowing(event -> {
			this.setStyle("-fx-background-color: rgba("+red+", "+green+", "+blue+", 0.4);" + "-fx-background-radius: 10;");
			this.messageText.lookup(".content").setStyle("-fx-background-color: rgba("+red+", "+green+", "+blue+", 0.4);" + "-fx-background-radius: 0;" );
		});

		this.setOnMouseEntered(event -> {
			this.setStyle("-fx-background-color: rgba("+red+", "+green+", "+blue+", 0.4);" + "-fx-background-radius: 10;");
			this.messageText.lookup(".content").setStyle("-fx-background-color: rgba("+red+", "+green+", "+blue+", 0.4);" + "-fx-background-radius: 0;" );

		});

		this.setOnMouseExited(event -> {
			if(this.userContextMenu.isShowing()==false && this.editMode==false /*&& event.isPrimaryButtonDown()==false*/) {
				this.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
				this.messageText.lookup(".content").setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
			}
		});

		Label userNameLabel = new Label(message.getUsername());
		userNameLabel.setStyle("-fx-font-weight: bold;");

		Label timeLabel = new Label(message.getTime());
		timeLabel.setStyle("-fx-font-size: 10px;");		

		messageText =new TextArea(message.getContent());
		messageText.setWrapText(true);
		messageText.setPadding(new Insets(-7, 0, 0, -2));
		messageText.getStyleClass().add("custom-message-text");
		messageText.setEditable(false);
		messageText.setContextMenu(userContextMenu);
		messageText.setMinHeight(0);

		messageText.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal) { // If TextArea loses focus
				messageText.deselect(); // Deselect the text
			}
		});

		messageText.sceneProperty().addListener((observableNewScene, oldScene, newScene) -> {
			if (newScene != null) {
				messageText.applyCss();
				Node text = lookup(".text");

				messageText.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> {
					double currentHeight = messageText.getFont().getSize() + text.getBoundsInLocal().getHeight();
					return currentHeight-15; // 
				}, text.boundsInLocalProperty()));
			}
		});


		HBox userInfoVBox = new HBox(10, userNameLabel, timeLabel);
		userInfoVBox.setAlignment(Pos.CENTER_LEFT);

		messageBox = new VBox();
		messageBox.setSpacing(5);

		if(message.getImage()!=null) {
			Image image=ImageToBlob.byteArrayToImage(message.getImage());

			imageView=new ImageView(image);

			if(image.getHeight()<maxPictureHeight) {
				imageView.setFitHeight(image.getHeight());
			}else {
				imageView.setFitHeight(maxPictureHeight);
			}
			imageView.setPreserveRatio(true);

			messageBox.getChildren().addAll(imageView, messageText);
		}else {			
			messageBox.getChildren().addAll(messageText);
		}

		VBox rightBox = new VBox(1, userInfoVBox, messageBox);
		rightBox.setPadding(new Insets(-5, 0, 0, 0));

		setSpacing(10);
		Image cachedAvatar = avatarCache.get(message.getSenderId());
		if (cachedAvatar != null) {
			ImageView avatarImageView = createAvatarImageView(cachedAvatar);
			getChildren().addAll(avatarImageView, rightBox);
		} else {
			loadAndCacheAvatar(message.getSenderId(), rightBox);
		}

		messageText.prefWidthProperty().bind(this.widthProperty());
	}

	private void loadAndCacheAvatar(int userId, VBox messageVBox) {

		UserDB userDb=new UserDB();
		byte[] avatarArray=userDb.fetchAvatarAsBytesFromUserId(userId);

		Image avatar = ImageToBlob.byteArrayToImage(avatarArray);
		if(avatar!=null) {
			avatarCache.put(userId, avatar);

			ImageView avatarImageView = createAvatarImageView(avatar);
			getChildren().addAll(avatarImageView, messageVBox);
		}else {
			Button button=SVGCodes.createIconButton(SVGCodes.defaultUserAvatarSVG, 40, 40, 25);
			button.setMouseTransparent(true);
			button.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
			button.setStyle("-fx-background-radius: 50%;");
			getChildren().addAll(button, messageVBox);
		}
	}

	private ImageView createAvatarImageView(Image image) {
		ImageView avatarImageView = new ImageView(image);
		avatarImageView.setFitWidth(40);
		avatarImageView.setFitHeight(40);
		avatarImageView.setClip(new Circle(20, 20, 20));
		return avatarImageView;
	}


	public void deleteMessage(Socket socket) {
		ChatMessageDB messageDb=new ChatMessageDB();
		messageDb.deleteMessage(message);

		sendUpdateMessage(socket, new UpdateChatMessageObject(true, message.getId()));
	}


	public MessageBox editMessage(Socket socket) {
		this.editMode=true;
		double width=messageText.getWidth();
		messageBox.getChildren().remove(messageText);
		TextField editMessageField=new TextField();
		editMessageField.setMinWidth(width);
		editMessageField.setText(message.getContent());
		Platform.runLater(() -> editMessageField.requestFocus());

		HBox buttonsBox=new HBox();
		buttonsBox.setSpacing(5);
		buttonsBox.setAlignment(Pos.CENTER_LEFT);

		Button saveButton=new Button("Save");
		saveButton.setCursor(Cursor.HAND);
		saveButton.setOnAction(event ->{
			String newContent=editMessageField.getText();
			messageBox.getChildren().removeAll(editMessageField, buttonsBox);
			messageText.setText(newContent);
			message.setContent(newContent);
			messageBox.getChildren().add(messageText);

			ChatMessageDB messageDb=new ChatMessageDB();
			messageDb.updateMessage(message);

			sendUpdateMessage(socket, new UpdateChatMessageObject(true, message.getId(), newContent));
			this.editMode=false;

		});
		Button cancelButton=new Button("Cancel");
		cancelButton.setCursor(Cursor.HAND);
		cancelButton.setOnAction(event ->{
			messageBox.getChildren().removeAll(editMessageField, buttonsBox);
			messageBox.getChildren().add(messageText);
			this.editMode=false;
		});
		buttonsBox.getChildren().addAll(saveButton, cancelButton);

		messageBox.getChildren().addAll(editMessageField, buttonsBox);

		editMessageField.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ESCAPE) {
				messageBox.getChildren().removeAll(editMessageField, buttonsBox);
				messageBox.getChildren().add(messageText);
				this.editMode=false;
			}
		});

		return this;
	}

	public void sendUpdateMessage(Socket socket, UpdateChatMessageObject object) {
		try {

			OutputStream outputStream=socket.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void extractAndCheckURLs(String str) {
		String regex = "\\b((?:https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(str);

		if (matcher.find()) {
			urlList.add(matcher.group());
		}
	}

	public static Map<Integer, Image> getAvatarcache() {
		return avatarCache;
	}

	public ChatMessage getMessage() {
		return message;
	}

	public void setMessage(ChatMessage message) {
		this.message = message;
	}

	public TextArea getMessageText() {
		return messageText;
	}

	public void setMessageText(TextArea messageText) {
		this.messageText = messageText;
	}

	public VBox getMessageBox() {
		return messageBox;
	}

	public void setMessageBox(VBox messageBox) {
		this.messageBox = messageBox;
	}

	public ImageView getImageView() {
		return imageView;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}
}