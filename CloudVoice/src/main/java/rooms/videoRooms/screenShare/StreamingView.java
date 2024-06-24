package rooms.videoRooms.screenShare;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.model.CSSLoader;
import main.model.SVGCodes;
import main.view.MainWindow;

public class StreamingView extends Stage{

	private MainWindow mainWindowView;
	private ImageView imageView;
	private Button stopButton;
	private BorderPane layout;
	private VBox rightBox;
	private HBox buttonsBox;	
	private HBox imageBox;
	private Button hideButton;
	private SplitPane splitPane;

	public StreamingView(MainWindow mainWindowView) {
		this.mainWindowView=mainWindowView;
		this.imageView=new ImageView();
		this.setMinHeight(555);
		this.setMinWidth(1000);
		initialize();
	}

	public void initialize() {

		setTitle("CloudVoice - Streaming View");
		layout = new BorderPane();

		hideButton=SVGCodes.createIconButton(SVGCodes.showChatSVG, 45, 37, 25);
		stopButton = SVGCodes.createIconButton(SVGCodes.stopScreenShareSVG, 45, 37, 25);
		mainWindowView.getButtonsBox().getChildren().addAll(stopButton, hideButton);

		rightBox=new VBox();
		rightBox.getChildren().add(mainWindowView.getPrimaryChatContainer());

		buttonsBox=mainWindowView.getButtonsBox();
		layout.setBottom(buttonsBox);

		imageView.setPreserveRatio(true);
		imageView.setImage(new Image("pause.png"));		

		imageBox=new HBox();
		imageBox.setMinWidth(250);
		imageBox.setAlignment(Pos.CENTER);
		imageBox.getChildren().addAll(imageView);

		splitPane = new SplitPane(imageBox, rightBox);


		Platform.runLater(() -> {
			splitPane.setDividerPositions(0.8);
			imageView.fitWidthProperty().bind(splitPane.getDividers().get(0).positionProperty().multiply(splitPane.widthProperty()));
			imageView.fitHeightProperty().bind(splitPane.getDividers().get(0).positionProperty().multiply(splitPane.heightProperty()).add(50));
		});

		layout.setCenter(splitPane);

		Scene scene = new Scene(layout, 1200, 700);		
		CSSLoader cssLoader=new CSSLoader();
		scene.getStylesheets().add(cssLoader.loadCss());		
		setScene(scene);
		setMaximized(mainWindowView.getPrimaryStage().isMaximized());
	}

	public Button getHideButton() {
		return hideButton;
	}

	public void setHideButton(Button hideButton) {
		this.hideButton = hideButton;
	}

	public ImageView getImageView() {
		return imageView;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}

	public Button getStopButton() {
		return stopButton;
	}

	public void setStopButton(Button stopButton) {
		this.stopButton = stopButton;
	}

	public BorderPane getLayout() {
		return layout;
	}

	public void setLayout(BorderPane layout) {
		this.layout = layout;
	}

	public HBox getButtonsBox() {
		return buttonsBox;
	}

	public void setButtonsBox(HBox buttonsBox) {
		this.buttonsBox = buttonsBox;
	}

	public VBox getRightBox() {
		return rightBox;
	}

	public void setRightBox(VBox rightBox) {
		this.rightBox = rightBox;
	}

	public HBox getImageBox() {
		return imageBox;
	}

	public void setImageBox(HBox imageBox) {
		this.imageBox = imageBox;
	}

	public SplitPane getSplitPane() {
		return splitPane;
	}

	public void setSplitPane(SplitPane splitPane) {
		this.splitPane = splitPane;
	}
}