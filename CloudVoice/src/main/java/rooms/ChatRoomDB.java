package rooms;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import main.model.DatabaseConnection;
import rooms.textRooms.TextRoom;
import rooms.videoRooms.VideoRoom;
import rooms.voiceRooms.VoiceRoom;
import serializedObjects.RoomsEnum;
import server.Server;

public class ChatRoomDB {

	public  HashMap<Integer, ChatRoom> selectChatListFromServer(Server server) {
		HashMap<Integer, ChatRoom> allRooms = new HashMap<>();

		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection conn = database.getConnection();

			String procedureCall = "{CALL SelectChatListFromServer(?)}";
			CallableStatement callableStatement = conn.prepareCall(procedureCall);
			callableStatement.setInt(1, server.getId());
			ResultSet resultSet = callableStatement.executeQuery();


			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				int port = resultSet.getInt("port");
				RoomsEnum type = RoomsEnum.valueOf(resultSet.getString("type"));
				int associatedRoom=resultSet.getInt("associated_room");

				ChatRoom room=RoomFactory.createRoom(id, name, server.getId(), port, type);

				if(associatedRoom!=0) {
					room.setHasAssociatedRoom(true);
					VoiceRoom mainRoom=(VoiceRoom)allRooms.get(associatedRoom);
					if(room.getType().equals(RoomsEnum.TEXT)) {
						mainRoom.setTextRoom((TextRoom)room);
					}else if(room.getType().equals(RoomsEnum.VIDEO)) {
						mainRoom.setVideoRoom((VideoRoom)room);
					}
				}
				else {
					allRooms.put(id, room);
				}

			}
			conn.close();
			return allRooms;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allRooms;
	}

	public void createRoom(ChatRoom room) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection conn = database.getConnection();

			String query = "{CALL createRoom(?, ?, ?)}";
			CallableStatement callableStatement = conn.prepareCall(query);
			callableStatement.setString(1, room.getName());
			callableStatement.setInt(2, room.getServerID());
			callableStatement.setInt(3, room.getPort());
			callableStatement.execute();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createTextOnlyRoom(ChatRoom room, Server server) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection conn = database.getConnection();

			String query = "{CALL createTextOnlyRoom(?, ?)}";
			CallableStatement callableStatement = conn.prepareCall(query);
			callableStatement.setString(1, room.getName());
			callableStatement.setInt(2, room.getServerID());
			callableStatement.execute();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}