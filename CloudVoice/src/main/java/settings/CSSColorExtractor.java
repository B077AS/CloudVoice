package settings;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javafx.scene.paint.Color;

public class CSSColorExtractor {

	private final String primary = "-primary-color: "; // La parola chiave desiderata
	private final Color defaultPrimaryColor=Color.web("#8a2be2");

	public CSSColorExtractor() {
	}

	public Color findPrimaryColor() {
		try {
			String cssContent = readCSSFile();
			int keywordIndex = cssContent.indexOf(primary);

			if (keywordIndex != -1) {
				int colorStartIndex = keywordIndex + primary.length();
				int colorEndIndex = cssContent.indexOf(';', colorStartIndex);

				if (colorEndIndex != -1) {
					String primaryColor = cssContent.substring(colorStartIndex, colorEndIndex);
					return Color.web(primaryColor);
				}
			}
		} catch (IOException e) {
			return defaultPrimaryColor;
		}
		return defaultPrimaryColor;
	}

	public  String readCSSFile() throws IOException {
		Path path = Paths.get(System.getProperty("user.home") + File.separator + "customcss.css");
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		return String.join("\n", lines);
	}

}