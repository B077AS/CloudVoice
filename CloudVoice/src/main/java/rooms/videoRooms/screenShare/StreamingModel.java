package rooms.videoRooms.screenShare;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import main.model.MainWindowModel;
import main.model.VPS;
import main.view.MainWindow;
import rooms.videoRooms.VideoRoom;
import rooms.voiceRooms.VoiceRoom;
import serializedObjects.ConnectToRoomObject;

public class StreamingModel {
	
	private MainWindow mainWindowView;
	private MainWindowModel mainWindowModel;
	private VideoRoom room;
	private Socket socket;	
	private boolean running;
	private Thread receiveFrameThread;
	private boolean closedChat=false;	
	
	public StreamingModel(MainWindow mainWindow, MainWindowModel model) {
		this.room=((VoiceRoom)model.getCurrentRoom()).getVideoRoom();
		this.mainWindowView = mainWindow;
		this.mainWindowModel = model;
	}
	

	public void sendRoomDetails() {
		try {
			ConnectToRoomObject object=new ConnectToRoomObject(room.getID(), room.getPort(), room.getType(), mainWindowModel.getUser().getUserDetails());
			socket=mainWindowModel.getSocket();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connectToRoom() {
		try {
			socket= new Socket(VPS.SERVER_ADDRESS, room.getPort());
			System.out.println("Request connection for port: "+room.getPort());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initialize() {
		sendRoomDetails();
		connectToRoom();
		running=true;
	}
	
	public void stopReceiving() {
		running=false;
		try {
			receiveFrameThread.interrupt();
			socket.close();
		} catch (Exception e) {
		}
	}

	public MainWindow getMainWindowView() {
		return mainWindowView;
	}


	public void setMainWindowView(MainWindow mainWindowView) {
		this.mainWindowView = mainWindowView;
	}


	public MainWindowModel getMainWindowModel() {
		return mainWindowModel;
	}


	public void setMainWindowModel(MainWindowModel mainWindowModel) {
		this.mainWindowModel = mainWindowModel;
	}


	public VideoRoom getRoom() {
		return room;
	}


	public void setRoom(VideoRoom room) {
		this.room = room;
	}


	public boolean isClosedChat() {
		return closedChat;
	}


	public void setClosedChat(boolean closedChat) {
		this.closedChat = closedChat;
	}


	public Socket getSocket() {
		return socket;
	}


	public void setSocket(Socket socket) {
		this.socket = socket;
	}


	public boolean isRunning() {
		return running;
	}


	public void setRunning(boolean running) {
		this.running = running;
	}


	public Thread getReceiveFrameThread() {
		return receiveFrameThread;
	}


	public void setReceiveFrameThread(Thread receiveFrameThread) {
		this.receiveFrameThread = receiveFrameThread;
	}
}