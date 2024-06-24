package main.model;

import java.io.File;
import java.io.InputStream;

public class CSSLoader {
	
	public String loadCss(){
		String fileName = System.getProperty("user.home") + File.separator + "customcss.css";
		File file = new File(fileName);
		String css;
		if (file.exists()) {
			css=file.toURI().toString();
		}else {
			css=getClass().getResource("/application.css").toExternalForm();
		}
		return css;
	}
	
	public InputStream getCssStream() {
		return getClass().getResourceAsStream("/application.css");
	}

}
