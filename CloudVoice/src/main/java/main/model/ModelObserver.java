package main.model;

import rooms.ChatRoom;
import serializedObjects.PingObject;
import serializedObjects.UpdateMessage;
import server.Server;

public interface ModelObserver {
	
    void onConnectedUsersUpdate(UpdateMessage message, ChatRoom room);
    
    void onRecoverMessagesUpdate();
    
    void handleServerSelection(Server server);
    
    void handleChatRoomSelection(ChatRoom selectedChatRoom);
    
    void setDividerPosition();
    
    void addServerToServerListView(Server server);
    
    void closureOperations();
    
    void pingUser(PingObject ping);
}
