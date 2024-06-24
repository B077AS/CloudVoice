package rooms.voiceRooms;

import org.jnativehook.keyboard.NativeKeyListener;
import main.model.MainWindowModel;
import user.UserSettings;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;

public class PushToTalkListener implements NativeKeyListener{

	private boolean isKeyPressed = false;
	private boolean isTransmissionStarted = false;
	private MainWindowModel mainWindow;
	private UserSettings settings;
	private final int pushToTalkDefaultKey=NativeKeyEvent.VC_P;
	private int pushToTalkKey;

	public PushToTalkListener(MainWindowModel mainWindow, int pushToTalkKey) {
		this.mainWindow=mainWindow;
		if(pushToTalkKey<0) {
			this.pushToTalkKey=pushToTalkDefaultKey;
		}else {
			this.pushToTalkKey=pushToTalkKey;
		}
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if (e.getKeyCode() == pushToTalkKey && !isKeyPressed) {
			isKeyPressed = true;
			if (!isTransmissionStarted) {
				isTransmissionStarted = true;
				if(settings==null) {
					mainWindow.getAudioManager().startAudioTransmission();
				}
				mainWindow.getAudioManager().resumeMicrophoneTransmission();
			}
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		try {
			if (e.getKeyCode() == pushToTalkKey) {
				isKeyPressed = false;
				isTransmissionStarted = false;
				this.settings= mainWindow.getAudioManager().getSettings();
				mainWindow.getAudioManager().pauseMicrophoneTransmission();
			}
		}catch(Exception ex) {
		}
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
		// Non necessario per la gestione del "Push to Talk"
	}

	public void stop() {
		try {	    	
			GlobalScreen.removeNativeKeyListener(this);
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e) {
			e.printStackTrace();
		}
	}


	public int getPushToTalkKey() {
		return pushToTalkKey;
	}


	public void setPushToTalkKey(int pushToTalkKey) {
		this.pushToTalkKey = pushToTalkKey;
	}

	public UserSettings getSettings() {
		return settings;
	}

	public void setSettings(UserSettings settings) {
		this.settings = settings;
	}
	
	
}