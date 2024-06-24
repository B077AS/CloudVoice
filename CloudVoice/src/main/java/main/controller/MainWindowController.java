package main.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import main.model.MainWindowModel;
import main.model.ModelObserver;
import main.model.PingGraphBox;
import main.model.SVGCodes;
import main.view.CustomAlert;
import main.view.MainWindow;
import rooms.ChatRoom;
import rooms.ChatRoomDB;
import rooms.RoomBox;
import rooms.RoomContainerBox;
import rooms.textRooms.MessagesManager;
import rooms.textRooms.messages.ChatContainer;
import rooms.textRooms.messages.ChatMessageDB;
import rooms.textRooms.messages.MessageBox;
import rooms.voiceRooms.AudioManager;
import rooms.voiceRooms.VoiceRoom;
import serializedObjects.ChatMessage;
import serializedObjects.PingObject;
import serializedObjects.RoomsEnum;
import serializedObjects.StopPingObject;
import serializedObjects.UpdateMessage;
import serializedObjects.UserDetailsObject;
import server.Server;
import server.ServerBox;
import user.ConnectedUserBox;
import user.UsersListView;

public class MainWindowController implements ModelObserver{

	private MainWindowModel model;
	private MainWindow view;

	public MainWindowController(MainWindowModel model, MainWindow view) {
		this.model = model;
		this.view = view;
		this.model.addObserver(this);
	}

	public void startController() {
		view.getSplitPane().setDividerPositions(0.19);
		setListeners();
		this.view.getPrimaryStage().setOnCloseRequest(event -> {
			closureOperations();
		});
	}

