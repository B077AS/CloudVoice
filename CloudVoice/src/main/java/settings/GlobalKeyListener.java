package settings;

import java.util.ArrayList;
import java.util.List;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalKeyListener implements NativeKeyListener {

	private boolean firstKeyPressCaptured = false;
	private int keyCode=-1;
	private List<KeyObserver> observers = new ArrayList<>();

	public void addKeyObserver(KeyObserver observer) {
		observers.add(observer);
	}

	public void removeKeyObserver(KeyObserver observer) {
		observers.remove(observer);
	}

	public void notifyKeyObservers(int keyCode) {
		for (KeyObserver observer : observers) {
			observer.updateKeyPressed(keyCode);
		}
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if (!firstKeyPressCaptured) {
			keyCode = e.getKeyCode();
			firstKeyPressCaptured = true;
			notifyKeyObservers(keyCode);
			try {
				GlobalScreen.unregisterNativeHook();
			} catch (NativeHookException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		// Implementa se necessario
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
		// Implementa se necessario
	}
	
	public void stop() {
		try {
			GlobalScreen.removeNativeKeyListener(this);
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException ex) {
			ex.printStackTrace();
		}
	}

	public int getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}
	
	
}
