package server;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Hashtable;
import javafx.scene.image.Image;
import main.model.DatabaseConnection;
import main.model.ImageToBlob;
import main.model.VPS;
import rooms.ChatRoomDB;
import rooms.textRooms.TextRoom;
import rooms.voiceRooms.VoiceRoom;
import serializedObjects.RoomsEnum;
import user.User;

public class ServerDB {

	public Hashtable<Integer, Server> selectServerListFromId(User user) {
		Hashtable<Integer, Server> serverHashtable = new Hashtable<>();
		Image avatar = null;

		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection conn = database.getConnection();

			String query = "{CALL SelectServerListFromId(?)}";
			PreparedStatement preparedStatement = conn.prepareStatement(query);
			preparedStatement.setInt(1, user.getId());

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				int owner = resultSet.getInt("owner");
				int numberOfUsers = resultSet.getInt("numberOfUsers");
				byte[] serverAvatar = resultSet.getBytes("avatar");
				int start = resultSet.getInt("start_port");
				int end = resultSet.getInt("end_port");

				try {
					avatar = ImageToBlob.byteArrayToImage(serverAvatar);
				} catch (Exception e) {
					avatar = null;
				}

				Server server = new Server(id, name, owner, numberOfUsers, avatar, start, end);
				serverHashtable.put(id, server);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return serverHashtable;
	}

	public Server fetchServerFromId(int id) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection conn = database.getConnection();

			String query = "{CALL fetch_server_from_id(?)}";
			PreparedStatement preparedStatement = conn.prepareStatement(query);
			preparedStatement.setInt(1, id);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();
			String name = resultSet.getString("name");
			int owner = resultSet.getInt("owner");
			int numberOfUsers = resultSet.getInt("numberOfUsers");
			byte[] serverAvatar = resultSet.getBytes("avatar");
			int start = resultSet.getInt("start_port");
			int end = resultSet.getInt("end_port");

			Image avatar = ImageToBlob.byteArrayToImage(serverAvatar);

			Server server = new Server(id, name, owner, numberOfUsers, avatar, start, end);
			return server;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public int[] insertServer(Server server) {
		try {
			int[] array=new int[3];

			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection(); 

			String sql = "{CALL GetLastServerEndPort()}";
			CallableStatement callableStatement = connection.prepareCall(sql);
			ResultSet resultSet=callableStatement.executeQuery();
			resultSet.next();
			int lastEndPort=resultSet.getInt(1);
			resultSet.close();	

			if(lastEndPort==0) {
				lastEndPort=VPS.SERVER_PORT;
			}

			int start=lastEndPort+1;
			int end=lastEndPort+44;

			array[0]=start;
			array[1]=end;

			sql = "{CALL InsertServer(?, ?, ?, ?, ?, ?, ?, ?)}";
			callableStatement = connection.prepareCall(sql);

			byte[] avatarArray=ImageToBlob.convertImageToByteArray(server.getAvatar());

			callableStatement.setString(1, server.getName());
			callableStatement.setInt(2, server.getOwner());
			callableStatement.setBytes(3, avatarArray);
			callableStatement.setInt(4, server.getNumberOfUsers()); // Numero di utenti
			callableStatement.setInt(5, start); // Porta di partenza
			callableStatement.setInt(6, end); // Porta di arrivo
			callableStatement.setString(7, "owner");
			callableStatement.registerOutParameter(8, Types.INTEGER);			
			callableStatement.execute();

			int serverId = callableStatement.getInt(8);
			array[2]=serverId;

			ChatRoomDB chatRoomDB=new ChatRoomDB();
			chatRoomDB.createRoom(new VoiceRoom(0, "Room 1", serverId, start, RoomsEnum.VOICE));//TODO
			chatRoomDB.createTextOnlyRoom(new TextRoom(0, "Text-Only Room 1", serverId, 0, RoomsEnum.TEXT), server);//TODO

			connection.close();
			return array;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void updateAvatar(Image image, Server server){

		byte[] avatarArray=ImageToBlob.convertImageToByteArray(image);
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection(); 

			CallableStatement callableStatement = connection.prepareCall("{CALL UpdateServerAvatar(?, ?)}");
			callableStatement.setBytes(1, avatarArray);
			callableStatement.setInt(2, server.getId());
			callableStatement.executeUpdate();

			connection.close();

		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}