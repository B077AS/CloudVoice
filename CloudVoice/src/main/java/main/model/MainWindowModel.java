package main.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import main.view.CustomAlert;
import main.view.MainWindow;
import rooms.ChatRoom;
import rooms.textRooms.MessagesManager;
import rooms.videoRooms.VideoManager;
import rooms.videoRooms.screenShare.WindowLister;
import rooms.voiceRooms.AudioManager;
import rooms.voiceRooms.PushToTalkListener;
import serializedObjects.ChatMessage;
import serializedObjects.OpenPortsMessage;
import serializedObjects.PingObject;
import serializedObjects.RequestPing;
import serializedObjects.RequestUserListObject;
import serializedObjects.ConnectToRoomObject;
import serializedObjects.FirstConnectionObject;
import serializedObjects.RoomsEnum;
import serializedObjects.StopPingObject;
import serializedObjects.UpdateMessage;
import serializedObjects.UserDetailsObject;
import server.Server;
import server.ServerBox;
import server.ServerCreateWindow;
import server.ServerDB;
import server.ServerDataPersistor;
import settings.SettingsWindow;
import user.User;

public class MainWindowModel {

	private boolean isSettingsOpen=false;
	private boolean isCreateServerDialogOpen=false;
	private boolean isPingMenuOpen=false;
	private User user;
	private ChatRoom currentRoom;
	private Server currentServer;
	private AudioManager audioManager;
	private MessagesManager messageManager;
	private VideoManager videoManager;
	private Socket socket;
	private Socket updateSocket;
	private ModelObserver observer;
	private PushToTalkListener pushToTalk;
	private ServerDataPersistor serverPersitor;

	public MainWindowModel(User user) {
		this.user=user;
		this.serverPersitor=new ServerDataPersistor();

		addPushToTalk();

		Thread serverPortThread = new Thread(() -> connectToServer());
		serverPortThread.setDaemon(true);
		serverPortThread.start();

		Thread updatePortThread = new Thread(() -> connectToUpdatePort());
		updatePortThread.setDaemon(true);
		updatePortThread.start();
	}

	public void openScreenShareLister(MainWindow mainWindow) {
		try {
			if(currentRoom!=null || currentRoom.getType()==RoomsEnum.VOICE) {
				WindowLister windowLister = new WindowLister(this, mainWindow);
				Stage stage = new Stage();
				windowLister.start(stage);

			}else {
				CustomAlert notInARoomAlert=new CustomAlert();
				notInARoomAlert.createAlert("Error", "Not in a Room");
			}
		}catch(Exception e) {
			CustomAlert notInARoomAlert=new CustomAlert();
			notInARoomAlert.createAlert("Error", "Not in a Room");
		}
	}

	public ChatMessage buildMessage(String messageText) {

		SimpleDateFormat dateDay = new SimpleDateFormat("dd MMMM", Locale.ENGLISH);
		String day = dateDay.format(new Date());
		SimpleDateFormat dateHour = new SimpleDateFormat("HH:mm");
		String hour = dateHour.format(new Date());
		String time=day+" at "+hour;

		ChatMessage messageObject=new ChatMessage(0, this.currentRoom.getID(), messageText, time, user.getId(), user.getUsername());
		return messageObject;
	}

	public void addPushToTalk() {

		if(user.getSettings().isPushToTalk()!=false) {
			try {
				Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
				logger.setLevel(Level.OFF);
				logger.setUseParentHandlers(false);
				pushToTalk=new PushToTalkListener(this, user.getSettings().getPushToTalkKey());
				GlobalScreen.registerNativeHook();
				GlobalScreen.addNativeKeyListener(pushToTalk);
			} catch (NativeHookException ex) {
				ex.printStackTrace();
			}
		}
	}

