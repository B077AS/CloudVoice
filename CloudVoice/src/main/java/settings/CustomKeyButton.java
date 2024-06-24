package settings;

import org.jnativehook.keyboard.NativeKeyEvent;

import javafx.application.Platform;
import javafx.scene.control.Button;
import user.UserSettings;

public class CustomKeyButton extends Button implements KeyObserver{

	public CustomKeyButton(UserSettings settings) {
		if(settings.getPushToTalkKey()<0) {
			this.setText("P");
		}else {
			this.setText(NativeKeyEvent.getKeyText(settings.getPushToTalkKey()));
		}
		this.setDisable(true);
		this.setPickOnBounds(false);
	}

	@Override
	public void updateKeyPressed(int keyCode) {
		Platform.runLater(() -> {
			this.setText(NativeKeyEvent.getKeyText(keyCode));
		});
	}

}
