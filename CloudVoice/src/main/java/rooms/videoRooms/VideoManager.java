package rooms.videoRooms;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.scene.control.Button;
import main.model.MainWindowModel;
import main.model.SVGCodes;
import main.model.VPS;
import main.view.MainWindow;
import rooms.voiceRooms.VoiceRoom;
import serializedObjects.ConnectToRoomObject;
import serializedObjects.UserDetailsObject;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

public class VideoManager {

	private static final int CAPTURE_INTERVAL = 90;
	private Robot robot;
	private boolean running;
	private boolean targetWindowInFocus = false;
	private WinDef.HWND targetWindow;
	private String appName;
	private MainWindowModel mainModel;
	private MainWindow mainWindow;
	private Thread focusMonitoringThread;
	private Socket socket;
	private VideoRoom room;
	private Button stopButton;
	private OutputStream outputVideoStream;
	private ObjectOutputStream objectOutputVideoStream;
	private ExecutorService sendImageExecutor = Executors.newSingleThreadExecutor();
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);


	public VideoManager(String appName, MainWindowModel mainModel, MainWindow mainWindow) {
		this.appName = appName;
		this.mainModel = mainModel;
		this.mainWindow=mainWindow;
		room=((VoiceRoom)mainModel.getCurrentRoom()).getVideoRoom();
		initialize();
	}

	private void initialize() {
		stopButton=SVGCodes.createIconButton(SVGCodes.offScreenShareSVG, 45, 37, 25);

		stopButton.setOnAction(event -> {
			stopScreenSharing();

			int index = mainWindow.getButtonsBox().getChildren().indexOf(stopButton);
			if (index != -1) {
				mainWindow.getButtonsBox().getChildren().remove(index);
				mainWindow.getButtonsBox().getChildren().add(index, mainWindow.getScreenShareButton());
			}

		});

		int index = mainWindow.getButtonsBox().getChildren().indexOf(mainWindow.getScreenShareButton());
		if (index != -1) {
			mainWindow.getButtonsBox().getChildren().remove(index);
			mainWindow.getButtonsBox().getChildren().add(index, stopButton);
		}

		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
			System.exit(1);
		}

		targetWindow = findTargetWindow(appName);

		sendRoomDetails();//da eliminare quando runno il programma intero
		connectToRoom();//da eliminare quando runno il programma intero

		createStream();//da eliminare quando runno il programma intero

		mainModel.getUser().getUserDetails().setLive(true);//da eliminare quando runno il programma intero
		mainModel.getUser().getUserDetails().setMainRoomId(mainModel.getCurrentRoom().getID());//da eliminare quando runno il programma intero
		updateUserTag(mainModel.getUser().getUserDetails());//da eliminare quando runno il programma intero

		/*if (targetWindow != null) {
			running=true;
			sendRoomDetails();
			connectToRoom();

			createStream();

			mainModel.getUser().getUserDetails().setLive(true);
			mainModel.getUser().getUserDetails().setMainRoomId(mainModel.getCurrentRoom().getID());
			updateUserTag(mainModel.getUser().getUserDetails());

			focusMonitoringThread = new Thread(this::monitorWindowFocus);
			focusMonitoringThread.setDaemon(true);
			focusMonitoringThread.start();

			executorService.scheduleAtFixedRate(this::captureScreen, 0, CAPTURE_INTERVAL, TimeUnit.MILLISECONDS);

		} else {
			System.out.println("No window found");
		}*/
	}


	public void monitorWindowFocus() {
		while (running) {
			WinDef.HWND currentForegroundWindow = User32.INSTANCE.GetForegroundWindow();
			if (currentForegroundWindow != null && !currentForegroundWindow.equals(targetWindow)) {
				targetWindowInFocus = false;
			} else {
				targetWindowInFocus = true;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

	public WinDef.HWND findTargetWindow(String targetAppName) {//TODO
		User32 user32 = User32.INSTANCE;
		WinDef.HWND[] hWnd = new WinDef.HWND[1];

		user32.EnumWindows((hWndParam, lParam) -> {
			char[] windowText = new char[512];
			user32.GetWindowText(hWndParam, windowText, 512);
			String title = Native.toString(windowText).trim();

			if (!title.isEmpty() && title.contains(targetAppName)) {
				hWnd[0] = hWndParam;
				return false;
			}
			return true;
		}, null);

		return hWnd[0];
	}

	public void captureScreen() {
		if (running && targetWindowInFocus) {
			Rectangle rect = getWindowRect(targetWindow);
			int borderWidth = 8;
			rect.x += borderWidth;
			rect.y += borderWidth;
			rect.width -= 2 * borderWidth;
			rect.height -= 2 * borderWidth;
			BufferedImage screenshot = robot.createScreenCapture(rect);
			sendImageExecutor.execute(() -> sendImageToServer(screenshot));
		} else {

		}
	}

	public static Rectangle getWindowRect(WinDef.HWND hWnd) {
		User32 user32 = User32.INSTANCE;
		WinDef.RECT rect = new WinDef.RECT();
		user32.GetWindowRect(hWnd, rect);
		return new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
	}

	public void sendRoomDetails() {
		try {
			ConnectToRoomObject object=new ConnectToRoomObject(room.getID(), room.getPort(), room.getType(), mainModel.getUser().getUserDetails());
			socket=mainModel.getSocket();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void connectToRoom() {
		try {
			socket= new Socket(VPS.SERVER_ADDRESS, room.getPort());
			System.out.println("Request connection for port: "+room.getPort());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendImageToServer(BufferedImage screenshot) {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(screenshot, "jpeg", byteArrayOutputStream);	        
			byte[] imageBytes = byteArrayOutputStream.toByteArray();

			objectOutputVideoStream.writeObject(imageBytes);
			objectOutputVideoStream.flush();
			//System.out.println("Frame sent");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	


	public void createStream() {
		try {
			outputVideoStream = socket.getOutputStream();
			objectOutputVideoStream = new ObjectOutputStream(outputVideoStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void updateUserTag(UserDetailsObject object) {
		Socket updateSocket=mainModel.getUpdateSocket();
		try {
			OutputStream outputStream=updateSocket.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

			objectOutputStream.writeObject(object);	
			objectOutputStream.flush();

		}catch(Exception e) {
			e.printStackTrace();
		}
	}


	public void stopScreenSharing() {
		mainModel.getUser().getUserDetails().setLive(false);
		mainModel.getUser().getUserDetails().setMainRoomId(room.getID());
		updateUserTag(mainModel.getUser().getUserDetails());

		running = false;
		sendImageExecutor.shutdown();
		stopScreenCaptureTimer();
		try {
			if (!sendImageExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
				sendImageExecutor.shutdownNow();
				if (!sendImageExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
					System.err.println("ExecutorService did not terminate");
				}
			}
		} catch (InterruptedException e) {
			sendImageExecutor.shutdownNow();
			Thread.currentThread().interrupt();
		}

		if (focusMonitoringThread != null) {
			focusMonitoringThread.interrupt();
			try {
				focusMonitoringThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		closeSocket();
	}

	private void closeSocket() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopScreenCaptureTimer() {
		executorService.shutdown();
	}

}