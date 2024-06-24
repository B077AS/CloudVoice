package rooms.videoRooms.screenShare;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.model.CSSLoader;
import main.model.MainWindowModel;
import main.view.MainWindow;
import rooms.videoRooms.VideoManager;
import java.io.File;
import java.util.Map.Entry;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

public class WindowLister extends Application {

	private MainWindowModel mainModel;
	private MainWindow mainWindow;
	private AppsListView listView;
	private final int WIDTH=700;
	private final int HEIGHT = 380;

	public WindowLister(MainWindowModel mainModel, MainWindow mainWindow) {
		this.mainModel=mainModel;
		this.mainWindow=mainWindow;
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Programs List");

		listView=new AppsListView();
		listRunningApplications();
		listView.setItems(FXCollections.observableArrayList(listView.getBoxes().values()));

		TextField searchField = new TextField();
		searchField.setPromptText("Search by Name");
		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			listView.getItems().clear(); // Clear existing items
			for (Entry<String, AppsBox> app : listView.getBoxes().entrySet()) { // originalListOfApps is the list containing all AppsBox objects
				if (app.getValue().getName().toLowerCase().contains(newValue)) { // Check if the app name contains the search text
					listView.getItems().add(app.getValue()); // Add matching app to the ListView
				}
			}
		});
		HBox searchFieldBox=new HBox();
		searchFieldBox.setPadding(new Insets(8, 4.5, 3, 4.5));
		searchFieldBox.setAlignment(Pos.CENTER);
		searchFieldBox.getChildren().add(searchField);
		HBox.setHgrow(searchField, Priority.ALWAYS);

		Button confirmButton = new Button("Confirm");
		confirmButton.setDisable(true);
		confirmButton.setOnAction(event -> {
			AppsBox selectedItem = listView.getSelectionModel().getSelectedItem();
			if (selectedItem != null) {

				VideoManager voiceManager=new VideoManager(selectedItem.getName(), mainModel, mainWindow);
				mainModel.setVideoManager(voiceManager);
				primaryStage.close();
			}
		});

		listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			try {
				if(oldValue!=newValue) {
					oldValue.setBoxUnFocused();
					newValue.setBoxFocused();
				}
			}catch(Exception e) {
				newValue.setBoxFocused();
			}
			confirmButton.setDisable(newValue == null);
		});

		VBox buttonBox = new VBox();
		buttonBox.setPrefWidth(WIDTH);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setPadding(new Insets(0, 0, 10, 0));
		
		Separator spacer=new Separator();
		spacer.setPrefWidth(WIDTH);
		spacer.setPadding(new Insets(0, 0, 10, 0));
		
		buttonBox.getChildren().addAll(spacer, confirmButton);

		VBox mainBox = new VBox();
		mainBox.getChildren().addAll(searchFieldBox, listView, buttonBox);
		
		Scene scene = new Scene(mainBox, WIDTH, HEIGHT);

		CSSLoader cssLoader=new CSSLoader();
		scene.getStylesheets().add(cssLoader.loadCss());
		primaryStage.setResizable(false);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public void listRunningApplications() {
		User32 user32 = User32.INSTANCE;

		user32.EnumWindows((hWnd, data) -> {
			char[] windowText = new char[512];
			user32.GetWindowText(hWnd, windowText, 512);
			String title = Native.toString(windowText).trim();

			if (!title.isEmpty() && isWindowVisible(hWnd) && isWindowEnabled(hWnd) && !isSystemWindow(title)) {

				IntByReference processId = new IntByReference();
				user32.GetWindowThreadProcessId(hWnd, processId);
				
				Image icon=getWindowIcon(hWnd, processId);
				AppsBox box=new AppsBox(icon, title);
				listView.getBoxes().put(title, box);
			}
			return true;
		}, null);
	}

	public boolean isWindowVisible(WinDef.HWND hWnd) {
		User32 user32 = User32.INSTANCE;
		return user32.IsWindowVisible(hWnd);
	}

	public boolean isWindowEnabled(WinDef.HWND hWnd) {
		User32 user32 = User32.INSTANCE;
		return user32.IsWindowEnabled(hWnd);
	}

	public boolean isSystemWindow(String title) {
		String[] excludedNames = {
				"Settings",
				"Calculator",
				"Microsoft Text Input Application",
				"NVIDIA GeForce Overlay",
				"Program Manager"
		};

		for (String excludedName : excludedNames) {
			if (title.contains(excludedName)) {
				return true;
			}
		}
		return false;
	}

	public Image getWindowIcon(WinDef.HWND hWnd, IntByReference processId) {
		File f = new File(getProcessFilePath(processId.getValue()));
		try {
			FileSystemView view = FileSystemView.getFileSystemView();
			Icon icon = view.getSystemIcon(f, 30, 30);
			return  convertToJavaFXImage(icon);
		}catch(Exception e) {
		}
		return null;
	}

	public Image convertToJavaFXImage(Icon icon) {
		// Convert javax.swing.Icon to java.awt.Image
		java.awt.Image awtImage = iconToAwtImage(icon);
		// Convert java.awt.Image to javafx.scene.image.Image
		return awtImageToJavaFXImage(awtImage);
	}

	public java.awt.Image iconToAwtImage(Icon icon) {
		// Create a BufferedImage to hold the icon's image data
		int width = icon.getIconWidth();
		int height = icon.getIconHeight();
		java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D graphics = bufferedImage.createGraphics();
		icon.paintIcon(null, graphics, 0, 0);
		graphics.dispose();
		return bufferedImage;
	}

	public Image awtImageToJavaFXImage(java.awt.Image awtImage) {
		return SwingFXUtils.toFXImage((java.awt.image.BufferedImage) awtImage, null);
	}

	public String getProcessFilePath(int pid) {
		WinNT.HANDLE processHandle = Kernel32.INSTANCE.OpenProcess(
				Kernel32.PROCESS_QUERY_LIMITED_INFORMATION,
				false,
				pid);
		if (processHandle != null) {
			char[] buffer = new char[1024];
			IntByReference bufferSize = new IntByReference(buffer.length);
			if (Kernel32.INSTANCE.QueryFullProcessImageName(processHandle, 0, buffer, bufferSize)) {
				return Native.toString(buffer);
			}
		}
		return null;
	}
}