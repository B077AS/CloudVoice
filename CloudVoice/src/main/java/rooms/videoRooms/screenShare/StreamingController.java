package rooms.videoRooms.screenShare;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import main.controller.MainWindowController;
import main.model.SVGCodes;
import server.ServerBox;
import user.ConnectedUserBox;
import user.UsersListView;

public class StreamingController {

	private StreamingView view;
	private StreamingModel model;

	public StreamingController(StreamingView view, StreamingModel model) {
		this.view = view;
		this.model = model;
		setListeners();
	}

	public void setListeners() {
		
		view.setOnCloseRequest(event -> {
			try {
				model.stopReceiving();
			}catch(Exception e) {			
			}
			try {
				model.getMainWindowModel().notifyClosureOperations();
			}catch(Exception e) {			
			}
		});

		view.getHideButton().setOnAction(event ->{
			
			if(model.isClosedChat()==false) {
				model.setClosedChat(true);
				view.getHideButton().setGraphic(SVGCodes.createSVGGraphic(SVGCodes.hideChatSVG, 25));
				view.getSplitPane().setDividerPositions(0.9);
				view.getSplitPane().getItems().remove(view.getRightBox());

			}else {
				model.setClosedChat(false);
				view.getHideButton().setGraphic(SVGCodes.createSVGGraphic(SVGCodes.showChatSVG, 25));				
				view.getSplitPane().getItems().add(view.getRightBox());
				view.getSplitPane().setDividerPositions(0.8);
				Platform.runLater(() -> {
					view.getImageView().fitWidthProperty().bind(view.getSplitPane().getDividers().get(0).positionProperty().multiply(view.getSplitPane().widthProperty()));
					view.getImageView().fitHeightProperty().bind(view.getSplitPane().getDividers().get(0).positionProperty().multiply(view.getSplitPane().heightProperty()).add(50));
				});
			}			
		});

		view.getStopButton().setOnAction(event ->{

			LinkedHashMap<Integer, ServerBox> serverBoxes=model.getMainWindowView().getServersBox().getServersList();
			ObservableList<Node> serverNodes=model.getMainWindowView().getServersBox().getChildren();
			
			HashMap<Integer, ConnectedUserBox> users=model.getMainWindowView().getUsersListView().getUsers();

			model.getMainWindowView().getButtonsBox().getChildren().removeAll(view.getStopButton(), view.getHideButton());
			model.getMainWindowView().start(model.getMainWindowView().getPrimaryStage());
			MainWindowController controller=new MainWindowController(model.getMainWindowModel(), model.getMainWindowView());
			controller.startController();

			model.getMainWindowView().getServersBox().setServersList(serverBoxes);
			model.getMainWindowView().getServersBox().getChildren().clear();
			model.getMainWindowView().getServersBox().getChildren().addAll(serverNodes);
			
			model.getMainWindowModel().notifyObserverServerSelection(model.getMainWindowModel().getCurrentServer());
			model.getMainWindowView().getServersBox().getServersList().get(model.getMainWindowModel().getCurrentServer().getId()).setBoxFocused();
			model.getMainWindowModel().notifyObserverChatRoomSelection(model.getMainWindowModel().getCurrentRoom());
			
			model.getMainWindowView().setUsersListView(new UsersListView());
			model.getMainWindowView().getTopSplitPane().getItems().remove(1);
			model.getMainWindowView().getTopSplitPane().getItems().add(model.getMainWindowView().getUsersListView());
			model.getMainWindowView().getUsersListView().setMinWidth(100);
			model.getMainWindowView().getUsersListView().setUsers(users);
			model.getMainWindowView().getUsersListView().setItems(FXCollections.observableArrayList(users.values()));
			
			model.getMainWindowModel().notifyObserverSetDivider();
			model.getMainWindowModel().notifyObserverForRecoverMessages();

			model.stopReceiving();			
			view.close();
		});
	}

	public void receiveVideo() {
		try {
			InputStream inputStream = model.getSocket().getInputStream();
			ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

			while (model.isRunning()) {				
				byte[] imageBytes = (byte[]) objectInputStream.readObject();
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
				BufferedImage receivedImage = ImageIO.read(byteArrayInputStream);
				Image fxImage = SwingFXUtils.toFXImage(receivedImage, null);
				Platform.runLater(() -> view.getImageView().setImage(fxImage));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}