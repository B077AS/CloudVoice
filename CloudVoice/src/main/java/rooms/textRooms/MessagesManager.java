package rooms.textRooms;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import javafx.application.Platform;
import main.controller.MainWindowController;
import main.model.VPS;
import rooms.ChatRoom;
import rooms.textRooms.messages.MessageBox;
import serializedObjects.ChatMessage;
import serializedObjects.UpdateChatMessageObject;

public class MessagesManager {

	private Socket socket;
	private MainWindowController controller;
	private boolean receivingMessages = true;

	public MessagesManager(ChatRoom room, MainWindowController controller) {
		try {
			this.socket= new Socket(VPS.SERVER_ADDRESS, room.getPort());
			System.out.println("Request connection for port: "+room.getPort());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.controller=controller;
	}

	public void receiveMessages() {
		try {
			InputStream inputStream = socket.getInputStream();
			ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
			while(receivingMessages) {

				Object receivedObject = (Object) objectInputStream.readObject();

				if (receivedObject instanceof ChatMessage) {
					ChatMessage object = (ChatMessage) receivedObject;
					Platform.runLater(() -> {
						MessageBox message=controller.getView().getPrimaryChatContainer().buildMessageComponent(object);
						controller.getView().getPrimaryChatContainer().getMessagesBox().getChildren().add(message);
					});

				}else if(receivedObject instanceof UpdateChatMessageObject) {
					UpdateChatMessageObject object = (UpdateChatMessageObject) receivedObject;

					if(object.isUpdate()==true) {

						Platform.runLater(() -> {
							controller.getView().getPrimaryChatContainer().getMessages().get(object.getMessageId()).getMessage().setContent(object.getNewContent());
							controller.getView().getPrimaryChatContainer().getMessages().get(object.getMessageId()).getMessageText().setText(object.getNewContent());
						});

					}else if(object.isDelete()==true) {
						Platform.runLater(() -> {
							controller.getView().getPrimaryChatContainer().getMessagesBox().getChildren().remove(controller.getView().getPrimaryChatContainer().getMessages().get(object.getMessageId()));
							controller.getView().getPrimaryChatContainer().getMessages().remove(object.getMessageId());
						});

					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage (ChatMessage object) {
		try {

			OutputStream outputStream=socket.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void stopReceivingMessages() {
		receivingMessages = false;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
}