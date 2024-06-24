package main.view;

import com.sun.jna.*;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;

public class FXWinUtil {

	public static WinDef.HWND getNativeHandleForStage(Stage stage) {
		try {
			var getPeer = Window.class.getDeclaredMethod("getPeer", null);
			getPeer.setAccessible(true);
			var tkStage = getPeer.invoke(stage);
			var getRawHandle = tkStage.getClass().getMethod("getRawHandle");
			getRawHandle.setAccessible(true);
			var pointer = new Pointer((Long) getRawHandle.invoke(tkStage));
			return new WinDef.HWND(pointer);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static void setDarkMode(Stage stage, boolean darkMode) {
		var hwnd = FXWinUtil.getNativeHandleForStage(stage);
		var dwmapi = Dwmapi.INSTANCE;
		WinDef.BOOLByReference darkModeRef = new WinDef.BOOLByReference(new WinDef.BOOL(darkMode));

		dwmapi.DwmSetWindowAttribute(hwnd, 20, darkModeRef, Native.getNativeSize(WinDef.BOOLByReference.class));

	}
	
	public static void forceRedrawOfWindowTitleBarFullScreen(Stage stage) {
        var maximized = stage.isMaximized();
        stage.setMaximized(!maximized);
        stage.setMaximized(maximized);
    }
    
	public static void forceRedrawOfWindowTitleBar(Stage stage) {
		double currentHeight = stage.getHeight();
		stage.setHeight(currentHeight - 17);
		Platform.runLater(() -> {
			stage.setHeight(currentHeight + 17);
		});
    }
}
