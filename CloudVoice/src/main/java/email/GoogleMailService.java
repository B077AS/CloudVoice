package email;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.codec.binary.Base64;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.auth.oauth2.Credential.Builder;
import com.google.api.client.auth.oauth2.*;

public class GoogleMailService {

	private static final String APPLICATION_NAME = "CloudVoice";
	private static final JsonFactory JSON_FACTORY = new GsonFactory();
	private String clientId;
	private String clientSecret;
	private String accessToken;
	private String refreshToken;
	private Gmail gmail;
	private String sender;
	private String receiver;
	private String code;
	private EmailDB emailDb;
	private Credential credentials;

	public GoogleMailService(String receiver, String code) {

		emailDb=new EmailDB();
		Email details=emailDb.fetchEmailDetails();
		this.clientId=details.getClientId();
		this.clientSecret=details.getSecretId();
		this.accessToken=details.getAccessToken();
		this.refreshToken=details.getRefreshToken();
		this.sender=details.getEmail();
		this.receiver=receiver;
		this.code=code;
		try {
			credentials = createCredentials();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			gmail = new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credentials)
					.setApplicationName(APPLICATION_NAME)
					.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendEmail() {
		try {
			Message message = createMessageWithEmail();
			gmail.users().messages().send("me", message).execute();
		} catch (Exception e) {
			try {
				credentials.refreshToken();
				Message message = createMessageWithEmail();
				gmail.users().messages().send("me", message).execute();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private Message createMessageWithEmail() throws Exception {
		MimeMessage email = createEmail();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		email.writeTo(buffer);
		byte[] bytes = buffer.toByteArray();
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes);

		Message message = new Message();
		message.setRaw(encodedEmail);

		return message;
	}

	private MimeMessage createEmail() throws Exception {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		InternetAddress from = new InternetAddress(this.sender);
		InternetAddress recipient = new InternetAddress(this.receiver);

		email.setFrom(from);
		email.addRecipient(javax.mail.Message.RecipientType.TO, recipient);
		email.setSubject("Verification Code");
		email.setText("Your Verification Code is: "+this.code);
		return email;
	}


	public Credential createCredentials() throws GeneralSecurityException, IOException {

		// Crea un oggetto Credential
		Credential credential = new Builder(BearerToken.authorizationHeaderAccessMethod())
				.setTransport(GoogleNetHttpTransport.newTrustedTransport())
				.setJsonFactory(JSON_FACTORY)
				.setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret))
				.setTokenServerEncodedUrl("https://accounts.google.com/o/oauth2/token")//("https://oauth2.googleapis.com/token")
				.addRefreshListener(new CredentialRefreshListener() {
					@Override
					public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
						accessToken=tokenResponse.getAccessToken().toString();
						emailDb.updateAccessToken(accessToken, clientId);
					}

					@Override
					public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {
						System.err.println("Token Refresh Error: " + tokenErrorResponse.getError());
					}
				})
				.build();
		credential.setAccessToken(accessToken);
		credential.setRefreshToken(refreshToken);
		return credential;
	}
}