	public void setListeners() {

		Thread loadServersThread = new Thread(() -> {

			ObservableList<ServerBox> serversList=model.checkServersOrder(view.getServerListView());

			for (ServerBox entry : serversList) {
				view.getServersBox().getServersList().put(entry.getServer().getId(), entry);
			}

			view.getServerListView().setItems(serversList);
			setDividerPosition();

		});
		loadServersThread.setDaemon(true);
		loadServersThread.start();


		view.getServerListView().setFocusTraversable(false);
		view.getServerListView().setCellFactory(new Callback<ListView<ServerBox>, ListCell<ServerBox>>() {
			@Override
			public ListCell<ServerBox> call(ListView<ServerBox> param) {
				return new ListCell<ServerBox>() {
					@Override
					protected void updateItem(ServerBox server, boolean empty) {
						try {
							super.updateItem(server, empty);
							int currentIndex = getIndex();
							if (server != null && !empty) {
								setStyle("-fx-padding: 1 0 1px 0;");
								setGraphic(server);

								setOnDragDetected(event -> {
									if (!isEmpty()) {
										Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
										ClipboardContent content = new ClipboardContent();
										content.putString(String.valueOf(currentIndex));
										dragboard.setContent(content);
									}
								});

								setOnDragOver(event -> {
									if (event.getGestureSource() != this && event.getDragboard().hasString()) {
										event.acceptTransferModes(TransferMode.MOVE);
									}
									event.consume();
								});

								setOnDragDropped(event -> {
									Dragboard dragboard = event.getDragboard();
									boolean success = false;

									if (dragboard.hasString()) {
										int draggedIndex = Integer.parseInt(dragboard.getString());

										ObservableList<ServerBox> children = view.getServerListView().getItems();
										ServerBox draggedNode = children.get(draggedIndex);
										ServerBox targetNode = children.get(currentIndex);

										children.remove(draggedNode);
										children.remove(targetNode);

										if(currentIndex<draggedIndex) {
											children.add(currentIndex, draggedNode);
											children.add(draggedIndex, targetNode);
										}else {
											children.add(draggedIndex, targetNode);
											children.add(currentIndex, draggedNode);
										}				

										success = true;
									}

									event.setDropCompleted(success);
									event.consume();
								});

							} else {
								setGraphic(null);
							}
						}catch(Exception ex) {
							ex.printStackTrace();
						}
					}
				};
			}
		});
		view.getServersBox().getChildren().add(view.getServerListView());
		view.getServerListPanel().setTop(view.getServersBox());

		microphoneButtonListener();
		speakersButtonListener();

		view.getWifiButton().setOnAction(event ->{

			if(model.isPingMenuOpen()==false) {
				model.setPingMenuOpen(true);
				view.setPingContextMenu(new ContextMenu());
				view.getPingContextMenu().setId("pingContextMenuItem");
				view.getPingContextMenu().setStyle("-fx-background-color: #e0e0e0; -fx-effect: null; -fx-padding: 0 0 0 0; -fx-background-radius: 10;");			

				MenuItem pingMenuItem = new MenuItem();
				pingMenuItem.setId("pingMenuItem");

				PingGraphBox ping=new PingGraphBox(305, 150);
				pingMenuItem.setGraphic(ping);

				view.getPingContextMenu().getItems().addAll(pingMenuItem);
				view.getPingContextMenu().show(view.getWifiButton(), Side.TOP, -115, -20);
				ping.startPinging();

				view.getPingContextMenu().setOnHidden(eventHide -> {
					model.setPingMenuOpen(false);
					ping.stopPinging();
				});
			}else {
				model.setPingMenuOpen(false);
				MenuItem pingMenuItem=(MenuItem)view.getPingContextMenu().getItems().get(0);
				PingGraphBox ping=(PingGraphBox)pingMenuItem.getGraphic();
				ping.stopPinging();
				view.getPingContextMenu().hide();
			}
		});

		view.getWifiButton().setCursor(Cursor.HAND);
		view.getDisconnectButton().setCursor(Cursor.HAND);
		view.getScreenShareButton().setCursor(Cursor.HAND);
		view.getSettingsButton().setCursor(Cursor.HAND);

		view.getDisconnectButton().setOnAction(event ->{
			
			view.getServersBox().getServersList().get(model.getCurrentServer().getId()).getChildren().remove(view.getServersBox().getServersList().get(model.getCurrentServer().getId()).getConnectedIcon());
			view.getServersBox().getServersList().get(model.getCurrentServer().getId()).setConnectedIcon(null);
			view.getServersBox().getServersList().get(model.getCurrentServer().getId()).disconnect();
			
			try {
			model.getUser().getUserDetails().setMainRoomId(model.getCurrentRoom().getID());
			model.disconnectFromChatRoom();
			}catch (Exception e) {
			}
			
			view.setChatAndRoomsPanel(new VBox());
			view.getSplitPane().getItems().set(1, view.getChatAndRoomsPanel());
			view.setButtomSplitContainer(new VBox());

			model.setCurrentRoom(null);
			model.setCurrentServer(null);
		});

		view.getScreenShareButton().setOnAction(event ->{
			String osName = System.getProperty("os.name").toLowerCase();

			if (osName.contains("linux")) {
				CustomAlert invalidCredentialsAlert=new CustomAlert();
				invalidCredentialsAlert.createAlert("Invalid OS", "This feature is not available on Linux yet.");
			} else {
				model.openScreenShareLister(view);
			}

		});

		view.getSettingsButton().setOnAction(event ->{
			view.getRotateTransition().stop(); // Stop the rotation animation when mouse exits			
			view.getSettingsButton().getGraphic().getTransforms().clear(); // Ensure the graphic is reset to its original position
			model.openSettings(view);
		});

		view.getSettingsButton().setOnMouseEntered(event -> {
			view.getRotateTransition().play(); // Start the rotation animation when mouse enters
		});

		view.getSettingsButton().setOnMouseExited(event -> {
			view.getRotateTransition().stop(); // Stop the rotation animation when mouse exits			
			view.getSettingsButton().getGraphic().getTransforms().clear(); // Ensure the graphic is reset to its original position
		});

	}

