package email;

public class Email {
	
	private String clientId;
	private String secretId;
	private String accessToken;
	private String refreshToken;
	private String email;
	
	public Email(String clientId, String secretId, String accessToken, String refreshToken, String email) {

		this.clientId = clientId;
		this.secretId = secretId;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.email=email;
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getSecretId() {
		return secretId;
	}
	
	public void setSecretId(String secretId) {
		this.secretId = secretId;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public String getRefreshToken() {
		return refreshToken;
	}
	
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
}