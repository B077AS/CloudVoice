package rooms.videoRooms.screenShare;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import settings.CSSColorExtractor;

public class AppsBox extends HBox{

	private Image icon;
	private String name;
	private CSSColorExtractor cssColorExtractor;
	private boolean selected;
	private Label mainNameLabel;
	private Label moreNameLabel;

	public AppsBox(Image icon, String name) {
		this.icon = icon;
		this.name = name;
		this.cssColorExtractor=new CSSColorExtractor();
		this.setAlignment(Pos.CENTER_LEFT);
		this.setSpacing(7);
		this.setPadding(new Insets(3, 4.5, 3, 4.5));

		Color primaryColor=this.cssColorExtractor.findPrimaryColor();		
		double red=primaryColor.getRed()*255;
		double green=primaryColor.getGreen()*255;
		double blue=primaryColor.getBlue()*255;

		this.setOnMouseEntered(event -> {
			if(this.selected==false) {
				this.setStyle("-fx-background-color: rgba("+red+", "+green+", "+blue+", 0.4);" + "-fx-background-radius: 7;");
			}
		});

		this.setOnMouseExited(event -> {
			if(this.selected==false) {
				this.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
			}
		});

		ImageView imageView = new ImageView(icon);
		String mainName;
		String moreName = "";
		if (name.contains(" - ")) {
			String[] splittedName = name.split(" - ");
			mainName = splittedName[splittedName.length - 1];

			StringBuilder stringBuilder = new StringBuilder();

			for (int i = 0; i < splittedName.length - 1; i++) {
				stringBuilder.append(splittedName[i]);
				if (i < splittedName.length - 2) {
					stringBuilder.append(" - ");
				}
			}
			moreName=stringBuilder.toString();
		} else {
			mainName=name;
		}

		mainNameLabel=new Label(mainName);
		moreNameLabel=new Label(moreName);
		moreNameLabel.setStyle("-fx-font-size: 10px;");

		this.getChildren().addAll(imageView, mainNameLabel, moreNameLabel);
	}

	public void setBoxFocused() {
		this.selected=true;
		this.setStyle("-fx-background-color: -primary-color;" + "-fx-background-radius: 7;");
		this.mainNameLabel.setStyle("-fx-text-fill: -text-fill-color;");
		this.moreNameLabel.setStyle("-fx-text-fill: -text-fill-color; -fx-font-size: 10px;");
	}


	public void setBoxUnFocused() {
		this.selected=false;
		this.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 0;");
		this.mainNameLabel.setStyle("-fx-text-fill: -primary-color;");
		this.moreNameLabel.setStyle("-fx-text-fill: -primary-color; -fx-font-size: 10px;");
	}

	public Image getIcon() {
		return icon;
	}

	public void setIcon(Image icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}