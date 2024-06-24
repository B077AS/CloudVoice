package registration;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;
import email.GoogleMailService;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import main.model.CSSLoader;
import main.view.CustomAlert;
import user.User;
import user.UserDB;

public class RegistrationPage extends Application {

	private Stage primaryStage;


	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		primaryStage.setTitle("Registration");

		GridPane registrationLayout = new GridPane();
		registrationLayout.setVgap(10); // Spaziatura verticale tra le righe
		registrationLayout.setHgap(10); // Spaziatura orizzontale tra le colonne

		TextField usernameField = new TextField();
		TextField emailField = new TextField();
		PasswordField passwordField = new PasswordField();
		PasswordField confirmPasswordField = new PasswordField();
		Label passwordInfoLabel = new Label("?");
		Tooltip passwordTooltip = new Tooltip("Your password must be at least 6 characters long \n and must contain both letters and numbers");
		passwordInfoLabel.setStyle("-fx-font-size: 16;");
		passwordInfoLabel.setCursor(Cursor.HAND);
		passwordInfoLabel.setTooltip(passwordTooltip);
		DatePicker birthdatePicker = new DatePicker();
		Button registerButton = new Button("Register");

		Label usernameLabel = new Label("Username:");
		Label emailLabel = new Label("Email:");
		Label passwordLabel = new Label("Password:");
		Label confirmPasswordLabel = new Label("Confirm Password:");
		Label dateLabel = new Label("Date of Birth:");

		registrationLayout.add(usernameLabel, 0, 0);
		registrationLayout.add(usernameField, 1, 0);
		registrationLayout.add(emailLabel, 0, 1);
		registrationLayout.add(emailField, 1, 1);
		registrationLayout.add(passwordLabel, 0, 2);
		registrationLayout.add(passwordField, 1, 2);
		registrationLayout.add(passwordInfoLabel, 3, 2);
		registrationLayout.add(confirmPasswordLabel, 0, 3);
		registrationLayout.add(confirmPasswordField, 1, 3);
		registrationLayout.add(dateLabel, 0, 4);
		registrationLayout.add(birthdatePicker, 1, 4);
		registrationLayout.add(registerButton, 1, 5);

		registrationLayout.setAlignment(Pos.CENTER);

		// Registration event
		registerButton.setOnAction(event -> {
			String username = usernameField.getText();
			String password = passwordField.getText();
			String confirmPassword = confirmPasswordField.getText();
			String email = emailField.getText();
			LocalDate dateOfBirth = birthdatePicker.getValue();
			validateRegistration(username, password, confirmPassword, email, dateOfBirth);
		});

		Scene registrationScene = new Scene(registrationLayout, 1200, 700);
		
		CSSLoader cssLoader=new CSSLoader();
		registrationScene.getStylesheets().add(cssLoader.loadCss());
		
		primaryStage.setScene(registrationScene);
		primaryStage.setMaximized(true);
		primaryStage.show();
	}


	// Valida i dati di registrazione
	private void validateRegistration(String username, String password, String confirmPassword, String email, LocalDate dateOfBirth) {
		CustomAlert registrationErrorAlert=new CustomAlert();
		if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty() || dateOfBirth==null) {
			registrationErrorAlert.createAlert("Registration Error", "All fields are required.");
			return;
		}

		String regex = "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(password);
		if(matcher.matches()==false) {
			registrationErrorAlert.createAlert("Registration Error", "Password too weak.");
			return;
		}

		if (!password.equals(confirmPassword)) {
			registrationErrorAlert.createAlert("Registration Error", "Passwords do not match.");
			return;
		}

		LocalDate dataAttuale = LocalDate.now();
		Period differenza = Period.between(dateOfBirth, dataAttuale);

		if (differenza.getYears() > 13) {
		} else {
			registrationErrorAlert.createAlert("Date Error", "You must be over 13 to register.");
			return;
		}

		//TODO verificare che la mail non sia gia nel sistema

		UserDB userDB=new UserDB();
		if(userDB.checkEmail(email)!=true) {
			registrationErrorAlert.createAlert("Registration Error", "Email already in use.");
			return;
		}

		String hashPassword=BCrypt.hashpw(password, BCrypt.gensalt());


		String code=generateVerificationCode();
		Thread sendEmailThread = new Thread(new Runnable() {
			public void run() {
				GoogleMailService sender=new GoogleMailService(email, code);
				sender.sendEmail();
			}
		});
		sendEmailThread.setDaemon(true);
		sendEmailThread.start();


		VerificationDialog verify=new VerificationDialog(code, primaryStage, new User(0, username, hashPassword, email, null, dateOfBirth));
		verify.show();
	}

	private String generateVerificationCode() {
		String CHARACTERS = "0123456789";
		int CODE_LENGTH = 5;
		SecureRandom random = new SecureRandom();
		StringBuilder code = new StringBuilder();

		for (int i = 0; i < CODE_LENGTH; i++) {
			int randomIndex = random.nextInt(CHARACTERS.length());
			char randomChar = CHARACTERS.charAt(randomIndex);
			code.append(randomChar);
		}
		return code.toString();
	}
}
