package rooms.textRooms.messages;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import main.model.ImageToBlob;
import main.model.MainWindowModel;
import main.model.SVGCodes;
import main.view.CustomAlert;
import rooms.ChatRoom;
import serializedObjects.ChatMessage;

public class ChatContainer extends VBox{

	private MainWindowModel model;
	private VBox messagesBox;
	private HBox inputBox;
	private ChatRoom correspondingRoom;
	private byte[] image;
	private ChatMessageDB messagesDb;
	private ScrollPane scrollPane;
	private LinkedHashMap<Long, MessageBox> messages;
	private boolean isLoadingMessages = false;
	private int offset=0;
	private Entry<Long, MessageBox> lastFirstElement = null;
	private ChangeListener<Number> vvalueChangeListener;
	private HBox labelBox;
	private Long replyMode=(long) 0;

	public ChatContainer(MainWindowModel model, ChatRoom correspondingRoom) {
		this.model=model;
		this.correspondingRoom=correspondingRoom;
		this.messagesDb=new ChatMessageDB();
		this.messages=new LinkedHashMap<Long, MessageBox>();

		this.inputBox = new HBox();
		this.inputBox.setSpacing(5);

		this.messagesBox = new VBox();
		this.messagesBox.setSpacing(10);

		this.inputBox.setPadding(new Insets(13, 15, 10, 15));

		TextField messageField = new TextField();
		messageField.setMinHeight(35);
		HBox.setHgrow(messageField, Priority.ALWAYS);

		HBox imageUploadedBox=new HBox();
		imageUploadedBox.setSpacing(3);
		imageUploadedBox.setPadding(new Insets(5, 0, -7, 15));

		Label imageNameLabel = new Label();
		imageNameLabel.setStyle("-fx-background-color: -primary-color; -fx-text-fill: -text-fill-color; -fx-font-size: 13; -fx-padding: 2;"
				+ "-fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;");
		imageNameLabel.setMinWidth(Label.USE_PREF_SIZE);

		Button cancelUploadButton=new Button("X");
		cancelUploadButton.setCursor(Cursor.HAND);
		cancelUploadButton.setStyle("-fx-font-size: 10px;");
		cancelUploadButton.setMinSize(23, 23);
		cancelUploadButton.setMaxSize(23, 23);
		cancelUploadButton.setOnAction(event -> {
			this.getChildren().remove(imageUploadedBox);
			this.image=null;
		});

		imageUploadedBox.getChildren().addAll(imageNameLabel, cancelUploadButton);

		Button sendButton=SVGCodes.createIconButton(SVGCodes.sendSVG, 35, 35, 23);
		sendButton.setCursor(Cursor.HAND);

		sendButton.setOnAction(event -> {
			String messageText = messageField.getText();

			if(messageText.isEmpty()==false || this.image!=null) {
				ChatMessage object=model.buildMessage(messageText);

				if(image!=null) {
					object.setImage(this.image);
					this.image=null;

					this.getChildren().removeAll(this.scrollPane, imageUploadedBox, this.inputBox);
					this.getChildren().addAll(this.scrollPane, this.inputBox);
				}

				MessageBox message=null;
				if(this.replyMode==0) {
					object=updateDB(object);
					message=buildMessageComponent(object);
					this.messagesBox.getChildren().add(message);
				}else {
					object.setReplyTo(this.replyMode);
					object=updateDB(object);
					message=buildReplyMessageComponent(object);
					this.replyMode=(long) 0;
					this.messagesBox.getChildren().add(message);
					setStoppedLoading();
				}

				sendToServer(object);
				messageField.clear();
				this.scrollPane.setVvalue(1);

			} else{
				CustomAlert emptyMessageAlert=new CustomAlert();
				emptyMessageAlert.createAlert("Error", "Empty Message");
			}
		});

		SVGPath path =SVGCodes.createSVGGraphic(SVGCodes.attachSVG, 26);
		path.setTranslateX(-0.5);
		Button attachPicture = new Button();
		attachPicture.setContentDisplay(ContentDisplay.CENTER);
		attachPicture.setPickOnBounds(true);
		attachPicture.setGraphic(path);
		attachPicture.setAlignment(Pos.CENTER);
		attachPicture.setMinSize(35, 35);
		attachPicture.setMaxSize(35, 35);
		attachPicture.setCursor(Cursor.HAND);

		attachPicture.setOnAction(event -> {
			if(this.image==null) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.getExtensionFilters().addAll(
						new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg")
						);

				Window ownerWindow = getScene().getWindow();
				File selectedFile = fileChooser.showOpenDialog(ownerWindow);
				String imageName=selectedFile.getName();

				if (selectedFile != null) {
					long fileSizeInBytes = selectedFile.length();
					double fileSizeInKB = fileSizeInBytes / 1024.0; // Converti in KB
					double fileSizeInMB = fileSizeInKB / 1024.0; // Converti in MB

					if(fileSizeInMB>5) {
						CustomAlert emptyMessageAlert=new CustomAlert();
						emptyMessageAlert.createAlert("Error", "Image Too Big");
					}

					Image selectedImage = new Image(selectedFile.toURI().toString());
					this.image=ImageToBlob.convertImageToByteArray(selectedImage);

					imageNameLabel.setText(imageName);
					this.getChildren().removeAll(scrollPane, inputBox);
					this.getChildren().addAll(scrollPane, imageUploadedBox, inputBox);
				}
			}
		});

