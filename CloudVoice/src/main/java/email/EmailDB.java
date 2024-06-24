package email;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import main.model.DatabaseConnection;

public class EmailDB {

	public Email fetchEmailDetails() {

		Email emailObj=null;
		
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection(); 
			String query = "SELECT * FROM email";	
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			resultSet.next();

			String clientId = resultSet.getString("client_id");
            String clientSecret = resultSet.getString("client_secret");
            String accessToken = resultSet.getString("access_token");
            String refreshToken = resultSet.getString("refresh_token");
            String email = resultSet.getString("email");
			emailObj=new Email(clientId, clientSecret, accessToken, refreshToken, email);
			return emailObj;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return emailObj;
	}
	
	public void updateAccessToken(String accessToken, String clientId) {
		try {
			DatabaseConnection database = new DatabaseConnection();
			Connection connection = database.getConnection(); 
			String query ="UPDATE email SET access_token = ? WHERE client_id = ? ";
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, accessToken);
			preparedStatement.setString(2, clientId);
			preparedStatement.executeUpdate();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}