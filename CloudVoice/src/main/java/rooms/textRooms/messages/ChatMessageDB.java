package rooms.textRooms.messages;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import main.model.DatabaseConnection;
import rooms.ChatRoom;
import serializedObjects.ChatMessage;

public class ChatMessageDB {

	public ChatMessage insertMessage(ChatMessage message) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();
			String query = "{CALL InsertMessage(?, ?, ?, ?, ?, ?, ?)}";
			CallableStatement statement = connection.prepareCall(query);
			statement.setInt(1, message.getRoomId());
			statement.setString(2, message.getContent());
			statement.setInt(3, message.getSenderId());
			statement.setString(4, message.getTime());
			statement.setBytes(5, message.getImage());
			statement.setLong(6, message.getReplyTo());
			statement.registerOutParameter(7, Types.INTEGER);	
			statement.execute();

			long generatedId = statement.getInt(7);
			message.setId(generatedId);

			connection.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	public LinkedHashMap<Long, ChatMessage> fetchMessagesFromRoom(ChatRoom room, int offset, boolean invert) {
		LinkedHashMap<Long, ChatMessage> messages = new LinkedHashMap<>();

		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();
			ResultSet resultSet;
			PreparedStatement preparedStatement;

			String query = "{CALL FetchMessagesFromRoom(?, ?, ?, ?)}";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, room.getID());
			preparedStatement.setInt(2, 40);
			preparedStatement.setInt(3, offset);
			preparedStatement.setBoolean(4, invert);

			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				// Retrieve all user information from the query result
				long messageId = resultSet.getLong("id");
				String content = resultSet.getString("content");
				int userId = resultSet.getInt("user_id");
				String time = resultSet.getString("time");
				String username = resultSet.getString("username");
				byte[] image = resultSet.getBytes("picture");
				long replyTo = resultSet.getLong("replyTo");

				ChatMessage message = new ChatMessage(messageId, room.getID(), content, time, userId, username);
				message.setReplyTo(replyTo);
				if (image != null) {
					message.setImage(image);
				}

				messages.put(messageId, message);
			}

			connection.close();
			return messages;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void deleteMessage(ChatMessage message) {
	    try {
	        DatabaseConnection database = new DatabaseConnection();
	        Connection connection = database.getConnection();
	        
	        String query = "{CALL DeleteMessage(?)}";
	        CallableStatement statement = connection.prepareCall(query);
	        statement.setLong(1, message.getId());
	        statement.executeUpdate();
	        
	        connection.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void updateMessage(ChatMessage message) {
	    try {
	        DatabaseConnection database = new DatabaseConnection();
	        Connection connection = database.getConnection();

	        String query = "{CALL UpdateMessage(?, ?)}";
	        CallableStatement statement = connection.prepareCall(query);
	        statement.setLong(1, message.getId());
	        statement.setString(2, message.getContent());
	        statement.executeUpdate();

	        connection.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}