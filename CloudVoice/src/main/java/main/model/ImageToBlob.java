package main.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class ImageToBlob {

	public static Image byteArrayToImage(byte[] byteArray){
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
			Image image = new Image(bis);
			bis.close();
			return image;
		} catch (Exception e) {
			return null;
		}
	}

	public static byte[] convertImageToByteArray(Image image) {
		try {
			BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			// Puoi specificare il formato dell'immagine, ad esempio "png" o "jpg"
			ImageIO.write(bufferedImage, "png", byteArrayOutputStream);

			return byteArrayOutputStream.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}

}