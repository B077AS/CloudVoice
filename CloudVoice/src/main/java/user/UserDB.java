package user;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.scene.image.Image;
import main.model.DatabaseConnection;
import main.model.ImageToBlob;
import serializedObjects.UserDetailsObject;

public class UserDB {

	/*public User login(String email, String password){
		User user = null;
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection(); 
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM login(?, ?)");
			statement.setString(1, email);
			statement.setString(2, password);

			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				int id = resultSet.getInt("user_id");
				String username = resultSet.getString("user_username");
				byte[] userAvatar = resultSet.getBytes("user_avatar");

				LocalDate dateOfBirth = resultSet.getDate("user_dateOfBirth").toLocalDate();

				Image avatar=null;
				try {
					avatar = ImageToBlob.byteArrayToImage(userAvatar);
				} catch (Exception e) {
					avatar=null;
				}

				user=new User(id, username, password, email, avatar, dateOfBirth);
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return user;
	}*/

	public User fetchUser(String email) {
		User user = null;
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection(); 
			String query = "{CALL fetchuser(?)}";
			CallableStatement callableStatement = connection.prepareCall(query);
			callableStatement.setString(1, email);
			ResultSet resultSet = callableStatement.executeQuery();

			if (resultSet.next()) {
				// Recupera tutte le informazioni dell'utente dal risultato della query
				int userId = resultSet.getInt("id");
				String username = resultSet.getString("username");
				byte[] userAvatar = resultSet.getBytes("avatar");
				LocalDate dateOfBirth = resultSet.getDate("dateOfBirth").toLocalDate();

				Image avatar=ImageToBlob.byteArrayToImage(userAvatar);

				// Crea un oggetto User con le informazioni recuperate
				user = new User(userId, username, null, email, avatar, dateOfBirth); // Aggiungi altri campi se necessario
			}

			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return user;
	}

	public User fetchMinimalUserInfoFromId(int userId) {
		User fetchedUser = null;
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection(); 

			CallableStatement statement = connection.prepareCall("{call GetUsernameAndAvatarFromId(?)}");
			statement.setInt(1, userId); // Assuming you want to get user info for user ID 1
			statement.execute();

			ResultSet resultSet = statement.getResultSet();

			if (resultSet.next()) {
				// Recupera tutte le informazioni dell'utente dal risultato della query
				String username = resultSet.getString("username");
				
				byte[] userAvatar = resultSet.getBytes("avatar");

				Image avatar=null;
				try {
					avatar = ImageToBlob.byteArrayToImage(userAvatar);
				} catch (Exception e) {
				}
				// Crea un oggetto User con le informazioni recuperate
				fetchedUser = new User(userId, username, null, null, avatar, null);
			}

			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return fetchedUser;
	}

	public boolean checkEmail(String email) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection(); 			
			CallableStatement statement = connection.prepareCall("{call CheckEmail(?)}");
			statement.setString(1, email);
			statement.execute();

			ResultSet resultSet = statement.getResultSet();