	public ObservableList<ServerBox> checkServersOrder(ListView<ServerBox> listview) {
		ServerDB serverDB = new ServerDB();
		Hashtable<Integer, Server> serverHashtable = serverDB.selectServerListFromId(this.user);

		serverPersitor = new ServerDataPersistor();
		List<Integer> loadedServers = serverPersitor.loadServers();

		ArrayList<ServerBox> orderedServers = new ArrayList<>();

		BiConsumer<ServerBox, Double> updateWidth = (serverBox, newValue) -> {
			serverBox.setPrefWidth(newValue-15);
		};

		for (int server : loadedServers) {
			if (serverHashtable.containsKey(server)) {
				ServerBox serverBox = new ServerBox(serverHashtable.get(server), this);
				listview.widthProperty().addListener((observable, oldValue, newValue) ->
				updateWidth.accept(serverBox, newValue.doubleValue()));
				orderedServers.add(serverBox);
			}
		}

		for (Entry<Integer, Server> entry : serverHashtable.entrySet()) {
			if (!loadedServers.contains(entry.getKey())) {
				ServerBox serverBox = new ServerBox(entry.getValue(), this);
				listview.widthProperty().addListener((observable, oldValue, newValue) ->
				updateWidth.accept(serverBox, newValue.doubleValue()));
				orderedServers.add(serverBox);
			}
		}

		/*for(int i=0; i<20; i++) {
	    	Server server = new Server(20+i, "test", 1, 1, null, 1, 2);
            ServerBox serverBox = new ServerBox(server, this);
           listview.widthProperty().addListener((observable, oldValue, newValue) ->
                    updateWidth.accept(serverBox, newValue.doubleValue()));
            orderedServers.add(serverBox);
	    }*/

		return FXCollections.observableArrayList(orderedServers);
	}


	public void disconnectFromChatRoom() {
		this.user.getUserDetails().setDisconnect(true);
		if(this.user.getUserDetails().isLive()==true) {
			this.user.getUserDetails().setLive(false);
		}
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(updateSocket.getOutputStream());
			objectOutputStream.writeObject(this.user.getUserDetails());
			objectOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//this.currentRoom=null;
		//this.currentServer=null;
	}