	public void microphoneButtonListener() {
		view.getMicrophoneButton().setCursor(Cursor.HAND);

		view.getMicrophoneButton().setOnAction(event ->{

			if(model.getUser().getUserDetails().isMicrophoneOn()==false && model.getUser().getUserDetails().isAudioOn()==true) {
				model.getUser().getUserDetails().setMicrophoneOn(true);
				view.getMicrophoneButton().setGraphic(SVGCodes.createSVGGraphic(SVGCodes.onMicrophoneSVG, 25));

				if(model.getAudioManager()!=null) {
					if(model.getUser().getSettings().isPushToTalk()==false) {
						model.getAudioManager().resumeMicrophoneTransmission();
					}

					model.getUser().getUserDetails().setMainRoomId(model.getCurrentRoom().getID());
					model.updateUserDetails(model.getUser().getUserDetails());
				}

			}else if(model.getUser().getUserDetails().isMicrophoneOn()==false && model.getUser().getUserDetails().isAudioOn()==false){
				model.getUser().getUserDetails().setMicrophoneOn(true);
				view.getMicrophoneButton().setGraphic(SVGCodes.createSVGGraphic(SVGCodes.onMicrophoneSVG, 25));
				model.getUser().getUserDetails().setAudioOn(true);
				view.getHeadphonesButton().setGraphic(SVGCodes.createSVGGraphic(SVGCodes.onHeadphonesSVG, 25));

				if(model.getAudioManager()!=null) {
					model.getAudioManager().resumeAudioReception();
					if(model.getUser().getSettings().isPushToTalk()==false) {
						model.getAudioManager().resumeMicrophoneTransmission();
					}

					model.getUser().getUserDetails().setMainRoomId(model.getCurrentRoom().getID());
					model.updateUserDetails(model.getUser().getUserDetails());
				}

			}else {
				model.getUser().getUserDetails().setMicrophoneOn(false);
				view.getMicrophoneButton().setGraphic(SVGCodes.createSVGGraphic(SVGCodes.offMicrophoneSVG, 25));

				if(model.getAudioManager()!=null) {
					model.getAudioManager().pauseMicrophoneTransmission();

					model.getUser().getUserDetails().setMainRoomId(model.getCurrentRoom().getID());
					model.updateUserDetails(model.getUser().getUserDetails());
				}

			}
		});
	}

	public void speakersButtonListener() {
		view.getHeadphonesButton().setCursor(Cursor.HAND);

		view.getHeadphonesButton().setOnAction(event ->{

			if(model.getUser().getUserDetails().isAudioOn()==true) {
				model.getUser().getUserDetails().setAudioOn(false);
				model.getUser().getUserDetails().setMicrophoneOn(false);
				view.getHeadphonesButton().setGraphic(SVGCodes.createSVGGraphic(SVGCodes.offHeadphonesSVG, 25));
				view.getMicrophoneButton().setGraphic(SVGCodes.createSVGGraphic(SVGCodes.offMicrophoneSVG, 25));

				if(model.getAudioManager()!=null) {
					model.getUser().getUserDetails().setMainRoomId(model.getCurrentRoom().getID());
					model.updateUserDetails(model.getUser().getUserDetails());

					model.getAudioManager().pauseAudioReception();
					model.getAudioManager().pauseMicrophoneTransmission();
				}
			}else {
				model.getUser().getUserDetails().setAudioOn(true);
				model.getUser().getUserDetails().setMicrophoneOn(true);
				view.getMicrophoneButton().setGraphic(SVGCodes.createSVGGraphic(SVGCodes.onMicrophoneSVG, 25));
				view.getHeadphonesButton().setGraphic(SVGCodes.createSVGGraphic(SVGCodes.onHeadphonesSVG, 25));

				if(model.getAudioManager()!=null) {
					model.getUser().getUserDetails().setMainRoomId(model.getCurrentRoom().getID());
					model.updateUserDetails(model.getUser().getUserDetails());

					model.getAudioManager().resumeAudioReception();
					if(model.getUser().getSettings().isPushToTalk()==false) {
						model.getAudioManager().resumeMicrophoneTransmission();
					}
				}
			}			
		});
	}

	@Override
	public void setDividerPosition() {
		if(model.getUser().getSettings().getSplitNum()!=0) {
			view.getSplitPane().setDividerPositions(model.getUser().getSettings().getSplitNum());
		}else {
			view.getSplitPane().setDividerPositions(0.2);
		}
	}

