package registration;

import javafx.stage.*;
import login.LoginPage;
import main.model.CSSLoader;
import main.view.CustomAlert;
import user.User;
import user.UserDB;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

public class VerificationDialog extends Stage {
	
    private TextField verificationCodeField;
    private Button verifyButton;
    private String code;
    private Stage primaryStage;
    private User user;

    public VerificationDialog(String code, Stage primaryStage, User user) {
        this.primaryStage = primaryStage;
        this.code = code;
        this.user = user;
        
        // Imposta il titolo della finestra
        setTitle("Verification");

        // Crea un layout per i controlli
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        // Crea un campo di testo per l'inserimento del codice di verifica
        verificationCodeField = new TextField();
        verificationCodeField.setPromptText("Insert Code");

        // Crea un pulsante per verificare il codice
        verifyButton = new Button("Verify");
        verifyButton.setOnAction(e -> verify());

        // Aggiungi i controlli al layout
        layout.getChildren().addAll(verificationCodeField, verifyButton);

        // Imposta l'allineamento al centro del layout
        layout.setAlignment(Pos.CENTER);

        // Imposta il layout della scena
        Scene scene = new Scene(layout, 400, 300); // Aumenta le dimensioni della finestra

		CSSLoader cssLoader=new CSSLoader();
		scene.getStylesheets().add(cssLoader.loadCss());
		
        setScene(scene);
    }

    private void verify() {
        String verificationCode = verificationCodeField.getText();
        if (verificationCode.equals(code)) {
        	UserDB userDb=new UserDB();
    		userDb.insertUser(user);
    		
			LoginPage loginPage = new LoginPage();
			loginPage.start(primaryStage);
			
            close();
        } else {
        	CustomAlert invalidCodeAlert=new CustomAlert();
        	invalidCodeAlert.createAlert("Error", "Code doesn't match, retry.");
        }
    }
}
