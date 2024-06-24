package login;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import main.controller.MainWindowController;
import main.model.CSSLoader;
import main.model.MainWindowModel;
import main.model.VPS;
import main.view.CustomAlert;
import main.view.MainWindow;
import registration.RegistrationPage;
import user.User;
import user.UserDB;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.mindrot.jbcrypt.BCrypt;

public class LoginPage extends Application {

	private TextField usernameField;
	private PasswordField passwordField;
	private UserDB userDB;
	private Stage primaryStage;
	private CheckBox rememberMeCheckbox;
	private String filePath;

	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage=primaryStage;		
		this.userDB=new UserDB();
		
		VPS.loadVpsDetails();
		
		JSONParser parser = new JSONParser();

		String userHome = System.getProperty("user.home");
		filePath = userHome + File.separator + "userPass.json";

		try {
			FileReader reader = new FileReader(filePath);
			Object obj = parser.parse(reader);

			JSONObject jsonObject = (JSONObject) obj;
			String username = (String) jsonObject.get("username");
			String password = (String) jsonObject.get("password");

			User loggedInUser=null;
			String dbPassword=userDB.checkPassword(username);

			if(BCrypt.checkpw(password, dbPassword)==true) {
				loggedInUser = userDB.fetchUser(username);
			}

			reader.close();

			if (loggedInUser != null) {
				openMainWindow(loggedInUser);
				return;
			}

		} catch (Exception e) {
		}

		primaryStage.setTitle("Login");

		VBox loginLayout = new VBox(10);
		usernameField = new TextField();
		passwordField = new PasswordField();
		Button loginButton = new Button("Login");
		Label signupLabel = new Label("Signup");
		rememberMeCheckbox = new CheckBox("Remember Me");

		usernameField.setMaxWidth(200);
		passwordField.setMaxWidth(200);

		signupLabel.setStyle("-fx-underline: true; -fx-cursor: hand;");
		signupLabel.setOnMouseClicked(event -> {
			RegistrationPage registrationPage = new RegistrationPage();
			Stage stage = new Stage();
			registrationPage.start(stage);
			primaryStage.close();
		});


		passwordField.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				performLoginAction();
			}
		});
		loginButton.setOnAction(event -> performLoginAction());

		loginLayout.setAlignment(Pos.CENTER);

		loginLayout.getChildren().addAll(new Label("Username:"), usernameField, new Label("Password:"), passwordField, rememberMeCheckbox, loginButton, signupLabel);

		Scene loginScene = new Scene(loginLayout, 1200, 700);

		CSSLoader cssLoader=new CSSLoader();
		loginScene.getStylesheets().add(cssLoader.loadCss());

		primaryStage.setScene(loginScene);
		primaryStage.setMaximized(true);

		primaryStage.widthProperty().addListener((obs, oldWidth, newWidth) -> {
			if (newWidth.intValue() < 400) {
				primaryStage.setWidth(500);
			}
		});

		primaryStage.heightProperty().addListener((obs, oldHeight, newHeight) -> {
			if (newHeight.intValue() < 400) {
				primaryStage.setHeight(500);
			}
		});

		primaryStage.show();
	}

	private void performLoginAction() {
		String username = usernameField.getText();
		String password = passwordField.getText();
		User loggedInUser=null;
		String dbPassword=userDB.checkPassword(username);

		try {
			if(BCrypt.checkpw(password, dbPassword)==true) {
				loggedInUser = userDB.fetchUser(username);
			}else {
				CustomAlert invalidCredentialsAlert=new CustomAlert();
				invalidCredentialsAlert.createAlert("Invalid Credentials", "Incorrect username or password.");
			}
		}catch(Exception e) {
			CustomAlert invalidCredentialsAlert=new CustomAlert();
			invalidCredentialsAlert.createAlert("Invalid Credentials", "Incorrect username or password.");
		}

		if (loggedInUser != null) {
			primaryStage.close();
			if (rememberMeCheckbox.isSelected()) {
				try {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("username", username);
					jsonObject.put("password", password);
					File file = new File(filePath);
					PrintWriter fileWriter = new PrintWriter(file);
					fileWriter.write(jsonObject.toJSONString());
					fileWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			openMainWindow(loggedInUser);
		}
	}

	private void openMainWindow(User user) {
		MainWindow mainWindow = new MainWindow(user);
		Stage stage = new Stage();
		mainWindow.start(stage);
		MainWindowModel model=new MainWindowModel(user);
		MainWindowController controller=new MainWindowController(model, mainWindow);
		controller.startController();
	}

	/*private void checkHashToConnect() {
		try {
			hashSocket=new Socket(VPS.SERVER_ADDRESS, VPS.SERVER_HASH_PORT);
			System.out.println("Request connection to port: " + VPS.SERVER_HASH_PORT);

			HashGenerator hashGenerator=new HashGenerator();
			String hash=hashGenerator.getCombinedHash();

			OutputStream outputStream = hashSocket.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(hash);

			receiveDatabaseDetails();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void receiveDatabaseDetails() {
		try {
			while(hashSocket.isClosed()==false) {
				InputStream inputStream = hashSocket.getInputStream();
				ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

				DatabaseDetailsObject receivedObject = (DatabaseDetailsObject) objectInputStream.readObject();
				DatabaseDetails databaseDetails=DatabaseDetails.getInstance();
				databaseDetails.setDB_URL(receivedObject.getDB_URL());
				databaseDetails.setDB_USER(receivedObject.getDB_USER());
				databaseDetails.setDB_PASSWORD(receivedObject.getDB_PASSWORD());
				if(databaseDetails!=null) {
					hashSocket.close();
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}*/
}
