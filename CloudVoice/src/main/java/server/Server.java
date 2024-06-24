package server;

import java.util.HashMap;
import javafx.scene.image.Image;
import rooms.ChatRoom;

public class Server{
	private int id;
	private String name;
	private int owner;
	private int numberOfUsers;
	private Image avatar;
	private int start_port;
	private int end_port;
	private HashMap<Integer, ChatRoom> allRooms;
	private HashMap<Integer, ChatRoom> showableRooms;

	public Server(int id, String name, int owner, int numberOfUsers, Image avatar, int start_port, int end_port) {
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.numberOfUsers = numberOfUsers;
		this.avatar = avatar;
		this.start_port=start_port;
		this.end_port=end_port;
		this.allRooms=new HashMap<>();
		this.showableRooms=new HashMap<>();
	}

	// Metodi getter e setter per gli attributi
	public String getName() {	
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public int getNumberOfUsers() {
		return numberOfUsers;
	}

	public void setNumberOfUsers(int numberOfUsers) {
		this.numberOfUsers = numberOfUsers;
	}

	public Image getAvatar() {
		return avatar;
	}

	public void setAvatar(Image avatar) {
		this.avatar = avatar;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStart_port() {
		return start_port;
	}

	public void setStart_port(int start_port) {
		this.start_port = start_port;
	}

	public int getEnd_port() {
		return end_port;
	}

	public void setEnd_port(int end_port) {
		this.end_port = end_port;
	}

	public HashMap<Integer, ChatRoom> getAllRooms() {
		return allRooms;
	}

	public void setAllRooms(HashMap<Integer, ChatRoom> allRooms) {
		this.allRooms = allRooms;
	}

	public HashMap<Integer, ChatRoom> getShowableRooms() {
		return showableRooms;
	}

	public void setShowableRooms(HashMap<Integer, ChatRoom> showableRooms) {
		this.showableRooms = showableRooms;
	}

	@Override
	public String toString() {
		return name; // Restituisci il nome del server come rappresentazione stringa
	}
}
