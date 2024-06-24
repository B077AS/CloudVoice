package server;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.Node;

public class ServerDataPersistor {
    private String fileName;
    private ObservableList<Node> serverList;

    public ServerDataPersistor() {
    	
    	fileName = System.getProperty("user.home") + File.separator + "server_data.txt";
    	File file = new File(fileName);
    	if (file.exists() && file.isFile() && file.length() > 0) {
    	} else {
    	    try {
    	        if (file.createNewFile()) {
    	        } else {
    	        }
    	    } catch (IOException e) {
    	        e.printStackTrace();
    	    }
    	}

    }

    public void saveServers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
            for (Node server : serverList) {
                ServerBox serverCasted = (ServerBox) server;
                writer.write(String.valueOf(serverCasted.getServer().getId()));
                writer.newLine(); // Goes to the next line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<Integer> loadServers() {
        List<Integer> serverIds = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Leggi l'ID del server dalla riga e convertilo in intero
                int serverId = Integer.parseInt(line);
                serverIds.add(serverId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverIds;
    }

	public ObservableList<Node> getServerList() {
		return serverList;
	}

	public void setServerList(ObservableList<Node> serverList) {
		this.serverList = serverList;
	}

}