		this.inputBox.getChildren().addAll(messageField, attachPicture, sendButton);

		this.scrollPane = new ScrollPane(this.messagesBox);
		this.scrollPane.fitToWidthProperty().set(true);
		this.scrollPane.setStyle("-fx-background-color: transparent;");

		vvalueChangeListener = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

				if (newValue.doubleValue() == 0.0 && !isLoadingMessages) {

					isLoadingMessages = true;
					offset++;

					if(offset==1) {
						Iterator<Entry<Long, MessageBox>> iterator = getMessages().entrySet().iterator();
						if (iterator.hasNext()) {
							lastFirstElement = iterator.next();
						}
					}else {
						Iterator<Entry<Long, MessageBox>> iterator = getMessages().entrySet().iterator();
						while (iterator.hasNext()) {
							lastFirstElement = iterator.next();
						}
					}

					Task<LinkedHashMap<Long, ChatMessage>> fetchMessagesTask = new Task<>() {

						@Override
						protected LinkedHashMap<Long, ChatMessage> call() throws Exception {
							Platform.runLater(() -> {
								setLoading();
							});
							ChatMessageDB messageDb = new ChatMessageDB();
							return messageDb.fetchMessagesFromRoom(correspondingRoom, offset * 40, false);
						}
					};

					fetchMessagesTask.setOnSucceeded(event -> {
						LinkedHashMap<Long, ChatMessage> messages = fetchMessagesTask.getValue();
						if (!messages.isEmpty()) {

							Task<Void> loadMessagesTask = new Task<>() {
								@Override
								public Void call() {
									List<MessageBox> loadedMessages = new ArrayList<>();
									for (ChatMessage message : messages.values()) {
										MessageBox messageComponent = buildMessageComponent(message);
										getMessages().put(messageComponent.getMessage().getId(), messageComponent);
										loadedMessages.add(messageComponent);
									}

									Platform.runLater(() -> {
										for (MessageBox messageComponent : loadedMessages) {
											messagesBox.getChildren().add(0, messageComponent);
										}

										scrollPane.applyCss();
										scrollPane.layout();

										Bounds boundsInParent = lastFirstElement.getValue().getBoundsInParent();

										// Calculate the vvalue based on the element's position
										double contentHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
										double currentVValue = boundsInParent.getMinY() / contentHeight;

										scrollPane.setVvalue(currentVValue + 0.03);
										setStoppedLoading();
									});
									isLoadingMessages = false;
									return null;
								}
							};
							new Thread(loadMessagesTask).start();
						}else {
							setStoppedLoading();
						}
					});

					new Thread(fetchMessagesTask).start();
				}
			}
		};

		scrollPane.vvalueProperty().addListener(vvalueChangeListener);

		messagesBox.getChildren().addListener((ListChangeListener<Node>) c -> {
			while (c.next()) {
				if (c.wasAdded() && isLoadingMessages==false) {
					scrollPane.applyCss();
					scrollPane.layout();
					scrollPane.setVvalue(1.0);
				}
			}
		});

		VBox.setVgrow(scrollPane, Priority.ALWAYS);
		this.getChildren().addAll(scrollPane, inputBox);

		messageField.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				String messageText = messageField.getText();

				if(messageText.isEmpty()==false || image!=null) {
					ChatMessage object=model.buildMessage(messageText);

					if(image!=null) {
						object.setImage(image);
						image=null;

						this.getChildren().removeAll(scrollPane, imageUploadedBox, inputBox);
						this.getChildren().addAll(scrollPane, inputBox);
					}

					MessageBox message=null;
					if(this.replyMode==0) {
						object=updateDB(object);
						message=buildMessageComponent(object);
						this.messagesBox.getChildren().add(message);
					}else {
						object.setReplyTo(this.replyMode);
						object=updateDB(object);
						message=buildReplyMessageComponent(object);
						this.replyMode=(long) 0;
						this.messagesBox.getChildren().add(message);
						setStoppedLoading();
					}

					sendToServer(object);
					messageField.clear();
					this.scrollPane.setVvalue(1);

				} else{
					CustomAlert emptyMessageAlert=new CustomAlert();
					emptyMessageAlert.createAlert("Error", "Empty Message");
				}
			}else if (event.getCode() == KeyCode.V && event.isControlDown()) {
				Clipboard clipboard = Clipboard.getSystemClipboard();
				if (clipboard.hasFiles()) {
					File selectedFile =clipboard.getFiles().getFirst();

					if(checkFileExtension(selectedFile)==true) {

						Image selectedImage = new Image(selectedFile.toURI().toString());
						image=ImageToBlob.convertImageToByteArray(selectedImage);

						imageNameLabel.setText(selectedFile.getName());
						this.getChildren().removeAll(scrollPane, inputBox);
						this.getChildren().addAll(scrollPane, imageUploadedBox, inputBox);
					}
				}else if(clipboard.hasImage()) {

					image=ImageToBlob.convertImageToByteArray(clipboard.getImage());
					imageNameLabel.setText("image");
					this.getChildren().removeAll(scrollPane, inputBox);
					this.getChildren().addAll(scrollPane, imageUploadedBox, inputBox);
				}
			}
		});
	}

	public void setLoading() {
		labelBox=new HBox();
		Label loadingLabel = new Label("Loading more messages...");
		loadingLabel.setStyle("-fx-background-color: -primary-color; -fx-text-fill: -text-fill-color; -fx-font-size: 13; -fx-padding: 2;"
				+ "-fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;");
		labelBox.getChildren().add(loadingLabel);
		labelBox.setPadding(new Insets(5, 0, -8, 15));
		this.getChildren().removeAll(scrollPane, inputBox);
		this.getChildren().addAll(scrollPane, labelBox, inputBox);
	}

	public void setReplying(long messageId) {
		this.replyMode=messageId;
		String userToReply=this.messages.get(replyMode).getMessage().getUsername();
		labelBox=new HBox();
		Label loadingLabel = new Label("Replying to: "+userToReply);
		loadingLabel.setStyle("-fx-background-color: -primary-color; -fx-text-fill: -text-fill-color; -fx-font-size: 13; -fx-padding: 2;"
				+ "-fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;");
		labelBox.getChildren().add(loadingLabel);
		labelBox.setPadding(new Insets(5, 0, -8, 15));
		this.getChildren().removeAll(scrollPane, inputBox);
		this.getChildren().addAll(scrollPane, labelBox, inputBox);
	}


	public void setStoppedLoading() {
		this.getChildren().removeAll(scrollPane, labelBox, inputBox);
		this.getChildren().addAll(scrollPane, inputBox);
	}

	public boolean checkFileExtension(File selectedFile) {
		String fileName = selectedFile.getName().toLowerCase();
		return fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg");
	}

	public ChatMessage updateDB(ChatMessage object) {
		return messagesDb.insertMessage(object);
	}

	public void sendToServer(ChatMessage object) {
		Thread sendMessageThread = new Thread(new Runnable() {
			public void run() {
				model.getMessageManager().sendMessage(object);
			}
		});
		sendMessageThread.setDaemon(true);
		sendMessageThread.start();
	}

	public MessageBox buildMessageComponent(ChatMessage message) {
		MessageBox messageComponent = new MessageBox(this.model, message, this);
		messages.put(message.getId(), messageComponent);
		return messageComponent;
	}

	public MessageBox buildReplyMessageComponent(ChatMessage message) {
		ReplyMessageBox replyMessage=new ReplyMessageBox(this.model, message, this, this.messages.get(message.getReplyTo()));
		messages.put(message.getId(), replyMessage);
		return replyMessage;
	}

	public VBox getMessagesBox() {
		return messagesBox;
	}

	public void setMessagesBox(VBox messagesBox) {
		this.messagesBox = messagesBox;
	}

	public HBox getInputBox() {
		return inputBox;
	}

	public void setInputBox(HBox inputBox) {
		this.inputBox = inputBox;
	}

	public ChatRoom getCorrespondingRoom() {
		return correspondingRoom;
	}

	public void setCorrespondingRoom(ChatRoom correspondingRoom) {
		this.correspondingRoom = correspondingRoom;
	}

	public LinkedHashMap<Long, MessageBox> getMessages() {
		return messages;
	}

	public void setMessages(LinkedHashMap<Long, MessageBox> messages) {
		this.messages = messages;
	}

	public Long getReplyMode() {
		return replyMode;
	}

	public void setReplyMode(Long replyMode) {
		this.replyMode = replyMode;
	}

	public ScrollPane getScrollPane() {
		return scrollPane;
	}

	public void setScrollPane(ScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}
}