	public void updateUserDetails(UserDetailsObject object) {
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(updateSocket.getOutputStream());
			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void alertServerForNewRoom(ChatRoom room) {
		try {
			user.getUserDetails().setDisconnect(false);
			ConnectToRoomObject object=new ConnectToRoomObject(room.getID(), room.getPort(), room.getType(), user.getUserDetails());
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void connectToServer() {
		try {
			socket = new Socket(VPS.SERVER_ADDRESS, VPS.SERVER_PORT);
			System.out.println("Request connection for port: " + VPS.SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void connectToUpdatePort() {
		try {
			updateSocket = new Socket(VPS.SERVER_ADDRESS, VPS.SERVER_UPDATE_PORT);	
			System.out.println("Request connection for port: " + VPS.SERVER_UPDATE_PORT);

			boolean connected = false;
			while (!connected) {
				if (updateSocket.isConnected() && updateSocket.getInputStream() != null && updateSocket.getOutputStream() != null) {
					connected = true;
					sendUserIdentifier();
					System.out.println("Connection Started");
				}
			}

			receiveUpdates();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendUserIdentifier() {
		try {
			OutputStream outputStream=updateSocket.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			FirstConnectionObject identifier=new FirstConnectionObject(this.user.getId());
			objectOutputStream.writeObject(identifier);
			objectOutputStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void receiveUpdates() {
		try {
			while(true) {
				InputStream inputStream = updateSocket.getInputStream();
				ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

				Object receivedObject = (Object) objectInputStream.readObject();

				if(receivedObject instanceof  UpdateMessage) {
					UpdateMessage object = (UpdateMessage) receivedObject;
					if(this.currentRoom!=null && object.getRoomID()==this.currentRoom.getID()) {
						notifyObserversForConnectedUsers(object, this.currentRoom);
					}
				}else if(receivedObject instanceof PingObject) {
					PingObject object = (PingObject) receivedObject;
					this.observer.pingUser(object);					
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void sendPorts(int start, int end) {
		try {

			if(updateSocket==null) {
				updateSocket = new Socket(VPS.SERVER_ADDRESS, VPS.SERVER_UPDATE_PORT);	
				System.out.println("Request connection for port: " + VPS.SERVER_UPDATE_PORT);
				OutputStream outputStream=updateSocket.getOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

				OpenPortsMessage message=new OpenPortsMessage(start, end);

				objectOutputStream.writeObject(message);
				objectOutputStream.flush();
				System.out.println("Ports sent to server");
			}
			else {
				OutputStream outputStream=updateSocket.getOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

				OpenPortsMessage message=new OpenPortsMessage(start, end);

				objectOutputStream.writeObject(message);
				objectOutputStream.flush();
				System.out.println("Ports sent to server");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void requestUsersList() {
		try {
			OutputStream outputStream=updateSocket.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

			RequestUserListObject request=new RequestUserListObject(this.currentRoom.getID());

			objectOutputStream.writeObject(request);
			objectOutputStream.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void requestUsersPing(RequestPing object) {
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(updateSocket.getOutputStream());
			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void stopUsersPing(StopPingObject object) {
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(updateSocket.getOutputStream());
			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void openSettings(MainWindow mainWindow) {
		if (isSettingsOpen) {
			return;
		}
		isSettingsOpen = true;

		SettingsWindow settings=new SettingsWindow(mainWindow, this);
		try {
			settings.start(new Stage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

	public void serverCreate(){
		if (isCreateServerDialogOpen) {
			return;
		}
		isCreateServerDialogOpen = true;

		ServerCreateWindow createDialog = new ServerCreateWindow(this, this.user);
		try {
			createDialog.start(new Stage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addObserver(ModelObserver observer) {
		this.observer=observer;
	}

	// Metodo chiamato quando è necessario notificare gli osservatori
	public void notifyObserversForConnectedUsers(UpdateMessage message, ChatRoom room) {
		this.observer.onConnectedUsersUpdate(message, room);
	}

	public void notifyObserverForRecoverMessages() {
		this.observer.onRecoverMessagesUpdate();
	}

	public void notifyObserverServerSelection(Server server) {
		this.observer.handleServerSelection(server);
	}

	public void notifyObserverChatRoomSelection(ChatRoom chatRoom) {
		this.observer.handleChatRoomSelection(chatRoom);
	}

	public void notifyObserverSetDivider() {
		this.observer.setDividerPosition();
	}

	public void notifyAddServerToServerListView(Server server) {
		this.observer.addServerToServerListView(server);
	}


	public void notifyClosureOperations() {
		this.observer.closureOperations();
	}

	public ChatRoom getCurrentRoom() {
		return currentRoom;
	}

	public void setCurrentRoom(ChatRoom currentRoom) {
		this.currentRoom = currentRoom;
	}

	public boolean isSettingsOpen() {
		return isSettingsOpen;
	}

	public void setSettingsOpen(boolean isSettingsOpen) {
		this.isSettingsOpen = isSettingsOpen;
	}

	public Server getCurrentServer() {
		return currentServer;
	}

	public void setCurrentServer(Server currentServer) {
		this.currentServer = currentServer;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public AudioManager getAudioManager() {
		return audioManager;
	}

	public void setAudioManager(AudioManager audioManager) {
		this.audioManager = audioManager;
	}

	public MessagesManager getMessageManager() {
		return messageManager;
	}

	public void setMessageManager(MessagesManager messageManager) {
		this.messageManager = messageManager;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public Socket getUpdateSocket() {
		return updateSocket;
	}

	public void setUpdateSocket(Socket updateSocket) {
		this.updateSocket = updateSocket;
	}

	public boolean isCreateServerDialogOpen() {
		return isCreateServerDialogOpen;
	}

	public void setCreateServerDialogOpen(boolean isCreateServerDialogOpen) {
		this.isCreateServerDialogOpen = isCreateServerDialogOpen;
	}

	public VideoManager getVideoManager() {
		return videoManager;
	}

	public void setVideoManager(VideoManager videoManager) {
		this.videoManager = videoManager;
	}

	public PushToTalkListener getPushToTalk() {
		return pushToTalk;
	}

	public void setPushToTalk(PushToTalkListener pushToTalk) {
		this.pushToTalk = pushToTalk;
	}

	public ServerDataPersistor getServerPersitor() {
		return serverPersitor;
	}

	public void setServerPersitor(ServerDataPersistor serverPersitor) {
		this.serverPersitor = serverPersitor;
	}

	public boolean isPingMenuOpen() {
		return isPingMenuOpen;
	}

	public void setPingMenuOpen(boolean isPingMenuOpen) {
		this.isPingMenuOpen = isPingMenuOpen;
	}
}