	@Override
	public void handleServerSelection(Server server) {

		boolean needRequest=false;

		if(model.getCurrentRoom()==null) {//se non sono connesso a nessuna stanza non ho una connessione con il server			
			if(model.getCurrentServer()==null) {//controllo se ho gia selezionato un server a cui mi piacerebbe connettermi

				model.setCurrentServer(server);
				view.getServersBox().getServersList().get(server.getId()).setConnected(true);

			}else {//se c'era gia un server a cui volevo connettermi lo sostituisco				
				if(model.getCurrentServer().getId()!=server.getId()) {//se è sempre lo stesso non faccio nulla
					view.getServersBox().getServersList().get(model.getCurrentServer().getId()).disconnect();//resetto lo stile

					model.setCurrentServer(server);
					view.getServersBox().getServersList().get(server.getId()).setConnected(true);
				}
			}
		}else {//c'è una stanza quindi ho una connessione con il server
			view.getServersBox().getServersList().get(model.getCurrentServer().getId()).disconnect();

			if(view.getServersBox().getFakeConnected()!=null) {//ho bisogno di una nuova variabile da usare per vedere a quale server vorrei connettermi, se è occupata vuol dire che ho gia scelto un server a cui vorrei connettermi ma lo voglio cambiare

				if(model.getCurrentServer().getId()!=server.getId() && view.getServersBox().getFakeConnected().getServer().getId()!=server.getId()) {

					view.getServersBox().getFakeConnected().disconnect();
					view.getServersBox().setFakeConnected(view.getServersBox().getServersList().get(server.getId()));
					view.getServersBox().getServersList().get(server.getId()).setConnected(true);				
				}else {
					needRequest=true;
					view.getServersBox().getFakeConnected().disconnect();
					view.getServersBox().setFakeConnected(null);

					server=model.getCurrentServer();
					view.getServersBox().getServersList().get(model.getCurrentServer().getId()).setConnected(true);
				}				
			}else {
				if(model.getCurrentServer().getId()!=server.getId()) {
					view.getServersBox().setFakeConnected(view.getServersBox().getServersList().get(server.getId()));
					view.getServersBox().getServersList().get(server.getId()).setConnected(true);				
				}else {
					needRequest=true;
					server=model.getCurrentServer();
					view.getServersBox().getServersList().get(model.getCurrentServer().getId()).setConnected(true);
				}
			}
		}

		view.getServersBox().getServersList().get(server.getId()).setBoxFocused();

		view.getChatAndRoomsPanel().getChildren().clear();

		VBox chatRoomListPanel=new VBox();

		ChatRoomDB chatRoomDB = new ChatRoomDB();
		server.setAllRooms(chatRoomDB.selectChatListFromServer(server));

		view.setRoomContainerBox(new RoomContainerBox());

		ObservableList<RoomBox> observableChatRoomList = FXCollections.observableArrayList();

		for(Entry<Integer, ChatRoom> room: server.getAllRooms().entrySet()) {
			RoomBox roomBox=new RoomBox(room.getValue(), server);
			observableChatRoomList.add(roomBox);
			view.getRoomContainerBox().getRoomsList().put(room.getKey(), roomBox);
		}

		view.setChatRoomListView(new ListView<RoomBox>(observableChatRoomList));
		view.getChatRoomListView().setFocusTraversable(false);
		view.getChatRoomListView().setCellFactory(new Callback<ListView<RoomBox>, ListCell<RoomBox>>() {
			@Override
			public ListCell<RoomBox> call(ListView<RoomBox> param) {
				return new ListCell<RoomBox>() {
					@Override
					protected void updateItem(RoomBox chatRoom, boolean empty) {
						int currentIndex = getIndex();
						super.updateItem(chatRoom, empty);
						setStyle("-fx-padding: 1 0 1px 0;");
						if (chatRoom != null && !empty) {
							setGraphic(chatRoom);
						} else {
							setGraphic(null);
						}

						setOnDragDetected(event -> {
							if (!isEmpty()) {
								Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
								ClipboardContent content = new ClipboardContent();
								content.putString(String.valueOf(currentIndex));
								dragboard.setContent(content);
							}
						});

						setOnDragOver(event -> {
							if (event.getGestureSource() != this && event.getDragboard().hasString()) {
								event.acceptTransferModes(TransferMode.MOVE);
							}
							event.consume();
						});

						setOnDragDropped(event -> {
							Dragboard dragboard = event.getDragboard();
							boolean success = false;

							if (dragboard.hasString()) {
								int draggedIndex = Integer.parseInt(dragboard.getString());

								ObservableList<RoomBox> children = view.getChatRoomListView().getItems();
								RoomBox draggedNode = children.get(draggedIndex);
								RoomBox targetNode = children.get(currentIndex);

								children.remove(draggedNode);
								children.remove(targetNode);

								if(currentIndex<draggedIndex) {
									children.add(currentIndex, draggedNode);
									children.add(draggedIndex, targetNode);
								}else {
									children.add(draggedIndex, targetNode);
									children.add(currentIndex, draggedNode);
								}				

								success = true;
							}

							event.setDropCompleted(success);
							event.consume();
						});
					}
				};
			}
		});

		if(model.getCurrentRoom()!=null && view.getRoomContainerBox().getRoomsList().get(model.getCurrentRoom().getID())!=null /*&& model.getCurrentServer().getId()==view.getChatRoomListView().getItems().get(model.getCurrentChatRoomIndex()).getServer().getId()*/) {

			view.getRoomContainerBox().getRoomsList().get(model.getCurrentRoom().getID()).setConnected(true);
			view.getRoomContainerBox().getRoomsList().get(model.getCurrentRoom().getID()).setBoxFocused();
		}

		view.getChatRoomListView().setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY) {
				// Only respond to left mouse button clicks
				ChatRoom newValue = view.getChatRoomListView().getSelectionModel().getSelectedItem().getRoom();
				if (newValue != null) {

					if(model.getCurrentRoom()!=null && view.getChatRoomListView().getSelectionModel().getSelectedItem().getRoom().getID()!=model.getCurrentRoom().getID()) {
						view.getRoomContainerBox().getRoomsList().get(model.getCurrentRoom().getID()).disconnect();
					}					
					view.getChatRoomListView().getSelectionModel().getSelectedItem().setConnected(true);
					view.getChatRoomListView().getSelectionModel().getSelectedItem().setBoxFocused();

					model.setCurrentServer(view.getChatRoomListView().getSelectionModel().getSelectedItem().getServer());

					Platform.runLater(() -> {
						handleChatRoomSelection(newValue);
					});
				}
			}
		});

		view.getRoomContainerBox().getChildren().add(view.getChatRoomListView());
		chatRoomListPanel.getChildren().add(view.getRoomContainerBox());

		view.setUsersListView(new UsersListView());
		view.getUsersListView().setMinWidth(100);
		view.setTopSplitPane(new SplitPane(chatRoomListPanel, view.getUsersListView()));

		if(needRequest==true) {
			model.requestUsersList();
		}

		SplitPane.setResizableWithParent(view.getUsersListView(), Boolean.FALSE);


		if(model.getUser().getSettings().getTopSpltNum()!=0) {
			view.getTopSplitPane().setDividerPositions(model.getUser().getSettings().getTopSpltNum());
		}else {
			view.getTopSplitPane().setDividerPositions(0.8);
		}

		view.setChatSplitPane(new SplitPane(view.getTopSplitPane(), view.getButtomSplitContainer()));
		view.getChatSplitPane().setOrientation(Orientation.VERTICAL);

		VBox.setVgrow(view.getChatSplitPane(), Priority.ALWAYS);

		view.getChatRoomListView().getSelectionModel().clearSelection();
		view.getChatAndRoomsPanel().getChildren().add(view.getChatSplitPane());
	}

	@Override
	public void handleChatRoomSelection(ChatRoom selectedChatRoom) {

		if (model.getCurrentRoom()==null || selectedChatRoom != null && selectedChatRoom.getID()!=model.getCurrentRoom().getID()) {//se non sono connesso a nessuna room e se la room scelta non è quella attuale
			if(model.getCurrentRoom()!=null && selectedChatRoom.getID()!=model.getCurrentRoom().getID()) {

				model.getUser().getUserDetails().setMainRoomId(model.getCurrentRoom().getID());
				model.disconnectFromChatRoom();
				view.getPrimaryChatContainer().getChildren().clear();
				model.getPushToTalk().stop();
				model.getMessageManager().stopReceivingMessages();
				if(model.getAudioManager()!=null) {
					model.getAudioManager().stopAudioAndSocket();
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if(view.getPrimaryChatContainer() != null && view.getPrimaryChatContainer().getCorrespondingRoom()!=null && view.getPrimaryChatContainer().getCorrespondingRoom().getID()!=selectedChatRoom.getID()) {
				view.getButtomSplitContainer().getChildren().remove(view.getPrimaryChatContainer());
			}

			view.setPrimaryChatContainer(new ChatContainer(model, selectedChatRoom));

			VBox.setVgrow(view.getPrimaryChatContainer(), Priority.ALWAYS);
			view.getButtomSplitContainer().getChildren().add(view.getPrimaryChatContainer());


			if(model.getCurrentServer().getId()!=selectedChatRoom.getServerID() || model.getCurrentRoom()==null) {
				view.getServersBox().getServersList().get(model.getCurrentServer().getId()).connectBox();
			}

			model.setCurrentRoom(selectedChatRoom);
			model.getUser().getUserDetails().setMainRoomId(selectedChatRoom.getID());
			model.alertServerForNewRoom(selectedChatRoom);


			selectedChatRoom.getUsers().add(model.getUser());

			if (selectedChatRoom.getType().equals(RoomsEnum.VOICE)) {

				CompletableFuture.runAsync(() -> {
					try {

						if(model.getUser().getSettings().isPushToTalk()==true) {
							model.getPushToTalk().stop();
							model.addPushToTalk();
							model.setAudioManager(new AudioManager(selectedChatRoom, model.getUser().getSettings()));
							model.getAudioManager().startAudioReception();

						}else {
							model.setAudioManager(new AudioManager(selectedChatRoom, model.getUser().getSettings()));
							model.getAudioManager().startAudioTransmission();
						}
						model.setMessageManager(new MessagesManager(((VoiceRoom) selectedChatRoom).getTextRoom(), this));
						model.getMessageManager().receiveMessages();

					} catch (Exception e) {
						e.printStackTrace();
					}
				});

				Task<Void> loadMessagesTask = new Task<Void>() {
					@Override
					public Void call() {
						loadMessages(selectedChatRoom);
						Platform.runLater(() -> {
							view.getPrimaryChatContainer().getMessagesBox().getChildren().addAll(view.getPrimaryChatContainer().getMessages().values());
							Platform.runLater(() -> {
								view.getPrimaryChatContainer().getScrollPane().setVvalue(1);
							});
						});
						return null;
					}
				};
				new Thread(loadMessagesTask).start();

			} else {
				CompletableFuture.runAsync(() -> {
					try {
						model.setMessageManager(new MessagesManager(selectedChatRoom, this));
						model.getMessageManager().receiveMessages();

					} catch (Exception e) {
						e.printStackTrace();
					}
				});

				Task<Void> loadMessagesTask = new Task<Void>() {
					@Override
					public Void call() {
						loadMessages(selectedChatRoom);
						Platform.runLater(() -> {
							view.getPrimaryChatContainer().getMessagesBox().getChildren().addAll(view.getPrimaryChatContainer().getMessages().values());
						});
						return null;
					}
				};
				new Thread(loadMessagesTask).start();
			}
		}
	}

	public void loadMessages(ChatRoom room) {
		view.getPrimaryChatContainer().getMessagesBox().getChildren().clear();
		view.getPrimaryChatContainer().getMessages().clear();
		ChatMessageDB messageDb=new ChatMessageDB();

		LinkedHashMap<Long, ChatMessage> messages = messageDb.fetchMessagesFromRoom(room, 0, true);
		for (ChatMessage message : messages.values()) {
			if(message.getReplyTo()<=0) {
				view.getPrimaryChatContainer().buildMessageComponent(message);
			}else if(message.getReplyTo()>0) {
				view.getPrimaryChatContainer().buildReplyMessageComponent(message);
			}
		}
	}

	public void updateConnectedUsers(UpdateMessage message, ChatRoom room) {

		HashMap<Integer, UserDetailsObject> users = message.getUsersList();
		for (Entry<Integer, UserDetailsObject> entry : users.entrySet()) {


			if(view.getUsersListView().getUsers().containsKey(entry.getKey())==true) {
				view.getUsersListView().getUsers().get(entry.getKey()).getUser().setUserDetails(users.get(entry.getKey()));
				view.getUsersListView().getUsers().get(entry.getKey()).checkDetails();
			}else {
				ConnectedUserBox connectedUser=new ConnectedUserBox(view, model, entry);
				try {
					view.getUsersListView().addUser(connectedUser);
				}catch(Exception e) {					
				}
			}
		}
	}

	@Override
	public void closureOperations() {
		try {
			model.getPushToTalk().stop();
		} catch (Exception e) {
		}

		try {
			model.getUser().getUserDb().updateSplitNum(view.getSplitPane().getDividerPositions()[0], model.getUser());
		} catch (Exception e) {
		}

		try {
			model.getUser().getUserDb().updateTopSplitNum(view.getTopSplitPane().getDividerPositions()[0], model.getUser());
		} catch (Exception e) {
		}

		try {
			model.getServerPersitor().setServerList(view.getServersBox().getChildren());
			model.getServerPersitor().saveServers();
		} catch (Exception e) {
		}

		try {
			model.getVideoManager().stopScreenSharing();
		} catch (Exception e) {
		}

		try {
			model.getSocket().close();
			model.getUpdateSocket().close();
		} catch (Exception e) {
		}

		try {
			model.getAudioManager().stopAudioAndSocket();
		} catch (Exception e) {
		}

		try {
			model.getMessageManager().stopReceivingMessages();
		} catch (Exception e) {
		}
	}

	@Override
	public void onConnectedUsersUpdate(UpdateMessage message, ChatRoom room) {
		Platform.runLater(() -> {
			updateConnectedUsers(message, room);
		});
	}

	@Override
	public void onRecoverMessagesUpdate() {
		LinkedHashMap<Long, MessageBox> messages=view.getPrimaryChatContainer().getMessages();
		view.getButtomSplitContainer().getChildren().remove(view.getPrimaryChatContainer());
		view.setPrimaryChatContainer(new ChatContainer(model, model.getCurrentRoom()));
		VBox.setVgrow(view.getPrimaryChatContainer(), Priority.ALWAYS);
		view.getButtomSplitContainer().getChildren().add(view.getPrimaryChatContainer());
		view.getPrimaryChatContainer().getMessagesBox().getChildren().addAll(messages.values());
	}	

	@Override
	public void pingUser(PingObject ping) {
		Platform.runLater(() -> {

			if(model.isPingMenuOpen()==false) {
				model.setPingMenuOpen(true);
				view.setPingContextMenu(new ContextMenu());
				view.getPingContextMenu().setId("pingContextMenuItem");
				view.getPingContextMenu().setStyle("-fx-background-color: #e0e0e0; -fx-effect: null; -fx-padding: 0 0 0 0; -fx-background-radius: 10;");			

				MenuItem pingMenuItem = new MenuItem();
				pingMenuItem.setId("pingMenuItem");

				view.getPingContextMenu().setOnHidden(eventHide -> {
					model.setPingMenuOpen(false);
					this.view.getUsersListView().getUsers().get(ping.getPingedUserId()).setShowingPing(false);					
					this.view.getUsersListView().getUsers().get(ping.getPingedUserId()).setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
					model.stopUsersPing(new StopPingObject(model.getUser().getId()));
				});

				double width=this.view.getUsersListView().getUsers().get(ping.getPingedUserId()).getWidth();
				if(width>=200) {
					PingGraphBox graph=new PingGraphBox(width-21, 150);
					view.setUserPingGraph(graph);
					pingMenuItem.setGraphic(view.getUserPingGraph());

					view.getPingContextMenu().getItems().addAll(pingMenuItem);
					view.getPingContextMenu().show(this.view.getUsersListView().getUsers().get(ping.getPingedUserId()), Side.BOTTOM, 0, 0);
					view.getUserPingGraph().updateChart(view.getUserPingGraph().getCounter(), ping.getPing());
					view.getUserPingGraph().setCounter(view.getUserPingGraph().getCounter()+1);
				}else {					
					HBox pingLabelBox=new HBox();
					pingLabelBox.setMinWidth(width-21);
					pingLabelBox.setMaxWidth(width-21);
					pingLabelBox.setAlignment(Pos.CENTER);
					pingLabelBox.setTranslateX(5);
					view.setSmallPingLabel(new Label());
					Platform.runLater(() -> {
						view.getSmallPingLabel().setStyle("-fx-text-fill: -primary-color;");
					});
					view.getSmallPingLabel().setText("Ping: "+ping.getPing()+" ms");
					pingLabelBox.getChildren().add(view.getSmallPingLabel());
					pingMenuItem.setGraphic(pingLabelBox);
					view.getPingContextMenu().getItems().addAll(pingMenuItem);
					view.getPingContextMenu().show(this.view.getUsersListView().getUsers().get(ping.getPingedUserId()), Side.BOTTOM, 0, 0);
				}
			}else {
				double width=this.view.getUsersListView().getUsers().get(ping.getPingedUserId()).getWidth();
				if(width>=200) {
					view.getUserPingGraph().updateChart(view.getUserPingGraph().getCounter(), ping.getPing());
					view.getUserPingGraph().setCounter(view.getUserPingGraph().getCounter()+1);
				}else {
					view.getSmallPingLabel().setText("Ping: "+ping.getPing()+" ms");
				}
			}
		});
	}

	@Override
	public void addServerToServerListView(Server server) {
		//view.getServerListView().getItems().add(server);
	}

	public MainWindowModel getModel() {
		return model;
	}

	public void setModel(MainWindowModel model) {
		this.model = model;
	}

	public MainWindow getView() {
		return view;
	}

	public void setView(MainWindow view) {
		this.view = view;
	}
}