			if (resultSet.next()) {
				connection.close();
				return false;
			}else {
				connection.close();
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public String checkPassword(String email) {
		String password = null;
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();
			CallableStatement statement = connection.prepareCall("{call CheckPassword(?)}");
			statement.setString(1, email);
			statement.execute();

			ResultSet rs = statement.getResultSet();
			if (rs.next()) {
				password = rs.getString(1);
			}

			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return password;
	}

	public UserSettings fetchSettings(User user) {
		UserSettings settings = null;
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection(); 
			String query = "{call FetchUserSettings(?)}";
			CallableStatement callableStatement = connection.prepareCall(query);
			callableStatement.setInt(1, user.getId());
			ResultSet resultSet = callableStatement.executeQuery();
			if (resultSet.next()) {
				// Retrieve all user information from the query result
				int id = resultSet.getInt("user_id");
				double splitNum = resultSet.getDouble("split_num");
				double dbNum = resultSet.getDouble("db_num");
				double topSplitNum = resultSet.getDouble("top_split_num");
				boolean filter = resultSet.getBoolean("filter");
				String prefMic = resultSet.getString("pref_mic");
				String prefSpeaker = resultSet.getString("pref_speaker");
				boolean pushToTalk = resultSet.getBoolean("push_to_talk");
				int pushToTalkKey = resultSet.getInt("push_to_talk_key");

				settings = new UserSettings(id, splitNum, dbNum, topSplitNum, filter, prefMic, prefSpeaker, pushToTalk, pushToTalkKey);
			}
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return settings;
	}

	public UserDetailsObject fetchUserDetails(User user) {
		UserDetailsObject userDetails = null;
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection(); 
			String query = "{call FetchUserDetails(?)}";
			CallableStatement callableStatement = connection.prepareCall(query);
			callableStatement.setInt(1, user.getId());
			ResultSet resultSet = callableStatement.executeQuery();
			if (resultSet.next()) {
				boolean microphoneOn = resultSet.getBoolean("microphone_on");
				boolean audioOn = resultSet.getBoolean("audio_on");

				userDetails = new UserDetailsObject(user.getId());
				userDetails.setMicrophoneOn(microphoneOn);
				userDetails.setAudioOn(audioOn);
			}
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userDetails;
	}

	public void insertUser(User user) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();
			String query = "{CALL InsertUser(?, ?, ?, ?)}";
			CallableStatement statement = connection.prepareCall(query);

			statement.setString(1, user.getUsername());
			statement.setString(2, user.getEmail());
			statement.setString(3, user.getPassword());
			statement.setDate(4, Date.valueOf(user.getDateOfBirth()));

			statement.execute();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateSplitNum(Double splitNum, User user) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();

			String callProcedure = "{CALL UpdateSplitNum(?, ?)}";
			CallableStatement callableStatement = connection.prepareCall(callProcedure);
			callableStatement.setDouble(1, splitNum);
			callableStatement.setInt(2, user.getId());
			callableStatement.execute();

			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateDbNum(Double dbNum, User user) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();

			String sql = "{CALL UpdateDBNum(?, ?)}";
			CallableStatement callableStatement = connection.prepareCall(sql);
			callableStatement.setDouble(1, dbNum);
			callableStatement.setInt(2, user.getId());
			callableStatement.executeUpdate();

			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateTopSplitNum(Double splitNum, User user) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();

			String sql = "{CALL UpdateTopSplitNum(?, ?)}";
			CallableStatement callableStatement = connection.prepareCall(sql);
			callableStatement.setDouble(1, splitNum);
			callableStatement.setInt(2, user.getId());
			callableStatement.executeUpdate();

			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateFilter(boolean filter, User user) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();

			String sql = "{CALL UpdateFilter(?, ?)}";
			CallableStatement callableStatement = connection.prepareCall(sql);
			callableStatement.setBoolean(1, filter);
			callableStatement.setInt(2, user.getId());
			callableStatement.executeUpdate();

			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateFilterNum(double filterNum, User user) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();

			String sql = "{CALL UpdateFilterNum(?, ?)}";
			CallableStatement callableStatement = connection.prepareCall(sql);
			callableStatement.setDouble(1, filterNum);
			callableStatement.setInt(2, user.getId());
			callableStatement.executeUpdate();

			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updatePrefMic(String prefMic, User user) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();

			String sql = "{CALL UpdatePrefMic(?, ?)}";
			CallableStatement callableStatement = connection.prepareCall(sql);
			callableStatement.setString(1, prefMic);
			callableStatement.setInt(2, user.getId());
			callableStatement.executeUpdate();

			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updatePrefSpeaker(String prefSpeaker, User user) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();

			String sql = "{CALL UpdatePrefSpeaker(?, ?)}";
			CallableStatement callableStatement = connection.prepareCall(sql);
			callableStatement.setString(1, prefSpeaker);
			callableStatement.setInt(2, user.getId());
			callableStatement.executeUpdate();

			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updatePushToTalk(boolean pushToTalk, User user) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();

			String sql = "{CALL UpdatePushToTalk(?, ?)}";
			CallableStatement callableStatement = connection.prepareCall(sql);
			callableStatement.setBoolean(1, pushToTalk);
			callableStatement.setInt(2, user.getId());
			callableStatement.executeUpdate();

			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updatePushToTalkKey(int pushToTalkKey, User user) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();

			String sql = "{CALL UpdatePushToTalkKey(?, ?)}";
			CallableStatement callableStatement = connection.prepareCall(sql);
			callableStatement.setInt(1, pushToTalkKey);
			callableStatement.setInt(2, user.getId());
			callableStatement.executeUpdate();

			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public byte[] fetchAvatarAsBytesFromUserId(int userId) {
		byte[] userAvatar = null;
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection(); 
			String query = "{CALL FetchAvatarFromUserId(?)}";
			CallableStatement callableStatement = connection.prepareCall(query);
			callableStatement.setInt(1, userId);
			ResultSet resultSet = callableStatement.executeQuery();
			resultSet.next();
			userAvatar = resultSet.getBytes("avatar");

			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userAvatar;
	}

	public void updateAvatar(Image avatar, User user) {
		byte[] avatarArray = ImageToBlob.convertImageToByteArray(avatar);

		DatabaseConnection database = new DatabaseConnection();
		PreparedStatement preparedStatement = null;

		try {
			Connection connection = database.getConnection(); 

			String sql = "{CALL UpdateAvatar(?, ?)}";
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setBytes(1, avatarArray);
			preparedStatement.setInt(2, user.getId());
			preparedStatement.executeUpdate();

			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateUsername(String username, User user) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection();

			String sql = "{CALL UpdateUsername(?, ?)}";
			CallableStatement callableStatement = connection.prepareCall(sql);
			callableStatement.setString(1, username);
			callableStatement.setInt(2, user.getId());
			callableStatement.executeUpdate();

			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}